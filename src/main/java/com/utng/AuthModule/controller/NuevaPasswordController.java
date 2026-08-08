package com.utng.AuthModule.controller;

import com.utng.AuthModule.services.AuthService;
import com.utng.util.Navigator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

public class NuevaPasswordController {

    private final AuthService authService = new AuthService();

    @FXML
    private PasswordField txtNuevaPassword;

    @FXML
    private PasswordField txtConfirmarPassword;

    @FXML
    private Label lblMensaje;

    private String correoUsuario;

    public void setCorreoUsuario(String correo) {
        this.correoUsuario = correo;
    }

    @FXML
    private void cambiarPassword() {
        String nuevaPass = txtNuevaPassword.getText().trim();
        String confirmarPass = txtConfirmarPassword.getText().trim();

        if (nuevaPass.isEmpty() || confirmarPass.isEmpty()) {
            mostrarMensaje("Por favor, llene todos los campos.", true);
            return;
        }

        if (!nuevaPass.equals(confirmarPass)) {
            mostrarMensaje("Las contraseñas no coinciden.", true);
            return;
        }

        if (nuevaPass.length() < 6) {
            mostrarMensaje("La contraseña debe tener al menos 6 caracteres.", true);
            return;
        }

        // Llamada al servicio para actualizar la contraseña en la BD
        boolean actualizada = authService.cambiaPasword(correoUsuario, nuevaPass);

        if (actualizada) {
            mostrarMensaje("Contraseña actualizada con éxito.", false);
            
            // Redirige al login tras completar el cambio
            Navigator.navigate("/com/utng/ui/Auth/pantallaLogin/Login.fxml");
        } else {
            mostrarMensaje("Ocurrió un error al actualizar la contraseña.", true);
        }
    }

    private void mostrarMensaje(String mensaje, boolean esError) {
        if (esError) {
            lblMensaje.setStyle("-fx-text-fill: red;");
        } else {
            lblMensaje.setStyle("-fx-text-fill: green;");
        }
        lblMensaje.setText(mensaje);
    }
}