package com.utng.UserModule.model.usuario;



import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    private Long idUsuario;
    private String nombreCompleto;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String correo;
    private String password;
    private TipoUsuario tipoUsuario;
    private Integer intentosRecuperacion = 0;
    private Boolean recuperacionActiva = false;
    private Timestamp fechaCodigo;
    private String codigoRecuperacion;
    private Timestamp fechaCreacion;
    private Boolean activo;

}