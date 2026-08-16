package com.utng;

import com.utng.config.ConectionDB;
import com.utng.util.Navigator;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        Navigator.setStage(stage);
        Navigator.navigate(
                "/com/utng/ui/Auth/pantallaLogin/PantallaLogin.fxml");
        ConectionDB.conectar();

    }

    public static void main(String[] args) {

        launch(args);

    }

}