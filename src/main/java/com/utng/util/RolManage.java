package com.utng.util;

import com.utng.UserModule.model.usuario.TipoUsuario;

public class RolManage {

    private static final RolManage INSTANCE = new RolManage();
    private TipoUsuario rol;

    private RolManage() {}

    public static RolManage getInstance() {
        return INSTANCE;
    }

    public TipoUsuario getRol() {
        return rol;
    }

    public void setRol(TipoUsuario rol) {
        this.rol = rol;
    }

    public void limpiar() {       // útil al cerrar sesión
        this.rol = null;
    }
}