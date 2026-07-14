package com.utng.AuthModule.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;


public class CodigoRecuperacionController {


    @FXML
    private TextField txtCodigo;


    @FXML
    private Label lblCorreo;


    @FXML
    private Label lblMensaje;



    private String correoUsuario;



    public void setCorreoUsuario(String correo){

        this.correoUsuario = correo;

        lblCorreo.setText(
            "Se envió un código de 6 dígitos al correo:\n"
            + correo
        );

    }



    @FXML
    private void verificarCodigo(){


        String codigo = txtCodigo.getText().trim();



        // Validar campo vacío
        if(codigo.isEmpty()){

            mostrarError(
                "Ingrese el código recibido."
            );

            return;
        }



        // Validar que solo sean números
        if(!codigo.matches("\\d+")){

            mostrarError(
                "El código solo debe contener números."
            );

            return;
        }



        // Validar longitud
        if(codigo.length() != 6){

            mostrarError(
                "El código debe tener 6 dígitos."
            );

            return;
        }



        /*
            Aquí después:

            UsuarioDAO

            Buscar usuario por correo

            Comparar:

            codigoRecuperacion

            Validar:

            expiracionCodigo

        */



        lblMensaje.setStyle(
            "-fx-text-fill: green;"
        );


        lblMensaje.setText(
            "Código correcto."
        );

    }



    private void mostrarError(String mensaje){

        lblMensaje.setStyle(
            "-fx-text-fill: red;"
        );

        lblMensaje.setText(
            mensaje
        );

    }

}