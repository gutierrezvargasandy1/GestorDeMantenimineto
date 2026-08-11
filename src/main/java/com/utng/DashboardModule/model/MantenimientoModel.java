package com.utng.DashboardModule.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MantenimientoModel {
    private final String equipo;
    private final String usuario;
    private final String tipo;
    private final String motivo;
    private final String tecnico;
    private final String fecha;
    private final String estado;

}
