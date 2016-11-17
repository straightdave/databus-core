package com.blueline.databus.core.datatype;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

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

    @JsonProperty
    private String ownerName;

    @JsonProperty
    private String description;

    @JsonProperty
    private Date createdAt;

    @JsonProperty
    private Date updateAt;

    @JsonProperty
    private int tableSize;

    @JsonProperty
    private int tableRows;

    public TableInfo(int id, String name, String dbName, int ownerId, String ownerName, String description, Date createdAt, Date updateAt, int tableSize, int tableRows) {
        this.id = id;
        this.name = name;
        this.dbName = dbName;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.description = description;
        this.createdAt = createdAt;
        this.updateAt = updateAt;
        this.tableSize = tableSize;
        this.tableRows = tableRows;
    }

    public Date getUpdateAt() {
        return updateAt;
    }

    public int getTableSize() {
        return tableSize;
    }

    public int getTableRows() {
        return tableRows;
    }

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

    public String getOwnerName() {
        return ownerName;
    }

    public String getDescription() {
        return description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public TableInfo() {}

    @Override
    public String toString() {
        return String.format(
                "{\"id\":\"%s\"," +
                "\"name\":\"%s\"," +
                "\"db_name\":\"%s\"," +
                "\"owner_id\":\"%s\"," +
                "\"owner_name\":\"%s\"" +
                "\"description\":\"%s\"," +
                "\"created_at\":\"%s\"," +
                "\"updated_at\":\"%s\"," +
                "\"table_size\":%s," +
                "\"table_rows\":%s}",
                this.id, this.name, this.dbName, this.ownerId, this.ownerName,
                this.description, this.createdAt, this.updateAt,
                this.tableSize, this.tableRows);
    }
}
