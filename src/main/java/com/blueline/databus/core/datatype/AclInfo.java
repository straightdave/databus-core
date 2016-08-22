package com.blueline.databus.core.datatype;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 在Redis中存储的控制访问列表(acl)数据:
 * api路径、HTTP方法、访问者appkey、允许的时间范围
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AclInfo {

    @JsonProperty(required = true)
    private String api;

    @JsonProperty(required = true)
    private String method;

    @JsonProperty(required = true)
    private String appkey;

    // 设定的时间范围,如'00002359'表示从00时00分到23时59分,即全天(可用'0'简单表示)
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

    /**
     * 定义一个acl信息
     * @param api api路径
     * @param method HTTP方法(GET,POST等)
     * @param appkey 访问者的appkey
     * @param duration 允许访问的时间范围:
     *                 <ol>
     *                 <li>形式为'HHmmHHmm',表示从每天的'HHmm'到'HHmm'</li>
     *                 <li>'00002359'表示从00时00分到23时59分,即全天</li>
     *                 <li>'0'也可以表示全天</li>
     *                 </ol>
     */
    public AclInfo(String api, String method, String appkey, String duration) {
        this.api = api;
        this.method = method;
        this.appkey = appkey;
        this.duration = duration;
    }

    /**
     * 仅用于json格式化
     */
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
