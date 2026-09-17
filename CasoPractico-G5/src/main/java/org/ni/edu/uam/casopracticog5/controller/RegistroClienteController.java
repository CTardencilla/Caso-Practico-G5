package org.ni.edu.uam.casopracticog5.controller;

import javafx.fxml.FXML;
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
import java.time.format.DateTimeFormatter;
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
        cmbCiudad.getItems().setAll("Managua", "León", "Granada", "Masaya",
                "Estelí", "Matagalpa", "Chinandega", "Jinotepe", "Rivas",
                "Juigalpa", "Boaco", "Jinotega", "Ocotal", "Somoto",
                "San Carlos", "Bluefields", "Bilwi", "Otra");

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/uuuu");
        dpFechaNacimiento.setConverter(new javafx.util.StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate fecha) {
                return fecha == null ? "" : formato.format(fecha);
            }

            @Override
            public LocalDate fromString(String texto) {
                return texto == null || texto.isBlank() ? null
                        : LocalDate.parse(texto, formato.withResolverStyle(
                        java.time.format.ResolverStyle.STRICT));
            }
        });
        dpFechaNacimiento.setDayCellFactory(calendario -> new DateCell() {
            @Override
            public void updateItem(LocalDate fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setDisable(vacia || fecha == null || fecha.isAfter(LocalDate.now()));
            }
        });
    }

    @FXML
    private void seleccionarFotografia() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Seleccionar fotografía del cliente");
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Imágenes (PNG, JPG, GIF, BMP)", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp",
                "*.PNG", "*.JPG", "*.JPEG", "*.GIF", "*.BMP"));
        File archivo = selector.showOpenDialog(txtNombres.getScene().getWindow());
        if (archivo == null) {
            return;
        }
        try {
            if (!archivo.isFile() || !archivo.canRead()) {
                mostrarAlerta(Alert.AlertType.ERROR, "Fotografía no disponible",
                        "Selecciona un archivo de imagen que se pueda leer.");
                return;
            }
            if (Files.size(archivo.toPath()) > 5L * 1024 * 1024) {
                mostrarAlerta(Alert.AlertType.WARNING, "Fotografía demasiado grande",
                        "La fotografía debe pesar como máximo 5 MB.");
                return;
            }
            try (InputStream entrada = Files.newInputStream(archivo.toPath())) {
                Image imagen = new Image(entrada, 400, 400, true, true);
                if (imagen.isError() || imagen.getWidth() == 0 || imagen.getHeight() == 0) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Imagen no válida",
                            "No se pudo abrir la imagen. Selecciona un archivo PNG, JPG, GIF o BMP válido.");
                    return;
                }
                imgFotografia.setImage(imagen);
                rutaFotografia = archivo.getAbsolutePath();
                lblFotografia.setText(archivo.getName());
            }
        } catch (IOException | SecurityException ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al abrir la fotografía",
                    "No se pudo leer el archivo. Comprueba que esté disponible e inténtalo nuevamente.");
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
        LocalDate fechaNacimiento = dpFechaNacimiento.getValue();
        if (fechaNacimiento == null) {
            errores.add("Selecciona la fecha de nacimiento en el calendario.");
        } else if (fechaNacimiento.isAfter(LocalDate.now())) {
            errores.add("La fecha de nacimiento no puede ser futura.");
        }
        if (grupoSolicitud.getSelectedToggle() == null) {
            errores.add("Selecciona el tipo de solicitud.");
        }
        if (servicios.isEmpty()) {
            errores.add("Selecciona al menos un servicio de interés.");
        }
        if (rutaFotografia != null && !Files.isReadable(new File(rutaFotografia).toPath())) {
            errores.add("La fotografía ya no está disponible. Selecciónala nuevamente o quítala.");
        }
        if (!errores.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Revisa los datos del cliente",
                    "• " + String.join("\n• ", errores));
            return;
        }

        RadioButton solicitud = (RadioButton) grupoSolicitud.getSelectedToggle();
        Cliente cliente = new Cliente(nombres, apellidos, cmbTipoCliente.getValue(),
                cmbCiudad.getValue(), fechaNacimiento, solicitud.getText(), servicios, rutaFotografia);
        DataStore.getClientes().add(cliente);
        limpiarFormulario();
        mostrarAlerta(Alert.AlertType.INFORMATION, "Registro exitoso",
                "El cliente " + cliente + " fue registrado correctamente.");
    }

    private void validarNombre(String texto, String campo, List<String> errores) {
        if (texto.isBlank()) {
            errores.add(campo + " son obligatorios.");
        } else if (texto.length() > 80) {
            errores.add(campo + " deben tener como máximo 80 caracteres.");
        } else if (!texto.matches("[\\p{L}\\p{M}]+(?:[ '\u2019-][\\p{L}\\p{M}]+)*")) {
            errores.add(campo + " deben contener letras; se permiten espacios, guiones y apóstrofos entre palabras.");
        }
    }

    private String normalizar(String texto) {
        return texto == null ? "" : texto.strip().replaceAll("\\s+", " ");
    }

    private List<String> obtenerServicios() {
        List<String> servicios = new ArrayList<>();
        for (CheckBox casilla : List.of(chkAsesoria, chkSoporte, chkCapacitacion)) {
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
        grupoSolicitud.selectToggle(null);
        chkAsesoria.setSelected(false);
        chkSoporte.setSelected(false);
        chkCapacitacion.setSelected(false);
        quitarFotografia();
        txtNombres.requestFocus();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.initOwner(txtNombres.getScene().getWindow());
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensaje);
        alerta.setResizable(true);
        alerta.getDialogPane().setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        alerta.showAndWait();
    }
}
