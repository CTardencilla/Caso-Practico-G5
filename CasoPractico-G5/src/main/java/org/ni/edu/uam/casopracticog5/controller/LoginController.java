package org.ni.edu.uam.casopracticog5.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private AnchorPane panelPrincipal;

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtPassword;

    @FXML
    public void detectarEnter(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            // Si el foco está en el usuario y la contraseña está vacía, pasa el foco a la contraseña
            if (event.getSource() == txtUsuario && (txtPassword.getText() == null || txtPassword.getText().trim().isEmpty())) {
                txtPassword.requestFocus();
            } else {
                iniciarSesion();
            }
        }
    }

    /**
     * Valida si las credenciales coinciden con las autorizadas en el sistema.
     * Método público para facilitar la verificación y pruebas unitarias.
     *
     * @param usuario  Nombre de usuario
     * @param password Contraseña
     * @return true si las credenciales son válidas, false en caso contrario
     */
    public boolean autenticar(String usuario, String password) {
        if (usuario == null || password == null) {
            return false;
        }
        return usuario.trim().equals("admin") && password.equals("12345");
    }

    @FXML
    public void iniciarSesion() {
        String usuario = txtUsuario.getText() != null ? txtUsuario.getText().trim() : "";
        String password = txtPassword.getText() != null ? txtPassword.getText() : "";

        if (usuario.isEmpty() || password.trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Error de Validación", "Por favor, completa todos los campos para iniciar sesión.");
            if (usuario.isEmpty()) {
                txtUsuario.requestFocus();
            } else {
                txtPassword.requestFocus();
            }
            return;
        }

        if (autenticar(usuario, password)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/ni/edu/uam/casopracticog5/view/MainView.fxml"));
                Parent root = loader.load();

                Stage stageActual = (Stage) txtUsuario.getScene().getWindow();
                Scene scene = new Scene(root);

                stageActual.setScene(scene);
                stageActual.setTitle("Sistema de Gestión de Clientes - Principal");
                stageActual.sizeToScene();
                stageActual.centerOnScreen();
                stageActual.show();
            } catch (IOException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error del Sistema", "No se pudo cargar la ventana principal: " + e.getMessage());
            }
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Autenticación", "Usuario o contraseña incorrectos. Por favor, verifica tus datos.");
            txtPassword.clear();
            txtPassword.requestFocus();
        }
    }

    @FXML
    public void salir() {
        javafx.stage.Window owner = txtUsuario != null && txtUsuario.getScene() != null
                ? txtUsuario.getScene().getWindow() : null;
        if (org.ni.edu.uam.casopracticog5.util.SceneUtil.confirmarSalida(owner)) {
            System.exit(0);
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);

        if (txtUsuario != null && txtUsuario.getScene() != null && txtUsuario.getScene().getWindow() != null) {
            alert.initOwner(txtUsuario.getScene().getWindow());
        }

        alert.showAndWait();
    }
}