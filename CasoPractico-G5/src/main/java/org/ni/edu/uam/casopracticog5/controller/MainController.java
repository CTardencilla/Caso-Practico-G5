package org.ni.edu.uam.casopracticog5.controller;

import java.io.IOException;
import java.util.Optional;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
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
    private Label lblSesionUsuario;

    private Object controladorActual;

    @FXML
    public void initialize() {
        var usuario = org.ni.edu.uam.casopracticog5.model.DataStore.getUsuarioActual();
        if (lblSesionUsuario != null && usuario != null) {
            lblSesionUsuario.setText(usuario.getUsername() + " (" + usuario.getRol() + ")");
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
     * Restablece la vista central al mensaje original restaurando la tarjeta completa de inicio.
     */
    @FXML
    public void onLimpiarVista(ActionEvent event) {
        if (!confirmarDescarteCambiosSiAplica()) {
            return;
        }
        if (cardBienvenida != null) {
            contentArea.getChildren().setAll(cardBienvenida);
        } else {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(lblBienvenida);
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