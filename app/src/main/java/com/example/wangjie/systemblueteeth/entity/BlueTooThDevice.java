package com.example.wangjie.systemblueteeth.entity;

import java.io.Serializable;

/**
 * Created by wangjie on 2017/6/14.
 */

public class BlueTooThDevice implements Serializable{
    private String name;
    private String address;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
