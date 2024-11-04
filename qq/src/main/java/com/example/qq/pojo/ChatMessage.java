package com.example.qq.pojo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ChatMessage 类，表示聊天消息的基本信息。
 */
public class ChatMessage {
    private final int avatarResId;           // 头像资源ID
    private final String nickname;            // 用户昵称
    private final String message;             // 消息内容
    private final LocalDateTime timestamp;    // 消息时间戳
    private final boolean isSender;           // 是否为发送者的消息

    /**
     * ChatMessage 构造函数
     *
     * @param avatarResId 头像资源ID
     * @param nickname 用户昵称
     * @param message 消息内容
     * @param timestampStr 时间戳（字符串格式）
     * @param isSender 是否为发送者的消息
     */
    public ChatMessage(int avatarResId, String nickname, String message, String timestampStr, boolean isSender) {
        this.avatarResId = avatarResId;
        this.nickname = nickname;
        this.message = message;
        this.timestamp = parseTimestamp(timestampStr);
        this.isSender = isSender;  // 初始化发送者标识
    }

    // Getter 方法

    public int getAvatarResId() {
        return avatarResId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getMessage() {
        return message;
    }

    public String getFormattedTime() {
        return formatTimestamp(timestamp);
    }

    public boolean isSender() {  // 添加 isSender 方法
        return isSender;
    }

    // Private 方法

    private LocalDateTime parseTimestamp(String timestampStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(timestampStr, formatter);
    }

    private String formatTimestamp(LocalDateTime timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return timestamp.format(formatter);
    }

    // 方法重写

    @Override
    public String toString() {
        return String.format("ChatMessage[nickname=%s, message=%s, time=%s, isSender=%b]",
                nickname, message, getFormattedTime(), isSender);
    }
}
