package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ClientGUI extends Component {
    private JTextField serverAddressField, portField, usernameField, messageField;
    private JButton connectButton, sendButton;
    private JTextArea chatArea;
    private JList<String> onlineList;
    private DefaultListModel<String> onlineListModel;
    private Client client;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientGUI().initializeUI());
    }

    private void initializeUI() {
        JFrame frame = new JFrame("Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        messageField = new JTextField(30);
        sendButton = new JButton("Send");
        inputPanel.add(messageField);
        inputPanel.add(sendButton);
        southPanel.add(inputPanel, BorderLayout.CENTER);

        JPanel connectPanel = new JPanel();
        connectPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        serverAddressField = new JTextField("localhost", 10);
        portField = new JTextField("12345", 5);
        usernameField = new JTextField("User", 10);
        connectButton = new JButton("Connect");
        connectPanel.add(new JLabel("Server: "));
        connectPanel.add(serverAddressField);
        connectPanel.add(new JLabel(" Port: "));
        connectPanel.add(portField);
        connectPanel.add(new JLabel(" Username: "));
        connectPanel.add(usernameField);
        connectPanel.add(connectButton);
        southPanel.add(connectPanel, BorderLayout.SOUTH);

        frame.add(southPanel, BorderLayout.SOUTH);

        onlineListModel = new DefaultListModel<>();
        onlineList = new JList<>(onlineListModel);
        JScrollPane onlineScrollPane = new JScrollPane(onlineList);
        onlineScrollPane.setPreferredSize(new Dimension(150, 0));
        frame.add(onlineScrollPane, BorderLayout.EAST);

        createEvents();

        frame.setVisible(true);
    }

    private void createEvents() {
        connectButton.addActionListener(e -> {
            String serverAddress = serverAddressField.getText();
            int port = Integer.parseInt(portField.getText());
            String username = usernameField.getText();
            connectButton.setEnabled(false);
            try {
                client = new Client(serverAddress, port, username, new GUIListener());
                client.connect();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "连接失败，请检查输入信息。", "连接错误", JOptionPane.ERROR_MESSAGE);
                connectButton.setEnabled(true);
            }
        });

        sendButton.addActionListener(e -> {
            String message = messageField.getText();
            if (!message.isEmpty()) {
                client.sendMessage(message);
                displaySentMessage(message);
                messageField.setText("");
            }
        });

        onlineList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selectedUser = onlineList.getSelectedValue();
                    if (selectedUser != null && client != null) {
                        String privateMessage = JOptionPane.showInputDialog(ClientGUI.this,
                                "输入要发送给 " + selectedUser + " 的消息:", "发送私信", JOptionPane.PLAIN_MESSAGE);
                        if (privateMessage != null && !privateMessage.trim().isEmpty()) {
                            client.sendPrivateMessage(selectedUser, privateMessage);
                            displaySentMessage("[私信给 " + selectedUser + "]: " + privateMessage); // 显示发送的私聊消息
                        }
                    }
                }
            }
        });
    }

    private void displaySentMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(client.getUsername() + ": " + message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    private class GUIListener implements Client.OnMessageReceivedListener {
        @Override
        public void onMessageReceived(String message) {
            SwingUtilities.invokeLater(() -> {
                if (message.startsWith("/users ")) { // 检查消息是否以/users开头
                    onUpdateOnlineUsers(message.substring(7)); // 去掉"/users "前缀，然后更新在线用户列表
                }else if(message.equals("/forceLogout")){
                    onForceLogout();
                }else if(message.equals("/server/ERROR: 用户名已被占用，请选择其他用户名。")){
                    JOptionPane.showMessageDialog(ClientGUI.this, "用户名已被占用，请选择其他用户名。", "连接错误", JOptionPane.ERROR_MESSAGE);
                    connectButton.setEnabled(true);
                    client.disconnect();
                }else if(message.equals("/server/SUCCESS: 连接成功")){
                    JOptionPane.showMessageDialog(ClientGUI.this, "连接成功", "连接状态", JOptionPane.INFORMATION_MESSAGE);
                }
                else {
                    chatArea.append(message + "\n");
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());
                }
            });
        }

        @Override
        public void onConnectionLost() {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(ClientGUI.this, "连接丢失，请检查网络或重新登录。", "连接错误", JOptionPane.ERROR_MESSAGE);
                connectButton.setEnabled(true);
            });
        }

        @Override
        public void onUpdateOnlineUsers(String userListData) {
            SwingUtilities.invokeLater(() -> {
                String[] usernames = userListData.split(","); // 假设用户列表是以逗号分隔的用户名
                onlineListModel.clear(); // 清空现有在线用户列表
                for (String username : usernames) {
                    onlineListModel.addElement(username.trim()); // 添加每个用户名到在线用户列表模型中
                }
            });
        }

        @Override
        public void onForceLogout(){
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, "抱歉！您已被服务器强制下线！", "强制下线", JOptionPane.WARNING_MESSAGE);
                System.exit(0); // 关闭客户端程序
            });
        }
    }
}
