package org.ni.edu.uam.casopracticog5.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.ni.edu.uam.casopracticog5.model.Cliente;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

/**
 * Controlador de DetalleClienteView.fxml (Integrante 4).
 * Recibe un objeto Cliente desde otra ventana y muestra su información detallada.
 */
public class DetalleClienteController {

    @FXML
    private Label lblNombres;

    @FXML
    private Label lblApellidos;

    @FXML
    private Label lblTipoCliente;

    @FXML
    private Label lblCiudad;

    @FXML
    private Label lblFechaNacimiento;

    @FXML
    private Label lblTipoSolicitud;

    @FXML
    private ListView<String> lstServicios;

    @FXML
    private ImageView imgFotografia;

    @FXML
    private Button btnCerrar;

    @FXML
    public void initialize() {
        // La inicialización de datos se realiza mediante cargarDatos(Cliente)
    }

    /**
     * Método público para recibir un objeto Cliente desde otra ventana
     * y poblar todos los componentes visuales con su información.
     *
     * @param cliente el cliente cuyos datos se mostrarán en la vista
     */
    public void cargarDatos(Cliente cliente) {
        if (cliente == null) {
            return;
        }

        lblNombres.setText(cliente.getNombres());
        lblApellidos.setText(cliente.getApellidos());
        lblTipoCliente.setText(cliente.getTipoCliente());
        lblCiudad.setText(cliente.getCiudad());

        // Formatear fecha de nacimiento consistente en dd/MM/yyyy y mostrar edad calculada
        if (cliente.getFechaNacimiento() != null) {
            LocalDate fecha = cliente.getFechaNacimiento();
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            int edad = Period.between(fecha, LocalDate.now()).getYears();
            lblFechaNacimiento.setText(formato.format(fecha) + " (" + edad + " años)");
        } else {
            lblFechaNacimiento.setText("No disponible");
        }

        lblTipoSolicitud.setText(cliente.getTipoSolicitud());

        // Cargar lista de servicios en el ListView
        if (cliente.getServicios() != null && !cliente.getServicios().isEmpty()) {
            lstServicios.setItems(FXCollections.observableArrayList(cliente.getServicios()));
        } else {
            lstServicios.setItems(FXCollections.observableArrayList("Sin servicios registrados"));
        }

        // Cargar fotografía si la ruta es válida
        if (cliente.getRutaFotografia() != null && !cliente.getRutaFotografia().isEmpty()) {
            try {
                Image imagen = new Image("file:" + cliente.getRutaFotografia());
                imgFotografia.setImage(imagen);
            } catch (Exception e) {
                System.err.println("No se pudo cargar la fotografía: " + e.getMessage());
            }
        }
    }

    /**
     * Cierra la ventana actual al presionar el botón Cerrar.
     */
    @FXML
    private void cerrarVentana() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }
}
