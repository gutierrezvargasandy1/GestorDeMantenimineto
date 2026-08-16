package com.utng.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;

public class Navigator {

    private static Stage stage;
    private static final Deque<String> historial = new ArrayDeque<>();
    private static String rutaActual = null;

    public static void setStage(Stage primaryStage) {
        stage = primaryStage;
        stage.setMaximized(true);
    }

    public static void navigate(String ruta) {
        navigateAndGetController(ruta);
    }

    public static <T> T navigateAndGetController(String ruta) {
        try {
            URL location = Navigator.class.getResource(ruta);

            if (location == null) {
                System.err.println("❌ Error: No se encontró el recurso en la ruta -> " + ruta);
                return null;
            }

            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();

            // Si la escena ya existe, solo cambiamos el contenido raíz para no perder el
            // estado maximizado
            if (stage.getScene() != null) {
                stage.getScene().setRoot(root);
            } else {
                Scene scene = new Scene(root);
                stage.setScene(scene);
            }

            stage.setMaximized(true);
            stage.show();

            if (rutaActual != null && !rutaActual.equals(ruta)) {
                historial.push(rutaActual);
            }
            rutaActual = ruta;

            return loader.getController();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void goBack() {
        if (!historial.isEmpty()) {
            String rutaAnterior = historial.pop();
            rutaActual = null;
            navigateAndGetController(rutaAnterior);
        }
    }
}