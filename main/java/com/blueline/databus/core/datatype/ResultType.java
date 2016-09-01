package com.blueline.databus.core.datatype;

/**
 * RestResult的结果表述；
 */
public enum ResultType {
    /**
     * 表示api调用成功
     */
    OK,

    /**
     * 表示api调用成功，但是结果是失败的
     */
    FAIL,

    /**
     * 表示api调用抛出异常
     */
    ERROR,

    /**
     * 未知结果
     */
    UNKNOWN;

    @Override
    public String toString() {
        switch(this) {
            case OK:    return "OK";
            case FAIL:  return "FAIL";
            case ERROR: return "ERROR";
            default:    return "UNKNOWN";
        }
    }
}
