package com.blueline.databus.core.datatype;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 在Redis中存储的控制访问列表数据
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AclInfo {

    @JsonProperty(required = true)
    private String api;

    @JsonProperty(required = true)
    private String method;

    @JsonProperty(required = true)
    private String appkey;

    /**
     * 设定的时间范围,如'00002359'表示从00时00分到23时59分,即全天(可用'0'简单表示)
     */
    @JsonProperty(required = true, defaultValue = "0")
    private String duration;

    public String getApi() {
        return api;
    }

    public String getMethod() {
        return method;
    }

    public String getAppkey() {
        return appkey;
    }

    public String getDuration() {
        return duration;
    }

    public AclInfo(String api, String method, String appkey, String duration) {
        this.api = api;
        this.method = method;
        this.appkey = appkey;
        this.duration = duration;
    }

    public AclInfo() {}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        sb.append(String.format("\"api\":\"%s\"", this.api));
        sb.append(String.format(",\"method\":\"%s\"", this.method));
        sb.append(String.format(",\"appkey\":\"%s\"", this.appkey));
        sb.append(String.format(",\"duration\":\"%s\"", this.duration));
        sb.append("}");
        return sb.toString();
    }
}
