package com.chleon.telematics;

import android.text.TextUtils;

import com.chleon.datacollection.BuildConfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Ryan Fan on 2016/2/24.
 */
public class MsgHeader implements BaseMsg {
    private static final String TAG = MsgHeader.class.getSimpleName();

    private static final short DEFAULT_TERMINAL_TYPE = 0x02;
    private static final short DEFAULT_PROTOCOL_VERSION =
            BuildConfig.PROTOCOL_VERSION;
    private static final String DEFAULT_TERMINAL_ID =
            DEFAULT_PROTOCOL_VERSION == 0x10 ? "000000000000000" : "00000000000000000";
    private static final int TERMINAL_ID_ORIGIN_SIZE =
            DEFAULT_PROTOCOL_VERSION == 0x10 ? 15 : 17;
    private static final int TERMINAL_ID_SIZE =
            DEFAULT_PROTOCOL_VERSION == 0x10 ? 8 : 17;
    public static final int MSG_HEADER_SIZE =
            DEFAULT_PROTOCOL_VERSION == 0x10 ? 15 : 24;

    public static enum MsgType {
        INVALID_MSG(0x00),
        CONNECTION_MSG(0x01),
        SET_PARAM_MSG(0x02),
        GET_PARAM_MSG(0x03),
        CAN_MSG(0x04),
        CONTROL_MSG(0x05),
        NAVIGATION_MSG(0x11),
        OTA_MSG(0x1A),
        PAIR_MSG(0x21),
        PAIR_CONFIRM_MSG(0x22),
        SINGLE_REPORT_MSG(0x41),
        BATCH_REPORT_MSG(0x42);

        private int value;

        private int invalidValue;

        private MsgType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public int getInvalidValue() {
            return invalidValue;
        }

        public void setInvalidValue(int invalidValue) {
            this.invalidValue = invalidValue;
        }

        public static MsgType getMsgType(int value) {
            MsgType msgType;
            switch (value) {
                case 0x01:
                    msgType = CONNECTION_MSG;
                    break;
                case 0x02:
                    msgType = SET_PARAM_MSG;
                    break;
                case 0x03:
                    msgType = GET_PARAM_MSG;
                    break;
                case 0x05:
                    msgType = CONTROL_MSG;
                    break;
                case 0x04:
                    msgType = CAN_MSG;
                    break;
                case 0x11:
                    msgType = NAVIGATION_MSG;
                    break;
                case 0x1A:
                    msgType = OTA_MSG;
                    break;
                case 0x21:
                    msgType = PAIR_MSG;
                    break;
                case 0x22:
                    msgType = PAIR_CONFIRM_MSG;
                    break;
                case 0x41:
                    msgType = SINGLE_REPORT_MSG;
                    break;
                case 0x42:
                    msgType = BATCH_REPORT_MSG;
                    break;
                default:
                    msgType = INVALID_MSG;
                    msgType.setInvalidValue(value);
                    break;
            }
            return msgType;
        }
    }

    private short version;
    private short terminalType;
    private String terminalId;
    private MsgType msgType;
    private int serialNumber;
    private int lenMsgBody;

    private void init() {
        version = DEFAULT_PROTOCOL_VERSION;
        terminalType = DEFAULT_TERMINAL_TYPE;
    }

    public MsgHeader() {
        init();
    }

    public MsgHeader(String terminalId, MsgType msgType,
                     int serialNumber, int lenMsgBody) {
        init();

        setTerminalId(terminalId);
        this.msgType = msgType;
        this.serialNumber = serialNumber;
        this.lenMsgBody = lenMsgBody;
    }

    public short getVersion() {
        return (short) (version & 0x3F);
    }

    public void setVersion(short version) {
        if ((version & 0xB0) != 0) {
            MyLog.e(TAG, "setVersion error, version = " + version);
        }
        this.version = (short) (version & 0x3F);
    }

    public short getTerminalType() {
        return terminalType;
    }

    public void setTerminalType(short terminalType) {
        this.terminalType = terminalType;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        if (TextUtils.isEmpty(terminalId)) {
            terminalId = DEFAULT_TERMINAL_ID;
        } else if (terminalId.length() > TERMINAL_ID_ORIGIN_SIZE) {
            MyLog.e(TAG, "setTerminalId error, terminalId = " + terminalId);
            terminalId = terminalId.substring(0, TERMINAL_ID_ORIGIN_SIZE);
        }
        this.terminalId = terminalId;
    }

