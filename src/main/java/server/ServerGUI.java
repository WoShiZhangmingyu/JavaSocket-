package server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ServerGUI extends JFrame {
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private Server server;

    public ServerGUI() {
        setTitle("Chat Server");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        JScrollPane scrollPane = new JScrollPane(userList);

        JButton forceLogoutButton = new JButton("Force Logout");
        forceLogoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedUser = userList.getSelectedValue();
                if (selectedUser != null) {
                    server.forceLogout(selectedUser);
                }
            }
        });

        // 系统消息输入框和发送按钮
        JTextField systemMessageField = new JTextField();
        JButton sendSystemMessageButton = new JButton("发送系统消息");
        sendSystemMessageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = systemMessageField.getText();
                if (message != null && !message.trim().isEmpty()) {
                    server.sendSystemMessage(message);
                    systemMessageField.setText(""); // 清空输入框
                }
            }
        });

        JPanel panel = new JPanel(new BorderLayout());

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(forceLogoutButton, BorderLayout.SOUTH);

        // 系统消息面板
        JPanel systemMessagePanel = new JPanel(new BorderLayout());
        systemMessagePanel.add(systemMessageField, BorderLayout.CENTER);
        systemMessagePanel.add(sendSystemMessageButton, BorderLayout.EAST);

        // 添加到主窗口
        add(panel, BorderLayout.CENTER);
        add(systemMessagePanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    public void updateUserList(List<ServerThread> clients) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (ServerThread client : clients) {
                userListModel.addElement(client.getUsername());
            }
        });
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
