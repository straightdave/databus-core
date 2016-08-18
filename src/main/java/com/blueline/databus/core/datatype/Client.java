package com.blueline.databus.core.datatype;

/**
 * 使用数据总线的客户(系统)
 */
public class Client {

    private int id;

    private String name;

    private String appKey;

    private String sKey;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAppKey() {
        return appKey;
    }

    public String getsKey() {
        return sKey;
    }

    public Client(int id, String name, String appKey, String sKey) {
        this.id = id;
        this.name = name;
        this.appKey = appKey;
        this.sKey = sKey;
    }
}
