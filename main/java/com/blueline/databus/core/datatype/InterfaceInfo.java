package com.blueline.databus.core.datatype;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 数据接口(即数据表访问API)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE
)
public class InterfaceInfo {

    @JsonProperty
    private int id;

    @JsonProperty
    private String tableName;

    @JsonProperty
    private String dbName;

    @JsonProperty
    private String api;

    @JsonProperty
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
