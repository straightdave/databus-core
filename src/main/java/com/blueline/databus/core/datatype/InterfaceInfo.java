package com.blueline.databus.core.datatype;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 数据接口(即数据表访问API)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InterfaceInfo {

    @JsonProperty(required = true)
    private int id;

    @JsonProperty(required = true)
    private String tableName;

    @JsonProperty(required = true)
    private String dbName;

    @JsonProperty(required = true)
    private String api;

    @JsonProperty(required = true)
    private String method;

    @JsonProperty
    private String description;

    public int getId() {
        return id;
    }

    public String getTableName() {
        return tableName;
    }

    public String getDbName() {
        return dbName;
    }

    public String getApi() {
        return api;
    }

    public String getMethod() {
        return method;
    }

    public String getDescription() {
        return description;
    }

    public InterfaceInfo(int id, String tableName, String dbName, String api, String method, String description) {
        this.id = id;
        this.tableName = tableName;
        this.dbName = dbName;
        this.api = api;
        this.method = method;
        this.description = description;
    }

    public InterfaceInfo() {}

    @Override
    public String toString() {
        return String.format(
                "{\"id\":%s,\"table_name\":\"%s\",\"db_name\":\"%s\",\"api\":\"%s\",\"method\":\"%s\",\"description\":\"%s\"}",
                this.id, this.tableName, this.dbName, this.api, this.method, this.description);
    }
}
