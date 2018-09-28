package com.chleon.telematics;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Ryan Fan on 2016/2/25.
 */
public class SetParamReqMsg implements BaseParamMsg, BaseMsg {
    private static final String TAG = SetParamReqMsg.class.getSimpleName();

    private static final int TIMESTAMP_SIZE = 6;
    private static final int SET_PARAM_REQ_MSG_MIN_SIZE = 7;

    private String timestamp;
    private ParamType paramType;
    private BaseMsg msgBody;

    public SetParamReqMsg() {
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

    public BaseMsg getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(BaseMsg msgBody) {
        this.msgBody = msgBody;
    }

    public static SetParamReqMsg fromByteArray(byte[] data) throws AutoLinkMsgException {
        SetParamReqMsg setParamReqMsg = null;

        if (data.length < SET_PARAM_REQ_MSG_MIN_SIZE) {
            MyLog.e(TAG, "SetParamReqMsg invalid");
            throw new AutoLinkMsgException(setParamReqMsg, ResultCode.UNKNOWN_ERROR);
        } else {
            setParamReqMsg = new SetParamReqMsg();
            byte[] timestamp = new byte[TIMESTAMP_SIZE];
            System.arraycopy(data, 0, timestamp, 0, TIMESTAMP_SIZE);
            setParamReqMsg.timestamp = CodecUtils.bytesToHexString(timestamp);
            setParamReqMsg.paramType = ParamType.getParamType(data[6] & 0xFF);
            if (setParamReqMsg.paramType == ParamType.INVALID_PARAM) {
                MyLog.e(TAG, "paramType invalid");
                throw new AutoLinkMsgException(setParamReqMsg, ResultCode.SUBMSG_TYPE_ERROR);
            }
/*            if(DateUtils.isMsgExpired(setParamReqMsg.timestamp)) {
                MyLog.e(TAG, "expired msg");
                throw new AutoLinkMsgException(setParamReqMsg, ResultCode.CMD_EXPIRE);
            }*/

            int lenMsgBody = data.length - SET_PARAM_REQ_MSG_MIN_SIZE;
            byte[] msgBody = new byte[lenMsgBody];
            System.arraycopy(data, SET_PARAM_REQ_MSG_MIN_SIZE, msgBody, 0, lenMsgBody);

            try {
                switch (setParamReqMsg.paramType) {
                    case CHANNEL_ID_PARAM: {
                        setParamReqMsg.msgBody = ChannelIdMsg.fromByteArray(msgBody);
                        break;
                    }
                    case NETWORK_PARAM: {
                        setParamReqMsg.msgBody = NetworkParamMsg.fromByteArray(msgBody);
                        break;
                    }
                    case TIMER_PARAM: {
                        setParamReqMsg.msgBody = TimerMsg.fromByteArray(msgBody);
                        break;
                    }
                    case ACC_REPORT_PARAM: {
                        setParamReqMsg.msgBody = ReportMsg.fromByteArray(msgBody);
                        break;
                    }
                    case STATUS_REPORT_PARAM: {
                        setParamReqMsg.msgBody = ReportMsg.fromByteArray(msgBody);
                        break;
                    }
                    case FAULT_REPORT_PARAM: {
                        setParamReqMsg.msgBody = ReportMsg.fromByteArray(msgBody);
                        break;
                    }
                    case ALARM_REPORT_PARAM: {
                        setParamReqMsg.msgBody = ReportMsg.fromByteArray(msgBody);
                        break;
                    }
                    case RESTRICTION_PARAM: {
                        setParamReqMsg.msgBody = RestrictionMsg.fromByteArray(msgBody);
                        break;
                    }
                    default: {
                        MyLog.e(TAG, "paramType invalid");
                        break;
                    }
                }
            } catch (AutoLinkMsgException exception) {
                setParamReqMsg.msgBody = exception.getBaseMsg();
                throw new AutoLinkMsgException(setParamReqMsg, exception.getResultCode());
            }
        }

        return setParamReqMsg;
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(256);

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

        if(msgBody != null) {
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
                + "timestamp=" + timestamp
                + ", paramType=" + paramType
                + ", msgBody=" + msgBody
                + "}";
    }
}
