package com.example.qq.domain;

import java.io.Serializable;

/**
 *  首页好友列表
 */
public class FriendList implements Serializable {
    private String friendNickName; // 好友昵称
    private String avatarUrl; // 好友头像地址
    private String lastContext; // 最新的聊天信息
    private String lastContextTime; // 最新聊天信息时间

    public FriendList(String friendNickName, String lastContext, String avatarUrl, String lastContextTime) {
        this.friendNickName = friendNickName;
        this.lastContext = lastContext;
        this.avatarUrl = avatarUrl;
        this.lastContextTime = lastContextTime;
    }

    public FriendList() {
    }

    public String getFriendNickName() {
        return friendNickName;
    }

    public void setFriendNickName(String friendNickName) {
        this.friendNickName = friendNickName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getLastContext() {
        return lastContext;
    }

    public void setLastContext(String lastContext) {
        this.lastContext = lastContext;
    }

    public String getLastContextTime() {
        return lastContextTime;
    }

    public void setLastContextTime(String lastContextTime) {
        this.lastContextTime = lastContextTime;
    }

    private static final long serialVersionUID = 1L;
}
