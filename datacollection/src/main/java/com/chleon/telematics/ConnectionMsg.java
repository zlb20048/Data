package com.chleon.telematics;

import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Ryan Fan on 2016/2/24.
 */
public class ConnectionMsg implements BaseMsg {
    private static final String TAG = ConnectionMsg.class.getSimpleName();

    private static final int CONNECTION_MSG_MIN_SIZE = 1;
    private static final int CONNECTION_MSG_MAX_SIZE = 1 + LoginReqMsg.LOGIN_REQUEST_MSG_SIZE;

    private MsgType msgType;
    private BaseMsg msgBody;

    public ConnectionMsg() {
    }

    public ConnectionMsg(MsgType msgType, BaseMsg msgBody) {
        this.msgType = msgType;
        this.msgBody = msgBody;
    }

    public MsgType getMsgType() {
        return msgType;
    }

    public void setMsgType(MsgType msgType) {
        this.msgType = msgType;
    }

    public BaseMsg getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(BaseMsg msgBody) {
        this.msgBody = msgBody;
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(CONNECTION_MSG_MAX_SIZE);

        outStream.write(msgType.getValue());

        if (msgBody != null) {
            try {
                outStream.write(msgBody.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return outStream.toByteArray();
    }

    public static ConnectionMsg fromByteArray(byte[] data) throws AutoLinkMsgException {
        ConnectionMsg connectionMsg = null;

        if (data.length < CONNECTION_MSG_MIN_SIZE) {
            MyLog.e(TAG, "ConnectionMsg invalid");
            throw new AutoLinkMsgException(connectionMsg, ResultCode.UNKNOWN_ERROR);
        } else {
            connectionMsg = new ConnectionMsg();
            connectionMsg.msgType = MsgType.getMsgType(data[0] & 0xFF);
            if (connectionMsg.msgType == MsgType.INVALID) {
                MyLog.e(TAG, "msgType invalid");
                throw new AutoLinkMsgException(connectionMsg, ResultCode.SUBMSG_TYPE_ERROR);
            }

            int lenMsgBody = data.length - CONNECTION_MSG_MIN_SIZE;
            byte[] msgBody = new byte[lenMsgBody];
            System.arraycopy(data, CONNECTION_MSG_MIN_SIZE, msgBody, 0, lenMsgBody);
            try {
                switch (connectionMsg.msgType) {
                    case LOGIN: {
                        connectionMsg.msgBody = LoginRspMsg.fromByteArray(msgBody);
                        break;
                    }
                    case HEARTBEAT: {
                        break;
                    }
                    case LOGOUT: {
                        break;
                    }
                    default: {
                        MyLog.e(TAG, "msgType invalid");
                        break;
                    }
                }
            } catch (AutoLinkMsgException exception) {
                connectionMsg.msgBody = exception.getBaseMsg();
                throw new AutoLinkMsgException(connectionMsg, exception.getResultCode());
            }
        }

        return connectionMsg;
    }

    public static enum MsgType {
        INVALID(0x00),
        LOGIN(0x01),
        HEARTBEAT(0x02),
        LOGOUT(0x03);

        private int value;

        private int invalidValue;

        private MsgType(int value) {
            this.value = value;
        }

        public int getInvalidValue() {
            return invalidValue;
        }

        public void setInvalidValue(int invalidValue) {
            this.invalidValue = invalidValue;
        }

        public int getValue() {
            return value;
        }

        public static MsgType getMsgType(int value) {
            MsgType msgType;
            switch (value) {
                case 0x01:
                    msgType = LOGIN;
                    break;
                case 0x02:
                    msgType = HEARTBEAT;
                    break;
                case 0x03:
                    msgType = LOGOUT;
                    break;
                default:
                    msgType = INVALID;
                    msgType.setInvalidValue(value);
                    break;
            }
            return msgType;
        }
    }

    public static class LoginReqMsg implements BaseMsg {
        private static final String DEFAULT_TERMINAL_TYPE = "000000000001";
        private static final String DEFAULT_VIN = "00000000000000000";
        private static final int LOGIN_REQUEST_IDENTIFIER = 0x73656778;
        private static final String DEFAULT_SOFTWARE_VERSION = "0000";
        private static final int CHANNEL_ID = 0x02;

        private static final int TERMINAL_TYPE_SIZE = 12;
        private static final int VIN_SIZE = 17;
        private static final int IDENTIFIER_SIZE = 4;
        private static final int SOFTWARE_VERSION_SIZE = 4;
        private static final int CHANNEL_ID_SIZE = 1;
        private static final int LOGIN_REQUEST_MSG_SIZE = TERMINAL_TYPE_SIZE + VIN_SIZE + IDENTIFIER_SIZE + SOFTWARE_VERSION_SIZE + CHANNEL_ID_SIZE;

        private String terminalType;
        private String vin;
        private long loginReqIdentifier;
        private String softwareVersion;
        private int channelId;

        private void init() {
            this.terminalType = DEFAULT_TERMINAL_TYPE;
            this.vin = DEFAULT_VIN;
            this.loginReqIdentifier = LOGIN_REQUEST_IDENTIFIER;
            this.softwareVersion = DEFAULT_SOFTWARE_VERSION;
            this.channelId = CHANNEL_ID;
        }

        public LoginReqMsg(String vin, int channelId) {
            init();

            setVin(vin);
            this.channelId = channelId;
        }

        public String getTerminalType() {
            return terminalType;
        }

        public void setTerminalType(String terminalType) {
            if (TextUtils.isEmpty(terminalType)) {
                MyLog.e(TAG, "LoginReqMsg setTerminalType error, terminalType = " + terminalType);
                terminalType = DEFAULT_TERMINAL_TYPE;
            }
            this.terminalType = terminalType;
        }

        public String getVin() {
            return vin;
        }

        public void setVin(String vin) {
            if (TextUtils.isEmpty(vin)) {
                MyLog.e(TAG, "LoginReqMsg setVin error, vin = " + vin);
                vin = DEFAULT_VIN;
            }
            this.vin = vin;
        }

        public String getSoftwareVersion() {
            return softwareVersion;
        }

        public void setSoftwareVersion(String softwareVersion) {
            if (TextUtils.isEmpty(softwareVersion)) {
                MyLog.e(TAG, "LoginReqMsg setSoftwareVersion error, softwareVersion = " + softwareVersion);
                softwareVersion = DEFAULT_SOFTWARE_VERSION;
            }
            this.softwareVersion = softwareVersion;
        }

        public int getChannelId() {
            return channelId;
        }

        public void setChannelId(int channelId) {
            this.channelId = channelId;
        }

        public byte[] toByteArray() {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream(LOGIN_REQUEST_MSG_SIZE);

            byte[] terminalType;
            byte[] tempTerminalType = this.terminalType.getBytes();
            int lenTempTerminalType = tempTerminalType.length;
            if (lenTempTerminalType < TERMINAL_TYPE_SIZE) {
                terminalType = new byte[TERMINAL_TYPE_SIZE];
                System.arraycopy(tempTerminalType, 0, terminalType, 0, lenTempTerminalType);
            } else {
                terminalType = tempTerminalType;
            }
            try {
                outStream.write(terminalType);
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte[] vin;
            byte[] tempVin = this.vin.getBytes();
            int lenTempVin = tempVin.length;
            if (lenTempVin < VIN_SIZE) {
                vin = new byte[VIN_SIZE];
                System.arraycopy(tempVin, 0, vin, 0, lenTempVin);
            } else {
                vin = tempVin;
            }
            try {
                outStream.write(vin);
            } catch (IOException e) {
                e.printStackTrace();
            }

            outStream.write((int) ((loginReqIdentifier & (0xFF << 24)) >> 24));
            outStream.write((int) ((loginReqIdentifier & (0xFF << 16)) >> 16));
            outStream.write((int) ((loginReqIdentifier & (0xFF << 8)) >> 8));
            outStream.write((int) (loginReqIdentifier & 0xFF));

            byte[] softwareVersion;
            byte[] tempSoftwareVersion = this.softwareVersion.getBytes();
            int lenTempSoftwareVersion = tempSoftwareVersion.length;
            if (lenTempSoftwareVersion < SOFTWARE_VERSION_SIZE) {
                softwareVersion = new byte[SOFTWARE_VERSION_SIZE];
                System.arraycopy(tempSoftwareVersion, 0, softwareVersion, 0, lenTempSoftwareVersion);
            } else {
                softwareVersion = tempSoftwareVersion;
            }
            try {
                outStream.write(softwareVersion);
            } catch (IOException e) {
                e.printStackTrace();
            }

            outStream.write(channelId & 0xFF);

            return outStream.toByteArray();
        }

        @Override
        public String toString() {
            return "{"
                    + "terminalType=" + terminalType
                    + ", vin=" + vin
                    + ", loginReqIdentifier=" + loginReqIdentifier
                    + ", softwareVersion=" + softwareVersion
                    + ", channelId=" + channelId
                    + "}";
        }
    }

    public static class LoginRspMsg implements BaseMsg {
        private static final int LOGIN_RESPONSE_IDENTIFIER = 0x5354475A;
        private static final int LOGIN_RESPONSE_SIZE = 12;
        private static final int TIMESTAMP_SIZE = 6;

        private ResultCode resultCode;
        private long loginRspIdentifier;
        private String timestamp;

        public LoginRspMsg() {
        }

        public ResultCode getResultCode() {
            return resultCode;
        }

        public long getLoginRspIdentifier() {
            return loginRspIdentifier;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public static LoginRspMsg fromByteArray(byte[] data) throws AutoLinkMsgException {
            LoginRspMsg loginRspMsg = null;

            if (data.length != LOGIN_RESPONSE_SIZE) {
                MyLog.e(TAG, "loginRspMsg invalid");
                throw new AutoLinkMsgException(loginRspMsg, ResultCode.UNKNOWN_ERROR);
            } else {
                loginRspMsg = new LoginRspMsg();
                loginRspMsg.resultCode = ResultCode.getResultCode((data[0] & 0xFF) << 8 | data[1] & 0xFF);
                loginRspMsg.loginRspIdentifier = (data[2] & 0xFF) << 24 | (data[3] & 0xFF) << 16 | (data[4] & 0xFF) << 8 | data[5] & 0xFF;
                byte[] timestamp = new byte[TIMESTAMP_SIZE];
                System.arraycopy(data, 6, timestamp, 0, TIMESTAMP_SIZE);
                loginRspMsg.timestamp = CodecUtils.bytesToHexString(timestamp);
                if (loginRspMsg.loginRspIdentifier != LOGIN_RESPONSE_IDENTIFIER) {
                    MyLog.e(TAG, "loginRspMsg loginRspIdentifier error");
                    throw new AutoLinkMsgException(loginRspMsg, ResultCode.FORMAT_ERROR);
                }
            }

            return loginRspMsg;
        }

        @Override
        public byte[] toByteArray() {
            return null;
        }

        @Override
        public String toString() {
            return "{"
                    + "resultCode=" + resultCode
                    + ", loginRspIdentifier=" + loginRspIdentifier
                    + ", timestamp=" + timestamp
                    + "}";
        }
    }

    @Override
    public String toString() {
        return "{"
                + "msgType=" + msgType
                + ", msgBody=" + msgBody
                + "}";
    }
}
