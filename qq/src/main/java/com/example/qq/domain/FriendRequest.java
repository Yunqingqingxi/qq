package com.example.qq.domain;

/**
 * 好友请求实体类
 * 用于存储和管理好友请求的相关信息
 */
public class FriendRequest {
    /** 用户ID */
    private String userId;
    /** 用户名 */
    private String username;
    /** 头像URL */
    private String avatarUrl;
    /** 验证消息 */
    private String message;
    /** 请求时间戳 */
    private long timestamp;
    /** 请求状态：0-待处理，1-已接受，2-已拒绝 */
    private int status;

    /**
     * 构造函数
     * @param userId 用户ID
     * @param username 用户名
     * @param avatarUrl 头像URL
     * @param message 验证消息
     * @param timestamp 请求时间戳
     */
    public FriendRequest(String userId, String username, String avatarUrl, String message, long timestamp) {
        this.userId = userId;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.message = message;
        this.timestamp = timestamp;
        this.status = 0; // 初始状态为待处理
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
     */
    public String getFormattedTime() {
        // TODO: 根据timestamp格式化时间
        return "刚刚"; // 临时返回，后续需要实现具体的时间格式化逻辑
    }

    /**
     * 从WebSocket消息创建好友请求对象
     * @param message WebSocket消息对象
     * @return 好友请求对象
     */
    public static FriendRequest fromWebSocketMessage(WebSocketMessage message) {
        return new FriendRequest(
            message.getUser(),  // 用户ID就用发送者的用户名
            message.getUser(),  // 用户名
            "",  // 头像URL暂时为空，后续可以通过用户信息接口获取
            message.getMessage(),  // 验证消息
            System.currentTimeMillis()  // 当前时间戳
        );
    }
} 