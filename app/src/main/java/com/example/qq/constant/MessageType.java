package com.example.qq.constant;

/**
 * 消息类型枚举类
 * 定义了系统中所有的消息类型
 * 
 * @author yunxi
 * @version 1.0
 */
public enum MessageType {
    /**
     * 普通聊天消息
     */
    CHAT(1),
    
    /**
     * 好友请求消息
     */
    FRIEND_REQUEST(2),
    
    /**
     * 接受好友请求的消息
     */
    FRIEND_ACCEPT(3),
    
    /**
     * 拒绝好友请求的消息
     */
    FRIEND_REJECT(4),
    
    /**
     * 好友删除通知消息
     */
    FRIEND_DELETED(5),
    
    /**
     * 强制下线消息
     */
    FORCE_OFFLINE(6),
    
    /**
     * 在线状态检测消息
     */
    ONLINE_CHECK(7);

    private final int value;

    /**
     * 构造函数
     * @param value 消息类型的数值
     */
    MessageType(int value) {
        this.value = value;
    }

    /**
     * 获取消息类型的数值
     * @return 消息类型对应的整数值
     */
    public int getValue() {
        return value;
    }
} 