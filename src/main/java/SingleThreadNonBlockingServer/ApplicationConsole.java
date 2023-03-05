package SingleThreadNonBlockingServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class ApplicationConsole {
    public static void main(String[] args) {
        // Get user name
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your name: ");
        String name = "[NOTICE_USER_NAME]" + scanner.nextLine();

        // Connect to the server
        String serverAddress = "127.0.0.1";
        int serverPort = 4242;
        // Create a socket channel and connect to the server
        try {
            SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(serverAddress, serverPort));
            // Create a reader and writer for the socket channel
            BufferedReader reader = new BufferedReader(new InputStreamReader(socketChannel.socket().getInputStream()));
            PrintWriter writer = new PrintWriter(socketChannel.socket().getOutputStream(), true);
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            // Send the user name to the server
            socketChannel.write(ByteBuffer.wrap(name.getBytes()));
            System.out.println("Type: '/help' or '/?' for help\nMessages must be as follow: \"<id>, <id> ... : <message>\".\n" +
                    "To send a message to all the users, type \"all\" or \"*\" instead of the id.");
            // Create a thread to read messages from the server
            Thread readerThread = new Thread(() -> {
                try {
                    String line;
                    byteBuffer.clear();
                    socketChannel.read(byteBuffer);
                    while (!(line = new String(byteBuffer.array()).trim()).equals("/exit")) {
                        System.out.println(line);
                        byteBuffer.clear();
                        System.out.print("Type your query: ");
                        socketChannel.read(byteBuffer);
                    }
                } catch (IOException e) {
                    System.out.println("Could not read from the server");
                }
            });
            readerThread.start();
            // Read messages from the console and send them to the server
            String request;
            while ((request = scanner.nextLine()) != null && !request.equals("/exit")) {
                socketChannel.write(ByteBuffer.wrap(request.getBytes()));
            }
        } catch (IOException e) {
            System.out.println("Could not connect to the server");
        }

    }
}
