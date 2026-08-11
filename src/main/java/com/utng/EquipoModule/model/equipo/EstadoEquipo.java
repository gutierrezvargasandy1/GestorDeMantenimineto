package com.utng.EquipoModule.model.equipo;

/**
 * Refleja el tipo enumerado {@code estado_equipo} de PostgreSQL:
 *
 * <pre>
 * CREATE TYPE estado_equipo AS ENUM ('activo', 'inactivo', 'en_mantenimiento', 'de_baja');
 * </pre>
 *
 * {@link #getValor()} devuelve EXACTAMENTE el literal que espera la base de
 * datos, para poder hacer {@code ps.setObject(n, estado.getValor(), Types.OTHER)}
 * cuando se conecte el repositorio.
 */
public enum EstadoEquipo {

    ACTIVO("activo", "Activo"),
    INACTIVO("inactivo", "Inactivo"),
    EN_MANTENIMIENTO("en_mantenimiento", "En mantenimiento"),
    DE_BAJA("de_baja", "De baja");

    private final String valor;
    private final String etiqueta;

    EstadoEquipo(String valor, String etiqueta) {
        this.valor = valor;
        this.etiqueta = etiqueta;
    }

    /** Literal tal cual está en el ENUM de PostgreSQL. */
    public String getValor() {
        return valor;
    }

    /** Texto legible para mostrar en la interfaz. */
    public String getEtiqueta() {
        return etiqueta;
    }

    public static EstadoEquipo fromValor(String valor) {
        if (valor == null) {
            return null;
        }
        for (EstadoEquipo estado : values()) {
            if (estado.valor.equalsIgnoreCase(valor.trim())) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de equipo no válido: " + valor);
    }

    public static EstadoEquipo fromEtiqueta(String etiqueta) {
        if (etiqueta == null) {
            return null;
        }
        for (EstadoEquipo estado : values()) {
            if (estado.etiqueta.equalsIgnoreCase(etiqueta.trim())) {
                return estado;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
