package com.blueline.databus.core.bean;

import org.springframework.util.StringUtils;

public class RestResult {
    private ResultType type;
    private String msg;
    private String ext;

    public RestResult(ResultType type, String msg, String ext) {
        this.type = type;
        this.msg = msg;
        this.ext = ext;
    }

    public RestResult(ResultType type, String msg) {
        this(type, msg, "");
    }

    public RestResult(ResultType type) {
        this(type, "", "");
    }

    public ResultType getType() {
        return type;
    }

    public String getMsg() {
        return msg;
    }

    public String getExt() {
        return ext;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        sb.append(String.format("\"ret\":\"%s\"", this.type.toString()));

        if (!StringUtils.isEmpty(this.msg)) {
            sb.append(String.format(",\"msg\":\"%s\"", this.msg));
        }

        if (!StringUtils.isEmpty(this.ext)) {
            sb.append(String.format(",\"ext\":\"%s\"", this.ext));
        }

        sb.append("}");
        return sb.toString();
    }
}
