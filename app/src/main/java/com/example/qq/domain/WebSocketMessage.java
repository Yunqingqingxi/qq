package com.example.qq.domain;

import com.example.qq.constant.MessageType;
import com.google.gson.annotations.SerializedName;

/**
 * WebSocket消息实体类
 * 用于与服务器进行WebSocket通信的消息格式，支持多种类型的消息传输
 *
 * @author yunxi
 * @version 1.0
 */
public class WebSocketMessage {
    
    /** 
     * 系统消息类型
     * 0: 系统消息
     * 1: 用户消息
     * 2: 好友请求
     * 3: 好友接受
     * @see MessageType
     */
    @SerializedName("system")
    private int systemType;

    /** 发送消息的用户名 */
    @SerializedName("user")
    private String user;

    /** 接收消息的用户名 */
    @SerializedName("targetname")
    private String targetname;

    /** 消息的具体内容 */
    @SerializedName("message")
    private String message;

    /** 消息发送的时间戳（Unix时间戳） */
    @SerializedName("timestamp")
    private long timestamp;

    /** 消息类型枚举值 */
    private MessageType type;

    /**
     * 无参构造函数
     */
    public WebSocketMessage() {
    }

    /**
     * 使用系统类型构造消息
     *
     * @param systemType 系统消息类型
     * @param user 发送者用户名
     * @param targetname 接收者用户名
     * @param message 消息内容
     */
    public WebSocketMessage(int systemType, String user, String targetname, String message) {
        this.systemType = systemType;
        this.user = user;
        this.targetname = targetname;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 使用消息类型枚举构造消息
     *
     * @param type 消息类型
     * @param user 发送者用户名
     * @param targetname 接收者用户名
     * @param message 消息内容
     */
    public WebSocketMessage(MessageType type, String user, String targetname, String message) {
        this.systemType = type.getValue();
        this.type = type;
        this.user = user;
        this.targetname = targetname;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 获取系统消息类型
     * @return 系统消息类型的数值
     */
    public int getSystemType() {
        return systemType;
    }

    /**
     * 设置系统消息类型
     * @param systemType 系统消息类型的数值
     */
    public void setSystemType(int systemType) {
        this.systemType = systemType;
    }

    /**
     * 获取发送者用户名
     * @return 发送消息的用户名
     */
    public String getUser() {
        return user;
    }

    /**
     * 设置发送者用户名
     * @param user 发送消息的用户名
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * 获取接收者用户名
     * @return 接收消息的用户名
     */
    public String getTargetname() {
        return targetname;
    }

    /**
     * 设置接收者用户名
     * @param targetname 接收消息的用户名
     */
    public void setTargetname(String targetname) {
        this.targetname = targetname;
    }

    /**
     * 获取消息内容
     * @return 消息的具体内容
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置消息内容
     * @param message 消息的具体内容
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 获取消息时间戳
     * @return 消息发送的时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 设置消息时间戳
     * @param timestamp 消息发送的时间戳
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 获取消息类型
     * @return 消息类型枚举值
     */
    public MessageType getType() {
        return type;
    }

    /**
     * 设置消息类型
     * @param type 消息类型枚举值
     */
    public void setType(MessageType type) {
        this.type = type;
        this.systemType = type.getValue();
    }
} 