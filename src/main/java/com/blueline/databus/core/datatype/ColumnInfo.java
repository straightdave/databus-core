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

    public int getDataLength() {
        return dataLength;
    }

    public int getPosition() {
        return position;
    }

    public String getComment() {
        return comment;
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

    private int dataLength;

    private int position;

    private String comment;

    private boolean isNullable;

    private String columnType; // to store full and long expression of column data type

    private String columnKey;

    public ColumnInfo() {}

    // used in create table sql parsing
    public ColumnInfo(String name, String dataType, int dataLength, String comment, boolean isNullable, String keys) {
        this(name, dataType, dataLength, -1, comment, isNullable, "", keys);
    }

    // used in test
    public ColumnInfo(String name, String dataType, int position) {
        this(name, dataType, 0, position, "", true, "", "");
    }

    public ColumnInfo(String name, String dataType, int dataLength, int position, String comment, boolean isNullable, String columnType, String columnKey) {
        this.name = name;
        this.dataType = dataType.toUpperCase();
        this.dataLength = dataLength;
        this.position = position;
        this.comment = comment;
        this.isNullable = isNullable;
        this.columnType = columnType;
        this.columnKey = columnKey;

        if (this.dataType.contains("CHAR") && this.dataLength <= 0) {
            this.dataLength = 255;
        }
    }
}
