package com.utng.TecnicoModule.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Vista minima de la tabla usuarios usada SOLO para el chat.
 * El tecnico no puede crear, editar ni eliminar usuarios: aqui unicamente
 * se leen id, nombre_completo y rol para poder conversar.
 */
public class UsuarioChat {

    public static class Mensaje {
        private final String texto;
        private final String hora;
        private final boolean mio; // true = lo escribio el tecnico

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

    private final Long id; // usuarios.id
    private final String nombreCompleto; // nombre_completo + apellidos
    private final String rol; // usuarios.rol -> "Consultor"
    private final String lugar; // contexto util para el tecnico
    private boolean enLinea;
    private int noLeidos;
    private final List<Mensaje> conversacion = new ArrayList<>();

    public UsuarioChat(Long id, String nombreCompleto, String rol, String lugar, boolean enLinea) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
        this.lugar = lugar;
        this.enLinea = enLinea;
    }

    public Long getId() {
        return id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getRol() {
        return rol;
    }

    public String getLugar() {
        return lugar;
    }

    public boolean isEnLinea() {
        return enLinea;
    }

    public int getNoLeidos() {
        return noLeidos;
    }

    public void setEnLinea(boolean v) {
        this.enLinea = v;
    }

    public void setNoLeidos(int v) {
        this.noLeidos = v;
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

    public String getIniciales() {
        String[] p = nombreCompleto.trim().split("\\s+");
        if (p.length == 1)
            return p[0].substring(0, Math.min(2, p[0].length())).toUpperCase();
        return ("" + p[0].charAt(0) + p[1].charAt(0)).toUpperCase();
    }

    @Override
    public String toString() {
        return nombreCompleto;
    }
}