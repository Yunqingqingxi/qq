package com.example.qq.pojo;

import java.util.Date;

public class Friend {
    private String avatar;  // 头像资源ID
    private String username;  // 用户名
    private String nickname;  // 昵称
    private String content;  // 消息内容
    private Date time;  // 消息时间

    public Friend(String avatar, String username, String nickname,  Date time) {
        this.avatar = avatar;
        this.nickname = nickname;
        this.username = username;
        this.time = time;
    }

    public Friend(String avatar, Date time, String content, String nickname, String username) {
        this.avatar = avatar;
        this.time = time;
        this.content = content;
        this.nickname = nickname;
        this.username = username;
    }
    public Friend() {

    }

    public Friend(String avatar, String username, String nickname) {
        this.avatar = avatar;
        this.username = username;
        this.nickname = nickname;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setContent(String content) {
        this.content = content;
    }

        public void setTime(Date time) {
        this.time = time;
    }

    public Friend(String avatar, String nickname) {
        this.avatar = avatar;
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getNickname() {
        return nickname;
    }

    public String getContent() {
        return content;
    }

    public Date getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "Friend{" +
                "avatar=" + avatar +
                ", username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", content='" + content + '\'' +
                ", time=" + time +
                '}';
    }
}
