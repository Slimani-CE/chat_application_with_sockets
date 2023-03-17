package test;

import SingleThreadNonBlockingServer.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.nio.charset.MalformedInputException;
import java.util.Scanner;

public class UserTestThread {
    public class Tester {
        public void testSingleThreadServer(String addr, int port, int secondsToWait){
            // This application will connect to the server
            // then it will wait 'secondsToWait' seconds and then it will close the socket
            SocketChannel socketChannel = null;
            try {
                socketChannel = SocketChannel.open();
                socketChannel.connect(new InetSocketAddress(addr, port));
                BufferedReader reader = new BufferedReader(new InputStreamReader(socketChannel.socket().getInputStream()));
                PrintWriter writer = new PrintWriter(socketChannel.socket().getOutputStream(), true);
                writer.println("Test username");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // wait 10 seconds and then close the socket
            SocketChannel finalSocketChannel = socketChannel;
            new Thread(() -> {
                try {
                    Thread.sleep(secondsToWait);
                    finalSocketChannel.close();
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        public void testMultiThreadServer(String addr, int port, int secondsToWait){
            // This application will connect to the server
            // then it will wait 'secondsToWait' seconds and then it will close the socket
            Socket socket = new Socket();
            try {
                socket.connect(new InetSocketAddress(addr, port));
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println("test user name");
            }catch (IOException e) {
                e.printStackTrace();
            }
            // wait 10 seconds and then close the socket
            new Thread(() -> {
                try {
                    Thread.sleep(secondsToWait);
                    socket.close();
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
