package com.example.qq.handler;

import com.example.qq.domain.WebSocketMessage;

public interface MessageHandler {
    /**
     * 发送好友请求
     * @param targetUsername 目标用户名
     * @param message 请求消息
     */
    void sendFriendRequest(String targetUsername, String message);

    /**
     * 接受好友请求
     * @param fromUsername 发送请求的用户名
     */
    void acceptFriendRequest(String fromUsername);

    /**
     * 拒绝好友请求
     * @param fromUsername 发送请求的用户名
     */
    void rejectFriendRequest(String fromUsername);

    void sendMessage(WebSocketMessage message);

    void sendChatMessage(String toUsername, String content);

    /**
     * 处理接收到的WebSocket消息
     * @param message 接收到的消息
     */
    void handleReceivedMessage(WebSocketMessage message);
} 