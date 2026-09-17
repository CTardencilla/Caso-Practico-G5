package org.ni.edu.uam.casopracticog5.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Paquete de utilidades compartidas para navegación y ciclo de vida de ventanas y diálogos.
 */
public final class SceneUtil {

    private SceneUtil() {
        // Constructor privado para clase de utilidad
    }

    /**
     * Muestra un diálogo de confirmación de salida centrado en la ventana propietaria.
     *
     * @param owner Ventana propietaria del diálogo
     * @return true si el usuario confirmó salir, false en caso contrario
     */
    public static boolean confirmarSalida(Window owner) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación de Salida");
        alert.setHeaderText("Estás a punto de salir del sistema...");
        alert.setContentText("¿Estás seguro de que deseas salir del programa?");
        if (owner != null) {
            alert.initOwner(owner);
        }
        Optional<ButtonType> respuesta = alert.showAndWait();
        return respuesta.isPresent() && respuesta.get() == ButtonType.OK;
    }

    /**
     * Muestra un diálogo de confirmación cuando existen datos sin guardar.
     *
     * @param owner Ventana propietaria del diálogo
     * @return true si el usuario acepta descartar cambios, false para permanecer
     */
    public static boolean confirmarDescarteCambios(Window owner) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cambios sin guardar");
        alert.setHeaderText("Existen datos sin guardar en el formulario");
        alert.setContentText("¿Deseas descartar los cambios y continuar con la navegación?");
        if (owner != null) {
            alert.initOwner(owner);
        }
        Optional<ButtonType> respuesta = alert.showAndWait();
        return respuesta.isPresent() && respuesta.get() == ButtonType.OK;
    }

    /**
     * Abre una ventana modal bloqueando la interacción con la ventana padre
     * hasta que se cierre, asegurando un ciclo de vida limpio y sin duplicidad.
     *
     * @param owner        Ventana propietaria
     * @param rutaFxml     Ruta al archivo FXML
     * @param titulo       Título de la ventana modal
     * @param configurador Callback opcional para configurar el controlador
     * @param <T>          Tipo del controlador
     * @return El controlador instanciado o null si falló
     * @throws IOException Si ocurre un error al cargar el FXML
     */
    public static <T> T abrirModal(Window owner, String rutaFxml, String titulo, Consumer<T> configurador) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneUtil.class.getResource(rutaFxml));
        Parent root = loader.load();
        T controlador = loader.getController();

        if (configurador != null && controlador != null) {
            configurador.accept(controlador);
        }

        Stage stage = new Stage();
        stage.setTitle(titulo);
        if (owner != null) {
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
        } else {
            stage.initModality(Modality.APPLICATION_MODAL);
        }
        stage.setResizable(false);
        stage.setScene(new Scene(root));
        stage.showAndWait();

        return controlador;
    }
}
