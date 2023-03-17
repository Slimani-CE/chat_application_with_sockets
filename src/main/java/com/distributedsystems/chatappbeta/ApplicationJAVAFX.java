package com.distributedsystems.chatappbeta;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ApplicationJAVAFX extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ApplicationJAVAFX.class.getResource("views/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Login");
        stage.setScene(scene);
        // add favicon to the window
        Image favicon = new Image(ApplicationJAVAFX.class.getResource("media/logo1.png").toString());
        stage.getIcons().add(favicon);
        stage.setResizable(false);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        // This method is called when the main window is closed
        super.stop();

        // Call System.exit to trigger the shutdown hook
        System.exit(0);
    }

    public static void main(String[] args) {
        launch();
    }
}