package com.blueline.databus.core.datatype;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.StringUtils;

/**
 * 记录服务接口的返回信息；内部元素为：
 * <ul>
 *     <li>ResultType - 结果表述（详细请见 ResultType定义）</li>
 *     <li>message - 结果信息主体；一般内容为数据的json字符串，或者提示信息字符串</li>
 *     <li>ext - 扩展信息；可选</li>
 * </ul>
 *
 * @see ResultType
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE
)
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

    public RestResult() {
        this(ResultType.UNKNOWN, "", "");
    }

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
