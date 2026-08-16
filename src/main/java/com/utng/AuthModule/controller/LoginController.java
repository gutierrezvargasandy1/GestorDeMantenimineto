package com.utng.AuthModule.controller;

import com.utng.AuthModule.services.AuthService;
import com.utng.UserModule.model.usuario.TipoUsuario;
import com.utng.util.Navigator;
import com.utng.util.RolManage;

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
            TipoUsuario rol = RolManage.getInstance().getRol();
            switch (rol) {
                case ADMINISTRADOR:
                    Navigator.navigate("/com/utng/ui/pantallaDashboard/PantallaDashboard.fxml");
                    lblMensaje.setText("Inicio correcto.");

                    break;
                case TECNICO:
                    Navigator.navigate("/com/utng/ui/tecnicoModule/pantallaDashBoardTecnico/PantallaTecnico.fxml");
                    lblMensaje.setText("Inicio correcto.");

                    break;
                case CONSULTA:
                    Navigator.navigate("/com/utng/ui/consultorModule/DashBoardConsultor.fxml");
                    lblMensaje.setText("Inicio correcto.");

                    break;
                default:
                    Navigator.navigate("/com/utng/ui/Auth/pantallaLogin/PantallaLogin.fxml");
                    lblMensaje.setText("Inicio correcto.");

            }
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