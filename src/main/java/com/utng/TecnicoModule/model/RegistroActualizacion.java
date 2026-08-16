package com.utng.TecnicoModule.model;

import java.time.LocalDate;

/** Tabla: registros_actualizaciones */
public class RegistroActualizacion {

    public static final String[] TIPOS = {
            "Sistema operativo", "Programa", "Driver", "Firmware", "Parche de seguridad"
    };

    private int id;
    private int idEquipo; // id_equipo (FK)
    private String equipoNombre;
    private String tipo;
    private String nombreActualizado; // nombre_actualizado
    private String versionActual; // version_actual (antes)
    private String versionActualizada; // version_actualizada (despues)
    private LocalDate fecha;
    private int idUsuarioResponsable;
    private String usuarioResponsable;
    private LocalDate fechaRegistro; // fecha_registro

    public RegistroActualizacion() {
    }

    public RegistroActualizacion(int id, int idEquipo, String equipoNombre, String tipo,
            String nombreActualizado, String versionActual, String versionActualizada,
            LocalDate fecha, int idUsuarioResponsable, String usuarioResponsable,
            LocalDate fechaRegistro) {
        this.id = id;
        this.idEquipo = idEquipo;
        this.equipoNombre = equipoNombre;
        this.tipo = tipo;
        this.nombreActualizado = nombreActualizado;
        this.versionActual = versionActual;
        this.versionActualizada = versionActualizada;
        this.fecha = fecha;
        this.idUsuarioResponsable = idUsuarioResponsable;
        this.usuarioResponsable = usuarioResponsable;
        this.fechaRegistro = fechaRegistro;
    }

    public int getId() {
        return id;
    }

    public int getIdEquipo() {
        return idEquipo;
    }

    public String getEquipoNombre() {
        return equipoNombre;
    }

    public String getTipo() {
        return tipo;
    }

    public String getNombreActualizado() {
        return nombreActualizado;
    }

    public String getVersionActual() {
        return versionActual;
    }

    public String getVersionActualizada() {
        return versionActualizada;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public int getIdUsuarioResponsable() {
        return idUsuarioResponsable;
    }

    public String getUsuarioResponsable() {
        return usuarioResponsable;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setId(int v) {
        this.id = v;
    }

    public void setIdEquipo(int v) {
        this.idEquipo = v;
    }

    public void setEquipoNombre(String v) {
        this.equipoNombre = v;
    }

    public void setTipo(String v) {
        this.tipo = v;
    }

    public void setNombreActualizado(String v) {
        this.nombreActualizado = v;
    }

    public void setVersionActual(String v) {
        this.versionActual = v;
    }

    public void setVersionActualizada(String v) {
        this.versionActualizada = v;
    }

    public void setFecha(LocalDate v) {
        this.fecha = v;
    }

    public void setIdUsuarioResponsable(int v) {
        this.idUsuarioResponsable = v;
    }

    public void setUsuarioResponsable(String v) {
        this.usuarioResponsable = v;
    }

    public void setFechaRegistro(LocalDate v) {
        this.fechaRegistro = v;
    }

    public String textoBusqueda() {
        return (id + " " + idEquipo + " " + equipoNombre + " " + tipo + " " + nombreActualizado + " "
                + versionActual + " " + versionActualizada + " " + usuarioResponsable).toLowerCase();
    }
}
