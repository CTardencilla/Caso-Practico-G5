package org.ni.edu.uam.casopracticog5.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.List;

/**
 * Almacén centralizado en memoria para persistencia de clientes, usuarios del sistema y sesión activa.
 */
public class DataStore {

    private static final ObservableList<Cliente> clientes = FXCollections.observableArrayList();
    private static final ObservableList<Usuario> usuarios = FXCollections.observableArrayList();
    private static Usuario usuarioActual;

    static {
        // Carga inicial de usuarios predeterminados
        usuarios.add(new Usuario("admin", "12345", "Administrador del Sistema", "ADMINISTRADOR", LocalDate.now(), true));
        usuarios.add(new Usuario("operador", "12345", "Operador de Registro", "OPERADOR", LocalDate.now(), true));

        // Carga inicial de clientes predeterminados solicitados
        clientes.add(new Cliente(
                "William Antonio",
                "Garcia Garcia",
                "Natural",
                "Managua",
                LocalDate.of(2003, 5, 14),
                "Nuevo servicio",
                List.of("Asesoría", "Soporte"),
                null
        ));
        clientes.add(new Cliente(
                "Andres Sebastian",
                "Gonzalez Maradiaga",
                "Natural",
                "León",
                LocalDate.of(2002, 8, 22),
                "Renovación",
                List.of("Capacitación", "Soporte"),
                null
        ));
        clientes.add(new Cliente(
                "Rafael",
                "Hernandez Sanchez",
                "Natural",
                "Granada",
                LocalDate.of(2001, 11, 30),
                "Nuevo servicio",
                List.of("Asesoría"),
                null
        ));
        clientes.add(new Cliente(
                "Caleb Jordan",
                "Tardencilla Alvarado",
                "Natural",
                "Masaya",
                LocalDate.of(2004, 2, 10),
                "Nuevo servicio",
                List.of("Asesoría", "Capacitación"),
                null
        ));
    }

    public static ObservableList<Cliente> getClientes() {
        return clientes;
    }

    public static ObservableList<Usuario> getUsuarios() {
        return usuarios;
    }

    public static Usuario getUsuarioActual() {
        if (usuarioActual == null && !usuarios.isEmpty()) {
            return usuarios.get(0); // Por defecto el administrador
        }
        return usuarioActual;
    }

    public static void setUsuarioActual(Usuario usuario) {
        usuarioActual = usuario;
    }

    /**
     * Valida credenciales contra la lista de usuarios activos.
     *
     * @param username Nombre de usuario
     * @param password Contraseña
     * @return El objeto Usuario si la autenticación fue exitosa y está activo, null en caso contrario
     */
    public static Usuario autenticar(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        String userTrim = username.trim();
        for (Usuario u : usuarios) {
            if (u.getUsername().equalsIgnoreCase(userTrim) && u.getPassword().equals(password) && u.isActivo()) {
                return u;
            }
        }
        return null;
    }

    public static Usuario buscarUsuario(String username) {
        if (username == null) return null;
        for (Usuario u : usuarios) {
            if (u.getUsername().equalsIgnoreCase(username.trim())) {
                return u;
            }
        }
        return null;
    }
}
