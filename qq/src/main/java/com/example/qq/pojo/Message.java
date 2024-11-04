//package com.example.qq.pojo;
//public class Message {
//    private String sender;
//    private String content;
//    private String receiver; // 可选，如果需要的话
//
//    public Message(String sender, String content) {
//        this.sender = sender;
//        this.content = content;
//    }
//
//    // 如果有接收者信息
//    public Message(String sender, String receiver, String content) {
//        this.sender = sender;
//        this.receiver = receiver;
//        this.content = content;
//    }
//     public Message(String content){
//         this.content=content;
//     }
//
//    public String getContent() {
//        return content;
//    }
//
//    public String getSender() {
//        return sender;
//    }
//
//    public String getReceiver() {
//        return receiver;
//    }
//
//    public boolean isSentByMe() {
//        return sender != null && sender.equals(new User().getName());
//    }
//
//    // 判断消息是否由当前用户发送
//    public boolean isSentByCurrentUser(String currentUser) {
//        return sender != null && sender.equals(currentUser); // 防止 NullPointerException
//    }
//}