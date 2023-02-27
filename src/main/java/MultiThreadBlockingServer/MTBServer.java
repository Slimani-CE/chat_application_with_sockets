package MultiThreadBlockingServer;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MTBServer extends Thread{

    private static final String NOTICE_USER_LOGOUT = "[NOTICE_USER_LOGOUT]";
    private static final String NOTICE_ONLINE_USERS_NBR = "[NOTICE_ONLINE_USERS_NBR]";
    // List of online sessions
    private ArrayList<Session> sessions = new ArrayList<>();
    private int usersCount;
    private int userId;
    public static void main(String[] args) {
        // Create a new thread
        Thread thread = new MTBServer();
        // Start the thread
        thread.start();
    }

    // This method will be called when the thread is started
    @Override
    public void run() {
        try {
            // Create ServerSocket
            ServerSocket serverSocket = new ServerSocket(5050);
            System.out.println("# Server is running on address: " + serverSocket.getInetAddress() + " port: " + serverSocket.getLocalPort() + "");
            // Receive connections from users
            while(true){
                // Accept the connection
                Socket socket = serverSocket.accept();
                // Create a new thread to handle the user's session
                Session session = new Session(socket, userId++, this);
                // Add the session to the list of sessions
                sessions.add(session);
                usersCount++;
                // Start the session
                session.start();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // remove the session from the list of sessions
    public boolean removeSession(Session session){
        if(this.sessions.remove(session)){
            this.usersCount--;
            String name = session.getUserName();
            int userId = session.getUserId();
            try {
                session.getSocket().close();
                System.out.println("# Session removed successfully. " + usersCount + " users online");
                // Notice the other users that the user has left
                for(Session s : sessions){
                    s.getPrintWriter().println(MTBServer.NOTICE_USER_LOGOUT + " : User [" + name + "@" + userId + "] has left the chat.");
                    s.getPrintWriter().println(MTBServer.NOTICE_ONLINE_USERS_NBR + this.usersCount);
                }
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
            return true;
        }
        return false;
    }
    public Session getSession(int userId){
        for(Session session : sessions){
            if(session.getUserId() == userId){
                return session;
            }
        }
        return null;
    }

    public ArrayList<Session> getSessions() {
        return sessions;
    }

    public void setSessions(ArrayList<Session> sessions) {
        this.sessions = sessions;
    }

    public int getUsersCount() {
        return usersCount;
    }

    public void setUsersCount(int usersCount) {
        this.usersCount = usersCount;
    }
}
