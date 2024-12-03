package com.example.qq.websocket;

import com.example.qq.domain.ChatMessage;

/**
 * WebSocket服务接口
 * 定义WebSocket的基本操作
 */
public interface WebSocketService {
    /**
     * 初始化并连接WebSocket
     */
    void init();

    /**
     * 发送消息
     * @param json 要发送的消息
     */
    void sendMessage(String json);

    /**
     * 添加监听器
     * @param listener 监听器
     */
    void addListener(WebSocketListener listener);

    /**
     * 移除监听器
     * @param listener 监听器
     */
    void removeListener(WebSocketListener listener);

    /**
     * 断开连接
     */
    void disconnect();

    /**
     * 接收到消息
     * @param message
     */
    void onMessage(String message);

    /**
     * 发送消息
     * @param message
     */
    void sendMessage(ChatMessage message);

    /**
     * WebSocket监听器接口
     */
    interface WebSocketListener {
        void onConnected();
        void onDisconnected();
        void onMessageReceived(String message);
        void onError(String error);
    }

    /**
     * 重新连接WebSocket
     */
    void reconnectIfNeeded();
} 