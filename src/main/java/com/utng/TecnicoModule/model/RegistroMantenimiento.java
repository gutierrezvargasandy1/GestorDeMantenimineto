package com.utng.TecnicoModule.model;

import java.time.LocalDate;

/** Tabla: registros_mantenimiento (esquema real de DB.sql) */
public class RegistroMantenimiento {

    /** Coincide EXACTO con el ENUM tipo_mantenimiento de PostgreSQL. */
    public static final String[] TIPOS = { "Preventivo", "Correctivo" };

    private Long id;
    private Long idEquipo;
    private String equipoNombre; // resuelto por JOIN, no se persiste directo
    private String tipo;
    private String motivo;
    private LocalDate fecha;
    private LocalDate fechaProxima;
    private boolean mantenimientoRealizado; // registros_mantenimiento.mantenimiento_realizado (BOOLEAN)
    private String notasRealizado; // registros_mantenimiento.notas_realizado (nueva columna)
    private Long idUsuarioResponsable;
    private String usuarioResponsable; // resuelto por JOIN

    public RegistroMantenimiento() {
    }

    public RegistroMantenimiento(Long id, Long idEquipo, String equipoNombre, String tipo, String motivo,
            LocalDate fecha, LocalDate fechaProxima, boolean mantenimientoRealizado, String notasRealizado,
            Long idUsuarioResponsable, String usuarioResponsable) {
        this.id = id;
        this.idEquipo = idEquipo;
        this.equipoNombre = equipoNombre;
        this.tipo = tipo;
        this.motivo = motivo;
        this.fecha = fecha;
        this.fechaProxima = fechaProxima;
        this.mantenimientoRealizado = mantenimientoRealizado;
        this.notasRealizado = notasRealizado;
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

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String v) {
        this.motivo = v;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate v) {
        this.fecha = v;
    }

    public LocalDate getFechaProxima() {
        return fechaProxima;
    }

    public void setFechaProxima(LocalDate v) {
        this.fechaProxima = v;
    }

    public boolean isMantenimientoRealizado() {
        return mantenimientoRealizado;
    }

    public void setMantenimientoRealizado(boolean v) {
        this.mantenimientoRealizado = v;
    }

    public String getNotasRealizado() {
        return notasRealizado;
    }

    public void setNotasRealizado(String v) {
        this.notasRealizado = v;
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

    /**
     * Estado calculado: la BD no guarda "estado", solo el booleano + fecha_proxima.
     */
    public String getEstado() {
        if (mantenimientoRealizado)
            return "Completado";
        if (fechaProxima != null && fechaProxima.isBefore(LocalDate.now()))
            return "Vencido";
        return "Pendiente";
    }

    public boolean estaVencido() {
        return !mantenimientoRealizado && fechaProxima != null && fechaProxima.isBefore(LocalDate.now());
    }

    public String textoBusqueda() {
        return (id + " " + idEquipo + " " + equipoNombre + " " + tipo + " " + motivo + " "
                + (notasRealizado == null ? "" : notasRealizado) + " " + getEstado() + " "
                + usuarioResponsable).toLowerCase();
    }
}