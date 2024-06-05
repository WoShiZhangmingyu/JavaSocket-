package client;

import server.Server;
import server.ServerGUI;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    private Socket socket;
    private DataOutputStream outputStream;
    private PrintWriter out;
    private BufferedReader in;
    private String serverAddress;
    private int port;
    private String username;
    private OnMessageReceivedListener listener;

    public Client(String serverAddress, int port, String username, OnMessageReceivedListener listener) throws IOException {
        this.serverAddress = serverAddress;
        this.port = port;
        this.username = username;
        this.listener = listener;
        initConnection();
    }



    private void initConnection() throws IOException {
        if (socket != null && !socket.isClosed()) {
            return;
        }
        socket = new Socket(serverAddress, port);
        outputStream = new DataOutputStream(socket.getOutputStream());
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
    }

    public void sendMessage(String message) {
        if (outputStream != null && !socket.isClosed()) {
            try {
                out.println(message);
                out.flush();
            } catch (Exception e) {
                handleSendError(e);
            }
        } else {
            System.err.println("Socket is not properly initialized or is closed.");
        }
    }

    public void sendPrivateMessage(String recipient, String message) {
        sendMessage("/pm " + recipient + " " + message);
    }

    private void handleSendError(Exception e) {
        e.printStackTrace();
        try {
            socket.close();
        } catch (IOException closeException) {
            closeException.printStackTrace();
        }
        listener.onConnectionLost();
    }

    public void connect() {
        try {
            initConnection();
            sendMessage(username); // 发送用户名以登录

            Thread readerThread = new Thread(() -> {
                String message;
                try {
                    while ((message = in.readLine()) != null) {
                        listener.onMessageReceived(message);
                    }
                } catch (IOException e) {
                    listener.onConnectionLost();
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            readerThread.start();
        } catch (UnknownHostException e) {
            System.err.println("Server not found: " + e.getMessage());
            listener.onConnectionLost();
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            listener.onConnectionLost();
        }
    }

    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }

    public interface OnMessageReceivedListener {
        void onMessageReceived(String message);
        void onConnectionLost();
        void onUpdateOnlineUsers(String userListData);
        void onForceLogout();
    }
}
