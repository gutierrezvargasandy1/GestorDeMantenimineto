package com.utng.AuthModule.controller;

import com.utng.AuthModule.services.AuthService;
import com.utng.util.Navigator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    private final AuthService authService = new AuthService();

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblMensaje;

    @FXML
    private void iniciarSesion() {
        String usuario = txtUsuario.getText().trim();
        String password = txtPassword.getText().trim();

        if (usuario.isEmpty() || password.isEmpty()) {
            lblMensaje.setStyle("-fx-text-fill:red;");
            lblMensaje.setText("Complete todos los campos.");
            return;
        }

        boolean res = authService.login(usuario, password);

        if (res) {
            lblMensaje.setStyle("-fx-text-fill:green;");
            lblMensaje.setText("Inicio correcto.");
            Navigator.navigate("/com/utng/ui/pantallaDashboard/PantallaDashboard.fxml");
        } else {
            lblMensaje.setStyle("-fx-text-fill:red;");
            lblMensaje.setText("Credenciales Incorrectas");
        }
    }

    @FXML
    private void recuperarPassword() {
        System.out.println("Abrir recuperación de contraseña");
        Navigator.navigate("/com/utng/ui/Auth/pantallaRecuperacion/RecuperarPassword.fxml");
    }
}