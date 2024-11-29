package com.example.qq.domain;

import com.google.gson.annotations.SerializedName;

/**
 * WebSocket消息实体类
 * 用于与服务器进行WebSocket通信的消息格式
 */
public class WebSocketMessage {
    
    /**
     * 系统消息类型
     * 0: 系统消息
     * 1: 用户消息
     * 2: 好友请求
     * 3: 好友接受
     */
    @SerializedName("system")
    private int systemType;

    /**
     * 发送者用户名
     */
    @SerializedName("user")
    private String user;

    /**
     * 接收者用户名
     */
    @SerializedName("targetname")
    private String target;

    /**
     * 消息内容
     */
    @SerializedName("message")
    private String message;

    // 构造函数
    public WebSocketMessage(int systemType, String user, String target, String message) {
        this.systemType = systemType;
        this.user = user;
        this.target = target;
        this.message = message;
    }

    // Getters and Setters
    public int getSystemType() {
        return systemType;
    }

    public void setSystemType(int systemType) {
        this.systemType = systemType;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
} 