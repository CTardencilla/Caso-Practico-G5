package org.ni.edu.uam.casopracticog5.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.ni.edu.uam.casopracticog5.model.Cliente;
import org.ni.edu.uam.casopracticog5.model.DataStore;
import org.ni.edu.uam.casopracticog5.model.Usuario;
import org.ni.edu.uam.casopracticog5.util.SceneUtil;

public class MainController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private StackPane contentArea;

    @FXML
    private VBox cardBienvenida;

    @FXML
    private Label lblBienvenida;

    @FXML
    private Label lblFechaDashboard;

    @FXML
    private Label lblBadgeRol;

    @FXML
    private Label lblTotalClientesDash;

    @FXML
    private Label lblSubClientesDash;

    @FXML
    private Label lblTotalCiudadesDash;

    @FXML
    private Label lblSubCiudadesDash;

    @FXML
    private Label lblTotalUsuariosDash;

    @FXML
    private Label lblSubUsuariosDash;

    @FXML
    private TableView<Cliente> tablaClientesDashboard;

    @FXML
    private TableColumn<Cliente, String> colDashNombre;

    @FXML
    private TableColumn<Cliente, String> colDashTipo;

    @FXML
    private TableColumn<Cliente, String> colDashCiudad;

    @FXML
    private TableColumn<Cliente, LocalDate> colDashFecha;

    @FXML
    private Label lblSesionUsuario;

    private Object controladorActual;

    @FXML
    public void initialize() {
        // Configurar tabla de clientes recientes en dashboard si está presente
        if (tablaClientesDashboard != null) {
            colDashNombre.setCellValueFactory(cellData -> {
                Cliente c = cellData.getValue();
                String full = c.getApellidos() != null && !c.getApellidos().isBlank()
                        ? c.getNombres() + " " + c.getApellidos()
                        : c.getNombres();
                return new SimpleStringProperty(full);
            });

            colDashTipo.setCellValueFactory(new PropertyValueFactory<>("tipoCliente"));
            colDashCiudad.setCellValueFactory(new PropertyValueFactory<>("ciudad"));
            colDashFecha.setCellValueFactory(new PropertyValueFactory<>("fechaNacimiento"));

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            colDashFecha.setCellFactory(col -> new TableCell<Cliente, LocalDate>() {
                @Override
                protected void updateItem(LocalDate fecha, boolean empty) {
                    super.updateItem(fecha, empty);
                    if (empty || fecha == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(dtf.format(fecha));
                        setStyle("-fx-alignment: CENTER;");
                    }
                }
            });

            colDashTipo.setCellFactory(col -> new TableCell<Cliente, String>() {
                @Override
                protected void updateItem(String tipo, boolean empty) {
                    super.updateItem(tipo, empty);
                    if (empty || tipo == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(tipo);
                        if ("Jurídico".equalsIgnoreCase(tipo)) {
                            setStyle("-fx-alignment: CENTER; -fx-text-fill: #E65100; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-alignment: CENTER; -fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                        }
                    }
                }
            });

            tablaClientesDashboard.setItems(DataStore.getClientes());

            // Doble clic en fila para abrir detalle modal del cliente
            tablaClientesDashboard.setOnMouseClicked((MouseEvent event) -> {
                if (event.getClickCount() == 2 && tablaClientesDashboard.getSelectionModel().getSelectedItem() != null) {
                    abrirDetalleClienteModal(tablaClientesDashboard.getSelectionModel().getSelectedItem());
                }
            });
        }

        // Suscribirse a cambios en clientes y usuarios para actualizar automáticamente el dashboard
        DataStore.getClientes().addListener((ListChangeListener<Cliente>) c -> actualizarDashboard());
        DataStore.getUsuarios().addListener((ListChangeListener<Usuario>) c -> actualizarDashboard());

        // Actualizar datos y tarjetas del dashboard
        actualizarDashboard();
    }

    /**
     * Actualiza dinámicamente las tarjetas de métricas, textos de bienvenida y tabla del Dashboard.
     */
    public void actualizarDashboard() {
        Usuario usuario = DataStore.getUsuarioActual();
        if (lblSesionUsuario != null && usuario != null) {
            lblSesionUsuario.setText(usuario.getUsername() + " (" + usuario.getRol() + ")");
        }

        if (lblBienvenida != null) {
            String nombreUser = (usuario != null && usuario.getNombreCompleto() != null)
                    ? usuario.getNombreCompleto()
                    : (usuario != null ? usuario.getUsername() : "Usuario");
            lblBienvenida.setText("Panel de Control · ¡Bienvenido, " + nombreUser + "!");
        }

        if (lblFechaDashboard != null) {
            LocalDate hoy = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-NI"));
            String fechaTexto = hoy.format(formatter);
            fechaTexto = fechaTexto.substring(0, 1).toUpperCase() + fechaTexto.substring(1);
            lblFechaDashboard.setText(fechaTexto + " · Resumen operativo y accesos rápidos");
        }

        if (lblBadgeRol != null && usuario != null) {
            if (usuario.esAdmin()) {
                lblBadgeRol.setText("🛡️ MODO ADMINISTRADOR");
                lblBadgeRol.setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; -fx-border-color: #C8E6C9; -fx-border-radius: 16; -fx-background-radius: 16; -fx-padding: 6 14; -fx-font-weight: bold; -fx-font-size: 11px;");
            } else {
                lblBadgeRol.setText("👤 MODO OPERADOR");
                lblBadgeRol.setStyle("-fx-background-color: #FFF3E0; -fx-text-fill: #E65100; -fx-border-color: #FFE0B2; -fx-border-radius: 16; -fx-background-radius: 16; -fx-padding: 6 14; -fx-font-weight: bold; -fx-font-size: 11px;");
            }
        }

        int totalClientes = DataStore.getClientes().size();
        long naturales = DataStore.getClientes().stream()
                .filter(c -> !"Jurídico".equalsIgnoreCase(c.getTipoCliente()))
                .count();
        long juridicos = totalClientes - naturales;

        if (lblTotalClientesDash != null) {
            lblTotalClientesDash.setText(String.valueOf(totalClientes));
        }
        if (lblSubClientesDash != null) {
            lblSubClientesDash.setText(naturales + " Naturales · " + juridicos + " Jurídicos");
        }

        long totalCiudades = DataStore.getClientes().stream()
                .map(Cliente::getCiudad)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .count();

        if (lblTotalCiudadesDash != null) {
            lblTotalCiudadesDash.setText(totalCiudades + " ciudad" + (totalCiudades != 1 ? "es" : ""));
        }
        if (lblSubCiudadesDash != null) {
            String ciudadesStr = DataStore.getClientes().stream()
                    .map(Cliente::getCiudad)
                    .filter(c -> c != null && !c.isBlank())
                    .distinct()
                    .limit(3)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Sin registros");
            if (totalCiudades > 3) {
                ciudadesStr += "...";
            }
            lblSubCiudadesDash.setText(ciudadesStr);
        }

        int totalUsuarios = DataStore.getUsuarios().size();
        long totalAdmins = DataStore.getUsuarios().stream().filter(Usuario::esAdmin).count();
        long totalOperadores = totalUsuarios - totalAdmins;

        if (lblTotalUsuariosDash != null) {
            lblTotalUsuariosDash.setText(String.valueOf(totalUsuarios));
        }
        if (lblSubUsuariosDash != null) {
            lblSubUsuariosDash.setText(totalAdmins + " Admin · " + totalOperadores + " Operador" + (totalOperadores != 1 ? "es" : ""));
        }

        if (tablaClientesDashboard != null) {
            tablaClientesDashboard.refresh();
        }
    }

    @FXML
    public void onRefrescarDashboard(ActionEvent event) {
        actualizarDashboard();
    }

    private void abrirDetalleClienteModal(Cliente cliente) {
        if (cliente == null) return;
        try {
            Stage stage = rootPane != null && rootPane.getScene() != null
                    ? (Stage) rootPane.getScene().getWindow() : null;
            String nombreCompleto = cliente.getApellidos() != null && !cliente.getApellidos().isBlank()
                    ? cliente.getNombres() + " " + cliente.getApellidos()
                    : cliente.getNombres();

            SceneUtil.abrirModal(
                    stage,
                    "/org/ni/edu/uam/casopracticog5/view/DetalleClienteView.fxml",
                    "Detalle del Cliente - " + nombreCompleto,
                    (org.ni.edu.uam.casopracticog5.controller.DetalleClienteController controlador) -> controlador.cargarDatos(cliente)
            );
        } catch (IOException e) {
            mostrarAlertaError("No fue posible abrir el detalle del cliente: " + e.getMessage());
        }
    }

    /**
     * Navegación hacia AdminUsuariosView.fxml dentro del contenedor central.
     */
    @FXML
    public void onAbrirAdminUsuarios(ActionEvent event) {
        if (!confirmarDescarteCambiosSiAplica()) {
            return;
        }
        cargarVista("/org/ni/edu/uam/casopracticog5/view/AdminUsuariosView.fxml");
    }

    /**
     * Navegación hacia RegistroClienteView.fxml dentro del contenedor central.
     */
    @FXML
    public void onAbrirRegistro(ActionEvent event) {
        if (!confirmarDescarteCambiosSiAplica()) {
            return;
        }
        cargarVista("/org/ni/edu/uam/casopracticog5/view/RegistroClienteView.fxml");
    }

    /**
     * Navegación hacia ConsultaClientesView.fxml dentro del contenedor central.
     */
    @FXML
    public void onAbrirConsulta(ActionEvent event) {
        if (!confirmarDescarteCambiosSiAplica()) {
            return;
        }
        cargarVista("/org/ni/edu/uam/casopracticog5/view/ConsultaClientesView.fxml");
    }

    /**
     * Restablece la vista central al Dashboard restaurando la tarjeta de inicio.
     */
    @FXML
    public void onLimpiarVista(ActionEvent event) {
        if (!confirmarDescarteCambiosSiAplica()) {
            return;
        }
        if (cardBienvenida != null) {
            contentArea.getChildren().setAll(cardBienvenida);
            actualizarDashboard();
        } else {
            contentArea.getChildren().clear();
        }
        controladorActual = null;
    }

    @FXML
    public void onAcercaDe(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acerca de");
        alert.setHeaderText("Caso Práctico G5 - Control de Clientes");
        alert.setContentText("Módulo de Navegación, Menús y Exportación.\nUAM 2026.");
        if (rootPane != null && rootPane.getScene() != null && rootPane.getScene().getWindow() != null) {
            alert.initOwner(rootPane.getScene().getWindow());
        }
        alert.showAndWait();
    }

    @FXML
    public void onCerrarSesion(ActionEvent event) {
        if (!confirmarDescarteCambiosSiAplica()) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cerrar Sesión");
        alert.setHeaderText("Vas a cerrar la sesión actual");
        alert.setContentText("¿Estás seguro de que deseas volver a la pantalla de inicio de sesión?");
        if (rootPane != null && rootPane.getScene() != null && rootPane.getScene().getWindow() != null) {
            alert.initOwner(rootPane.getScene().getWindow());
        }
        Optional<ButtonType> respuesta = alert.showAndWait();
        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
            return;
        }
        DataStore.setUsuarioActual(null);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/ni/edu/uam/casopracticog5/view/LoginView.fxml"));
            Parent loginRoot = loader.load();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(loginRoot);
            stage.setScene(scene);
            stage.setTitle("Sistema de Gestión de Clientes");
            stage.sizeToScene();
            stage.centerOnScreen();
        } catch (IOException e) {
            mostrarAlertaError("No se pudo cargar la vista de Login: " + e.getMessage());
        }
    }

    @FXML
    public void onSalir(ActionEvent event) {
        if (!confirmarDescarteCambiosSiAplica()) {
            return;
        }
        Stage stage = rootPane != null && rootPane.getScene() != null
                ? (Stage) rootPane.getScene().getWindow() : null;
        if (SceneUtil.confirmarSalida(stage)) {
            Platform.exit();
        }
    }

    /**
     * Verifica si la vista actual tiene cambios pendientes sin guardar antes de navegar.
     *
     * @return true si es seguro navegar, false si el usuario canceló la navegación
     */
    private boolean confirmarDescarteCambiosSiAplica() {
        if (controladorActual instanceof RegistroClienteController registroCtrl) {
            if (registroCtrl.hayCambiosSinGuardar()) {
                Stage stage = rootPane != null && rootPane.getScene() != null
                        ? (Stage) rootPane.getScene().getWindow() : null;
                return SceneUtil.confirmarDescarteCambios(stage);
            }
        }
        return true;
    }

    /**
     * Método auxiliar para intercambiar subvistas dinámicamente en el centro.
     */
    private void cargarVista(String rutaFxml) {
        try {
            var url = getClass().getResource(rutaFxml);
            if (url == null) {
                mostrarAlertaError("Archivo FXML no encontrado:\n" + rutaFxml);
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Node nodo = loader.load();
            controladorActual = loader.getController();
            contentArea.getChildren().setAll(nodo);
        } catch (IOException e) {
            mostrarAlertaError("Error al cargar la vista:\n" + e.getMessage());
        }
    }

    private void mostrarAlertaError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Navegación");
        alert.setHeaderText("No fue posible cargar el recurso");
        alert.setContentText(mensaje);
        if (rootPane != null && rootPane.getScene() != null && rootPane.getScene().getWindow() != null) {
            alert.initOwner(rootPane.getScene().getWindow());
        }
        alert.showAndWait();
    }
}