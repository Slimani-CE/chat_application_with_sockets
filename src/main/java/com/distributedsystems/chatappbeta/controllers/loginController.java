package com.distributedsystems.chatappbeta.controllers;

import com.distributedsystems.chatappbeta.ApplicationJAVAFX;
import com.distributedsystems.chatappbeta.SingletonData;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class loginController {

    @FXML
    private TextField nameField;

    private Stage stage;
    private Scene scene;
    private Parent root;

    // Login to the application
    public void login(Event event) {
        // get the name entered by the user
        String name = nameField.getText();

        // if the name is not empty
        if (!name.isEmpty()) {
            // set the name in the singleton data
            SingletonData.getInstance().setUsername(name);
            // Change scene to the chat scene
            System.out.println("Name: " + name);
            try {
                switchToChatScene(event);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Name is empty");
            alert.setContentText("Please enter your name");
            alert.showAndWait();
        }
    }

    // Switch to the sign in scene
    public void switchToChatScene(Event event) throws IOException {
        root = FXMLLoader.load(ApplicationJAVAFX.class.getResource("views/chatView.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("GlobalConnect - Chat");
        stage.show();
    }

}