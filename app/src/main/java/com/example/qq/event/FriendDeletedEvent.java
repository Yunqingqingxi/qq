package com.example.qq.event;

/**
 * 好友删除事件类
 * 当好友被删除时触发此事件，用于通知相关组件更新界面和数据
 * 
 * @author yunxi
 * @version 1.0
 * @see com.example.qq.domain.WebSocketMessage
 * @see com.example.qq.constant.MessageType#FRIEND_DELETED
 */
public class FriendDeletedEvent {
    /** 被删除好友的用户名 */
    private final String friendUsername;

    /**
     * 构造一个好友删除事件
     * 
     * @param friendUsername 被删除好友的用户名
     * @throws IllegalArgumentException 如果friendUsername为null或空
     */
    public FriendDeletedEvent(String friendUsername) {
        if (friendUsername == null || friendUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Friend username cannot be null or empty");
        }
        this.friendUsername = friendUsername;
    }

    /**
     * 获取被删除好友的用户名
     * 
     * @return 好友的用户名
     */
    public String getFriendUsername() {
        return friendUsername;
    }
} 