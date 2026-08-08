package com.utng.AuthModule.controller;

import com.utng.AuthModule.services.AuthService;
import com.utng.util.Navigator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class CodigoRecuperacionController {

    private final AuthService authService = new AuthService();

    @FXML
    private TextField txtCodigo;
    @FXML
    private Label lblCorreo;
    @FXML
    private Label lblMensaje;
    private String correoUsuario;

    public void setCorreoUsuario(String correo) {
        this.correoUsuario = correo;

        lblCorreo.setText(
                "Se envió un código de 6 dígitos al correo:\n"
                        + correo);

    }

    @FXML
    private void reenviarCodigo() {
        if (correoUsuario == null || correoUsuario.isEmpty()) {
            mostrarError("No se pudo obtener el correo para reenviar el código.");
            return;
        }

        // Llamada al servicio para reenviar o generar un nuevo código
        boolean enviado = authService.recuperacionDeCredenciales(correoUsuario);

        if (enviado) {
            lblMensaje.setStyle("-fx-text-fill: green;");
            lblMensaje.setText("Se ha reenviado un nuevo código a tu correo.");
        } else {
            mostrarError("Error al reenviar el código. Inténtelo más tarde.");
        }
    }

    @FXML
    private void verificarCodigo() {

        String codigo = txtCodigo.getText().trim();
        System.out.print(correoUsuario);

        if (codigo.isEmpty()) {

            mostrarError(
                    "Ingrese el código recibido.");

            return;
        }

        if (!codigo.matches("\\d+")) {

            mostrarError(
                    "El código solo debe contener números.");

            return;
        }

        if (codigo.length() != 6) {

            mostrarError(
                    "El código debe tener 6 dígitos.");

            return;
        }

        boolean res = authService.confirmarRecuperacion(correoUsuario, codigo);
        if (res == true) {
            lblMensaje.setStyle(
                    "-fx-text-fill: green;");

            lblMensaje.setText(
                    "Código correcto.");

            NuevaPasswordController controller = Navigator
                    .navigateAndGetController("/com/utng/ui/Auth/pantallaCambioPassword/NuevaPassword.fxml");
            if (controller != null) {
                controller.setCorreoUsuario(correoUsuario);
            }

        } else {

            lblMensaje.setText(
                    "Código incorrecto o vencido.");
        }

    }

    private void mostrarError(String mensaje) {

        lblMensaje.setStyle(
                "-fx-text-fill: red;");

        lblMensaje.setText(
                mensaje);

    }

}