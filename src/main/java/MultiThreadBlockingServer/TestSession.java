package MultiThreadBlockingServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TestSession extends Thread{

    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private Socket socker;
    public TestSession(Socket socket) {
        System.out.println("# New session created for: " + socket.getInetAddress() + " port: " + socket.getPort());
        this.socker = socket;
        try {
            printWriter = new PrintWriter(socket.getOutputStream(), true);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void run() {
        try {
            // Receive the user's name
            String name = bufferedReader.readLine();
            // Send a welcome message to the user
            printWriter.println("Welcome " + name + "!");
            // Send a message to the user to inform him that he is connected
            printWriter.println("You are connected to the server!");
            // Send a message to the user to inform him that he can start chatting
            printWriter.println("You can start chatting now!");
            // Receive messages from the user
            while(true){
                String message = bufferedReader.readLine();
                if(message == null){
                    break;
                }
                System.out.println(name + " : " + message);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
