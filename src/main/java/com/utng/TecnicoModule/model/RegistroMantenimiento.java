package com.utng.TecnicoModule.model;

import java.time.LocalDate;

/** Tabla: registros_mantenimiento */
public class RegistroMantenimiento {

    public static final String[] TIPOS = {
            "Preventivo", "Correctivo", "Limpieza", "Instalacion", "Revision"
    };
    public static final String[] ESTADOS = {
            "Pendiente", "En proceso", "Completado", "Cancelado"
    };

    private int id;
    private int idEquipo; // id_equipo (FK)
    private String equipoNombre; // resuelto por JOIN
    private String tipo;
    private String motivo;
    private LocalDate fecha;
    private LocalDate fechaProxima; // fecha_proxima
    private String mantenimientoRealizado; // mantenimiento_realizado
    private String estado;
    private int idUsuarioResponsable; // id_usuario_responsable (el tecnico)
    private String usuarioResponsable;
    private LocalDate fechaRegistro; // fecha_registro

    public RegistroMantenimiento() {
    }

    public RegistroMantenimiento(int id, int idEquipo, String equipoNombre, String tipo, String motivo,
            LocalDate fecha, LocalDate fechaProxima, String mantenimientoRealizado,
            String estado, int idUsuarioResponsable, String usuarioResponsable,
            LocalDate fechaRegistro) {
        this.id = id;
        this.idEquipo = idEquipo;
        this.equipoNombre = equipoNombre;
        this.tipo = tipo;
        this.motivo = motivo;
        this.fecha = fecha;
        this.fechaProxima = fechaProxima;
        this.mantenimientoRealizado = mantenimientoRealizado;
        this.estado = estado;
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

    public String getMotivo() {
        return motivo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalDate getFechaProxima() {
        return fechaProxima;
    }

    public String getMantenimientoRealizado() {
        return mantenimientoRealizado;
    }

    public String getEstado() {
        return estado;
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

    public void setMotivo(String v) {
        this.motivo = v;
    }

    public void setFecha(LocalDate v) {
        this.fecha = v;
    }

    public void setFechaProxima(LocalDate v) {
        this.fechaProxima = v;
    }

    public void setMantenimientoRealizado(String v) {
        this.mantenimientoRealizado = v;
    }

    public void setEstado(String v) {
        this.estado = v;
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
        return (id + " " + idEquipo + " " + equipoNombre + " " + tipo + " " + motivo + " "
                + mantenimientoRealizado + " " + estado + " " + usuarioResponsable).toLowerCase();
    }

    /** true si la fecha_proxima ya paso y el registro sigue abierto. */
    public boolean estaVencido() {
        return fechaProxima != null
                && fechaProxima.isBefore(LocalDate.now())
                && !"Completado".equalsIgnoreCase(estado)
                && !"Cancelado".equalsIgnoreCase(estado);
    }
}
