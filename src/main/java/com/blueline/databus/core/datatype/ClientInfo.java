package com.blueline.databus.core.datatype;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 使用数据总线的客户(系统)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientInfo {

    @JsonProperty(required = true)
    private int id;

    @JsonProperty(required = true)
    private String name;

    @JsonProperty(required = true)
    private String displayName;

    @JsonProperty(required = true)
    private String description;

    @JsonProperty(required = true)
    private int status;

    @JsonProperty(required = true)
    private String appKey;

    @JsonProperty(required = true)
    private String sKey;

    @JsonProperty(required = true)
    private String clientType;

    @JsonProperty(required = true)
    private String clientCategory;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getStatus() {
        return status;
    }

    public String getAppKey() {
        return appKey;
    }

    public String getSKey() {
        return sKey;
    }

    public String getClientType() {
        return clientType;
    }

    public String getClientCategory() {
        return clientCategory;
    }

    public ClientInfo(int id, String name, String displayName, String description, int status, String appKey,
                      String sKey, String clientType, String clientCategory) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.status = status;
        this.appKey = appKey;
        this.sKey = sKey;
        this.clientType = clientType;
        this.clientCategory = clientCategory;
    }

    public ClientInfo() {}

    @Override
    public String toString() {
        return String.format(
                "{\"id\":\"%s\",\"name\":\"%s\",\"display_name\":\"%s\",\"description\":\"%s\"," +
                        "\"appkey\":\"%s\",\"skey\":\"%s\",\"type\":\"%s\", \"category\":\"%s\"}",
                this.id, this.name, this.displayName, this.description, this.appKey,
                this.sKey, this.clientType, this.clientCategory);
    }
}
