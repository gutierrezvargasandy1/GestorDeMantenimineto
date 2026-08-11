package com.utng.EquipoModule.model.equipo;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Espejo en Java de la tabla {@code equipos} de DB.sql.
 *
 * <pre>
 * id                      BIGSERIAL PRIMARY KEY
 * modelo                  VARCHAR(100)  NOT NULL
 * lugar                   VARCHAR(150)
 * almacenamiento          VARCHAR(50)
 * memoria_ram             VARCHAR(50)
 * procesador              VARCHAR(100)
 * anio_creacion           SMALLINT
 * estado                  estado_equipo NOT NULL DEFAULT 'activo'
 * id_sistema_operativo    BIGINT  -> sistemas_operativos(id)  ON DELETE SET NULL
 * id_usuario_responsable  BIGINT  -> usuarios(id)             ON DELETE SET NULL
 * fecha_creacion          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
 * fecha_actualizacion     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
 * </pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Equipo {

    /** equipos.id */
    private Long idEquipo;

    /** equipos.modelo — obligatorio, máx. 100 caracteres */
    private String modelo;

    /** equipos.lugar — máx. 150 caracteres */
    private String lugar;

    /** equipos.almacenamiento — máx. 50 caracteres (ej. "512GB SSD") */
    private String almacenamiento;

    /** equipos.memoria_ram — máx. 50 caracteres (ej. "16GB") */
    private String memoriaRam;

    /** equipos.procesador — máx. 100 caracteres */
    private String procesador;

    /** equipos.anio_creacion — SMALLINT, puede ser nulo */
    private Short anioCreacion;

    /** equipos.estado — ENUM estado_equipo, nunca nulo */
    private EstadoEquipo estado = EstadoEquipo.ACTIVO;

    /** equipos.id_sistema_operativo — FK opcional */
    private Long idSistemaOperativo;

    /** equipos.id_usuario_responsable — FK opcional */
    private Long idUsuarioResponsable;

    /** equipos.fecha_creacion */
    private Timestamp fechaCreacion;

    /**
     * equipos.fecha_actualizacion — la mantiene el trigger trg_equipos_actualizado
     */
    private Timestamp fechaActualizacion;
}
