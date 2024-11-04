package com.example.qq.pojo;

public class User {
    private String id; // 唯一标识符
    private String name; // 用户名
    private String password; // 密码
    private String avatar; // 头像资源ID

    public User(String id, String name, String avatar) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
    }
    public User(String name,String password){
        this.name=name;
        this.password=password;
    }
    public User(String name) {
        this.name = name;
    }

    public User() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}