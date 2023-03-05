package MultiThreadBlockingServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TestServer {
    public static void main(String[] args) {
        try {
            // Create ServerSocket
            ServerSocket serverSocket = new ServerSocket(7331);
            System.out.println("# Server is running on address: " + serverSocket.getInetAddress() + " port: " + serverSocket.getLocalPort() + "");
            // Receive connections from users
            while(true){
                // Accept the connection
                Socket socket = serverSocket.accept();
                System.out.println("# New connection accepted: " + socket.getInetAddress() + " port: " + socket.getPort());
                // Create a new thread to handle the user's session
                new TestSession(socket).start();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
