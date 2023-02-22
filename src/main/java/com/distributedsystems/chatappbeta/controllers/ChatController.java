package com.distributedsystems.chatappbeta.controllers;

import com.distributedsystems.chatappbeta.ApplicationJAVAFX;
import com.distributedsystems.chatappbeta.SingletonData;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.Style;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.Socket;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

public class ChatController extends Thread implements Initializable {

    private static final String NOTIFICATION = "[NOTIFICATION]";
    private static final String NOTICE_ONLINE_USERS_NBR = "[NOTICE_ONLINE_USERS_NBR]";
    private static final String REQUEST_ONLINE_USERS_NBR = "[REQUEST_ONLINE_USERS_NBR]";
    private static final String NOTICE_USER_LOGOUT = "[NOTICE_USER_LOGOUT]";
    private static final String REQUEST_USER_IDENTIFIER = "[REQUEST_USER_IDENTIFIER]";
    private static final String NOTICE_USER_IDENTIFIER = "[NOTICE_USER_IDENTIFIER]";
    private static final String REQUEST_LIST_USERS = "[REQUEST_LIST_USERS]";
    private static final String NOTICE_LIST_USERS = "[NOTICE_LIST_USERS]";
    private static final String NOTICE_NEW_USERS = "[NOTICE_NEW_USERS]";
    private static final String NOTICE_USER_LEFT = "[NOTICE_USER_LEFT]";
    private static final String REQUEST_USER_LOGOUT = "[REQUEST_USER_LOGOUT]";

    @FXML
    private VBox listMessages;
    @FXML
    private TextField searchField;
    @FXML
    private CheckBox selectAll;
    @FXML
    private Label userId;
    @FXML
    private Label curUserName;
    @FXML
    private Label onlineUsersNbr;
    @FXML
    private VBox onlineUsersVbox;
    @FXML
    private TextField messageField;
    @FXML
    private Button sendButton;
    @FXML
    private ScrollPane scrollPane;
    private SingletonData singletonData = SingletonData.getInstance();
    private Socket socket ;
    private PrintWriter writer;
    private BufferedReader reader;

    private int onlineUsersNbrInt = -1;
    private int userIdentifier = -1;
    private String[] listUsers; // name@identifier
    private String checkedUsers; // id,id,id...

    private long counter = 0;
    // TODO: 17/02/2023 Restrict the character @ in the username
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Connect to the server
        try {
            // Create a socket
            socket = new Socket("localhost", 1234);
            // Add it to the SingletonData
            singletonData.setSocket(socket);
            // Initialize the writer and the reader
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            // Error connecting to the server
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error while connecting to the server");
            alert.setContentText("Please try again later");
            alert.showAndWait();
        }

        // Send the username to the server
        writer.println(singletonData.getUsername());
        System.out.println("Username: " + singletonData.getUsername());
        // Set username in the label
        curUserName.setText(singletonData.getUsername());

        // Get user identifier
        getUserIdentifier();

        // Get list of users
        getListOfOnlineUsers();

