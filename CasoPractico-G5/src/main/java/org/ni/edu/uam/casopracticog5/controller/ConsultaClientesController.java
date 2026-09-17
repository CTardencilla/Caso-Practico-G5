package org.ni.edu.uam.casopracticog5.controller;

import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;
import org.ni.edu.uam.casopracticog5.model.Cliente;
import org.ni.edu.uam.casopracticog5.model.DataStore;
import org.ni.edu.uam.casopracticog5.util.SceneUtil;

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
    private TextField txtBuscar;

    @FXML
    private Label lblContador;

    @FXML
    private Button btnVerDetalle;

    private FilteredList<Cliente> clientesFiltrados;

    @FXML
    public void initialize() {
        // Configuración de columnas con PropertyValueFactory
        colNombres.setCellValueFactory(new PropertyValueFactory<>("nombres"));
        colApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colTipoCliente.setCellValueFactory(new PropertyValueFactory<>("tipoCliente"));
        colCiudad.setCellValueFactory(new PropertyValueFactory<>("ciudad"));
        colFechaNacimiento.setCellValueFactory(new PropertyValueFactory<>("fechaNacimiento"));
        colTipoSolicitud.setCellValueFactory(new PropertyValueFactory<>("tipoSolicitud"));

        // Formato consistente en apellidos: si es vacío o nulo (Persona Jurídica), mostrar "No aplica"
        colApellidos.setCellFactory(col -> new TableCell<Cliente, String>() {
            @Override
            protected void updateItem(String apellido, boolean vacio) {
                super.updateItem(apellido, vacio);
                if (vacio) {
                    setText(null);
                    setStyle("");
                } else if (apellido == null || apellido.isBlank()) {
                    setText("No aplica");
                    setStyle("-fx-alignment: CENTER; -fx-text-fill: #9E9E9E; -fx-font-style: italic;");
                } else {
                    setText(apellido);
                    setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: #333333;");
                }
            }
        });

        // Formato consistente dd/MM/yyyy para la columna Fecha de Nacimiento
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        colFechaNacimiento.setCellFactory(col -> new TableCell<Cliente, LocalDate>() {
            @Override
            protected void updateItem(LocalDate fecha, boolean vacio) {
                super.updateItem(fecha, vacio);
                if (vacio) {
                    setText(null);
                    setStyle("");
                } else if (fecha == null) {
                    setText("—");
                    setStyle("-fx-alignment: CENTER; -fx-text-fill: #9E9E9E;");
                } else {
                    setText(formatoFecha.format(fecha));
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        // Placeholder descriptivo para tabla vacía
        tablaClientes.setPlaceholder(new Label("No hay clientes registrados en el sistema."));

        // Filtro reactivo en tiempo real
        clientesFiltrados = new FilteredList<>(DataStore.getClientes(), p -> true);

        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, viejo, nuevo) -> {
                clientesFiltrados.setPredicate(cliente -> {
                    if (nuevo == null || nuevo.isBlank()) {
                        return true;
                    }
                    String criterio = nuevo.toLowerCase().trim();
                    boolean coincideNombre = cliente.getNombres() != null && cliente.getNombres().toLowerCase().contains(criterio);
                    boolean coincideApellido = cliente.getApellidos() != null && cliente.getApellidos().toLowerCase().contains(criterio);
                    boolean coincideCiudad = cliente.getCiudad() != null && cliente.getCiudad().toLowerCase().contains(criterio);
                    boolean coincideTipo = cliente.getTipoCliente() != null && cliente.getTipoCliente().toLowerCase().contains(criterio);
                    boolean coincideSolicitud = cliente.getTipoSolicitud() != null && cliente.getTipoSolicitud().toLowerCase().contains(criterio);
                    return coincideNombre || coincideApellido || coincideCiudad || coincideTipo || coincideSolicitud;
                });
                actualizarContador();
            });
        }

        SortedList<Cliente> clientesOrdenados = new SortedList<>(clientesFiltrados);
        clientesOrdenados.comparatorProperty().bind(tablaClientes.comparatorProperty());
        tablaClientes.setItems(clientesOrdenados);

        // Control de activación del botón Ver Detalle según selección
        if (btnVerDetalle != null) {
            btnVerDetalle.disableProperty().bind(tablaClientes.getSelectionModel().selectedItemProperty().isNull());
        }

        actualizarContador();

        // Escuchar cambios en la lista base de DataStore para actualizar el contador dinámicamente
        DataStore.getClientes().addListener((ListChangeListener<Cliente>) c -> actualizarContador());

        // Evento MouseEvent: doble clic para abrir detalle del cliente
        tablaClientes.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2 && tablaClientes.getSelectionModel().getSelectedItem() != null) {
                abrirVentanaDetalle(tablaClientes.getSelectionModel().getSelectedItem());
            }
        });

        // Evento KeyEvent: tecla ENTER para abrir detalle del cliente
        tablaClientes.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == KeyCode.ENTER && tablaClientes.getSelectionModel().getSelectedItem() != null) {
                abrirVentanaDetalle(tablaClientes.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void actualizarContador() {
        if (lblContador != null) {
            int total = DataStore.getClientes().size();
            int visibles = clientesFiltrados != null ? clientesFiltrados.size() : total;
            if (visibles == total) {
                lblContador.setText("Total: " + total + " cliente" + (total != 1 ? "s" : ""));
            } else {
                lblContador.setText("Mostrando " + visibles + " de " + total + " clientes");
            }
        }
    }

    @FXML
    public void onVerDetalle(ActionEvent event) {
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            abrirVentanaDetalle(seleccionado);
        }
    }

    /**
     * Abre la ventana modal de Detalle del Cliente, bloqueando la interacción con la ventana padre
     * y pasando el objeto Cliente al controlador receptor de forma segura.
     *
     * @param cliente el cliente seleccionado en la tabla
     */
    private void abrirVentanaDetalle(Cliente cliente) {
        try {
            Window owner = tablaClientes.getScene() != null ? tablaClientes.getScene().getWindow() : null;
            String nombreCompleto = cliente.getApellidos() != null && !cliente.getApellidos().isBlank()
                    ? cliente.getNombres() + " " + cliente.getApellidos()
                    : cliente.getNombres();

            SceneUtil.abrirModal(
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
