package com.chleon.telematics;

/**
 * Created by Ryan Fan on 2016/3/14.
 */
public class AutoLinkMsgException extends Exception {
    private BaseMsg baseMsg;
    private ResultCode resultCode;

    public AutoLinkMsgException(BaseMsg baseMsg, ResultCode resultCode) {
        this.baseMsg = baseMsg;
        this.resultCode = resultCode;
    }

    public BaseMsg getBaseMsg() {
        return baseMsg;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }

    @Override
    public String toString() {
        return "{"
                + "baseMsg=" + baseMsg
                + ", resultCode=" + resultCode
                + "}";
    }
}
