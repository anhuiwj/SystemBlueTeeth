package com.example.wangjie.systemblueteeth.entity;

import java.io.Serializable;

/**
 * Created by wangjie on 2017/6/14.
 */

public class BlueTooThDevice implements Serializable{
    private String name;
    private String address;
    private String connect_status;

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

    public String getConnect_status() {
        return connect_status;
    }

    public void setConnect_status(String connect_status) {
        this.connect_status = connect_status;
    }
}
