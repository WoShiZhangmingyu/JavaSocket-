package server;

import common.Message;

import java.io.*;
import java.net.Socket;

public class ServerThread extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private Server server;
    private volatile boolean running = true;

    public ServerThread(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error initializing streams for client: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            this.username = in.readLine();
            if (server.isUsernameTaken(this.username)) {
                sendRawMessage("/server/ERROR: 用户名已被占用，请选择其他用户名。");
                socket.close();
                return;
            }

            sendRawMessage("/server/SUCCESS: 连接成功");
            System.out.println("账号" + this.username + "已经登录");
            server.addClient(this);  // 注意不要在构造函数中重复调用 addClient
            server.broadcast(new Message(username, "Server", username + " has joined the chat!"), this);

            String inputLine;
            while (running && (inputLine = in.readLine()) != null) {
                if (inputLine.startsWith("/pm ")) {
                    handlePrivateMessage(inputLine);
                }
                else {
                    server.broadcast(new Message(username, "Everyone", inputLine), this);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            boolean removed = server.removeClient(this);
            if (removed) {
                server.broadcast(new Message(username, "Server", username + " has left the chat!"), null);
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void send(Message message) {
        if (out != null && !out.checkError()) {
            out.println(message.toString());
            out.flush();
        }
    }

    public void sendRawMessage(String message) {
        try {
            if (out != null && !out.checkError()) {
                out.println(message);
                out.flush();
            } else {
                System.err.println("Output stream is closed, cannot send message.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    private void handlePrivateMessage(String inputLine) {
        int firstSpace = inputLine.indexOf(" ");
        int secondSpace = inputLine.indexOf(" ", firstSpace + 1);
        if (secondSpace != -1) {
            String recipient = inputLine.substring(firstSpace + 1, secondSpace);
            String message = inputLine.substring(secondSpace + 1);
            for (ServerThread client : server.getClients()) {
                if (client.getUsername().equals(recipient)) {
                    client.send(new Message(username, recipient, message));
                    break;
                }
            }
        }
    }

    public String getUsername() {
        return username;
    }
    public void forceLogout() {
        running = false; // 停止主循环
        sendRawMessage("/forceLogout");
        try {
            socket.close(); // 关闭Socket以触发IOException并停止线程
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
