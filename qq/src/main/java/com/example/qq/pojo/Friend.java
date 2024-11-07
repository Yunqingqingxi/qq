package com.example.qq.pojo;

import java.util.Date;

public class Friend {
    private int avatar;  // 头像资源ID
    private String username;  // 用户名
    private String nickname;  // 昵称
    private String message;  // 消息内容
    private Date time;  // 消息时间

    public Friend(int avatar, String username,String nickname,  Date time) {
        this.avatar = avatar;
        this.nickname = nickname;
        this.username = username;
        this.time = time;
    }

    public Friend(int avatar, Date time, String message, String nickname, String username) {
        this.avatar = avatar;
        this.time = time;
        this.message = message;
        this.nickname = nickname;
        this.username = username;
    }
    public Friend() {

    }

    public Friend(int avatar, String username, String nickname) {
        this.avatar = avatar;
        this.username = username;
        this.nickname = nickname;
    }

    public void setAvatar(int avatar) {
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

    public void setMessage(String message) {
        this.message = message;
    }

        public void setTime(Date time) {
        this.time = time;
    }

    public Friend(int avatar, String nickname) {
        this.avatar = avatar;
        this.nickname = nickname;
    }

    public int getAvatar() {
        return avatar;
    }

    public String getNickname() {
        return nickname;
    }

    public String getMessage() {
        return message;
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
                ", message='" + message + '\'' +
                ", time=" + time +
                '}';
    }
}
