package com.example.qq.pojo;

import android.annotation.SuppressLint;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ChatMessage 类，表示聊天消息的基本信息。
 */
public class ChatMessage {
    private long id;                    // 消息ID（数据库自增主键）
    private final String content;             // 消息内容
    private final LocalDateTime timestamp;    // 消息时间戳
    private final String sender;              // 发送者
    private final String receiver;            // 接收者
    private final int avatarResId;            // 头像资源ID

    /**
     * ChatMessage 构造函数
     *
     * @param id 消息ID
     * @param content 消息内容
     * @param timestampStr 时间戳（字符串格式）
     * @param sender 发送者
     * @param receiver 接收者
     * @param avatarResId 头像资源ID
     */
    public ChatMessage(long id, String content, String timestampStr, String sender, String receiver, int avatarResId) {
        this.id = id;
        this.content = content;
        this.timestamp = parseTimestamp(timestampStr);
        this.sender = sender;
        this.receiver = receiver;
        this.avatarResId = avatarResId;  // 初始化头像资源ID
    }

    /**
     * ChatMessage 构造函数
     *
     * @param sender 发送者
     * @param receiver 接收者
     * @param content 消息内容
     * @param timestampStr 时间戳（字符串格式）
     * @param avatarResId 头像资源ID
     */
    public ChatMessage(String sender, String receiver ,String content, String timestampStr, int avatarResId) {
        this.content = content;
        this.timestamp = parseTimestamp(timestampStr);
        this.sender = sender;
        this.receiver = receiver;
        this.avatarResId = avatarResId;  // 初始化头像资源ID
    }

    // Getter 方法

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getFormattedTime() {
        return formatTimestamp(timestamp);
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public int getAvatarResId() {
        return avatarResId;
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

    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        return String.format("ChatMessage[id=%d, content=%s, time=%s, sender=%s, receiver=%s, avatarResId=%d]",
                id, content, getFormattedTime(), sender, receiver, avatarResId);
    }
}
