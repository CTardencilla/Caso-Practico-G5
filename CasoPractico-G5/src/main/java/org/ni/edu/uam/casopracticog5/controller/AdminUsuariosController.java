package org.ni.edu.uam.casopracticog5.controller;

import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Window;
import org.ni.edu.uam.casopracticog5.model.DataStore;
import org.ni.edu.uam.casopracticog5.model.Usuario;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Controlador para el Panel de Administración y Gestión de Usuarios.
 * Implementa control de acceso basado en roles (RBAC).
 */
public class AdminUsuariosController {

    @FXML
    private Label lblEstadoPermisos;

    @FXML
    private HBox bannerPermisos;

    @FXML
    private Label lblMensajeBanner;

    @FXML
    private TextField txtNombreCompleto;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private ComboBox<String> cmbRol;

    @FXML
    private CheckBox chkActivo;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnActualizar;

    @FXML
    private Button btnLimpiar;

    @FXML
    private TableView<Usuario> tablaUsuarios;

    @FXML
    private TableColumn<Usuario, String> colUsername;

    @FXML
    private TableColumn<Usuario, String> colNombre;

    @FXML
    private TableColumn<Usuario, String> colRol;

    @FXML
    private TableColumn<Usuario, Boolean> colEstado;

    @FXML
    private TableColumn<Usuario, LocalDate> colFecha;

    @FXML
    private Label lblTotalUsuarios;

    @FXML
    private Button btnModificar;

    @FXML
    private Button btnEliminar;

    private Usuario usuarioEnEdicion = null;

    @FXML
    public void initialize() {
        // Inicializar opciones de rol
        cmbRol.getItems().addAll("ADMINISTRADOR", "OPERADOR");
        cmbRol.setValue("OPERADOR");

        // Configurar columnas de la tabla
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("activo"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));

        // Formato visual de Estado (Activo / Inactivo)
        colEstado.setCellFactory(col -> new TableCell<Usuario, Boolean>() {
            @Override
            protected void updateItem(Boolean activo, boolean vacio) {
                super.updateItem(activo, vacio);
                if (vacio || activo == null) {
                    setText(null);
                    setStyle("");
                } else if (activo) {
                    setText("Activo");
                    setStyle("-fx-alignment: CENTER; -fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                } else {
                    setText("Inactivo");
                    setStyle("-fx-alignment: CENTER; -fx-text-fill: #C62828; -fx-font-style: italic;");
                }
            }
        });

        // Formato visual de Fecha dd/MM/yyyy
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        colFecha.setCellFactory(col -> new TableCell<Usuario, LocalDate>() {
            @Override
            protected void updateItem(LocalDate fecha, boolean vacio) {
                super.updateItem(fecha, vacio);
                if (vacio || fecha == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(formatoFecha.format(fecha));
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        // Vincular con la lista observable de DataStore
        tablaUsuarios.setItems(DataStore.getUsuarios());
        actualizarContador();

        DataStore.getUsuarios().addListener((ListChangeListener<Usuario>) c -> actualizarContador());

        // Control de selección de tabla
        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            boolean esAdmin = esUsuarioActualAdmin();
            boolean haySeleccion = seleccionado != null;
            btnModificar.setDisable(!esAdmin || !haySeleccion);
            btnEliminar.setDisable(!esAdmin || !haySeleccion);
        });

        // Doble clic para cargar en formulario
        tablaUsuarios.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2 && tablaUsuarios.getSelectionModel().getSelectedItem() != null) {
                if (esUsuarioActualAdmin()) {
                    onModificarUsuario(null);
                }
            }
        });

