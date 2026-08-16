package com.utng.TecnicoModule.model;

import java.time.LocalDate;

/** Tabla: equipos_programas (JOIN con programas para el nombre) */
public class EquipoPrograma {

    private int id;
    private int idEquipo; // id_equipo (FK)
    private String equipoNombre;
    private int idPrograma; // id_programa (FK)
    private String nombrePrograma; // programas.nombre
    private String versionActual; // version_actual
    private LocalDate fechaInstalacion; // fecha_instalacion

    public EquipoPrograma() {
    }

    public EquipoPrograma(int id, int idEquipo, String equipoNombre, int idPrograma,
            String nombrePrograma, String versionActual, LocalDate fechaInstalacion) {
        this.id = id;
        this.idEquipo = idEquipo;
        this.equipoNombre = equipoNombre;
        this.idPrograma = idPrograma;
        this.nombrePrograma = nombrePrograma;
        this.versionActual = versionActual;
        this.fechaInstalacion = fechaInstalacion;
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

    public int getIdPrograma() {
        return idPrograma;
    }

    public String getNombrePrograma() {
        return nombrePrograma;
    }

    public String getVersionActual() {
        return versionActual;
    }

    public LocalDate getFechaInstalacion() {
        return fechaInstalacion;
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

    public void setIdPrograma(int v) {
        this.idPrograma = v;
    }

    public void setNombrePrograma(String v) {
        this.nombrePrograma = v;
    }

    public void setVersionActual(String v) {
        this.versionActual = v;
    }

    public void setFechaInstalacion(LocalDate v) {
        this.fechaInstalacion = v;
    }

    public String textoBusqueda() {
        return (id + " " + idEquipo + " " + equipoNombre + " " + nombrePrograma + " "
                + versionActual).toLowerCase();
    }
}
