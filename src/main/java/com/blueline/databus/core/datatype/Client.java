package com.blueline.databus.core.datatype;

import com.blueline.databus.core.helper.RandomStringHelper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 使用数据总线的客户(系统)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Client {

    @JsonProperty(required = true)
    private int id;

    @JsonProperty(required = true)
    private String name;

    @JsonProperty(required = true)
    private String appKey;

    @JsonProperty(required = true)
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

    public Client(int id, String name) {
        this.id = id;
        this.name = name;
        this.appKey = RandomStringHelper.getRandomString(10);
        this.sKey = RandomStringHelper.hashKey(this.appKey);
    }

    @Override
    public String toString() {
        return String.format("{\"id\":\"%s\",\"name\":\"%s\",\"appkey\":\"%s\",\"skey\":\"%s\"}",
                this.id, this.name, this.appKey, this.sKey);
    }
}
