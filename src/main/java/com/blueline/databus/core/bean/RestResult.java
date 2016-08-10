package com.blueline.databus.core.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RestResult {
    @JsonProperty
    private ResultType resultType;

    @JsonProperty
    private String message;

    @JsonProperty
    private String ext;

    public RestResult(ResultType type, String msg, String ext) {
        this.resultType = type;
        this.message = msg;
        this.ext = ext;
    }

    public RestResult(ResultType type, String msg) {
        this(type, msg, "");
    }

    public RestResult() {}

    public ResultType getResultType() {
        return resultType;
    }

    public String getMessage() {
        return message;
    }

    public String getExt() {
        return ext;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        sb.append(String.format("\"resultType\":\"%s\"", this.resultType.toString()));

        if (!StringUtils.isEmpty(this.message)) {
            sb.append(String.format(",\"message\":\"%s\"", this.message));
        }

        if (!StringUtils.isEmpty(this.ext)) {
            sb.append(String.format(",\"ext\":\"%s\"", this.ext));
        }

        sb.append("}");
        return sb.toString();
    }
}
