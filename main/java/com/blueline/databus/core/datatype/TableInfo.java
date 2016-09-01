package com.blueline.databus.core.datatype;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 数据表信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE
)
public class TableInfo {

    @JsonProperty
    private int id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String dbName;

    @JsonProperty
    private int ownerId;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDbName() {
        return dbName;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public TableInfo(int id, String name, String dbName, int ownerId) {
        this.id = id;
        this.name = name;
        this.dbName = dbName;
        this.ownerId = ownerId;
    }

    public TableInfo() {}

    @Override
    public String toString() {
        return String.format("{\"id\":%s,\"name\":\"%s\",\"db_name\":\"%s\",\"owner_id\":%s}",
                this.id, this.name, this.dbName, this.ownerId);
    }
}
