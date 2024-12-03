package com.example.qq.domain;

/**
 * 联系人实体类
 * 用于存储联系人的基本信息，包括昵称、头像和用户名
 * 
 * @author yunxi
 * @version 1.0
 */
public class Contact {
    /** 联系人昵称 */
    private String nickName;
    /** 联系人头像URL */
    private String avatarUrl;
    /** 联系人用户名（唯一标识） */
    private String username;

    /**
     * 获取联系人昵称
     * @return 联系人的昵称
     */
    public String getNickName() {
        return nickName;
    }

    /**
     * 设置联系人昵称
     * @param nickName 要设置的昵称
     */
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    /**
     * 获取联系人头像URL
     * @return 头像的URL地址
     */
    public String getAvatarUrl() {
        return avatarUrl;
    }

    /**
     * 设置联系人头像URL
     * @param avatarUrl 要设置的头像URL地址
     */
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    /**
     * 获取联系人用户名
     * @return 联系人的用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置联系人用户名
     * @param username 要设置的用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }
}