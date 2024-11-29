package com.example.qq.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * 当前用户类
 */

public class User implements Serializable {
    private String userId; // 用户id

    private String userName; // 用户账号名

    private String userPassword; // 用户密码

    private String userNickName; // 用户昵称

    private String userAvatarUrl; // 用户头像

    private String userSignature; // 用户签名

    private String email; // 用户邮箱

    private String phone; // 用户手机号

    private String address; // 用户地址

    private Object gender; // 用户性别

    private String signature; // 用户签名

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserNickName() {
        return userNickName;
    }

    public void setUserNickName(String userNickName) {
        this.userNickName = userNickName;
    }

    public String getUserAvatarUrl() {
        return userAvatarUrl;
    }

    public void setUserAvatarUrl(String userAvatarUrl) {
        this.userAvatarUrl = userAvatarUrl;
    }

    public String getUserSignature() {
        return userSignature;
    }

    public void setUserSignature(String userSignature) {
        this.userSignature = userSignature;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Object getGender() {
        return gender;
    }

    public void setGender(Object gender) {
        this.gender = gender;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public User() {
    }

    public User(String userId, Object gender, String userName, String userPassword, String userNickName, String userAvatarUrl, String userSignature, String email, String phone, String address, String signature) {
        this.userId = userId;
        this.gender = gender;
        this.userName = userName;
        this.userPassword = userPassword;
        this.userNickName = userNickName;
        this.userAvatarUrl = userAvatarUrl;
        this.userSignature = userSignature;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.signature = signature;
    }

    private static final long serialVersionUID = 1L;
}
