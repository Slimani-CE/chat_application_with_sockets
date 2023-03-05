package SingleThreadNonBlockingServer;

import MultiThreadBlockingServer.Session;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class STNBServer {

    private static final String NOTIFICATION = "[NOTIFICATION]";
    private static final String NOTICE_ONLINE_USERS_NBR = "[NOTICE_ONLINE_USERS_NBR]";
    private static final String REQUEST_ONLINE_USERS_NBR = "[REQUEST_ONLINE_USERS_NBR]";
    private static final String NOTICE_USER_LOGOUT = "[NOTICE_USER_LOGOUT]";
    private static final String REQUEST_USER_IDENTIFIER = "[REQUEST_USER_IDENTIFIER]";
    private static final String NOTICE_USER_IDENTIFIER = "[NOTICE_USER_IDENTIFIER]";

    private static final String REQUEST_LIST_USERS = "[REQUEST_LIST_USERS]";
    private static final String NOTICE_USER_NAME = "[NOTICE_USER_NAME]";
    private static final String NOTICE_LIST_USERS = "[NOTICE_LIST_USERS]";
    private static final String NOTICE_NEW_USERS = "[NOTICE_NEW_USERS]";
    private static final String REQUEST_USER_LOGOUT = "[REQUEST_USER_LOGOUT]";
    private static ArrayList<User> users = new ArrayList<User>();
    private static int idCounter = 0;
    private static int onlineUsersNbr = 0;
    public static void main(String[] args){
        Selector selector = null;
        ServerSocketChannel serverSocketChannel = null;
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress("0.0.0.0", 4242));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            System.out.println("Could not start server");
        }

        while(true){
            int channelCount = 0;
            try {
                channelCount = selector.select();
            } catch (IOException e) {
                System.out.println("Could not select channel");
            }
            System.out.println("# New connection...");
            if(channelCount == 0) continue;
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while(iterator.hasNext()){
                SelectionKey selectionKey = iterator.next();
                if(selectionKey.isAcceptable()){
                    System.out.println("# Accepting connection...");
                    try {
                        handleAccept(selectionKey, selector);
                    } catch (IOException e) {
                        System.out.println("Could not accept connection");
                        removeUserBySocketChannel((SocketChannel) selectionKey.channel());
                        onlineUsersNbr--;
                        selectionKey.cancel();
                    }
                }
                else if(selectionKey.isReadable()){
                    System.out.println("# Reading data...");
                    try {
                        handleReadWrite(selectionKey, selector);
                    } catch (IOException e) {
                        System.out.println("Could not read data");
                        removeUserBySocketChannel((SocketChannel) selectionKey.channel());
                        onlineUsersNbr--;
                        selectionKey.cancel();
                    }
                }
                iterator.remove();
            }
        }
    }

    // Handle new connection
    private static void handleAccept(SelectionKey selectionKey, Selector selector) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        // Create a new user
        User user = new User();
        user.setSocketChannel(socketChannel);
        user.setId(idCounter++);
        onlineUsersNbr++;
        users.add(user);
        System.out.println(String.format("# New connection from %s", socketChannel.getRemoteAddress()));
        // Responde to the client
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.put(String.format("You are connected!").getBytes());
        byteBuffer.flip();
        socketChannel.write(byteBuffer);

    }

    // Handle read/write
    private static void handleReadWrite(SelectionKey selectionKey, Selector selector) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        int dataSize = socketChannel.read(byteBuffer);

        if(dataSize == -1){
            onlineUsersNbr--;
            socketChannel.close();
        }
        else{
            // Get user
            User user = getUserBySocketChannel(socketChannel);
            String request = new String(byteBuffer.array()).trim();

            // Trait request
            String response = traitRequest(request, user);
            if(response != null){
                // Send response
                ByteBuffer responseBuffer = ByteBuffer.allocate(1024);
                responseBuffer.put(response.getBytes());
                responseBuffer.flip();
                socketChannel.write(responseBuffer);
                responseBuffer.clear();
                responseBuffer.flip();
            }
        }
    }

    // Trait request
    private static String traitRequest(String request, User sender){
        System.out.println(request);
        // check if the query is a notification
        if(request.startsWith(STNBServer.NOTIFICATION)){
            // Display the notification
            return request.split(STNBServer.NOTIFICATION)[1];
        }
        // If user wants to update his name
        else if(request.startsWith(STNBServer.NOTICE_USER_NAME)){
            // Update the user name
            sender.setName(request.substring(STNBServer.NOTICE_USER_NAME.length()));
            System.out.println(sender.getName());
            users.forEach(u -> System.out.println("\n\t" + u.getName()));
            if(sender.isUsingGUI())
                return STNBServer.NOTICE_USER_NAME + "User name updated";
            return "User name updated";
        }
        // If user asking for number of online users
        else if(request.startsWith(STNBServer.REQUEST_ONLINE_USERS_NBR)){
            // Send a message to the user
            return STNBServer.NOTICE_ONLINE_USERS_NBR + users.size();
        }
        // Check if the user wants to exit
        else if(request.trim().equals("\\exit") || request.trim().equals("/exit") || request.equals(STNBServer.REQUEST_USER_LOGOUT)){
            // remove the user from the server and close its connection
            System.out.println("# User " + sender.getName() + " / " + sender.getSocketChannel() + " disconnected.");
            users.remove(sender);
            // close the connection
            try {
                sender.getSocketChannel().close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
        // If user asking for his identifier
        else if(request.startsWith(STNBServer.REQUEST_USER_IDENTIFIER)){
            // Send a message to the user
            return STNBServer.NOTICE_USER_IDENTIFIER + sender.getName() + "@" + sender.getId();
        }
        // If user asking for list of users
        else if(request.startsWith(STNBServer.REQUEST_LIST_USERS)){
            System.out.println("User asking for list of users");
            String users = STNBServer.NOTICE_LIST_USERS; // name@id,...
            for (User u : STNBServer.users) {
                users += u.getName() + "@" + u.getId() + ",";
            }
            return users;
        }

        // Check if the user wants help
        else if(request.trim().equals("\\help") || request.trim().equals("/help") || request.trim().equals("/?") || request.trim().equals("\\?")){
            return """
                    Messages must be as follow: "<id>, <id> ... : <message>".
                    To send a message to all the users, type "all" or "*" instead of the id.
                    "\\exit" \t  : To exit the application.
                    "\\help" \t  : To display help.
                    "\\users"\t  : To display all the users with IDs
                    "\\myid" \t  : To display your ID.
                    """;
        }

        // This block reserved for user help commands
        // - users
        else if(request.trim().equals("\\users") || request.trim().equals("/users")){
            String response =  "Number of users online: " + users.size() + "\n";
            for(int i = 0; i < users.size(); i++){
                response += "\t " + users.get(i).getName() + " : " + users.get(i).getId() + ( (sender.equals(users.get(i)))? " (YOU)" : "") +"\n";
            }
            return response;
        }
        // - myid
        else if(request.trim().equals("\\myid") || request.trim().equals("/myid")){
            return "Your ID is: " + sender.getId();
        }
        // Check if the user wants to broadcast a message
        else if(request.contains(":")){
            // If the user wants to send a message to all the users
            if( (request.split(":")[0].trim().trim().equals("all") || request.split(":")[0].trim().trim().equals("*")) && request.split(":").length > 1){
                // Broadcast the message to all the users
                broadcast("[" + sender.getName() + "@" + sender.getId() + "] : " + request.split(":")[1], sender);
            }
            // If the user wants to send a message to specific users
            else if(request.split(":").length > 1){
                System.out.println("DEBUG: send to specific users");
                // Get the users Sessions using their IDs
                ArrayList<User> tmpUsers = new ArrayList<>();
                String error = "";
                boolean invalidId = false;
                for(String id : request.split(":")[0].trim().split(",")){
                    try{
                        tmpUsers.add(getUserById(Integer.parseInt(id.trim())));
                    }
                    catch(NumberFormatException e){
                        invalidId = true;
                        error += "[ERROR]:Message could not be sent. Invalid ID: " + id + "\n";
                    }
                }
                // Filter offline users
                tmpUsers.removeIf(u -> u == null);
                // Broadcast the message to the users
                broadcast("[" + sender.getName() + "@" + sender.getId() + "] : " + request.split(":")[1], tmpUsers, sender);
                // If there is an invalid ID
                if(invalidId){
                    return error;
                }
            }
        }
        return null;
    }

    // Broadcast a message to specific users
    public static void broadcast(String message, ArrayList<User> users, User sender){
        // Send the message to all the users
        for(User user : users){
            try {
                if(user.equals(sender))
                    continue;
                user.getSocketChannel().write(ByteBuffer.wrap(message.getBytes()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Broadcast a message to all the users
    public static void broadcast(String message, User sender) {
        broadcast(message, users, sender);
    }

    // Get user by socket channel
    private static User getUserBySocketChannel(SocketChannel socketChannel){
        for(User user : users){
            if(user.getSocketChannel() == socketChannel){
                return user;
            }
        }
        return null;
    }

    // Get user by id
    private static User getUserById(int id) {
        for (User user : users) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }

    // Remove user by socket channel
    private static void removeUserBySocketChannel(SocketChannel socketChannel){
        for(User user : users){
            if(user.getSocketChannel() == socketChannel){
                users.remove(user);
                onlineUsersNbr--;
                break;
            }
        }
    }
}