        // Start the thread that will listen to the server
        Thread thread = new Thread(() -> {
            // While the socket is connected
            while (!socket.isClosed()){
                try {

                    String message = this.reader.readLine();
                    System.out.println("Message received: " + message);
                    this.checkMessage(message);
                    // Get the number of online users
                    this.getOnlineUsersNbr();
//                    this.getListOfOnlineUsers();
                    // TODO: 16/02/2023 Handle the received messages error.
                } catch (IOException e) {
                    System.out.println("Error while reading from the server");
                    // Close the socket
                    try {
                        socket.close();
                        // TODO: 16/02/2023 Handle the error while closing the socket
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                try {
                    Thread.currentThread().sleep(10);
                } catch (InterruptedException e) {
                }
            }
        });
        thread.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                Socket socket = SingletonData.getInstance().getSocket();
                System.out.println("Application is shutting down...");
                // Send request to the server to logout
                System.out.println("DEBUG: Sending logout request to the server");
                writer.println(REQUEST_USER_LOGOUT);
                try {
                    System.out.println("DEBUG: Closing the socket");
                    socket.close();
                    System.out.println("DEBUG: Joining the thread");
                    if(thread.isAlive()){
                        System.out.println("DEBUG: Interrupting the thread");
                        thread.interrupt();
                        System.out.println("DEBUG: Joining the thread");
                        try {
                            thread.join();
                            System.out.println("DEBUG: Thread joined");
                            Thread.currentThread().interrupt();
                            System.out.println("DEBUG: Thread interrupted");
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    System.out.println("DEBUG: Thread joined");
                    System.exit(0);
                    System.out.println("DEBUG: System exited");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // Search bar handler
        searchField.textProperty().addListener((observableValue, s, t1) -> {
            search(t1);
        });
    }

    // Send a message to the server
    public void sendMessage(Event event){
        // Get the message
        String message = messageField.getText();
        if (!message.isEmpty()){
            // Send the message to the server
            //  - Get list of selected users;
            getCheckUsers();
            //  - Send the message to the server

            writer.println(checkedUsers + ":" + message);
            //  - Print the message in the chat
            addMessage("[YOU]:" + message);
        }
    }

    // Add message to the chat
    private void addMessage(String message){
        System.out.println("Message: " + message);
        boolean isCurrentUser = message.startsWith("[YOU]:");
        boolean isErrorMessage = message.startsWith("[ERROR]:");
        String messageText = message;
        String senderName = "";
        String senderIdentifier = "";
        if (isCurrentUser && !isErrorMessage){
            messageText = message.substring("[YOU]:".length());
        }
        else if (!isErrorMessage){
            messageText = message.substring(message.indexOf(":") + 1);
            senderName = message.substring(1, message.indexOf("@"));
            senderIdentifier = message.substring(message.indexOf("@") + 1, message.indexOf(":"));
        }
        else if (isErrorMessage){
            messageText = message.substring("[ERROR]:".length());
        }
        System.out.println("Message text: " + messageText);
        System.out.println("Sender name: " + senderName);
        System.out.println("Sender identifier: " + senderIdentifier);
        Pane messagePane = null;
        try {
            messagePane = FXMLLoader.load(ApplicationJAVAFX.class.getResource("views/messageBar.fxml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        messagePane.setId("" + counter++);
        System.out.println(messagePane);
        // Get the Text
        Text text = (Text) messagePane.lookup("#messageText");
        // Get the TextFlow
        TextFlow textFlow = (TextFlow) messagePane.lookup("#messageTextFlow");
        // Set the text
        text.setText(messageText);
        // If user is the current user change TextFlow style and align it to the right
        if (isCurrentUser && !isErrorMessage){
            textFlow.setStyle("-fx-background-color: rgba(55, 220, 109, 0.3); -fx-border-color: #1ba13d;");
            messagePane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            textFlow.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        }
        else if (isErrorMessage){
            textFlow.setStyle("-fx-background-color: rgba(255, 0, 0, 0.3); -fx-border-color: #ff0000;");
            messagePane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            textFlow.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        }
        // Add the Pane to the list of messages
        listMessages.getChildren().add(messagePane);
        // If the scroll bar is 10px from the bottom
        // Don't scroll to the bottom
        System.out.println(scrollPane.getVvalue());
        System.out.println(scrollPane.getVmax());
        // Move the scroll bar to the bottom
        ScrollBar vbar = (ScrollBar) scrollPane.lookup(".scroll-bar:vertical");
        vbar.setValue(vbar.getMax());
        // Set the value of the vertical scroll bar to its maximum value after the layout of the ScrollPane has been performed
        scrollPane.layout();
        scrollPane.setVvalue(1);
    }

    // Update the list of checked users
    // So the message will be sent to them
    private void getCheckUsers() {
        // Get the list of checkboxes
        ObservableList<Node> children = onlineUsersVbox.getChildren();
        // Initialize the string
        checkedUsers = "";
        // For each checkbox
        for (Node child : children) {
            // Get checkbox
            CheckBox checkBox = (CheckBox) child.lookup("#userCheckBox");
            // If the checkbox is checked
            if (checkBox.isSelected()){
                // Get the user identifier
                String identifier = ((Label)child.lookup("#userId")).getText();
                // Add the identifier to the string
                checkedUsers += identifier + ",";
            }
        }
        System.out.println("Checked users: " + checkedUsers);
    }

    // Check / Uncheck the "Select all" checkbox
    public void selectAllUsers(Event event){
        getCheckUsers();
        // Get the check state of the "Select all" checkbox
        boolean selected = selectAll.isSelected();
        // Get the list of checkboxes
        ObservableList<Node> children = onlineUsersVbox.getChildren();
        // For each checkbox
        for (Node child : children) {
            // Get checkbox
            CheckBox checkBox = (CheckBox) child.lookup("#userCheckBox");
            // Check or uncheck the checkbox based on the "Select all" checkbox
            checkBox.setSelected(selected);
        }
        getCheckUsers();
    }

    // Select one user
    public void selectUser(Event event){
        System.out.println("Select user");
        getCheckUsers();
        // Get the list of checkboxes
        ObservableList<Node> children = onlineUsersVbox.getChildren();
        // Initialize the number of checked users
        int nbrChecked = 0;
        // For each checkbox
        for (Node child : children) {
            // Get checkbox
            CheckBox checkBox = (CheckBox) child.lookup("#userCheckBox");
            // If the checkbox is checked
            if (checkBox.isSelected()){
                // Increment the number of checked users
                nbrChecked++;
            }
        }
        // If all the checkboxes are checked
        if (nbrChecked == children.size()){
            // Check the "Select all" checkbox
            selectAll.setSelected(true);
        }
        // If not all the checkboxes are checked
        else {
            // Uncheck the "Select all" checkbox
            selectAll.setSelected(false);
        }
        getCheckUsers();
    }

    // Check the message received from the server
    private void checkMessage(String message){
        // If the message is not empty
        if (message != null && !message.isEmpty()){
            // If the message is a notification
            if (message.startsWith(ChatController.NOTIFICATION)){
                // Handle it later
                // TODO: 16/02/2023 handle the notification
            }
            // If message is  NOTICE_ONLINE_USERS_NBR
            else if (message.startsWith(NOTICE_ONLINE_USERS_NBR)){
                // Get the number of online users
                onlineUsersNbrInt = Integer.parseInt(message.substring(NOTICE_ONLINE_USERS_NBR.length()));
                // Print the number of online users
                Platform.runLater(() -> {
                    onlineUsersNbr.setText("" + onlineUsersNbrInt);
                });
            }
            // If message is NOTICE_USER_LOGOUT
            else if (message.startsWith(ChatController.NOTICE_USER_LOGOUT)){
                // Remove the user from the list of online users
                // - Get the user identifier
                String identifier = message.substring(NOTICE_USER_LOGOUT.length()).split("@")[1].split("]")[0];
                // - Get the list of checkboxes
                ObservableList<Node> children = onlineUsersVbox.getChildren();
                // - For each checkbox
                for (Node child : children) {
                    // - Get the user identifier
                    String id = ((Label)child.lookup("#userId")).getText();
                    // - If the user identifier is the same as the one that logged out
                    if (id.equals(identifier)){
                        // - Remove the checkbox
                        Platform.runLater(() -> {
                            onlineUsersVbox.getChildren().remove(child);
                        });
                        // - Break the loop
                        break;
                    }
                }
            }
            // If message is NOTICE_USER_IDENTIFIER
            else if (message.startsWith(ChatController.NOTICE_USER_IDENTIFIER)){
                // Get the user identifier
                System.out.println("User identifier: " + message.substring(NOTICE_USER_IDENTIFIER.length()));
                userIdentifier = Integer.parseInt(message.substring(NOTICE_USER_IDENTIFIER.length()));
                // Print the user identifier
                Platform.runLater(() -> {
                    userId.setText("" + userIdentifier);
                });
            }
            // If message is NOTICE_LIST_USERS
            else if(message.startsWith(ChatController.NOTICE_LIST_USERS)){
                // Get the list of users
                String users[] = message.substring(NOTICE_LIST_USERS.length()).split(",");
                int size = users.length;
                for(int i = 0; i < size; i++){
                    String name ;
                    String identifier = users[i].split("@")[1];
                    // If the user is the current user
                    if(identifier.equals("" + userIdentifier))
                        name = "You";
                    else
                        name = users[i].split("@")[0];

                    // Import anchor pane
                    AnchorPane anchorPane = null;
                    try {
                        anchorPane = FXMLLoader.load(ApplicationJAVAFX.class.getResource("views/profileBar.fxml"));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    ((Label) anchorPane.lookup("#userName")).setText(name);
                    ((Label) anchorPane.lookup("#userId")).setText(identifier);
                    ((CheckBox) anchorPane.lookup("#userCheckBox")).setOnAction(this::selectUser);
                    // If the user is the current user then change his profile picture
                    if(identifier.equals("" + userIdentifier)){
                        Image image = new Image(ApplicationJAVAFX.class.getResourceAsStream("media/profileCurrentUser.png"));
                        ((ImageView)anchorPane.lookup("#pfp")).setImage(image);
                    }
                    AnchorPane finalAnchorPane = anchorPane;
                    Platform.runLater(() -> {
                        onlineUsersVbox.getChildren().add(finalAnchorPane);
                    });
                }
            }

            // If message is NOTICE_NEW_USERS
            else if (message.startsWith(ChatController.NOTICE_NEW_USERS)) {
                // Get new user
                String newUser = message.substring(NOTICE_NEW_USERS.length());
                // Add new user to the list
                listUsers = newUser.split("@");
                // Print the new user
                // Import anchor pane
                AnchorPane anchorPane = null;
                try {
                    anchorPane = FXMLLoader.load(ApplicationJAVAFX.class.getResource("views/profileBar.fxml"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                AnchorPane finalAnchorPane = anchorPane;
                Platform.runLater(() -> {
                    ((Label) finalAnchorPane.lookup("#userName")).setText(listUsers[0]);
                    ((Label) finalAnchorPane.lookup("#userId")).setText(listUsers[1]);
                    ((CheckBox) finalAnchorPane.lookup("#userCheckBox")).setOnAction(this::selectUser);
                    onlineUsersVbox.getChildren().add(finalAnchorPane);
                    // Uncheck the "Select all checkbox"
                    selectAll.setSelected(false);
                });
            }
            else{
                // Print the message in the chat
                System.out.println(message);
                Platform.runLater(() -> {
                    addMessage(message);
                });
            }
        }
    }

    // Get number of online users
    public void getOnlineUsersNbr(){
        // Send a request to the server
        // The response will be handled in the checkMessage method
        // and will be stored in the onlineUsersNbr variable
        writer.println(REQUEST_ONLINE_USERS_NBR);
    }

    // Get userIdentifier
    public void getUserIdentifier(){
        // Send a request to the server
        // The response will be handled in the checkMessage method
        // and will be stored in the userIdentifier variable
        writer.println(REQUEST_USER_IDENTIFIER);
    }

    //Get list of online users
    public void getListOfOnlineUsers() {
        // Send a request to the server
        // The response will be handled in the checkMessage method
        // and will be stored in the onlineUsers variable
        writer.println(REQUEST_LIST_USERS);
    }

    // Search for a user
    public void search(String query) {
        System.out.println("Search for: " + query);
        // Check if the query is empty
        if (query.isEmpty()){
            // If it is, show all the anchors
            ObservableList<Node> children = onlineUsersVbox.getChildren();
            for (Node child : children) {
                child.setVisible(true);
                child.setManaged(true);
            }
            return;
        }
        // iterate over the list of anchors
        ObservableList<Node> children = onlineUsersVbox.getChildren();
        for (Node child : children) {
            // Get the username and the id
            String name = ((Label)child.lookup("#userName")).getText();
            String id = ((Label)child.lookup("#userId")).getText();
            // If the username or id contains the query
            if ((name + id).toLowerCase().contains(query.toLowerCase())){
                // Show the anchor
                child.setVisible(true);
                child.setManaged(true);
            }
            // If the username does not contain the query
            else{
                // Hide the anchor
                child.setVisible(false);
                child.setManaged(false);
            }
        }
    }
}
