package com.blueline.databus.core.datatype;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TableInfo {

    @JsonProperty(required = true)
    private int id;

    @JsonProperty(required = true)
    private String name;

    @JsonProperty(required = true)
    private String dbName;

    @JsonProperty(required = true)
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
