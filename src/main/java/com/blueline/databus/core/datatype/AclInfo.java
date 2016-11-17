package com.blueline.databus.core.datatype;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * 控制访问列表(acl)记录数据:
 * api路径、HTTP方法、客户端app key、允许的时间范围
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE
)
public class AclInfo {

    @JsonProperty
    private String api;

    @JsonProperty
    private String method;

    @JsonProperty
    private String appKey;

    @JsonProperty
    private String clientName;

    @JsonProperty
    private String duration;

    @JsonProperty
    private Date createdAt;

    public String getApi() {
        return api;
    }

    public String getMethod() {
        return method;
    }

    public String getAppKey() {
        return appKey;
    }

    public String getClientName() {
        return clientName;
    }

    public String getDuration() {
        return duration;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * 定义一个acl信息
     * @param api api路径
     * @param method HTTP方法(GET,POST等)
     * @param appKey 访问者app key
     * @param duration 允许访问的时间范围:
     *                 <ol>
     *                 <li>形式为'HHmmHHmm',表示从每天的'HHmm'到'HHmm'</li>
     *                 <li>'00002359'表示从00时00分到23时59分,即全天</li>
     *                 <li>'0'也可以表示全天</li>
     *                 </ol>
     */
    public AclInfo(String api, String method, String appKey, String duration) {
        this.api = api;
        this.method = method;
        this.appKey = appKey;
        this.duration = duration;
    }

    /**
     * 重载的构造函数，增加createAt参数
     * @param api api路径
     * @param method HTTP方法
     * @param appKey 访问者appKey
     * @param duration 允许访问的时间范围
     * @param createdAt 创建时间
     */
    public AclInfo(String api, String method, String appKey, String clientName, String duration, Date createdAt) {
        this.api = api;
        this.method = method;
        this.appKey = appKey;
        this.clientName = clientName;
        this.duration = duration;
        this.createdAt = createdAt;
    }
    
    /**
     * 仅用于json格式化
     */
    public AclInfo() {}

    @Override
    public String toString() {
        return String.format(
                "{\"api\":\"%s\",\"method\":\"%s\",\"appkey\":\"%s\",\"client_name\":\"%s\",\"duration\":\"%s\",\"created_at\":\"%s\"}",
                this.api, this.method, this.appKey, this.clientName, this.duration, this.createdAt);
    }
}
