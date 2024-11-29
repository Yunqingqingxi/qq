package com.example.qq.domain;

/**
 * 通知消息实体类
 * 用于封装系统通知消息的相关信息，包括标题、内容和时间戳
 * 
 * @author yunxi
 * @version 1.0
 */
public class NotificationMessage {
    /** 通知标题 */
    private final String title;
    /** 通知内容 */
    private final String content;
    /** 通知时间戳 */
    private final long timestamp;

    /**
     * 构造一个新的通知消息对象
     * 
     * @param title 通知标题
     * @param content 通知内容
     * @param timestamp 通知发送时的时间戳
     */
    public NotificationMessage(String title, String content, long timestamp) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
    }

    /**
     * 获取通知标题
     * @return 通知的标题
     */
    public String getTitle() { return title; }

    /**
     * 获取通知内容
     * @return 通知的文本内容
     */
    public String getContent() { return content; }

    /**
     * 获取通知时间戳
     * @return 通知发送时的时间戳
     */
    public long getTimestamp() { return timestamp; }

    /**
     * 返回通知的字符串表示
     * @return 包含通知所有字段的字符串
     */
    @Override
    public String toString() {
        return "NotificationMessage{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
} 