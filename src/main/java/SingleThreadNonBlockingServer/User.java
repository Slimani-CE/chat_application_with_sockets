package SingleThreadNonBlockingServer;

import java.nio.channels.SocketChannel;

public class User {
    private String name;
    private int id;
    private SocketChannel socketChannel;
    private boolean isUsingGUI = false;

    public User() {
    }

    public User(String name, int id, SocketChannel socketChannel) {
        this.name = name;
        this.id = id;
        this.socketChannel = socketChannel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public void setUsingGUI(boolean usingGUI) {
        isUsingGUI = usingGUI;
    }

    public boolean isUsingGUI() {
        return isUsingGUI;
    }

    @Override
    public String toString() {
        return "user{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", socketChannel=" + socketChannel +
                '}';
    }
}
