package com.utng.MantenimientoModule.model.mantenimiento;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Espejo en Java de la tabla {@code registros_mantenimiento} de DB.sql.
 *
 * <pre>
 * id                       BIGSERIAL PRIMARY KEY
 * id_equipo                BIGINT NOT NULL -&gt; equipos(id) ON DELETE CASCADE
 * fecha                    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
 * motivo                   TEXT NOT NULL
 * tipo                     tipo_mantenimiento NOT NULL
 * fecha_proxima            TIMESTAMP
 * mantenimiento_realizado  BOOLEAN NOT NULL DEFAULT FALSE
 * </pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroMantenimiento {

    /** registros_mantenimiento.id */
    private Long idMantenimiento;

    /** registros_mantenimiento.id_equipo — FK obligatoria hacia equipos(id) */
    private Long idEquipo;

    /** registros_mantenimiento.fecha — nunca nula */
    private Timestamp fecha;

    /** registros_mantenimiento.motivo — TEXT obligatorio */
    private String motivo;

    /** registros_mantenimiento.tipo — ENUM tipo_mantenimiento, nunca nulo */
    private TipoMantenimiento tipo;

    /** registros_mantenimiento.fecha_proxima — puede ser nula */
    private Timestamp fechaProxima;

    /** registros_mantenimiento.mantenimiento_realizado — NOT NULL DEFAULT FALSE */
    private boolean mantenimientoRealizado;
}
