package com.utng.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Navigator {

    private static Stage stage;

    public static void setStage(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void navigate(String ruta) {
        navigateAndGetController(ruta);
    }

    @SuppressWarnings("unchecked")
    public static <T> T navigateAndGetController(String ruta) {
        try {
            URL location = Navigator.class.getResource(ruta);

            if (location == null) {
                System.err.println("❌ Error: No se encontró el recurso en la ruta -> " + ruta);
                return null;
            }

            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

            return loader.getController();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}