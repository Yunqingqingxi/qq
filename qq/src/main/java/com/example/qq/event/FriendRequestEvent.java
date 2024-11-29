package com.example.qq.event;

import com.example.qq.domain.WebSocketMessage;

/**
 * 好友请求事件类
 * 用于EventBus在不同组件间传递好友请求相关的消息
 */
public class FriendRequestEvent {
    private final WebSocketMessage message;
    private final int eventType;  // 2: 好友请求, 3: 接受请求, 4: 拒绝请求

    public FriendRequestEvent(WebSocketMessage message) {
        this.message = message;
        this.eventType = message.getSystemType();  // 直接使用系统消息类型
    }

    /**
     * 获取消息内容
     * @return WebSocket消息对象
     */
    public WebSocketMessage getMessage() {
        return message;
    }

    /**
     * 获取事件类型
     * @return 事件类型 2: 好友请求, 3: 接受请求, 4: 拒绝请求
     */
    public int getEventType() {
        return eventType;
    }

    /**
     * 获取发送者用户名
     * @return 发送者用户名
     */
    public String getFromUsername() {
        return message.getUser();
    }

    /**
     * 获取接收者用户名
     * @return 接收者用户名
     */
    public String getToUsername() {
        return message.getTarget();
    }

    /**
     * 获取消息内容
     * @return 消息内容
     */
    public String getContent() {
        return message.getMessage();
    }

    /**
     * 判断是否为新的好友请求
     * @return 是否为新的好友请求
     */
    public boolean isNewRequest() {
        return eventType == 2;  // systemType = 2 表示好友请求
    }

    /**
     * 判断是否为接受好友请求
     * @return 是否为接受好友请求
     */
    public boolean isAccepted() {
        return eventType == 3;  // systemType = 3 表示接受请求
    }

    /**
     * 判断是否为拒绝好友请求
     * @return 是否为拒绝好友请求
     */
    public boolean isRejected() {
        return eventType == 4;  // systemType = 4 表示拒绝请求
    }

    /**
     * 获取格式化的显示消息
     * @return 格式化后的消息
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
     * @param currentUsername 当前用户名
     * @return 是否为消息接收者
     */
    public boolean isTargetUser(String currentUsername) {
        return currentUsername.equals(message.getTarget());
    }
} 