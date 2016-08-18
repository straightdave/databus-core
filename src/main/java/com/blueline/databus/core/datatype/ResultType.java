package com.blueline.databus.core.datatype;

public enum ResultType {
    OK, FAIL, ERROR;

    @Override
    public String toString() {
        switch(this) {
            case OK: return "OK";
            case FAIL: return "FAIL";
            case ERROR: return "ERROR";
            default: return "UNKNOWN";
        }
    }
}
