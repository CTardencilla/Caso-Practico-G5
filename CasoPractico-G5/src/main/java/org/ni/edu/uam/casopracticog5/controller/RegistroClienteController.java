package org.ni.edu.uam.casopracticog5.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.ni.edu.uam.casopracticog5.model.Cliente;
import org.ni.edu.uam.casopracticog5.model.DataStore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

public class RegistroClienteController {

    @FXML private TextField txtNombres;
    @FXML private TextField txtApellidos;
    @FXML private ComboBox<String> cmbTipoCliente;
    @FXML private ComboBox<String> cmbCiudad;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private ToggleGroup grupoSolicitud;
    @FXML private CheckBox chkAsesoria;
    @FXML private CheckBox chkSoporte;
    @FXML private CheckBox chkCapacitacion;
    @FXML private ImageView imgFotografia;
    @FXML private Label lblFotografia;

    private String rutaFotografia;

    @FXML
    public void initialize() {
        cmbTipoCliente.getItems().setAll("Natural", "Jurídico");

        cmbCiudad.getItems().setAll(
                "Managua", "León", "Granada", "Masaya",
                "Estelí", "Matagalpa", "Chinandega", "Jinotepe",
                "Rivas", "Juigalpa", "Boaco", "Jinotega",
                "Ocotal", "Somoto", "San Carlos", "Bluefields",
                "Bilwi", "Otra"
        );

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/uuuu");

        dpFechaNacimiento.setConverter(
                new javafx.util.StringConverter<LocalDate>() {
                    @Override
                    public String toString(LocalDate fecha) {
                        return fecha == null ? "" : formato.format(fecha);
                    }

                    @Override
                    public LocalDate fromString(String texto) {
                        if (texto == null || texto.isBlank()) {
                            return null;
                        }
                        try {
                            return LocalDate.parse(
                                    texto.trim(),
                                    formato.withResolverStyle(
                                            ResolverStyle.STRICT
                                    )
                            );
                        } catch (DateTimeParseException e) {
                            return null;
                        }
                    }
                }
        );

        // En el calendario emergente, deshabilitar fechas no válidas:
        // No se permite: fechas futuras, la fecha de hoy, ni fechas mayores a 120 años en el pasado
        dpFechaNacimiento.setDayCellFactory(calendario -> new DateCell() {
            @Override
            public void updateItem(LocalDate fecha, boolean vacia) {
                super.updateItem(fecha, vacia);

                if (vacia || fecha == null) {
                    setDisable(true);
                } else {
                    LocalDate hoy = LocalDate.now();
                    boolean noPermitida = !fecha.isBefore(hoy) || fecha.isBefore(hoy.minusYears(120));
                    setDisable(noPermitida);
                    if (noPermitida) {
                        setStyle("-fx-background-color: #F0F0F0; -fx-text-fill: #BDBDBD;");
                    }
                }
            }
        });

        // Sincronizar automáticamente cuando el usuario escribe en el editor y pierde el foco o presiona ENTER
        dpFechaNacimiento.getEditor().focusedProperty().addListener((obs, antes, enfocado) -> {
            if (!enfocado) {
                sincronizarFechaDesdeEditor();
            }
        });
        dpFechaNacimiento.getEditor().setOnAction(e -> sincronizarFechaDesdeEditor());
    }

    private void sincronizarFechaDesdeEditor() {
        String texto = dpFechaNacimiento.getEditor().getText();
        if (texto == null || texto.isBlank()) {
            dpFechaNacimiento.setValue(null);
        } else {
            LocalDate parsed = dpFechaNacimiento.getConverter().fromString(texto);
            if (parsed != null) {
                dpFechaNacimiento.setValue(parsed);
            }
        }
    }

