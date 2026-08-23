package com.utng.TecnicoModule.model;

import java.time.LocalDate;

/** Tabla: registros_actualizaciones (esquema real de DB.sql) */
public class RegistroActualizacion {

    /** Coincide EXACTO con el ENUM tipo_actualizacion de PostgreSQL. */
    public static final String[] TIPOS = { "Sistema operativo", "Programa", "Firmware", "Driver" };

    private Long id;
    private Long idEquipo;
    private String equipoNombre;
    private String tipo;
    private String nombreActualizado;
    private String versionActual;
    private String versionActualizada;
    private LocalDate fecha;
    private Long idUsuarioResponsable;
    private String usuarioResponsable;

    public RegistroActualizacion() {
    }

    public RegistroActualizacion(Long id, Long idEquipo, String equipoNombre, String tipo,
            String nombreActualizado, String versionActual, String versionActualizada,
            LocalDate fecha, Long idUsuarioResponsable, String usuarioResponsable) {
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
    }

    public Long getId() {
        return id;
    }

    public void setId(Long v) {
        this.id = v;
    }

    public Long getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(Long v) {
        this.idEquipo = v;
    }

    public String getEquipoNombre() {
        return equipoNombre;
    }

    public void setEquipoNombre(String v) {
        this.equipoNombre = v;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String v) {
        this.tipo = v;
    }

    public String getNombreActualizado() {
        return nombreActualizado;
    }

    public void setNombreActualizado(String v) {
        this.nombreActualizado = v;
    }

    public String getVersionActual() {
        return versionActual;
    }

    public void setVersionActual(String v) {
        this.versionActual = v;
    }

    public String getVersionActualizada() {
        return versionActualizada;
    }

    public void setVersionActualizada(String v) {
        this.versionActualizada = v;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate v) {
        this.fecha = v;
    }

    public Long getIdUsuarioResponsable() {
        return idUsuarioResponsable;
    }

    public void setIdUsuarioResponsable(Long v) {
        this.idUsuarioResponsable = v;
    }

    public String getUsuarioResponsable() {
        return usuarioResponsable;
    }

    public void setUsuarioResponsable(String v) {
        this.usuarioResponsable = v;
    }

    public String textoBusqueda() {
        return (id + " " + idEquipo + " " + equipoNombre + " " + tipo + " " + nombreActualizado + " "
                + versionActual + " " + versionActualizada + " " + usuarioResponsable).toLowerCase();
    }
}