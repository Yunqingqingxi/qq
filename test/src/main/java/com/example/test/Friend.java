package com.example.test;

public class Friend {
    private int avatar;  // 头像资源ID
    private String nickname;  // 昵称
    private String message;  // 消息内容
    private String time;  // 消息时间

    public Friend(int avatar, String nickname, String message, String time) {
        this.avatar = avatar;
        this.nickname = nickname;
        this.message = message;
        this.time = time;
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

    public String getTime() {
        return time;
    }
}
