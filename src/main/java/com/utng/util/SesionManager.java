package com.utng.util;

import com.utng.UserModule.model.usuario.Usuario;

/**
 * Guarda al usuario que inicio sesion (id, nombre, correo, rol...).
 * Mismo patron singleton que RolManage.
 */
public class SesionManager {

    private static final SesionManager INSTANCE = new SesionManager();
    private Usuario usuario;

    private SesionManager() {
    }

    public static SesionManager getInstance() {
        return INSTANCE;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Long getIdUsuario() {
        return usuario == null ? null : usuario.getIdUsuario();
    }

    public String getNombreCompleto() {
        if (usuario == null)
            return "Usuario";
        String nombre = (usuario.getNombreCompleto() + " " + usuario.getApellidoPaterno()).trim();
        return nombre.isEmpty() ? "Usuario" : nombre;
    }

    public void limpiar() { // util al cerrar sesion
        this.usuario = null;
    }
}