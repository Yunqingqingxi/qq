package com.example.qq.event;

/**
 * 好友列表更新事件类
 * 当好友列表发生变化时触发此事件，用于通知相关组件刷新好友列表UI和数据。
 * 变化情况包括：添加新好友、删除好友、好友信息更新等。
 * 
 * @author yunxi
 * @version 1.0
 * @see com.example.qq.domain.FriendList
 * @see com.example.qq.event.FriendDeletedEvent
 * @see com.example.qq.constant.MessageType#FRIEND_REQUEST
 * @see com.example.qq.constant.MessageType#FRIEND_ACCEPT
 */
public class FriendListUpdateEvent {
    
    /**
     * 默认构造函数
     * 由于这是一个简单的事件通知类，不需要携带额外数据，
     * 仅用于通知观察者刷新好友列表
     */
    public FriendListUpdateEvent() {
    }
} 