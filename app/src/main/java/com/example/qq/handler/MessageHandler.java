package com.example.qq.handler;

import com.example.qq.domain.WebSocketMessage;
import com.example.qq.handler.impl.BaseMessageHandler;

import okhttp3.Response;
import okhttp3.WebSocket;

/**
 * 消息处理器接口
 * 定义了处理好友关系和即时通讯的核心功能，包括好友请求、聊天消息和未读消息管理等
 *
 * @author yunxi
 * @version 1.0
 * @see BaseMessageHandler
 * @see WebSocketMessage
 */
public interface MessageHandler {
    
    /**
     * 发送好友请求
     * 向指定用户发送添加好友请求
     *
     * @param targetUsername 目标用户名
     * @param message 请求消息内容
     */
    void sendFriendRequest(String targetUsername, String message);

    /**
     * 接受好友请求
     * 接受来自指定用户的好友请求
     *
     * @param fromUsername 发送请求的用户名
     */
    void acceptFriendRequest(String fromUsername);

    /**
     * 拒绝好友请求
     * 拒绝来自指定用户的好友请求
     *
     * @param fromUsername 发送请求的用户名
     */
    void rejectFriendRequest(String fromUsername);

    /**
     * 发送WebSocket消息
     * @param message WebSocket消息对象
     */
    void sendMessage(WebSocketMessage message);

    /**
     * 发送聊天消息
     * 向指定用户发送聊天消息
     *
     * @param toUsername 接收消息的用户名
     * @param content 消息内容
     */
    void sendChatMessage(String toUsername, String content);

    /**
     * 处理接收到的WebSocket消息
     * 根据消息类型进行相应处理
     *
     * @param message 接收到的消息对象
     */
    void handleReceivedMessage(WebSocketMessage message);

    /**
     * 删除好友关系
     * 可选择是否通知对方
     *
     * @param username 要删除的好友用户名
     * @param notifyPeer 是否需要通知对方
     */
    void deleteFriend(String username, boolean notifyPeer);

    /**
     * 删除好友关系
     * 默认会通知对方
     *
     * @param username 要删除的好友用户名
     */
    void deleteFriend(String username);

    /**
     * WebSocket连接建立时的回调
     * @param webSocket WebSocket实例
     * @param response 连接响应
     */
    void onOpen(WebSocket webSocket, Response response);

    /**
     * WebSocket连接失败时的回调
     * @param webSocket WebSocket实例
     * @param t 异常信息
     * @param response 失败响应
     */
    void onFailure(WebSocket webSocket, Throwable t, Response response);

    /**
     * 更新未读消息计数
     * 更新来自指定用户的未读消息数量
     *
     * @param fromUsername 发送消息的用户名
     * @param count 未读消息数量
     */
    void updateUnreadCount(String fromUsername, int count);

    /**
     * 清除指定用户的未读消息计数
     * 通常在查看聊天记录后调用
     *
     * @param username 用户名
     */
    void clearUnreadCount(String username);

    /**
     * 获取指定用户的未读消息数量
     * 
     * @param username 用户名
     * @return 未读消息数量
     */
    int getUnreadCount(String username);

    /**
     * 更新好友请求计数
     * 更新未处理的好友请求数量
     *
     * @param count 未处理的好友请求数量
     */
    void updateFriendRequestCount(int count);
}