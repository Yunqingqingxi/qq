package com.example.qq.pojo;

import android.annotation.SuppressLint;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * ChatMessage 类，表示聊天消息的基本信息。
 */
public class ChatMessage {
    private final String content;             // 消息内容
    private final LocalDateTime timestamp;    // 消息时间戳
    private final String sender;              // 发送者
    private final String receiver;            // 接收者
    private final int avatarResId;            // 头像资源ID

    /**
     * ChatMessage 构造函数
     *
     * @param content 消息内容
     * @param timestampStr 时间戳（字符串格式）
     * @param sender 发送者
     * @param receiver 接收者
     * @param avatarResId 头像资源ID
     */
    public ChatMessage( String sender, String receiver, String content, String timestampStr ,int avatarResId) {
        this.content = content;
        this.timestamp = parseTimestamp(timestampStr);
        this.sender = sender;
        this.receiver = receiver;
        this.avatarResId = avatarResId;  // 初始化头像资源ID
    }

//    /**
//     * ChatMessage 构造函数
//     *
//     * @param sender 发送者
//     * @param receiver 接收者
//     * @param content 消息内容
//     * @param timestampStr 时间戳（字符串格式）
//     * @param avatarResId 头像资源ID
//     */
//    public ChatMessage(String sender, String receiver ,String content, String timestampStr, int avatarResId) {
//        this.content = content;
//        this.timestamp = parseTimestamp(timestampStr);
//        this.sender = sender;
//        this.receiver = receiver;
//        this.avatarResId = avatarResId;  // 初始化头像资源ID
//    }

    // Getter 方法

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        // 解析时间部分
        LocalTime time = LocalTime.parse(timestampStr, formatter);
        // 使用当前日期并将时间部分设置为解析的时间
        return LocalDate.now().atTime(time);
    }

    private String formatTimestamp(LocalDateTime timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return timestamp.format(formatter);
    }


    // 方法重写

    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        return String.format("ChatMessage[ content=%s, time=%s, sender=%s, receiver=%s, avatarResId=%d]",
               content, getFormattedTime(), sender, receiver, avatarResId);
    }
}
