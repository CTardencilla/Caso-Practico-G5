package org.ni.edu.uam.casopracticog5;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.ni.edu.uam.casopracticog5.util.SceneUtil;

import java.io.IOException;

/**
 * Punto de entrada principal de la aplicación JavaFX.
 * Carga la vista de Login como primera pantalla y administra el ciclo de vida de la ventana.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/ni/edu/uam/casopracticog5/view/LoginView.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("Sistema de Gestión de Clientes");
        primaryStage.setScene(scene);

        // Controlar el evento de cierre de ventana para solicitar confirmación
        primaryStage.setOnCloseRequest(event -> {
            if (!SceneUtil.confirmarSalida(primaryStage)) {
                event.consume();
            }
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
