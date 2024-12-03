package com.example.qq.domain;

import java.io.Serializable;

/**
 * 用户实体类
 * 用于存储用户的基本信息，包括个人资料和账号信息
 *
 * @author yunxi
 * @version 1.0
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 用户唯一标识ID */
    private String userId;

    /** 用户登录账号 */
    private String userName;

    /** 用户登录密码 */
    private String userPassword;

    /** 用户昵称 */
    private String userNickName;

    /** 用户头像URL */
    private String userAvatarUrl;

    /** 用户个性签名 */
    private String userSignature;

    /** 用户邮箱地址 */
    private String email;

    /** 用户手机号码 */
    private String phone;

    /** 用户居住地址 */
    private String address;

    /** 
     * 用户性别
     * @deprecated 建议改用枚举类型定义性别
     */
    private Object gender;

    /** 
     * 用户签名
     * @deprecated 与userSignature字段重复，建议删除
     */
    private String signature;

    /**
     * 无参构造函数
     */
    public User() {
    }

    /**
     * 全参数构造函数
     *
     * @param userId 用户ID
     * @param gender 用户性别
     * @param userName 用户名
     * @param userPassword 用户密码
     * @param userNickName 用户昵称
     * @param userAvatarUrl 用户头像URL
     * @param userSignature 用户签名
     * @param email 邮箱地址
     * @param phone 手机号码
     * @param address 居住地址
     * @param signature 签名（重复字段）
     */
    public User(String userId, Object gender, String userName, String userPassword,
            String userNickName, String userAvatarUrl, String userSignature,
            String email, String phone, String address, String signature) {
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

    /**
     * 获取用户ID
     * @return 用户的唯一标识ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 设置用户ID
     * @param userId 用户的唯一标识ID
     */
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
}
