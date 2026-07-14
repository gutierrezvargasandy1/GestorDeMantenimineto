package com.utng.AuthModule.controller;


import com.utng.util.Navigator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;


public class LoginController {


    @FXML
    private TextField txtUsuario;


    @FXML
    private PasswordField txtPassword;


    @FXML
    private Label lblMensaje;



    @FXML
    private void iniciarSesion(){


        String usuario = txtUsuario.getText().trim();
        String password = txtPassword.getText().trim();


        if(usuario.isEmpty() || password.isEmpty()){

            lblMensaje.setStyle(
                "-fx-text-fill:red;"
            );

            lblMensaje.setText(
                "Complete todos los campos."
            );

            return;
        }


        lblMensaje.setStyle(
            "-fx-text-fill:green;"
        );

        lblMensaje.setText(
            "Inicio correcto."
        );

    }



    @FXML
    private void recuperarPassword(){

        System.out.println(
            "Abrir recuperación de contraseña"
        );
        Navigator.navigate("/com/utng/ui/Auth/pantallaRecuperacion/RecuperarPassword.fxml");

    }

}