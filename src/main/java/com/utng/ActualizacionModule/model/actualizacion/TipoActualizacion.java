package com.utng.ActualizacionModule.model.actualizacion;

/**
 * Refleja el tipo enumerado {@code tipo_actualizacion} de PostgreSQL:
 *
 * <pre>
 * CREATE TYPE tipo_actualizacion AS ENUM ('sistema_operativo', 'programa', 'firmware', 'driver');
 * </pre>
 *
 * {@link #getValor()} devuelve EXACTAMENTE el literal que espera la base de
 * datos, para poder hacer
 * {@code ps.setObject(n, tipo.getValor(), Types.OTHER)}
 * cuando se conecte el repositorio.
 *
 * Mismo patrón que {@code TipoMantenimiento} y {@code EstadoEquipo}.
 */
public enum TipoActualizacion {

    SISTEMA_OPERATIVO("sistema_operativo", "Sistema operativo"),
    PROGRAMA("programa", "Programa"),
    FIRMWARE("firmware", "Firmware"),
    DRIVER("driver", "Driver");

    private final String valor;
    private final String etiqueta;

    TipoActualizacion(String valor, String etiqueta) {
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

    public static TipoActualizacion fromValor(String valor) {
        if (valor == null) {
            return null;
        }
        for (TipoActualizacion tipo : values()) {
            if (tipo.valor.equalsIgnoreCase(valor.trim())) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de actualización no válido: " + valor);
    }

    public static TipoActualizacion fromEtiqueta(String etiqueta) {
        if (etiqueta == null) {
            return null;
        }
        for (TipoActualizacion tipo : values()) {
            if (tipo.etiqueta.equalsIgnoreCase(etiqueta.trim())) {
                return tipo;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
