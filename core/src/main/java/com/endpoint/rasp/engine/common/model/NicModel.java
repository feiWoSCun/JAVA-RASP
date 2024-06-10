package com.endpoint.rasp.engine.common.model;

/**
 * 网络对象
 *
 * Created by yunchao.zheng on 2023-03-20
 */
public class NicModel {
    private String name;
    private String ip;

    public NicModel(String name, String ip) {
        this.name = name;
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

}
