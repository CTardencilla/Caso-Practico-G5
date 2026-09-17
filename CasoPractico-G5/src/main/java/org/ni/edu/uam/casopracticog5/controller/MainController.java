package org.ni.edu.uam.casopracticog5.controller;

import java.io.File;
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
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
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
    private Label lblRutaCarpeta;

    private File carpetaSeleccionada;
    private Object controladorActual;

    @FXML
    public void initialize() {
        // Inicialización de estado predeterminado
    }

    /**
     * Requisito: DirectoryChooser para seleccionar carpeta de respaldos/reportes
     * y reflejar la ruta en el Label de la barra inferior.
     */
    @FXML
    public void onSeleccionarCarpeta(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar Carpeta para Respaldos y Reportes");

        if (carpetaSeleccionada != null && carpetaSeleccionada.exists()) {
            directoryChooser.setInitialDirectory(carpetaSeleccionada);
        }

        Stage stage = (Stage) rootPane.getScene().getWindow();
        File folder = directoryChooser.showDialog(stage);

        if (folder != null) {
            carpetaSeleccionada = folder;
            lblRutaCarpeta.setText(folder.getAbsolutePath());
        }
    }

    /**
     * Requisito: Dialog tipo TextInputDialog para solicitar datos al usuario.
     */
    @FXML
    public void onSolicitarObservaciones(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Observaciones del Sistema");
        dialog.setHeaderText("Gestión de Respaldos / Notas");
        dialog.setContentText("Ingrese las observaciones o prefijo:");
        if (rootPane != null && rootPane.getScene() != null && rootPane.getScene().getWindow() != null) {
            dialog.initOwner(rootPane.getScene().getWindow());
        }

        Optional<String> resultado = dialog.showAndWait();
        resultado.ifPresent(observacion -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Confirmación");
            alert.setHeaderText("Observación Registrada");
            alert.setContentText("Texto ingresado: " + observacion);
            if (rootPane != null && rootPane.getScene() != null && rootPane.getScene().getWindow() != null) {
                alert.initOwner(rootPane.getScene().getWindow());
            }
            alert.showAndWait();
        });
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