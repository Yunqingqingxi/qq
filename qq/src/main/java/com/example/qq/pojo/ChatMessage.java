package com.example.qq.pojo;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

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
    private LocalDateTime parseTimestamp(String timestamp) {
        try {
            // 检查是否为ISO 8601格式
            if (timestamp.contains("T")) {
                // 按ISO 8601格式解析
                DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
                return LocalDateTime.parse(timestamp, isoFormatter);
            } else {
                // 按yyyy-MM-dd HH:mm:ss格式解析
                DateTimeFormatter defaultFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                return LocalDateTime.parse(timestamp, defaultFormatter);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 如果解析失败，返回null
        }
    }



    private String formatTimestamp(LocalDateTime timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.format(formatter);
    }

    // 将时间戳字符串转换为完整的日期时间格式
    private String formatTimestamp(String timestamp) {
        // 定义目标格式
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        try {
            // 解析输入时间戳
            Date date = inputFormat.parse(timestamp);
            // 返回格式化后的日期时间字符串
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            // 返回原始时间戳（如果解析失败）
            return timestamp;
        }
    }


    // 方法重写

    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        return String.format("ChatMessage[ content=%s, time=%s, sender=%s, receiver=%s, avatarResId=%d]",
               content, getFormattedTime(), sender, receiver, avatarResId);
    }
}