    public MsgType getMsgType() {
        return msgType;
    }

    public void setMsgType(MsgType msgType) {
        this.msgType = msgType;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getLenMsgBody() {
        return lenMsgBody;
    }

    public void setLenMsgBody(int lenMsgBody) {
        this.lenMsgBody = lenMsgBody;
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(MSG_HEADER_SIZE);

        outStream.write(version & 0xFF);
        outStream.write(terminalType & 0xFF);

        byte[] terminalId;
        byte[] tempTerminalId;
        if (DEFAULT_PROTOCOL_VERSION == 0x10) {
            tempTerminalId = CodecUtils.hexStringToBytes(this.terminalId + "0");
        } else {
            tempTerminalId = this.terminalId.getBytes();
        }
        int lenTempTerminalId = tempTerminalId.length;
        if (lenTempTerminalId < TERMINAL_ID_SIZE) {
            terminalId = new byte[TERMINAL_ID_SIZE];
            System.arraycopy(tempTerminalId, 0, terminalId, 0, lenTempTerminalId);
        } else {
            terminalId = tempTerminalId;
        }
        try {
            outStream.write(terminalId);
        } catch (IOException e) {
            e.printStackTrace();
        }

        outStream.write(msgType.getValue() & 0xFF);
        outStream.write((serialNumber & (0xFF << 8)) >> 8);
        outStream.write(serialNumber & 0xFF);
        outStream.write((lenMsgBody & (0xFF << 8)) >> 8);
        outStream.write(lenMsgBody & 0xFF);

        return outStream.toByteArray();
    }

    public static MsgHeader fromByteArray(byte[] data) throws AutoLinkMsgException {
        MsgHeader msgHeader = null;

        if (data.length != MSG_HEADER_SIZE) {
            MyLog.e(TAG, "MsgHeader invalid");
            throw new AutoLinkMsgException(msgHeader, ResultCode.UNKNOWN_ERROR);
        } else {
            msgHeader = new MsgHeader();
            msgHeader.version = (short) (data[0] & 0xFF);
            msgHeader.terminalType = (short) (data[1] & 0xFF);
            byte[] terminalId = new byte[TERMINAL_ID_SIZE];
            System.arraycopy(data, 2, terminalId, 0, TERMINAL_ID_SIZE);
            if (DEFAULT_PROTOCOL_VERSION == 0x10) {
                String terminalIdExtended = CodecUtils.bytesToHexString(terminalId);
                if(terminalIdExtended != null && terminalIdExtended.length() > TERMINAL_ID_ORIGIN_SIZE) {
                    msgHeader.terminalId = terminalIdExtended.substring(0, TERMINAL_ID_ORIGIN_SIZE);
                } else {
                    msgHeader.terminalId = terminalIdExtended;
                }
                msgHeader.msgType = MsgType.getMsgType(data[10] & 0xFF);
                msgHeader.serialNumber = (data[11] & 0xFF) << 8 | data[12] & 0xFF;
                msgHeader.lenMsgBody = (data[13] & 0xFF) << 8 | data[14] & 0xFF;
            } else {
                msgHeader.terminalId = new String(terminalId);
                MyLog.i(TAG, "terminalId = " + msgHeader.terminalId);
                msgHeader.msgType = MsgType.getMsgType(data[19] & 0xFF);
                msgHeader.serialNumber = (data[20] & 0xFF) << 8 | data[21] & 0xFF;
                msgHeader.lenMsgBody = (data[22] & 0xFF) << 8 | data[23] & 0xFF;
            }
            if (msgHeader.msgType == MsgType.INVALID_MSG) {
                MyLog.e(TAG, "msgType invalid");
                throw new AutoLinkMsgException(msgHeader, ResultCode.MSG_TYPE_ERROR);
            }
        }

        return msgHeader;
    }

    @Override
    public String toString() {
        return "{"
                + "version=" + version
                + ", terminalType = " + terminalType
                + ", terminalId = " + terminalId
                + ", msgType = " + msgType
                + ", serialNumber = " + serialNumber
                + ", lenMsgBody = " + lenMsgBody
                + "}";
    }
}
