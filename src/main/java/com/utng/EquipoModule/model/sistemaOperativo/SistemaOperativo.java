package com.utng.EquipoModule.model.sistemaOperativo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Espejo en Java del catálogo {@code sistemas_operativos} de DB.sql.
 *
 * <pre>
 * id              BIGSERIAL PRIMARY KEY
 * tipo            VARCHAR(50)
 * nombre          VARCHAR(100)  NOT NULL
 * version_actual  VARCHAR(50)   NOT NULL
 * UNIQUE (nombre, version_actual)
 * </pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SistemaOperativo {

    /** sistemas_operativos.id */
    private Long idSistemaOperativo;

    /** sistemas_operativos.tipo — ej. "escritorio", "servidor" */
    private String tipo;

    /** sistemas_operativos.nombre — obligatorio */
    private String nombre;

    /** sistemas_operativos.version_actual — obligatorio */
    private String versionActual;

    /** Texto listo para mostrar en tablas y ComboBox: "Windows 11 Pro 23H2". */
    public String getDescripcion() {
        String base = nombre == null ? "" : nombre.trim();
        String version = versionActual == null ? "" : versionActual.trim();
        return version.isEmpty() ? base : (base + " " + version).trim();
    }
}
