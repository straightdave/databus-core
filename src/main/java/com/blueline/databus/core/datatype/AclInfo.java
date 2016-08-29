package com.blueline.databus.core.datatype;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 在Redis中存储的控制访问列表(acl)数据:
 * api路径、HTTP方法、client name、允许的时间范围
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AclInfo {

    @JsonProperty(required = true)
    private String api;

    @JsonProperty(required = true)
    private String method;

    @JsonProperty(required = true)
    private String clientName;

    @JsonProperty(required = true, defaultValue = "0")
    private String duration;

    public String getApi() {
        return api;
    }

    public String getMethod() {
        return method;
    }

    public String getClientName() {
        return clientName;
    }

    public String getDuration() {
        return duration;
    }

    /**
     * 定义一个acl信息
     * @param api api路径
     * @param method HTTP方法(GET,POST等)
     * @param clientName 访问者用户名(不变、唯一)
     * @param duration 允许访问的时间范围:
     *                 <ol>
     *                 <li>形式为'HHmmHHmm',表示从每天的'HHmm'到'HHmm'</li>
     *                 <li>'00002359'表示从00时00分到23时59分,即全天</li>
     *                 <li>'0'也可以表示全天</li>
     *                 </ol>
     */
    public AclInfo(String api, String method, String clientName, String duration) {
        this.api = api;
        this.method = method;
        this.clientName = clientName;
        this.duration = duration;
    }

    /**
     * 仅用于json格式化
     */
    public AclInfo() {}

    @Override
    public String toString() {
        return String.format(
                "{\"api\":\"%s\",\"method\":\"%s\",\"username\":\"%s\",\"duration\":\"%s\"",
                this.api, this.method, this.clientName, this.duration);
    }
}
