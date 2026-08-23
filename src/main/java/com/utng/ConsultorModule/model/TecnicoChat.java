package com.utng.ConsultorModule.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Usuario de tipo TECNICO con el que el Consultor puede chatear.
 * Guarda su propia conversacion en memoria.
 */
public class TecnicoChat {

    /** Un mensaje dentro de la conversacion. */
    public static class Mensaje {
        private final String texto;
        private final String hora;
        private final boolean mio; // true = lo escribio el consultor

        public Mensaje(String texto, String hora, boolean mio) {
            this.texto = texto;
            this.hora = hora;
            this.mio = mio;
        }

        public String getTexto() {
            return texto;
        }

        public String getHora() {
            return hora;
        }

        public boolean isMio() {
            return mio;
        }
    }

    private Long id;
    private String nombre;
    private String area;
    private boolean enLinea;
    private final List<Mensaje> conversacion = new ArrayList<>();

    public TecnicoChat(Long id, String nombre, String area, boolean enLinea) {
        this.id = id;
        this.nombre = nombre;
        this.area = area;
        this.enLinea = enLinea;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getArea() {
        return area;
    }

    public boolean isEnLinea() {
        return enLinea;
    }

    public void setEnLinea(boolean enLinea) {
        this.enLinea = enLinea;
    }

    public List<Mensaje> getConversacion() {
        return conversacion;
    }

    public void agregarMensaje(Mensaje m) {
        conversacion.add(m);
    }

    public void limpiarConversacion() {
        conversacion.clear();
    }

    /** Iniciales para el avatar circular: "Ana Torres" -> "AT". */
    public String getIniciales() {
        String[] partes = nombre.trim().split("\\s+");
        if (partes.length == 1) {
            return partes[0].substring(0, Math.min(2, partes[0].length())).toUpperCase();
        }
        return ("" + partes[0].charAt(0) + partes[1].charAt(0)).toUpperCase();
    }

    @Override
    public String toString() {
        return nombre;
    }
}