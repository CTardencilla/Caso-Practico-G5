package org.ni.edu.uam.casopracticog5.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.ni.edu.uam.casopracticog5.model.Cliente;
import org.ni.edu.uam.casopracticog5.model.DataStore;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Controlador de ConsultaClientesView.fxml (Integrante 4).
 */
public class ConsultaClientesController {

    @FXML
    private TableView<Cliente> tablaClientes;

    @FXML
    private TableColumn<Cliente, String> colNombres;

    @FXML
    private TableColumn<Cliente, String> colApellidos;

    @FXML
    private TableColumn<Cliente, String> colTipoCliente;

    @FXML
    private TableColumn<Cliente, String> colCiudad;

    @FXML
    private TableColumn<Cliente, LocalDate> colFechaNacimiento;

    @FXML
    private TableColumn<Cliente, String> colTipoSolicitud;

    @FXML
    public void initialize() {
        // Configuración de columnas con PropertyValueFactory
        colNombres.setCellValueFactory(new PropertyValueFactory<>("nombres"));
        colApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colTipoCliente.setCellValueFactory(new PropertyValueFactory<>("tipoCliente"));
        colCiudad.setCellValueFactory(new PropertyValueFactory<>("ciudad"));
        colFechaNacimiento.setCellValueFactory(new PropertyValueFactory<>("fechaNacimiento"));
        colTipoSolicitud.setCellValueFactory(new PropertyValueFactory<>("tipoSolicitud"));

        // Formato consistente dd/MM/yyyy para la columna Fecha de Nacimiento
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        colFechaNacimiento.setCellFactory(col -> new TableCell<Cliente, LocalDate>() {
            @Override
            protected void updateItem(LocalDate fecha, boolean vacio) {
                super.updateItem(fecha, vacio);
                if (vacio || fecha == null) {
                    setText(null);
                } else {
                    setText(formatoFecha.format(fecha));
                }
            }
        });

        // Placeholder descriptivo para tabla vacía
        tablaClientes.setPlaceholder(new javafx.scene.control.Label("No hay clientes registrados en el sistema."));

        // Vincular la tabla con la lista observable centralizada del DataStore
        tablaClientes.setItems(DataStore.getClientes());

        // Evento MouseEvent: doble clic para abrir detalle del cliente
        tablaClientes.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2
                    && tablaClientes.getSelectionModel().getSelectedItem() != null) {
                Cliente clienteSeleccionado = tablaClientes.getSelectionModel().getSelectedItem();
                abrirVentanaDetalle(clienteSeleccionado);
            }
        });

        // Evento KeyEvent: tecla ENTER para abrir detalle del cliente
        tablaClientes.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == KeyCode.ENTER
                    && tablaClientes.getSelectionModel().getSelectedItem() != null) {
                Cliente clienteSeleccionado = tablaClientes.getSelectionModel().getSelectedItem();
                abrirVentanaDetalle(clienteSeleccionado);
            }
        });
    }

    /**
     * Abre la ventana modal de Detalle del Cliente, bloqueando la interacción con la ventana padre
     * y pasando el objeto Cliente al controlador receptor de forma segura.
     *
     * @param cliente el cliente seleccionado en la tabla
     */
    private void abrirVentanaDetalle(Cliente cliente) {
        try {
            javafx.stage.Window owner = tablaClientes.getScene() != null ? tablaClientes.getScene().getWindow() : null;
            String nombreCompleto = cliente.getApellidos() != null && !cliente.getApellidos().isBlank()
                    ? cliente.getNombres() + " " + cliente.getApellidos()
                    : cliente.getNombres();

            org.ni.edu.uam.casopracticog5.util.SceneUtil.abrirModal(
                    owner,
                    "/org/ni/edu/uam/casopracticog5/view/DetalleClienteView.fxml",
                    "Detalle del Cliente - " + nombreCompleto,
                    (DetalleClienteController controlador) -> controlador.cargarDatos(cliente)
            );

        } catch (IOException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            if (tablaClientes.getScene() != null && tablaClientes.getScene().getWindow() != null) {
                alerta.initOwner(tablaClientes.getScene().getWindow());
            }
            alerta.setTitle("Error");
            alerta.setHeaderText("No se pudo abrir la ventana de detalle");
            alerta.setContentText("Ocurrió un error al cargar la vista: " + e.getMessage());
            alerta.showAndWait();
        }
    }
}
