package com.utng;

import com.utng.AuthModule.services.AuthService;
import com.utng.config.ConectionDB;
import com.utng.util.Navigator;

import javafx.application.Application;
import javafx.stage.Stage;


public class App extends Application {
    private final AuthService authService = new AuthService();


    @Override
    public void start(Stage stage) {
        Navigator.setStage(stage);
        Navigator.navigate(
            "/com/utng/ui/Auth/pantallaLogin/PantallaLogin.fxml"
        );
        ConectionDB.conectar();
        //authService.recuperacionDeCredenciales("androoz706@gmail.com");
        // authService.confirmarRecuperacion("androoz706@gmail.com", "983385");
      //authService.cambiaPasword("androoz706@gmail.com", "Hola");
      //authService.login("androoz706@gmail.com", "Hola");

    }

    public static void main(String[] args) {

        launch(args);

    }

}