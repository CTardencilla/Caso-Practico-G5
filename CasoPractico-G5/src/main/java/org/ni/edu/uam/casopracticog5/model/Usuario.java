package org.ni.edu.uam.casopracticog5.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Modelo que representa un usuario del sistema con su rol y permisos de acceso.
 */
public class Usuario {

    private String username;
    private String password;
    private String nombreCompleto;
    private String rol; // "ADMINISTRADOR" o "OPERADOR"
    private LocalDate fechaCreacion;
    private boolean activo;

    public Usuario() {
        this.fechaCreacion = LocalDate.now();
        this.activo = true;
    }

    public Usuario(String username, String password, String nombreCompleto, String rol, LocalDate fechaCreacion, boolean activo) {
        this.username = username;
        this.password = password;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
        this.fechaCreacion = fechaCreacion != null ? fechaCreacion : LocalDate.now();
        this.activo = activo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    /**
     * Determina si el usuario tiene privilegios de administrador.
     *
     * @return true si el rol es ADMINISTRADOR, false en caso contrario
     */
    public boolean esAdmin() {
        return "ADMINISTRADOR".equalsIgnoreCase(this.rol);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(username != null ? username.toLowerCase() : null,
                usuario.username != null ? usuario.username.toLowerCase() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username != null ? username.toLowerCase() : null);
    }

    @Override
    public String toString() {
        return nombreCompleto + " (" + username + " - " + rol + ")";
    }
}