    @FXML
    private void seleccionarFotografia() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Seleccionar fotografía del cliente");

        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Imágenes (PNG, JPG, GIF, BMP)",
                        "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp",
                        "*.PNG", "*.JPG", "*.JPEG", "*.GIF", "*.BMP"
                )
        );

        File archivo = selector.showOpenDialog(
                txtNombres.getScene().getWindow()
        );

        if (archivo == null) {
            return;
        }

        try {
            if (!archivo.isFile() || !archivo.canRead()) {
                mostrarAlerta(
                        Alert.AlertType.ERROR,
                        "Fotografía no disponible",
                        "Selecciona un archivo de imagen que se pueda leer."
                );
                return;
            }

            if (Files.size(archivo.toPath()) > 5L * 1024 * 1024) {
                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Fotografía demasiado grande",
                        "La fotografía debe pesar como máximo 5 MB."
                );
                return;
            }

            try (InputStream entrada = Files.newInputStream(archivo.toPath())) {
                Image imagen = new Image(entrada, 400, 400, true, true);

                if (imagen.isError()
                        || imagen.getWidth() == 0
                        || imagen.getHeight() == 0) {

                    mostrarAlerta(
                            Alert.AlertType.ERROR,
                            "Imagen no válida",
                            "No se pudo abrir la imagen. Selecciona un archivo PNG, JPG, GIF o BMP válido."
                    );
                    return;
                }

                imgFotografia.setImage(imagen);
                rutaFotografia = archivo.getAbsolutePath();
                lblFotografia.setText(archivo.getName());
            }

        } catch (IOException | SecurityException ex) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error al abrir la fotografía",
                    "No se pudo leer el archivo. Comprueba que esté disponible e inténtalo nuevamente."
            );
        }
    }

    @FXML
    private void quitarFotografia() {
        imgFotografia.setImage(null);
        rutaFotografia = null;
        lblFotografia.setText("Sin fotografía seleccionada");
    }

    @FXML
    private void guardarCliente() {
        String nombres = normalizar(txtNombres.getText());
        String apellidos = normalizar(txtApellidos.getText());

        List<String> servicios = obtenerServicios();
        List<String> errores = new ArrayList<>();

        validarNombre(nombres, "Los nombres", errores);
        validarNombre(apellidos, "Los apellidos", errores);

        if (cmbTipoCliente.getValue() == null
                || !cmbTipoCliente.getItems().contains(cmbTipoCliente.getValue())) {
            errores.add("Selecciona el tipo de cliente.");
        }

        if (cmbCiudad.getValue() == null
                || !cmbCiudad.getItems().contains(cmbCiudad.getValue())) {
            errores.add("Selecciona la ciudad.");
        }

        sincronizarFechaDesdeEditor();
        LocalDate fechaNacimiento = dpFechaNacimiento.getValue();
        String textoEditor = dpFechaNacimiento.getEditor().getText();

        validarFechaNacimiento(
                fechaNacimiento,
                textoEditor,
                cmbTipoCliente.getValue(),
                errores
        );

        if (grupoSolicitud.getSelectedToggle() == null) {
            errores.add("Selecciona el tipo de solicitud.");
        }

        if (servicios.isEmpty()) {
            errores.add("Selecciona al menos un servicio de interés.");
        }

        if (rutaFotografia != null
                && !Files.isReadable(new File(rutaFotografia).toPath())) {
            errores.add(
                    "La fotografía ya no está disponible. Selecciónala nuevamente o quítala."
            );
        }

        if (!errores.isEmpty()) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Revisa los datos del cliente",
                    "• " + String.join("\n• ", errores)
            );
            return;
        }

        RadioButton solicitud =
                (RadioButton) grupoSolicitud.getSelectedToggle();

        Cliente cliente = new Cliente(
                nombres,
                apellidos,
                cmbTipoCliente.getValue(),
                cmbCiudad.getValue(),
                fechaNacimiento,
                solicitud.getText(),
                servicios,
                rutaFotografia
        );

        DataStore.getClientes().add(cliente);

        limpiarFormulario();

        mostrarAlerta(
                Alert.AlertType.INFORMATION,
                "Registro exitoso",
                "El cliente " + cliente + " fue registrado correctamente."
        );
    }

    private void validarNombre(
            String texto,
            String campo,
            List<String> errores
    ) {
        if (texto.isBlank()) {
            errores.add(campo + " son obligatorios.");

        } else if (texto.length() > 80) {
            errores.add(campo + " deben tener como máximo 80 caracteres.");

        } else if (!texto.matches(
                "[\\p{L}\\p{M}]+(?:[ '\u2019-][\\p{L}\\p{M}]+)*"
        )) {
            errores.add(
                    campo + " deben contener letras; se permiten espacios, guiones y apóstrofos entre palabras."
            );
        }
    }

    private void validarFechaNacimiento(
            LocalDate fechaNacimiento,
            String textoEditor,
            String tipoCliente,
            List<String> errores
    ) {
        if (fechaNacimiento == null) {
            if (textoEditor != null && !textoEditor.isBlank()) {
                errores.add("La fecha de nacimiento es inválida o no existe. Ingresa una fecha real en formato dd/mm/aaaa.");
            } else {
                errores.add("Selecciona o ingresa la fecha de nacimiento.");
            }
            return;
        }

        LocalDate hoy = LocalDate.now();
        if (fechaNacimiento.isAfter(hoy)) {
            errores.add("La fecha de nacimiento no puede ser futura.");
        } else if (fechaNacimiento.isEqual(hoy)) {
            errores.add("La fecha de nacimiento no puede ser la fecha de hoy.");
        } else if (fechaNacimiento.isBefore(hoy.minusYears(120))) {
            errores.add("La fecha de nacimiento no es válida (el límite máximo es 120 años en el pasado).");
        } else {
            int edad = Period.between(fechaNacimiento, hoy).getYears();
            if ("Natural".equals(tipoCliente) && edad < 18) {
                errores.add("El cliente persona natural debe ser mayor de edad (al menos 18 años). Edad actual calculada: " + edad + " años.");
            }
        }
    }

    private String normalizar(String texto) {
        return texto == null
                ? ""
                : texto.strip().replaceAll("\\s+", " ");
    }

    private List<String> obtenerServicios() {
        List<String> servicios = new ArrayList<>();

        for (CheckBox casilla : List.of(
                chkAsesoria,
                chkSoporte,
                chkCapacitacion
        )) {
            if (casilla.isSelected()) {
                servicios.add(casilla.getText());
            }
        }

        return servicios;
    }

    @FXML
    private void limpiarFormulario() {
        txtNombres.clear();
        txtApellidos.clear();

        cmbTipoCliente.getSelectionModel().clearSelection();
        cmbCiudad.getSelectionModel().clearSelection();

        dpFechaNacimiento.setValue(null);
        dpFechaNacimiento.getEditor().clear();
        grupoSolicitud.selectToggle(null);

        chkAsesoria.setSelected(false);
        chkSoporte.setSelected(false);
        chkCapacitacion.setSelected(false);

        quitarFotografia();
        txtNombres.requestFocus();
    }

    @FXML
    private void cancelarRegistro() {
        try {
            Parent menu = FXMLLoader.load(
                    getClass().getResource(
                            "/org/ni/edu/uam/casopracticog5/view/MainView.fxml"
                    )
            );

            txtNombres.getScene().setRoot(menu);

        } catch (IOException ex) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error de navegación",
                    "No se pudo abrir el menú principal."
            );
        }
    }

    private void mostrarAlerta(
            Alert.AlertType tipo,
            String titulo,
            String mensaje
    ) {
        Alert alerta = new Alert(tipo);

        alerta.initOwner(txtNombres.getScene().getWindow());
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensaje);
        alerta.setResizable(true);

        alerta.getDialogPane().setMinHeight(
                javafx.scene.layout.Region.USE_PREF_SIZE
        );

        alerta.showAndWait();
    }
}