package com.zxq.vo;

import java.io.Serializable;

/**
 * Created by zxq on 2014/9/16.
 */
public class PersonEntityInfo implements Serializable{
    private String name;
    private String qq;
    private String phone;
    private String email;
    private String signature;

    public PersonEntityInfo() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getName() {
        return name;
    }

    public String getQq() {
        return qq;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getSignature() {
        return signature;
    }
}
