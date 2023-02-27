module com.distributedsystems.chatappbeta {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.distributedsystems.chatappbeta to javafx.fxml;
    exports com.distributedsystems.chatappbeta;
    exports com.distributedsystems.chatappbeta.controllers;
    opens com.distributedsystems.chatappbeta.controllers to javafx.fxml;
    exports SingleThreadNonBlockingServer;
    opens SingleThreadNonBlockingServer to javafx.fxml;
}