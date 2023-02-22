package com.distributedsystems.chatappbeta;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

// This is a singleton class that provides data to all the controllers
public class SingletonData {

    private static final SingletonData instance = new SingletonData();
    private String username;
    private Socket socket;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private SingletonData() {
    }
    public static SingletonData getInstance() {
        return instance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setBufferedReader(BufferedReader bufferedReader) {
        this.bufferedReader = bufferedReader;
    }

    public void setPrintWriter(PrintWriter printWriter) {
        this.printWriter = printWriter;
    }

    public BufferedReader getBufferedReader() {
        return bufferedReader;
    }

    public PrintWriter getPrintWriter() {
        return printWriter;
    }
}
