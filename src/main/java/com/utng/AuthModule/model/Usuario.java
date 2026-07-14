package com.utng.AuthModule.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    private Integer idUsuario;
    private String nombreCompleto;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String correo;
    private String password;
    private TipoUsuario tipoUsuario;
    private Boolean recuperacionActiva;
    private String codigoRecuperacion;
    private LocalDateTime expiracionCodigo;

}