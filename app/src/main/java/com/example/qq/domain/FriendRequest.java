package com.example.qq.domain;

/**
 * 好友请求实体类
 * 用于存储和管理好友请求的相关信息，包括请求状态的跟踪和处理
 *
 * @author yunxi
 * @version 1.0
 */
public class FriendRequest {
    /** 用户ID */
    private String userId;
    
    /** 用户昵称 */
    private String nickname;
    
    /** 用户名（登录账号） */
    private String username;
    
    /** 用户头像URL */
    private String avatarUrl;
    
    /** 好友验证消息 */
    private String message;
    
    /** 请求发送的时间戳（Unix时间戳） */
    private long timestamp;
    
    /** 
     * 请求状态
     * 0-待处理
     * 1-已接受
     * 2-已拒绝 
     */
    private int status;

    /**
     * 无参构造函数
     */
    public FriendRequest() {
    }

    /**
     * 全参数构造函数
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param nickname 用户昵称
     * @param avatarUrl 头像URL
     * @param message 验证消息
     * @param timestamp 请求时间戳
     * @param status 请求状态
     */
    public FriendRequest(String userId, String username, String nickname, 
            String avatarUrl, String message, long timestamp, int status) {
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
    }

    /**
     * 获取用户昵称
     * @return 用户昵称
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 设置用户昵称
     * @param nickname 用户昵称
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 获取用户ID
     * @return 用户ID
     */
    public String getUserId() { return userId; }

    /**
     * 设置用户ID
     * @param userId 用户ID
     */
    public void setUserId(String userId) { this.userId = userId; }

    /**
     * 获取用户名
     * @return 用户名
     */
    public String getUsername() { return username; }

    /**
     * 设置用户名
     * @param username 用户名
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * 获取头像URL
     * @return 头像URL
     */
    public String getAvatarUrl() { return avatarUrl; }

    /**
     * 设置头像URL
     * @param avatarUrl 头像URL
     */
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    /**
     * 获取验证消息
     * @return 验证消息
     */
    public String getMessage() { return message; }

    /**
     * 设置验证消息
     * @param message 验证消息
     */
    public void setMessage(String message) { this.message = message; }

    /**
     * 获取请求时间戳
     * @return 请求时间戳
     */
    public long getTimestamp() { return timestamp; }

    /**
     * 设置请求时间戳
     * @param timestamp 请求时间戳
     */
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    /**
     * 获取请求状态
     * @return 请求状态：0-待处理，1-已接受，2-已拒绝
     */
    public int getStatus() { return status; }

    /**
     * 设置请求状态
     * @param status 请求状态：0-待处理，1-已接受，2-已拒绝
     */
    public void setStatus(int status) { this.status = status; }

    /**
     * 获取格式化后的时间字符串
     * @return 格式化后的时间字符串
     * @deprecated 临时实现，需要替换为真实的时间格式化逻辑
     */
    public String getFormattedTime() {
        // TODO: 根据timestamp格式化时间
        return "刚刚"; // 临时返回，后续需要实现具体的时间格式化逻辑
    }

    /**
     * 从WebSocket消息创建好友请求对象
     * 
     * @param message WebSocket消息对象
     * @return 新创建的好友请求对象
     * @throws IllegalArgumentException 如果消息对象为null或缺少必要信息
     */
    public static FriendRequest fromWebSocketMessage(WebSocketMessage message) {
        if (message == null || message.getUser() == null) {
            throw new IllegalArgumentException("WebSocket message or user cannot be null");
        }
        
        return new FriendRequest(
            message.getUser(),  // userId
            message.getUser(),  // username
            "",                 // nickname - 将在后续设置
            "",                 // avatarUrl - 将在后续设置
            message.getMessage(),
            System.currentTimeMillis(),
            0                   // 初始状态为待处理
        );
    }
} 