package com.chleon.telematics;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Ryan Fan on 2016/2/25.
 */
public class GetParamRspMsg extends BaseRspMsg implements BaseParamMsg, BaseMsg {
    private static final String TAG = GetParamRspMsg.class.getSimpleName();

    private ParamType paramType;
    private ResultCode resultCode;
    private BaseMsg msgBody;

    public GetParamRspMsg(int serial) {
        super(serial);
    }

    public GetParamRspMsg(int serial, ParamType paramType, ResultCode resultCode, BaseMsg msgBody) {
        super(serial);
        this.paramType = paramType;
        this.resultCode = resultCode;
        this.msgBody = msgBody;
    }

    public ParamType getParamType() {
        return paramType;
    }

    public void setParamType(ParamType paramType) {
        this.paramType = paramType;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }

    public void setResultCode(ResultCode resultCode) {
        this.resultCode = resultCode;
    }

    public BaseMsg getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(BaseMsg msgBody) {
        this.msgBody = msgBody;
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(256);

        outStream.write(paramType.getValue() & 0xFF);
        outStream.write((resultCode.getValue() & (0xFF << 8)) >> 8);
        outStream.write(resultCode.getValue() & 0xFF);

        if (msgBody != null) {
            try {
                outStream.write(msgBody.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return outStream.toByteArray();
    }

    @Override
    public String toString() {
        return "{"
                + "paramType=" + paramType
                + ", resultCode=" + resultCode
                + ", msgBody=" + msgBody
                + "}";
    }
}
