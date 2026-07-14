package com.utng.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class Navigator {


    private static Stage stage;


    public static void setStage(Stage primaryStage){

        stage = primaryStage;

    }



    public static void navigate(String ruta){


        try {

            FXMLLoader loader = new FXMLLoader(
                    Navigator.class.getResource(
                            ruta
                    )
            );


            Parent root = loader.load();


            Scene scene = new Scene(root);


            stage.setScene(scene);

            stage.show();



        } catch (IOException e) {

            e.printStackTrace();

        }

    }

}