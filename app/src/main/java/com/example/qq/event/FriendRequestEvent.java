package com.example.qq.event;

import com.example.qq.constant.MessageType;
import com.example.qq.domain.WebSocketMessage;

/**
 * 好友请求事件类
 * 用于EventBus在不同组件间传递好友请求相关的消息，包括发送请求、接受和拒绝等操作。
 * 
 * @author yunxi
 * @version 1.0
 * @see com.example.qq.domain.WebSocketMessage
 * @see com.example.qq.constant.MessageType#FRIEND_REQUEST
 * @see com.example.qq.constant.MessageType#FRIEND_ACCEPT
 * @see com.example.qq.constant.MessageType#FRIEND_REJECT
 */
public class FriendRequestEvent {
    /** WebSocket消息对象 */
    private final WebSocketMessage message;
    
    /** 
     * 事件类型
     * 2: 好友请求 {@link MessageType#FRIEND_REQUEST}
     * 3: 接受请求 {@link MessageType#FRIEND_ACCEPT}
     * 4: 拒绝请求 {@link MessageType#FRIEND_REJECT}
     */
    private final int eventType;

    /**
     * 构造好友请求事件
     * 
     * @param message WebSocket消息对象，不能为null
     * @throws IllegalArgumentException 如果message为null
     */
    public FriendRequestEvent(WebSocketMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("WebSocket message cannot be null");
        }
        this.message = message;
        this.eventType = message.getSystemType();
    }

    /**
     * 获取原始WebSocket消息对象
     * @return WebSocket消息对象
     */
    public WebSocketMessage getMessage() {
        return message;
    }

    /**
     * 获取事件类型
     * @return 事件类型代码
     * @see MessageType#FRIEND_REQUEST
     * @see MessageType#FRIEND_ACCEPT
     * @see MessageType#FRIEND_REJECT
     */
    public int getEventType() {
        return eventType;
    }

    /**
     * 获取发送者用户名
     * @return 发送请求的用户名
     */
    public String getFromUsername() {
        return message.getUser();
    }

    /**
     * 获取接收者用户名
     * @return 接收请求的用户名
     */
    public String getToUsername() {
        return message.getTargetname();
    }

    /**
     * 获取请求消息内容
     * @return 请求消息的具体内容
     */
    public String getContent() {
        return message.getMessage();
    }

    /**
     * 判断是否为新的好友请求
     * @return true表示是新的好友请求，false表示不是
     */
    public boolean isNewRequest() {
        return eventType == MessageType.FRIEND_REQUEST.getValue();
    }

    /**
     * 判断是否为接受好友请求
     * @return true表示是接受请求，false表示不是
     */
    public boolean isAccepted() {
        return eventType == MessageType.FRIEND_ACCEPT.getValue();
    }

    /**
     * 判断是否为拒绝好友请求
     * @return true表示是拒绝请求，false表示不是
     */
    public boolean isRejected() {
        return eventType == MessageType.FRIEND_REJECT.getValue();
    }

    /**
     * 获取格式化的显示消息
     * @return 根据事件类型格式化后的消息文本
     */
    public String getDisplayMessage() {
        switch (eventType) {
            case 2:
                return String.format("%s 请求添加您为好友: %s", getFromUsername(), getContent());
            case 3:
                return String.format("%s 接受了您的好友请求", getFromUsername());
            case 4:
                return String.format("%s 拒绝了您的好友请求", getFromUsername());
            default:
                return getContent();
        }
    }

    /**
     * 判断当前用户是否为消息接收者
     * 
     * @param currentUsername 当前用户名
     * @return true表示当前用户是接收者，false表示不是
     * @throws IllegalArgumentException 如果currentUsername为null或空
     */
    public boolean isTargetUser(String currentUsername) {
        if (currentUsername == null || currentUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Current username cannot be null or empty");
        }
        return currentUsername.equals(message.getTargetname());
    }
} 