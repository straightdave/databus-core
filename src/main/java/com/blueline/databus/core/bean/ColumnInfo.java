package com.blueline.databus.core.bean;

/**
 * 记录数据表内的列的信息
 */
public class ColumnInfo {
    public String getName() {
        return name;
    }

    private String name;

    public String getType() {
        return type;
    }

    private String type;

    public int getPosition() {
        return position;
    }

    private int position;

    public ColumnInfo(String name, String type, int pos) {
        this.name = name;
        this.type = type;
        this.position = pos;
    }
}
