package com.utng.UserModule.model.usuario;

public enum TipoUsuario {

    ADMINISTRADOR("administrador"),
    TECNICO("tecnico"),
    CONSULTA("consulta");

    private final String valor;

    TipoUsuario(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static TipoUsuario fromValor(String valor) {
        for (TipoUsuario rol : TipoUsuario.values()) {
            if (rol.valor.equalsIgnoreCase(valor)) {
                return rol;
            }
        }

        throw new IllegalArgumentException(
            "Rol no válido: " + valor
        );
    }
}