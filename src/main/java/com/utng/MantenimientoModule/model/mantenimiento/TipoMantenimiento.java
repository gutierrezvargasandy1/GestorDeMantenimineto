package com.utng.MantenimientoModule.model.mantenimiento;

/**
 * Refleja el tipo enumerado {@code tipo_mantenimiento} de PostgreSQL:
 *
 * <pre>
 * CREATE TYPE tipo_mantenimiento AS ENUM ('preventivo', 'correctivo');
 * </pre>
 *
 * {@link #getValor()} devuelve EXACTAMENTE el literal que espera la base de
 * datos, para poder hacer
 * {@code ps.setObject(n, tipo.getValor(), Types.OTHER)}
 * cuando se conecte el repositorio.
 */
public enum TipoMantenimiento {

    PREVENTIVO("preventivo", "Preventivo"),
    CORRECTIVO("correctivo", "Correctivo");

    private final String valor;
    private final String etiqueta;

    TipoMantenimiento(String valor, String etiqueta) {
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

    public static TipoMantenimiento fromValor(String valor) {
        if (valor == null) {
            return null;
        }
        for (TipoMantenimiento tipo : values()) {
            if (tipo.valor.equalsIgnoreCase(valor.trim())) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de mantenimiento no válido: " + valor);
    }

    public static TipoMantenimiento fromEtiqueta(String etiqueta) {
        if (etiqueta == null) {
            return null;
        }
        for (TipoMantenimiento tipo : values()) {
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
