package com.example.as003;



public class ChatMessage {
    private int imageResId;
    private String nickname;
    private String message;
    private String time;

    public ChatMessage(int imageResId, String nickname, String message, String time) {
        this.imageResId = imageResId;
        this.nickname = nickname;
        this.message = message;
        this.time = time;
    }

    public int getImageResId() {
        return imageResId;
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