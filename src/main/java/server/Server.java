package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import common.Message;


public class Server {
    private static final int PORT = 12345; // 服务器端口
    private final List<ServerThread> clients = new ArrayList<>(); // 存储所有连接的客户端线程
    private ServerGUI gui;

    public Server(ServerGUI gui) {
        this.gui = gui;
    }
    public static void main(String[] args) {
        ServerGUI gui = new ServerGUI();
        Server server = new Server(gui);
        gui.setServer(server);
        server.startServer();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("服务器启动，正在监听端口: " + PORT);

            while (true) {
                Socket socket = serverSocket.accept(); // 阻塞等待客户端连接
                ServerThread serverThread = new ServerThread(socket, this);
                serverThread.start(); // 启动线程处理客户端请求
                System.out.println("新客户端连接: " + socket.getInetAddress());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("服务器启动失败");
        }
    }

    public synchronized void addClient(ServerThread client) {
        clients.add(client);
        updateOnlineUsers();
        gui.updateUserList(clients);
    }

    public synchronized boolean removeClient(ServerThread client) {
        boolean removed = clients.remove(client);
        if (removed) {
            updateOnlineUsers();
            gui.updateUserList(clients);
        }
        return removed;
    }

    public synchronized void broadcast(Message message, ServerThread excludeClient) {
        for (ServerThread client : clients) {
            if (client != excludeClient) {
                client.send(message);
            }
        }
    }

    public synchronized void updateOnlineUsers() {
        StringBuilder userList = new StringBuilder("/users ");
        for (ServerThread client : clients) {
            userList.append(client.getUsername()).append(",");
        }
        String userListMessage = userList.toString();
        if (userListMessage.endsWith(",")) {
            userListMessage = userListMessage.substring(0, userListMessage.length() - 1);
        }
        for (ServerThread client : clients) {
            client.sendRawMessage(userListMessage);
        }
    }

    public List<ServerThread> getClients() {
        return clients;
    }

    public void forceLogout(String username) {
        for (ServerThread client : clients) {
            if (client.getUsername().equals(username)) {
                client.interrupt(); // 中断客户端线程以强制下线
                removeClient(client); // 从列表中移除客户端
                client.forceLogout();
                gui.updateUserList(clients);
                break;
            }
        }
    }
    public synchronized boolean isUsernameTaken(String username) {
        for (ServerThread client : clients) {
            if (client.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void sendSystemMessage(String content) {
        Message systemMessage = new Message("Server", "Everyone", content);
        broadcast(systemMessage, null);
    }
}
