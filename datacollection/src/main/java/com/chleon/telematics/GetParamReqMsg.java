package com.chleon.telematics;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Ryan Fan on 2016/2/25.
 */
public class GetParamReqMsg implements BaseMsg {
    private static final String TAG = GetParamReqMsg.class.getSimpleName();

    private static final int TIMESTAMP_SIZE = 6;
    private static final int GET_PARAM_REQ_MSG_SIZE = 7;

    private String timestamp;
    private ParamType paramType;

    public GetParamReqMsg() {
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public ParamType getParamType() {
        return paramType;
    }

    public void setParamType(ParamType paramType) {
        this.paramType = paramType;
    }

    public static GetParamReqMsg fromByteArray(byte[] data) throws AutoLinkMsgException {
        GetParamReqMsg getParamReqMsg = null;

        if (data.length != GET_PARAM_REQ_MSG_SIZE) {
            MyLog.e(TAG, "GetParamReqMsg invalid");
            throw new AutoLinkMsgException(getParamReqMsg, ResultCode.UNKNOWN_ERROR);
        } else {
            getParamReqMsg = new GetParamReqMsg();
            byte[] timestamp = new byte[TIMESTAMP_SIZE];
            System.arraycopy(data, 0, timestamp, 0, TIMESTAMP_SIZE);
            getParamReqMsg.timestamp = CodecUtils.bytesToHexString(timestamp);
            getParamReqMsg.paramType = ParamType.getParamType(data[6] & 0xFF);
            if (getParamReqMsg.paramType == ParamType.INVALID_PARAM) {
                MyLog.e(TAG, "paramType invalid");
                throw new AutoLinkMsgException(getParamReqMsg, ResultCode.SUBMSG_TYPE_ERROR);
            }
/*            if(DateUtils.isMsgExpired(getParamReqMsg.timestamp)) {
                MyLog.e(TAG, "expired msg");
                throw new AutoLinkMsgException(getParamReqMsg, ResultCode.CMD_EXPIRE);
            }*/
        }

        return getParamReqMsg;
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(GET_PARAM_REQ_MSG_SIZE);

        byte[] timestamp = CodecUtils.hexStringToBytes(this.timestamp);
        int lenTimestamp = timestamp.length;
        if (lenTimestamp != TIMESTAMP_SIZE) {
            MyLog.e(TAG, "lenTimestamp error");
        }
        try {
            outStream.write(timestamp);
        } catch (IOException e) {
            e.printStackTrace();
        }

        outStream.write(paramType.getValue() & 0xFF);

        return outStream.toByteArray();
    }

    @Override
    public String toString() {
        return "{"
                + "timestamp=" + timestamp
                + ", paramType=" + paramType
                + "}";
    }
}
