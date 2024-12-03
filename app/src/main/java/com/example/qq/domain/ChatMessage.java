package com.example.qq.domain;

/**
 * 聊天消息实体类
 * 用于封装聊天消息的相关信息，包括发送者、接收者、消息内容和时间戳
 * 
 * @author yunxi
 * @version 1.0
 */
public class ChatMessage {
    /** 消息发送者 */
    private  String sender;
    /** 消息接收者 */
    private  String receiver;
    /** 消息内容 */
    private  String content;
    /** 消息时间戳 */
    private long timestamp;

    /**
     * 设置消息发送者
     * @param sender 发送者的用户名
     */
    public void setSender(String sender) {
        this.sender = sender;
    }

    /**
     * 设置消息接收者
     * @param receiver 接收者的用户名
     */
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    /**
     * 设置消息内容
     * @param content 消息的文本内容
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 设置消息时间戳
     * @param timestamp 消息发送时的时间戳（Unix时间戳格式）
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 构造一个新的聊天消息对象
     * 
     * @param sender 发送者的用户名
     * @param receiver 接收者的用户名
     * @param content 消息内容
     * @param timestamp 消息发送时的时间戳
     */
    public ChatMessage(String sender, String receiver, String content, long timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = timestamp;
    }

    public ChatMessage() {
    }

    /**
     * 返回消息的字符串表示
     *
     * @return 包含消息所有字段的字符串
     */
    @Override
    public String toString() {
        return "ChatMessage{" +
                "sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    /**
     * 获取消息发送者
     * @return 发送者的用户名
     */
    public String getSender() { 
        return sender; 
    }

    /**
     * 获取消息接收者
     * @return 接收者的用户名
     */
    public String getReceiver() { 
        return receiver; 
    }

    /**
     * 获取消息内容
     * @return 消息的文本内容
     */
    public String getContent() { 
        return content; 
    }

    /**
     * 获取消息时间戳
     * @return 消息发送时的时间戳（Unix时间戳格式）
     */
    public long getTimestamp() { 
        return timestamp; 
    }

}