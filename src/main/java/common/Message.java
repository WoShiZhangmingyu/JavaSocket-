package common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {
    private String sender;
    private String recipient;
    private String content;
    private LocalDateTime timestamp;

    // 构造函数
    public Message(String sender, String recipient, String content) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.timestamp = LocalDateTime.now(); // 当前时间作为发送时间
    }

    public Message(String sender, String content) {
        this(sender, "Everyone", content);
    }

    // Getter 和 Setter 方法
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // 格式化时间戳的字符串表示
    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.format(formatter);
    }

    // 重写toString方法，便于打印或显示消息详情
    @Override
    public String toString() {
        return String.format("[%s] %s -> %s: %s",
                getFormattedTimestamp(),
                sender,
                recipient,
                content);
    }
}
