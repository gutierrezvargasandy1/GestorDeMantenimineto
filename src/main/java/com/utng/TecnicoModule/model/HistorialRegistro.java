package com.utng.TecnicoModule.model;

import java.time.LocalDate;

/**
 * Tabla: historial_registros
 * Traza cada alta/cambio: apunta a un registro de mantenimiento O a uno de
 * actualizacion (el que no aplique queda en 0 / NULL).
 */
public class HistorialRegistro {

    private int id;
    private int idEquipo; // id_equipo
    private String equipoNombre;
    private String tipo; // "Mantenimiento" | "Actualizacion"
    private int idRegistroMantenimiento; // id_registro_mantenimiento (0 = no aplica)
    private int idRegistroActualizacion; // id_registro_actualizacion (0 = no aplica)
    private String descripcion;
    private LocalDate fecha;

    public HistorialRegistro() {
    }

    public HistorialRegistro(int id, int idEquipo, String equipoNombre, String tipo,
            int idRegistroMantenimiento, int idRegistroActualizacion,
            String descripcion, LocalDate fecha) {
        this.id = id;
        this.idEquipo = idEquipo;
        this.equipoNombre = equipoNombre;
        this.tipo = tipo;
        this.idRegistroMantenimiento = idRegistroMantenimiento;
        this.idRegistroActualizacion = idRegistroActualizacion;
        this.descripcion = descripcion;
        this.fecha = fecha;
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

    public int getIdRegistroMantenimiento() {
        return idRegistroMantenimiento;
    }

    public int getIdRegistroActualizacion() {
        return idRegistroActualizacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDate getFecha() {
        return fecha;
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

    public void setIdRegistroMantenimiento(int v) {
        this.idRegistroMantenimiento = v;
    }

    public void setIdRegistroActualizacion(int v) {
        this.idRegistroActualizacion = v;
    }

    public void setDescripcion(String v) {
        this.descripcion = v;
    }

    public void setFecha(LocalDate v) {
        this.fecha = v;
    }
}
