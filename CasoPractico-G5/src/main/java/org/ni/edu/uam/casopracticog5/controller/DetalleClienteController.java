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

import java.io.File;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

/**
 * Controlador de DetalleClienteView.fxml (Integrante 4).
 * Recibe un objeto Cliente desde otra ventana y muestra su información detallada.
 */
public class DetalleClienteController {

    @FXML
    private Label lblTituloNombres;

    @FXML
    private Label lblNombres;

    @FXML
    private Label lblTituloApellidos;

    @FXML
    private Label lblApellidos;

    @FXML
    private Label lblTipoCliente;

    @FXML
    private Label lblCiudad;

    @FXML
    private Label lblTituloFecha;

    @FXML
    private Label lblFechaNacimiento;

    @FXML
    private Label lblTipoSolicitud;

    @FXML
    private ListView<String> lstServicios;

    @FXML
    private ImageView imgFotografia;

    @FXML
    private Label lblEstadoFoto;

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

        boolean esJuridico = "Jurídico".equalsIgnoreCase(cliente.getTipoCliente());

        // Adaptación semántica de etiquetas según Persona Natural o Jurídica
        if (lblTituloNombres != null) {
            lblTituloNombres.setText(esJuridico ? "Razón Social:" : "Nombres:");
        }
        if (lblTituloApellidos != null) {
            lblTituloApellidos.setText(esJuridico ? "Representante / Apellidos:" : "Apellidos:");
        }
        if (lblTituloFecha != null) {
            lblTituloFecha.setText(esJuridico ? "Fecha de Constitución:" : "Fecha de Nacimiento:");
        }

        lblNombres.setText(cliente.getNombres() != null ? cliente.getNombres() : "");
        lblApellidos.setText(cliente.getApellidos() != null && !cliente.getApellidos().isBlank()
                ? cliente.getApellidos()
                : "No aplica");
        lblTipoCliente.setText(cliente.getTipoCliente() != null ? cliente.getTipoCliente() : "");
        lblCiudad.setText(cliente.getCiudad() != null ? cliente.getCiudad() : "");

        // Formatear fecha de nacimiento consistente en dd/MM/yyyy y mostrar años calculados
        if (cliente.getFechaNacimiento() != null) {
            LocalDate fecha = cliente.getFechaNacimiento();
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            int anios = Period.between(fecha, LocalDate.now()).getYears();
            String sufijo = esJuridico ? " (" + anios + " años de constitución)" : " (" + anios + " años)";
            lblFechaNacimiento.setText(formato.format(fecha) + sufijo);
        } else {
            lblFechaNacimiento.setText("No disponible");
        }

        lblTipoSolicitud.setText(cliente.getTipoSolicitud() != null ? cliente.getTipoSolicitud() : "");

        // Cargar lista de servicios en el ListView
        if (cliente.getServicios() != null && !cliente.getServicios().isEmpty()) {
            lstServicios.setItems(FXCollections.observableArrayList(cliente.getServicios()));
        } else {
            lstServicios.setItems(FXCollections.observableArrayList("Sin servicios registrados"));
        }

        // Cargar fotografía del cliente o usar defaultUser.png si no tiene
        cargarFotografia(cliente.getRutaFotografia());
    }

    private void cargarFotografia(String ruta) {
        if (ruta != null && !ruta.isBlank()) {
            try {
                File archivoFoto = new File(ruta);
                if (archivoFoto.exists() && archivoFoto.canRead()) {
                    Image imagen = new Image(archivoFoto.toURI().toString());
                    if (!imagen.isError()) {
                        imgFotografia.setImage(imagen);
                        if (lblEstadoFoto != null) {
                            lblEstadoFoto.setText("");
                        }
                        return;
                    }
                }
            } catch (Exception e) {
                System.err.println("No se pudo cargar la fotografía personalizada: " + e.getMessage());
            }
        }
        cargarImagenPredeterminada();
    }

    private void cargarImagenPredeterminada() {
        try {
            var url = getClass().getResource("/org/ni/edu/uam/casopracticog5/images/defaultUser.png");
            if (url != null) {
                imgFotografia.setImage(new Image(url.toExternalForm()));
                if (lblEstadoFoto != null) {
                    lblEstadoFoto.setText("Foto predeterminada");
                }
            } else {
                imgFotografia.setImage(null);
            }
        } catch (Exception e) {
            imgFotografia.setImage(null);
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
