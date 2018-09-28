package com.chleon.telematics;

/**
 * Created by Ryan Fan on 2016/3/2.
 */
public class BaseRspMsg {
    private int reqSerial;

    public BaseRspMsg(int reqSerial) {
        this.reqSerial = reqSerial;
    }

    public int getReqSerial() {
        return reqSerial;
    }

    public void setReqSerial(int reqSerial) {
        this.reqSerial = reqSerial;
    }
}
