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
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class MainController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private StackPane contentArea;

    @FXML
    private Label lblBienvenida;

    @FXML
    private Label lblRutaCarpeta;

    private File carpetaSeleccionada;

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

        Optional<String> resultado = dialog.showAndWait();
        resultado.ifPresent(observacion -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Confirmación");
            alert.setHeaderText("Observación Registrada");
            alert.setContentText("Texto ingresado: " + observacion);
            alert.showAndWait();
        });
    }

    /**
     * Navegación hacia RegistroClienteView.fxml dentro del contenedor central.
     */
    @FXML
    public void onAbrirRegistro(ActionEvent event) {
        cargarVista("/org/ni/edu/uam/casopracticog5/view/RegistroClienteView.fxml");
    }

    /**
     * Navegación hacia ConsultaClientesView.fxml dentro del contenedor central.
     */
    @FXML
    public void onAbrirConsulta(ActionEvent event) {
        cargarVista("/org/ni/edu/uam/casopracticog5/view/ConsultaClientesView.fxml");
    }

    /**
     * Restablece la vista central al mensaje original.
     */
    @FXML
    public void onLimpiarVista(ActionEvent event) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(lblBienvenida);
    }

    @FXML
    public void onAcercaDe(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acerca de");
        alert.setHeaderText("Caso Práctico G5 - Control de Clientes");
        alert.setContentText("Módulo de Navegación, Menús y Exportación.\nUAM 2026.");
        alert.showAndWait();
    }

    @FXML
    public void onCerrarSesion(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/ni/edu/uam/casopracticog5/view/LoginView.fxml"));
            Parent loginRoot = loader.load();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.getScene().setRoot(loginRoot);
        } catch (IOException e) {
            mostrarAlertaError("No se pudo cargar la vista de Login: " + e.getMessage());
        }
    }

    @FXML
    public void onSalir(ActionEvent event) {
        Platform.exit();
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
            Node nodo = FXMLLoader.load(url);
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
        alert.showAndWait();
    }
}