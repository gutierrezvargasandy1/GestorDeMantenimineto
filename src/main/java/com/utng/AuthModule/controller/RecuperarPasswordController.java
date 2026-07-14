package com.utng.AuthModule.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class RecuperarPasswordController {


    @FXML
    private TextField txtCorreo;


    @FXML
    private Label lblMensaje;



    @FXML
    private void recuperarPassword() {


        String correo = txtCorreo.getText().trim();


        // Validación campo vacío
        if(correo.isEmpty()) {

            lblMensaje.setStyle(
                "-fx-text-fill: red;"
            );

            lblMensaje.setText(
                "Ingrese su correo electrónico."
            );

            return;
        }


        // Validación formato correo
        if(!correo.matches(
                "^[A-Za-z0-9+_.-]+@(.+)$"
        )) {

            lblMensaje.setStyle(
                "-fx-text-fill: red;"
            );

            lblMensaje.setText(
                "Ingrese un correo electrónico válido."
            );

            return;
        }


        /*
            Aquí después se conectará con:

            UsuarioDAO
            |
            Buscar correo
            |
            Generar código recuperación
            |
            Guardar:
                recuperacionActiva = true
                codigoRecuperacion
                expiracionCodigo

        */


        lblMensaje.setStyle(
            "-fx-text-fill: green;"
        );

        lblMensaje.setText(
            "Solicitud enviada correctamente."
        );

    }

}