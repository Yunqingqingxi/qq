package com.example.qq.domain;

import java.io.Serializable;

/**
 * 首页好友列表实体类
 * 用于展示好友列表中的好友信息，包含好友基本信息和最近的聊天记录
 *
 * @author yunxi
 * @version 1.0
 */
public class FriendList implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 好友的用户名（账号） */
    private String friendUsername;
    
    /** 好友的昵称 */
    private String friendNickName;
    
    /** 好友的头像URL */
    private String avatarUrl;
    
    /** 与该好友的最新聊天消息 */
    private String lastContext;
    
    /** 最新聊天消息的时间 */
    private String lastContextTime;

    /**
     * 无参构造函数
     */
    public FriendList() {
    }

    /**
     * 构造函数（不包含好友用户名）
     * 
     * @param friendNickName 好友昵称
     * @param lastContext 最新聊天消息
     * @param avatarUrl 头像URL
     * @param lastContextTime 最新消息时间
     */
    public FriendList(String friendNickName, String lastContext, String avatarUrl, String lastContextTime) {
        this.friendNickName = friendNickName;
        this.lastContext = lastContext;
        this.avatarUrl = avatarUrl;
        this.lastContextTime = lastContextTime;
    }

    /**
     * 构造函数（包含所有字段）
     * 
     * @param friendUsername 好友用户名
     * @param friendNickName 好友昵称
     * @param avatarUrl 头像URL
     * @param lastContext 最新聊天消息
     * @param lastContextTime 最新消息时间
     */
    public FriendList(String friendUsername, String friendNickName, String avatarUrl, String lastContext, String lastContextTime) {
        this.friendUsername = friendUsername;
        this.friendNickName = friendNickName;
        this.avatarUrl = avatarUrl;
        this.lastContext = lastContext;
        this.lastContextTime = lastContextTime;
    }

    /**
     * 获取好友用户名
     * @return 好友的用户名
     */
    public String getFriendUsername() {
        return friendUsername;
    }

    /**
     * 设置好友用户名
     * @param friendUsername 好友的用户名
     */
    public void setFriendUsername(String friendUsername) {
        this.friendUsername = friendUsername;
    }

    /**
     * 获取好友昵称
     * @return 好友的昵称
     */
    public String getFriendNickName() {
        return friendNickName;
    }

    /**
     * 设置好友昵称
     * @param friendNickName 好友的昵称
     */
    public void setFriendNickName(String friendNickName) {
        this.friendNickName = friendNickName;
    }

    /**
     * 获取好友头像URL
     * @return 好友的头像URL
     */
    public String getAvatarUrl() {
        return avatarUrl;
    }

    /**
     * 设置好友头像URL
     * @param avatarUrl 好友的头像URL
     */
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    /**
     * 获取最新聊天消息
     * @return 最新的聊天消息内容
     */
    public String getLastContext() {
        return lastContext;
    }

    /**
     * 设置最新聊天消息
     * @param lastContext 最新的聊天消息内容
     */
    public void setLastContext(String lastContext) {
        this.lastContext = lastContext;
    }

    /**
     * 获取最新消息时间
     * @return 最新消息的时间
     */
    public String getLastContextTime() {
        return lastContextTime;
    }

    /**
     * 设置最新消息时间
     * @param lastContextTime 最新消息的时间
     */
    public void setLastContextTime(String lastContextTime) {
        this.lastContextTime = lastContextTime;
    }
}
