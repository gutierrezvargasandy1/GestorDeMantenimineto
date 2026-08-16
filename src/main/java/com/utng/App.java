package com.utng;

import com.mongodb.client.MongoDatabase;
import com.utng.config.ConectionDB;
import com.utng.config.MongoDBConnection;
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
        MongoDatabase database = MongoDBConnection.getDatabase();
        System.out.println(
                "Base de datos: " + database.getName());

    }

    public static void main(String[] args) {

        launch(args);

    }

}