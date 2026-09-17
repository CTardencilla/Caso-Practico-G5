package org.ni.edu.uam.casopracticog5.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;


public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtPassword;

    @FXML
    public void iniciarSesion() {

        String usuario = txtUsuario.getText();
        String password = txtPassword.getText();

        if (usuario == null || usuario.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Valicación", "Por favor, completa los campos solicitados.");
        }else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/ni/edu/uam/casopracticog5/view/MainView.fxml"));
                Parent root = loader.load();

                Stage stageActual = (Stage) txtUsuario.getScene().getWindow();
                Scene scene = new Scene(root);

                stageActual.setScene(scene);
                stageActual.setTitle("Ventana Principal");
                stageActual.show();
            } catch (IOException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Valicación", "No se pudo cargar la ventana principal.");
            }
        }
    }
}
