package org.ni.edu.uam.casopracticog5.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;

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
