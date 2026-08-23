package com.utng.TecnicoModule.model;

import java.time.LocalDate;

/** Tabla: equipos */
public class Equipo {

    private Long id;
    private String equipos; // nombre / etiqueta
    private String modelo;
    private String procesador;
    private String memoriaRam; // memoria_ram
    private String almacenamiento;
    private int idSistemaOperativo; // id_sistema_operativo (FK)
    private String sistemaOperativo; // nombre resuelto por JOIN
    private String lugar;
    private String estado;
    private int anioCreacion; // anio_creacion
    private int idUsuarioResponsable; // id_usuario_responsable (FK)
    private String usuarioResponsable; // nombre resuelto por JOIN
    private LocalDate fechaCreacion; // fecha_creacion
    private LocalDate fechaActualizacion; // fecha_actualizacion

    public Equipo() {
    }

    public Equipo(Long id, String equipos, String modelo, String procesador, String memoriaRam,
            String almacenamiento, int idSistemaOperativo, String sistemaOperativo,
            String lugar, String estado, int anioCreacion, int idUsuarioResponsable,
            String usuarioResponsable, LocalDate fechaCreacion, LocalDate fechaActualizacion) {
        this.id = id;
        this.equipos = equipos;
        this.modelo = modelo;
        this.procesador = procesador;
        this.memoriaRam = memoriaRam;
        this.almacenamiento = almacenamiento;
        this.idSistemaOperativo = idSistemaOperativo;
        this.sistemaOperativo = sistemaOperativo;
        this.lugar = lugar;
        this.estado = estado;
        this.anioCreacion = anioCreacion;
        this.idUsuarioResponsable = idUsuarioResponsable;
        this.usuarioResponsable = usuarioResponsable;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }

    public Long getId() {
        return id;
    }

    public String getEquipos() {
        return equipos;
    }

    public String getModelo() {
        return modelo;
    }

    public String getProcesador() {
        return procesador;
    }

    public String getMemoriaRam() {
        return memoriaRam;
    }

    public String getAlmacenamiento() {
        return almacenamiento;
    }

    public int getIdSistemaOperativo() {
        return idSistemaOperativo;
    }

    public String getSistemaOperativo() {
        return sistemaOperativo;
    }

    public String getLugar() {
        return lugar;
    }

    public String getEstado() {
        return estado;
    }

    public int getAnioCreacion() {
        return anioCreacion;
    }

    public int getIdUsuarioResponsable() {
        return idUsuarioResponsable;
    }

    public String getUsuarioResponsable() {
        return usuarioResponsable;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDate getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setId(long v) {
        this.id = v;
    }

    public void setEquipos(String v) {
        this.equipos = v;
    }

    public void setModelo(String v) {
        this.modelo = v;
    }

    public void setProcesador(String v) {
        this.procesador = v;
    }

    public void setMemoriaRam(String v) {
        this.memoriaRam = v;
    }

    public void setAlmacenamiento(String v) {
        this.almacenamiento = v;
    }

    public void setIdSistemaOperativo(int v) {
        this.idSistemaOperativo = v;
    }

    public void setSistemaOperativo(String v) {
        this.sistemaOperativo = v;
    }

    public void setLugar(String v) {
        this.lugar = v;
    }

    public void setEstado(String v) {
        this.estado = v;
    }

    public void setAnioCreacion(int v) {
        this.anioCreacion = v;
    }

    public void setIdUsuarioResponsable(int v) {
        this.idUsuarioResponsable = v;
    }

    public void setUsuarioResponsable(String v) {
        this.usuarioResponsable = v;
    }

    public void setFechaCreacion(LocalDate v) {
        this.fechaCreacion = v;
    }

    public void setFechaActualizacion(LocalDate v) {
        this.fechaActualizacion = v;
    }

    public String textoBusqueda() {
        return (id + " " + equipos + " " + modelo + " " + procesador + " " + memoriaRam + " "
                + almacenamiento + " " + sistemaOperativo + " " + lugar + " " + estado + " "
                + anioCreacion + " " + usuarioResponsable).toLowerCase();
    }

    /** Etiqueta corta para los ComboBox de los dialogos. */
    @Override
    public String toString() {
        return "#" + id + " - " + equipos + " (" + modelo + ")";
    }
}
