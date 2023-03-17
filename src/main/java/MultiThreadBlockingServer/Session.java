package MultiThreadBlockingServer;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

// This class will be used to handle the client's session
public class Session extends Thread{

    private Socket socket;
    private String nameUser;
    private String email;
    private int id;
    private MTBServer server;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;
    private static final String NOTIFICATION = "[NOTIFICATION]";
    private static final String NOTICE_ONLINE_USERS_NBR = "[NOTICE_ONLINE_USERS_NBR]";
    private static final String REQUEST_ONLINE_USERS_NBR = "[REQUEST_ONLINE_USERS_NBR]";
    private static final String NOTICE_USER_LOGOUT = "[NOTICE_USER_LOGOUT]";
    private static final String REQUEST_USER_IDENTIFIER = "[REQUEST_USER_IDENTIFIER]";
    private static final String NOTICE_USER_IDENTIFIER = "[NOTICE_USER_IDENTIFIER]";

    private static final String REQUEST_LIST_USERS = "[REQUEST_LIST_USERS]";
    private static final String NOTICE_LIST_USERS = "[NOTICE_LIST_USERS]";
    private static final String NOTICE_NEW_USERS = "[NOTICE_NEW_USERS]";
    private static final String REQUEST_USER_LOGOUT = "[REQUEST_USER_LOGOUT]";

    public Session(Socket socket, int id, MTBServer server) {
        this.socket = socket;
        this.id = id;
        this.server = server;
    }

    @Override
    public void run() {

        // Input & Output streams
        try {
            System.out.println("# New session started. IP: " + socket.getRemoteSocketAddress());

            // Create input & output stream
            InputStreamReader isr = new InputStreamReader(socket.getInputStream());
            OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
            bufferedReader = new BufferedReader(isr);
            printWriter = new PrintWriter(osw, true);
            // Send a message to the user
            print("Your Id         : " + id, Session.NOTIFICATION);
            print("Enter your name.", Session.NOTIFICATION);
            nameUser = bufferedReader.readLine();

            // Broadcast this Session to all the other sessions
            broadcast(Session.NOTICE_NEW_USERS + nameUser + "@" + id);
            // Broadcast the number of online users
            broadcast(Session.NOTICE_ONLINE_USERS_NBR + server.getUsersCount());

            // Display instructions and guidance to the user
            print("You can start typing messages now. To exit, type \"\\exit\" for help type \"\\help\".", Session.NOTIFICATION);
            print("Messages must be as follow: \"<id>, <id> ... : <message>\".", Session.NOTIFICATION);
            print("To send a message to all the users, type \"all\" or \"*\" instead of the id.", Session.NOTIFICATION);

            // Receive messages from the user
            String query;
            while((query = bufferedReader.readLine()) != null){
                // Check query
                checkQuery(query);
            }
        } catch (IOException e) {
        }
    }

    // Broadcast a message to specific users
    public void broadcast(String message, ArrayList<Session> sessions){
        // Send the message to all the users
        System.out.println("# Message sent by user IP: " + socket.getRemoteSocketAddress() + " message: " + message);
        sessions.forEach(session -> {
            try {
                if (session != this){
                    // Send the message
                    PrintWriter pw = new PrintWriter(new OutputStreamWriter(session.getSocket().getOutputStream()), true);
                    pw.println(message);
                    System.out.println("\tto -> " + session.getSocket().getRemoteSocketAddress() + "/" + session.getSocket().getPort() + " Name: " + session.getName());
                }
            } catch (IOException e) {
                System.out.println("Error while broadcasting message");
                e.printStackTrace();
            }
        });
        System.out.println("\n");
    }

    // Broadcast a message to all online users
    public void broadcast(String message){
        this.broadcast(message, server.getSessions());
    }

    // Print a message to the user that is connected to this session
    public void print(String message, String messageType){
        if (messageType == Session.NOTIFICATION){
            printWriter.println(Session.NOTIFICATION + message);
        }
    }
    public void print(String message){
        printWriter.println(message);
    }

