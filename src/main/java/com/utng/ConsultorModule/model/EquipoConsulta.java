package com.utng.ConsultorModule.model;

/**
 * Modelo de solo lectura para la pantalla del rol Consultor.
 * Los nombres de las propiedades coinciden con las columnas de la tabla:
 * id, equipos, modelo, procesador, memoria_ram, almacenamiento,
 * id_sistema_operativo, lugar, estado, anio_creacion,
 * id_usuario_responsable, fecha_creacion, fecha_actualizacion.
 *
 * Si ya tienes una clase Equipo en tu proyecto, puedes borrar esta y
 * cambiar el tipo generico de la tabla en el controlador.
 */
public class EquipoConsulta {

    private int id;
    private String equipos; 
    private String modelo;
    private String procesador;
    private String memoriaRam; 
    private String almacenamiento;
    private String idSistemaOperativo; 
    private String lugar;
    private String estado; 
    private int anioCreacion; 
    private String idUsuarioResponsable; 
    private String fechaCreacion; 
    private String fechaActualizacion; 

    public EquipoConsulta() {
    }

    public EquipoConsulta(int id, String equipos, String modelo, String procesador,
            String memoriaRam, String almacenamiento, String idSistemaOperativo,
            String lugar, String estado, int anioCreacion,
            String idUsuarioResponsable, String fechaCreacion, String fechaActualizacion) {
        this.id = id;
        this.equipos = equipos;
        this.modelo = modelo;
        this.procesador = procesador;
        this.memoriaRam = memoriaRam;
        this.almacenamiento = almacenamiento;
        this.idSistemaOperativo = idSistemaOperativo;
        this.lugar = lugar;
        this.estado = estado;
        this.anioCreacion = anioCreacion;
        this.idUsuarioResponsable = idUsuarioResponsable;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }

    public int getId() {
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

    public String getIdSistemaOperativo() {
        return idSistemaOperativo;
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

    public String getIdUsuarioResponsable() {
        return idUsuarioResponsable;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public String getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setEquipos(String equipos) {
        this.equipos = equipos;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public void setProcesador(String procesador) {
        this.procesador = procesador;
    }

    public void setMemoriaRam(String memoriaRam) {
        this.memoriaRam = memoriaRam;
    }

    public void setAlmacenamiento(String almacenamiento) {
        this.almacenamiento = almacenamiento;
    }

    public void setIdSistemaOperativo(String idSistemaOperativo) {
        this.idSistemaOperativo = idSistemaOperativo;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setAnioCreacion(int anioCreacion) {
        this.anioCreacion = anioCreacion;
    }

    public void setIdUsuarioResponsable(String v) {
        this.idUsuarioResponsable = v;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public void setFechaActualizacion(String fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    /** Texto plano usado por el buscador global de la tabla. */
    public String textoBusqueda() {
        return (id + " " + equipos + " " + modelo + " " + procesador + " " + memoriaRam + " "
                + almacenamiento + " " + idSistemaOperativo + " " + lugar + " " + estado + " "
                + anioCreacion + " " + idUsuarioResponsable + " " + fechaCreacion + " "
                + fechaActualizacion).toLowerCase();
    }

    @Override
    public String toString() {
        return equipos + " (" + modelo + ")";
    }
}