package com.utng.AuthModule.controller;

import com.utng.AuthModule.services.AuthService;
import com.utng.util.AppException;
import com.utng.util.Navigator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class RecuperarPasswordController {
    private final AuthService authService = new AuthService();

    @FXML
    private TextField txtCorreo;

    @FXML
    private Label lblMensaje;

    @FXML
    private void recuperarPassword() {

        String correo = txtCorreo.getText().trim();

        // Validación campo vacío
        if (correo.isEmpty()) {

            lblMensaje.setStyle(
                    "-fx-text-fill: red;");

            lblMensaje.setText(
                    "Ingrese su correo electrónico.");

            return;
        }

        // Validación formato correo
        if (!correo.matches(
                "^[A-Za-z0-9+_.-]+@(.+)$")) {

            lblMensaje.setStyle(
                    "-fx-text-fill: red;");

            lblMensaje.setText(
                    "Ingrese un correo electrónico válido.");

            return;
        }

        try {
            boolean res = authService.recuperacionDeCredenciales(correo);


            if (res == true) {

                lblMensaje.setStyle(
                        "-fx-text-fill: green;");

                lblMensaje.setText(
                        "Solicitud enviada correctamente.");

                CodigoRecuperacionController controller = Navigator.navigateAndGetController(
                        "/com/utng/ui/Auth/pantallaRecuperacionCodigo/CodigoRecuperacion.fxml"

                );
                if (controller != null) {
                    controller.setCorreoUsuario(correo);
                }

                
            } else {
                lblMensaje.setStyle("Correo no encontrado");
            }

        } catch (AppException e) {
            throw new AppException("Error al mandar el correo de recuperacion Nivel controlador", e);

        }

    }

}