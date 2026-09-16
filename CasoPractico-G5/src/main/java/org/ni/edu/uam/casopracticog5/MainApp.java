package org.ni.edu.uam.casopracticog5;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Punto de entrada principal de la aplicación JavaFX.
 * Carga la vista de Login como primera pantalla.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/ni/edu/uam/casopracticog5/view/LoginView.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("Sistema de Gestión de Clientes");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
