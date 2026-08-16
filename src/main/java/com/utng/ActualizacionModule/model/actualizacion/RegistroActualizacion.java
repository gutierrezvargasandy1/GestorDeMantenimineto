package com.utng.ActualizacionModule.model.actualizacion;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Espejo en Java de la tabla {@code registros_actualizaciones} de DB.sql.
 *
 * <pre>
 * id                   BIGSERIAL PRIMARY KEY
 * id_equipo            BIGINT NOT NULL -&gt; equipos(id) ON DELETE CASCADE
 * tipo                 tipo_actualizacion NOT NULL
 * fecha                TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
 * nombre_actualizado   VARCHAR(150) NOT NULL
 * version_actual       VARCHAR(50)
 * version_actualizada  VARCHAR(50) NOT NULL
 * </pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroActualizacion {

    /** registros_actualizaciones.id */
    private Long idActualizacion;

    /** registros_actualizaciones.id_equipo — FK obligatoria hacia equipos(id) */
    private Long idEquipo;

    /** registros_actualizaciones.tipo — ENUM tipo_actualizacion, nunca nulo */
    private TipoActualizacion tipo;

    /** registros_actualizaciones.fecha — nunca nula */
    private Timestamp fecha;

    /** registros_actualizaciones.nombre_actualizado — obligatorio, máx. 150 */
    private String nombreActualizado;

    /** registros_actualizaciones.version_actual — versión previa, puede ser nula, máx. 50 */
    private String versionActual;

    /** registros_actualizaciones.version_actualizada — obligatorio, máx. 50 */
    private String versionActualizada;
}
