package com.blueline.databus.core.datatype;

/**
 * 记录数据表内的列的信息
 */
public class ColumnInfo {

    public String getName() {
        return name;
    }

    public String getDataType() {
        return dataType;
    }

    public int getPosition() {
        return position;
    }

    public boolean isNullable() {
        return isNullable;
    }

    public String getColumnType() {
        return columnType;
    }

    public String getColumnKey() {
        return columnKey;
    }

    private String name;

    private String dataType;

    private int position;

    private boolean isNullable;

    private String columnType;

    private String columnKey;

    public ColumnInfo(String name, String columnType, boolean isNullable, String keys) {
        this(name, "", -1, isNullable, columnType, keys);
    }

    public ColumnInfo(String name, String dataType, int position) {
        this(name, dataType, position, true, "", "");
    }

    public ColumnInfo(String name, String dataType, int position, boolean isNullable, String columnType, String columnKey) {
        this.name = name;
        this.dataType = dataType;
        this.position = position;
        this.isNullable = isNullable;
        this.columnType = columnType;
        this.columnKey = columnKey;
    }
}