    // Check query entered by the user
    public void checkQuery(String query){
        // check if the query is a notification
        if(query.startsWith(Session.NOTIFICATION)){
            // Display the notification
            printWriter.println(query.split(Session.NOTIFICATION)[1]);
        }
        // If user asking for number of online users
        else if(query.startsWith(Session.REQUEST_ONLINE_USERS_NBR)){
            // Send a message to the user
            print(Session.NOTICE_ONLINE_USERS_NBR + server.getSessions().size());
        }
        // Check if the user wants to exit
        else if(query.equals("\\exit") || query.equals("/exit") || query.equals(Session.REQUEST_USER_LOGOUT)){
            // remove the session from the server and close it
            System.out.println("# User " + nameUser + " disconnected.");
            server.removeSession(this);
            // close the socket
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // If user asking for his identifier
        else if(query.startsWith(Session.REQUEST_USER_IDENTIFIER)){
            // Send a message to the user
            print(Session.NOTICE_USER_IDENTIFIER + id);
        }
        // If user asking for list of users
        else if(query.startsWith(Session.REQUEST_LIST_USERS)){
            System.out.println("User asking for list of users");
            String users = ""; // name@id,...
            for (Session session : server.getSessions()) {
                users += session.getUserName() + "@" + session.getUserId() + ",";
            }
            print(Session.NOTICE_LIST_USERS + users);
        }

        // Check if the user wants help
        else if(query.equals("\\help") || query.equals("/help")){
            printWriter.println("Messages must be as follow: \"<id>, <id> ... : <message>\".");
            printWriter.println("To send a message to all the users, type \"all\" or \"*\" instead of the id.");
            printWriter.println("\"\\exit\" \t  : To exit the application.");
            printWriter.println("\"\\help\" \t  : To display help.");
            printWriter.println("\"\\users\"\t  : To display all the users with IDs");
            printWriter.println("\"\\myid\" \t  : To display your ID.");
        }

        // This block reserved for user help commands
        // - users
        else if(query.equals("\\users") || query.equals("/users")){
            // Display number of users online
            printWriter.println("Number of users online: " + server.getSessions().size());
            // Get all the sessions
            ArrayList<Session> sessions = server.getSessions();
            // Display all the sessions
            sessions.forEach(session -> {
                printWriter.println("[" + session.getUserId() + "] " + session.getUserName() + (((session.equals(this))? " (You)":"")));
            });
        }
        // - myid
        else if(query.equals("\\myid") || query.equals("/myid")){
            printWriter.println("Your ID: " + id);
        }

        // Check if the user wants to broadcast a message
        else if(query.contains(":")){
            // If the user wants to send a message to all the users
            if( (query.split(":")[0].contains("all") || query.split(":")[0].contains("*")) && query.split(":").length > 1){
                // Broadcast the message to all the users
                System.out.println("DEBUG: send to all");
                broadcast("[" + nameUser + "@" + id + "] : " + query.split(":")[1]);
            }
            // If the user wants to send a message to specific users
            else if(query.split(":").length > 1){
                System.out.println("DEBUG: send to specific users");
                // Get the users Sessions using their IDs
                ArrayList<Session> sessions = new ArrayList<>();
                for(String id : query.split(":")[0].split(",")){
                    try{
                        sessions.add(server.getSession(Integer.parseInt(id)));
                    }
                    catch(NumberFormatException e){
                        printWriter.println("[ERROR]:Message could not be sent. Invalid ID: " + id);
                    }
                }
                // Filter offline sessions
                sessions.removeIf(session -> session == null);
                // Broadcast the message to the users
                broadcast("[" + nameUser + "@" + id + "] : " + query.split(":")[1], sessions);
            }
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public int getUserId() {
        return id;
    }

    public BufferedReader getBufferedReader() {
        return bufferedReader;
    }

    public void setBufferedReader(BufferedReader bufferedReader) {
        this.bufferedReader = bufferedReader;
    }

    public PrintWriter getPrintWriter() {
        return printWriter;
    }

    public void setPrintWriter(PrintWriter printWriter) {
        this.printWriter = printWriter;
    }

    public String getUserName() {
        return nameUser;
    }

    public void setUserName(String name) {
        this.nameUser = name;
    }
}
