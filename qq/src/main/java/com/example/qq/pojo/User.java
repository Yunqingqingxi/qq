package com.example.qq.pojo;

import java.util.Date;

public class User {

    private Integer userid;

    private String username;

    private String nickname;

    private String password;

    private String email;

    private String phone;

    private String address;

    private Object gender;

    private String signature;

    private Date signaturecreatedat;

    private Date signaturemodifiedat;

    private Date accountcreatedat;

    private Date passwordmodifiedat;

    private Object accountstatus;

    private Object usertype;

    private String avatar;

    public User(String username, String nickname, String avatar) {
        this.username = username;
        this.nickname = nickname;
        this.avatar = avatar;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public Date getSignaturecreatedat() {
        return signaturecreatedat;
    }

    public void setSignaturecreatedat(Date signaturecreatedat) {
        this.signaturecreatedat = signaturecreatedat;
    }

    public Date getSignaturemodifiedat() {
        return signaturemodifiedat;
    }

    public void setSignaturemodifiedat(Date signaturemodifiedat) {
        this.signaturemodifiedat = signaturemodifiedat;
    }

    public Date getAccountcreatedat() {
        return accountcreatedat;
    }

    public void setAccountcreatedat(Date accountcreatedat) {
        this.accountcreatedat = accountcreatedat;
    }

    public Date getPasswordmodifiedat() {
        return passwordmodifiedat;
    }

    public void setPasswordmodifiedat(Date passwordmodifiedat) {
        this.passwordmodifiedat = passwordmodifiedat;
    }

    public Object getAccountstatus() {
        return accountstatus;
    }

    public void setAccountstatus(Object accountstatus) {
        this.accountstatus = accountstatus;
    }

    public Object getUsertype() {
        return usertype;
    }

    public void setUsertype(Object usertype) {
        this.usertype = usertype;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}