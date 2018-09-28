package com.chleon.telematics;

import java.io.ByteArrayOutputStream;

/**
 * Created by Ryan Fan on 2016/2/25.
 */
public class SetParamRspMsg extends BaseRspMsg implements BaseMsg {
    private static final String TAG = SetParamRspMsg.class.getSimpleName();

    public static final int SET_PARAM_RSP_MSG_SIZE = 3;

    private ParamType paramType;
    private ResultCode resultCode;

    public SetParamRspMsg(int serial) {
        super(serial);
    }

    public SetParamRspMsg(int serial, ParamType paramType, ResultCode resultCode) {
        super(serial);
        this.paramType = paramType;
        this.resultCode = resultCode;
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

    public byte[] toByteArray() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(SET_PARAM_RSP_MSG_SIZE);

        outStream.write(paramType.getValue() & 0xFF);

        outStream.write((resultCode.getValue() & (0xFF << 8)) >> 8);
        outStream.write(resultCode.getValue() & 0xFF);

        return outStream.toByteArray();
    }

    @Override
    public String toString() {
        return "{"
                + "paramType=" + paramType
                + ", resultCode=" + resultCode
                + "}";
    }
}