        // Aplicar verificación de permisos RBAC
        aplicarPermisosRBAC();
    }

    /**
     * Verifica si el usuario en sesión actual tiene privilegios de administrador.
     */
    public boolean esUsuarioActualAdmin() {
        Usuario actual = DataStore.getUsuarioActual();
        return actual != null && actual.esAdmin();
    }

    private void aplicarPermisosRBAC() {
        boolean esAdmin = esUsuarioActualAdmin();
        Usuario actual = DataStore.getUsuarioActual();

        if (esAdmin) {
            String userStr = actual != null ? actual.getUsername() : "admin";
            lblEstadoPermisos.setText("Sesión: " + userStr + " (ADMINISTRADOR)");
            lblMensajeBanner.setText("Control total activado: Tienes permisos para registrar, modificar y eliminar usuarios.");
            bannerPermisos.setStyle("-fx-background-color: #E8F5E9; -fx-border-color: #C8E6C9; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 14;");
            lblMensajeBanner.setStyle("-fx-text-fill: #2E7D32; -fx-font-size: 12px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
            btnGuardar.setDisable(false);
            btnActualizar.setDisable(true);
        } else {
            String userStr = actual != null ? actual.getUsername() : "operador";
            lblEstadoPermisos.setText("Sesión: " + userStr + " (SOLO LECTURA)");
            lblMensajeBanner.setText("⚠️ Modo solo lectura: Se requieren permisos de ADMINISTRADOR para modificar o eliminar usuarios.");
            bannerPermisos.setStyle("-fx-background-color: #FFF3E0; -fx-border-color: #FFE0B2; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 14;");
            lblMensajeBanner.setStyle("-fx-text-fill: #E65100; -fx-font-size: 12px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");

            // Deshabilitar todas las acciones de modificación
            btnGuardar.setDisable(true);
            btnActualizar.setDisable(true);
            btnModificar.setDisable(true);
            btnEliminar.setDisable(true);
            txtNombreCompleto.setDisable(true);
            txtUsername.setDisable(true);
            txtPassword.setDisable(true);
            cmbRol.setDisable(true);
            chkActivo.setDisable(true);
        }
    }

    private void actualizarContador() {
        if (lblTotalUsuarios != null) {
            int total = DataStore.getUsuarios().size();
            lblTotalUsuarios.setText("Total: " + total + " usuario" + (total != 1 ? "s" : ""));
        }
    }

    @FXML
    public void onGuardarUsuario(ActionEvent event) {
        if (!esUsuarioActualAdmin()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Acceso Denegado", "Solo un usuario con rol de Administrador puede registrar usuarios.");
            return;
        }

        String nombre = txtNombreCompleto.getText() != null ? txtNombreCompleto.getText().trim() : "";
        String username = txtUsername.getText() != null ? txtUsername.getText().trim() : "";
        String password = txtPassword.getText() != null ? txtPassword.getText().trim() : "";
        String rol = cmbRol.getValue();
        boolean activo = chkActivo.isSelected();

        // Validaciones
        if (nombre.isBlank() || username.isBlank() || password.isBlank() || rol == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Incompletos", "Por favor, completa todos los campos requeridos.");
            return;
        }

        if (username.length() < 3) {
            mostrarAlerta(Alert.AlertType.WARNING, "Usuario Inválido", "El nombre de usuario debe tener al menos 3 caracteres.");
            return;
        }

        if (password.length() < 4) {
            mostrarAlerta(Alert.AlertType.WARNING, "Contraseña Corta", "La contraseña debe tener al menos 4 caracteres.");
            return;
        }

        // Validar unicidad
        if (DataStore.buscarUsuario(username) != null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Usuario Duplicado", "Ya existe un usuario registrado con el identificador '" + username + "'.");
            return;
        }

        Usuario nuevoUsuario = new Usuario(username, password, nombre, rol, LocalDate.now(), activo);
        DataStore.getUsuarios().add(nuevoUsuario);

        mostrarAlerta(Alert.AlertType.INFORMATION, "Usuario Registrado", "El usuario '" + username + "' ha sido registrado exitosamente con rol " + rol + ".");
        onLimpiarFormulario(null);
    }

    @FXML
    public void onModificarUsuario(ActionEvent event) {
        if (!esUsuarioActualAdmin()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Acceso Denegado", "Solo un usuario con rol de Administrador puede modificar usuarios.");
            return;
        }

        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida", "Selecciona un usuario de la tabla para modificarlo.");
            return;
        }

        usuarioEnEdicion = seleccionado;
        txtNombreCompleto.setText(seleccionado.getNombreCompleto());
        txtUsername.setText(seleccionado.getUsername());
        txtUsername.setDisable(true); // El username actúa como clave primaria y no se edita
        txtPassword.setText(seleccionado.getPassword());
        cmbRol.setValue(seleccionado.getRol());
        chkActivo.setSelected(seleccionado.isActivo());

        btnGuardar.setDisable(true);
        btnActualizar.setDisable(false);
    }

    @FXML
    public void onActualizarUsuario(ActionEvent event) {
        if (!esUsuarioActualAdmin()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Acceso Denegado", "Solo un usuario con rol de Administrador puede modificar usuarios.");
            return;
        }

        if (usuarioEnEdicion == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección", "No hay ningún usuario en modo de edición.");
            return;
        }

        String nombre = txtNombreCompleto.getText() != null ? txtNombreCompleto.getText().trim() : "";
        String password = txtPassword.getText() != null ? txtPassword.getText().trim() : "";
        String rol = cmbRol.getValue();
        boolean activo = chkActivo.isSelected();

        if (nombre.isBlank() || password.isBlank() || rol == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Incompletos", "Por favor, completa todos los campos requeridos.");
            return;
        }

        // Protección: Si es el único admin, no puede cambiar su propio rol a operador ni desactivarse
        Usuario actual = DataStore.getUsuarioActual();
        if (usuarioEnEdicion.equals(actual)) {
            if (!activo) {
                mostrarAlerta(Alert.AlertType.ERROR, "Acción Inválida", "No puedes desactivar tu propia cuenta mientras tienes la sesión activa.");
                return;
            }
            if (!"ADMINISTRADOR".equalsIgnoreCase(rol) && contarAdministradores() <= 1) {
                mostrarAlerta(Alert.AlertType.ERROR, "Acción Inválida", "No puedes quitarte el rol de Administrador porque eres el único administrador en el sistema.");
                return;
            }
        }

        usuarioEnEdicion.setNombreCompleto(nombre);
        usuarioEnEdicion.setPassword(password);
        usuarioEnEdicion.setRol(rol);
        usuarioEnEdicion.setActivo(activo);

        tablaUsuarios.refresh();

        mostrarAlerta(Alert.AlertType.INFORMATION, "Usuario Actualizado", "Los datos del usuario '" + usuarioEnEdicion.getUsername() + "' fueron actualizados correctamente.");
        onLimpiarFormulario(null);
    }

    @FXML
    public void onEliminarUsuario(ActionEvent event) {
        if (!esUsuarioActualAdmin()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Acceso Denegado", "Solo un usuario con rol de Administrador puede eliminar usuarios.");
            return;
        }

        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida", "Selecciona un usuario de la tabla para eliminarlo.");
            return;
        }

        // Protección 1: No eliminar la sesión activa
        Usuario actual = DataStore.getUsuarioActual();
        if (seleccionado.equals(actual)) {
            mostrarAlerta(Alert.AlertType.ERROR, "Acción Prohibida", "No puedes eliminar al usuario con el que has iniciado sesión actualmente.");
            return;
        }

        // Protección 2: No eliminar al último administrador
        if (seleccionado.esAdmin() && contarAdministradores() <= 1) {
            mostrarAlerta(Alert.AlertType.ERROR, "Acción Prohibida", "No es posible eliminar al único administrador del sistema.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("Eliminar usuario del sistema");
        confirmacion.setContentText("¿Estás seguro de que deseas eliminar permanentemente al usuario '"
                + seleccionado.getUsername() + "' (" + seleccionado.getNombreCompleto() + ")?");

        asignarPropietario(confirmacion);

        Optional<ButtonType> resp = confirmacion.showAndWait();
        if (resp.isPresent() && resp.get() == ButtonType.OK) {
            DataStore.getUsuarios().remove(seleccionado);
            if (usuarioEnEdicion != null && usuarioEnEdicion.equals(seleccionado)) {
                onLimpiarFormulario(null);
            }
            mostrarAlerta(Alert.AlertType.INFORMATION, "Usuario Eliminado", "El usuario ha sido eliminado satisfactoriamente.");
        }
    }

    @FXML
    public void onLimpiarFormulario(ActionEvent event) {
        usuarioEnEdicion = null;
        txtNombreCompleto.clear();
        txtUsername.clear();
        txtUsername.setDisable(false);
        txtPassword.clear();
        cmbRol.setValue("OPERADOR");
        chkActivo.setSelected(true);

        boolean esAdmin = esUsuarioActualAdmin();
        btnGuardar.setDisable(!esAdmin);
        btnActualizar.setDisable(true);
        btnModificar.setDisable(true);
        btnEliminar.setDisable(true);
        tablaUsuarios.getSelectionModel().clearSelection();
    }

    private int contarAdministradores() {
        int count = 0;
        for (Usuario u : DataStore.getUsuarios()) {
            if (u.esAdmin() && u.isActivo()) {
                count++;
            }
        }
        return count;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensaje);
        asignarPropietario(alerta);
        alerta.showAndWait();
    }

    private void asignarPropietario(Alert alerta) {
        if (tablaUsuarios != null && tablaUsuarios.getScene() != null && tablaUsuarios.getScene().getWindow() != null) {
            alerta.initOwner(tablaUsuarios.getScene().getWindow());
        }
    }
}
