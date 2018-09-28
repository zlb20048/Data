package com.chleon.telematics;

import com.chleon.datacollection.BuildConfig;

/**
 * Created by Ryan Fan on 2016/2/29.
 */
public class AutoLinkMsg implements BaseMsg {

    private static final String TAG = AutoLinkMsg.class.getSimpleName();

    private static final boolean DBG = true;

    private static final int AUTOLINK_MSG_MIN_SIZE = MsgHeader.MSG_HEADER_SIZE;

    private MsgHeader msgHeader;

    private BaseMsg msgBody;

    private ResultCode resultCode;

    private void init() {
        resultCode = ResultCode.OK;
    }

    public AutoLinkMsg() {
        init();
    }

    public AutoLinkMsg(MsgHeader msgHeader, BaseMsg msgBody) {
        init();
        this.msgHeader = msgHeader;
        this.msgBody = msgBody;
    }

    public MsgHeader getMsgHeader() {
        return msgHeader;
    }

    public void setMsgHeader(MsgHeader msgHeader) {
        this.msgHeader = msgHeader;
    }

    public BaseMsg getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(BaseMsg msgBody) {
        this.msgBody = msgBody;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }

    public void setResultCode(ResultCode resultCode) {
        this.resultCode = resultCode;
    }

    @Override
    public byte[] toByteArray() {
        byte[] msgHeader = this.msgHeader.toByteArray();
        byte[] msgBody = this.msgBody.toByteArray();
        int lenMsgHeader = msgHeader.length;
        if (lenMsgHeader != MsgHeader.MSG_HEADER_SIZE) {
            MyLog.e(TAG, "lenMsgHeader error");
        }
        int lenMsgBody = msgBody.length;
        if (BuildConfig.PROTOCOL_VERSION == 0x10) {
            msgHeader[13] = (byte) ((lenMsgBody & (0xFF << 8)) >> 8);
            msgHeader[14] = (byte) (lenMsgBody & 0xFF);
        } else {
            msgHeader[22] = (byte) ((lenMsgBody & (0xFF << 8)) >> 8);
            msgHeader[23] = (byte) (lenMsgBody & 0xFF);
        }
        MyLog.d(TAG, "lenMsgBody = " + lenMsgBody);
        int lenAutoLinkMsg = lenMsgHeader + lenMsgBody;
        byte[] autoLinkMsg = new byte[lenAutoLinkMsg];
        System.arraycopy(msgHeader, 0, autoLinkMsg, 0, lenMsgHeader);
        System.arraycopy(msgBody, 0, autoLinkMsg, lenMsgHeader, lenMsgBody);
        return autoLinkMsg;
    }

    public byte[] toSendFrame() {
        byte[] data = toByteArray();
        int lenData = data.length;
        int crcCheckSum = CodecUtils.crcCheckSum(data, lenData);
        MyLog.i(TAG, "autoLinkMsg = " + CodecUtils.bytesToHexString(data));
        byte[] cipherText = RSASecurityUtils.Encrypt(data, MsgHeader.MSG_HEADER_SIZE, lenData - MsgHeader.MSG_HEADER_SIZE);
        if (DBG) {
            MyLog.d(TAG, "cipherText = " + CodecUtils.bytesToHexString(cipherText));
            MyLog.d(TAG, "plainText = " + CodecUtils.bytesToHexString(RSASecurityUtils.Decrypt(cipherText, 0, cipherText.length)));
        }
        int lenCipherText = cipherText.length;
        byte[] autoLinkMsgBytes = new byte[MsgHeader.MSG_HEADER_SIZE + lenCipherText + 2];
        System.arraycopy(data, 0, autoLinkMsgBytes, 0, MsgHeader.MSG_HEADER_SIZE);
        System.arraycopy(cipherText, 0, autoLinkMsgBytes, MsgHeader.MSG_HEADER_SIZE, lenCipherText);
        autoLinkMsgBytes[MsgHeader.MSG_HEADER_SIZE + lenCipherText] = (byte) ((crcCheckSum & (0xFF << 8)) >> 8);
        autoLinkMsgBytes[MsgHeader.MSG_HEADER_SIZE + lenCipherText + 1] = (byte) (crcCheckSum & 0xFF);

        int lenFrame = MsgHeader.MSG_HEADER_SIZE + lenCipherText + 4;
        for (byte byteVal : autoLinkMsgBytes) {
            if (byteVal == 0x7E || byteVal == 0x7D) {
                lenFrame++;
            }
        }

        byte[] frame = new byte[lenFrame];
        int index = 0;
        frame[index] = 0x7D;
        index++;
        for (byte byteVal : autoLinkMsgBytes) {
            if (byteVal == 0x7E) {
                frame[index] = 0x7E;
                index++;
                frame[index] = 0x02;
            } else if (byteVal == 0x7D) {
                frame[index] = 0x7E;
                index++;
                frame[index] = 0x01;
            } else {
                frame[index] = byteVal;
            }
            index++;
        }
        frame[index] = 0x7D;
        index++;

        return frame;
    }

    public static AutoLinkMsg fromByteArray(byte[] data) throws AutoLinkMsgException {
        AutoLinkMsg autoLinkMsg = null;

        if (data.length < AUTOLINK_MSG_MIN_SIZE) {
            MyLog.e(TAG, "AutoLinkMsg invalid");
            throw new AutoLinkMsgException(autoLinkMsg, ResultCode.UNKNOWN_ERROR);
        } else {
            autoLinkMsg = new AutoLinkMsg();
            byte[] msgHeaderBytes = new byte[MsgHeader.MSG_HEADER_SIZE];
            System.arraycopy(data, 0, msgHeaderBytes, 0, MsgHeader.MSG_HEADER_SIZE);
            MsgHeader msgHeader;
            try {
                msgHeader = MsgHeader.fromByteArray(msgHeaderBytes);
            } catch (AutoLinkMsgException exception) {
                msgHeader = (MsgHeader) (exception.getBaseMsg());
                autoLinkMsg.setMsgHeader(msgHeader);
                throw new AutoLinkMsgException(autoLinkMsg, exception.getResultCode());
            }
            autoLinkMsg.setMsgHeader(msgHeader);

            int lenMsgBody = data.length - MsgHeader.MSG_HEADER_SIZE;
            if (lenMsgBody != msgHeader.getLenMsgBody()) {
                MyLog.e(TAG, "lenMsgBody error");
                throw new AutoLinkMsgException(autoLinkMsg, ResultCode.UNKNOWN_ERROR);
            }
            byte[] msgBodyBytes = new byte[lenMsgBody];
            System.arraycopy(data, MsgHeader.MSG_HEADER_SIZE, msgBodyBytes, 0, lenMsgBody);
            BaseMsg msgBody = null;
            MyLog.i(TAG, "msgHeader.getMsgType() = " + msgHeader.getMsgType());
            try {
                switch (msgHeader.getMsgType()) {
                    case CONNECTION_MSG: {
                        msgBody = ConnectionMsg.fromByteArray(msgBodyBytes);
                        break;
                    }
                    case SET_PARAM_MSG: {
                        msgBody = SetParamReqMsg.fromByteArray(msgBodyBytes);
                        break;
                    }
                    case GET_PARAM_MSG: {
                        msgBody = GetParamReqMsg.fromByteArray(msgBodyBytes);
                        break;
                    }
                    case CONTROL_MSG: {
                        // TODO
                        MyLog.d(TAG, "CONTROL_MSG");
                        break;
                    }
                    case NAVIGATION_MSG: {
                        // TODO
                        MyLog.d(TAG, "NAVIGATION_MSG");
                        break;
                    }
                    case OTA_MSG: {
                        // TODO
                        MyLog.d(TAG, "OTA_MSG");
                        break;
                    }
                    case PAIR_MSG: {
                        // TODO
                        MyLog.d(TAG, "PAIR_MSG");
                        break;
                    }
                    case PAIR_CONFIRM_MSG: {
                        // TODO
                        MyLog.d(TAG, "PAIR_CONFIRM_MSG");
                        break;
                    }
                    case CAN_MSG: {
                        MyLog.d(TAG, "Can msg");
                        msgBody = CanRspMsg.fromByteArray(msgBodyBytes);
                        break;
                    }

                    case SINGLE_REPORT_MSG: {
                        msgBody = ReportRspMsg.fromByteArray(msgBodyBytes);
                        break;
                    }
                    case BATCH_REPORT_MSG: {
                        msgBody = ReportRspMsg.fromByteArray(msgBodyBytes);
                        break;
                    }
                    default: {
                        MyLog.w(TAG, "INVALID_MSG");
                        break;
                    }
                }
            } catch (AutoLinkMsgException exception) {
                msgBody = exception.getBaseMsg();
                autoLinkMsg.setMsgBody(msgBody);
                throw new AutoLinkMsgException(autoLinkMsg, exception.getResultCode());
            }
            autoLinkMsg.setMsgBody(msgBody);
        }

        return autoLinkMsg;
    }

    public static AutoLinkMsg fromRcvFrame(byte[] data, int len) {
        AutoLinkMsg autoLinkMsg = null;

        if (data[0] != 0x7D || data[len - 1] != 0x7D) {
            MyLog.e(TAG, "invalid frame, start or end error");
        } else {
            // escape
            byte[] msgBytes = new byte[len - 2];
            int lenMsgBytes = 0;
            for (int i = 1; i < len - 1; i++) {
                if (data[i] == 0x7E) {
                    if (data[i + 1] == 0x01) {
                        msgBytes[lenMsgBytes] = 0x7D;
                        i++;
                    } else if (data[i + 1] == 0x02) {
                        msgBytes[lenMsgBytes] = 0x7E;
                        i++;
                    } else {
                        msgBytes[lenMsgBytes] = data[i];
                    }
                } else {
                    msgBytes[lenMsgBytes] = data[i];
                }
                lenMsgBytes++;
            }

            if (lenMsgBytes < AUTOLINK_MSG_MIN_SIZE + 2) {
                MyLog.e(TAG, "invalid frame, frame size error");
            } else {
                int lenMsgBodyCipher = lenMsgBytes - 2 - MsgHeader.MSG_HEADER_SIZE;
                MyLog.i(TAG, "lenMsgBodyCipher = " + lenMsgBodyCipher + " MsgHeader.MSG_HEADER_SIZE = " + MsgHeader.MSG_HEADER_SIZE);
                if (DBG) {
                    MyLog.d(TAG, "cipherText = " + CodecUtils.bytesToHexString(msgBytes, MsgHeader.MSG_HEADER_SIZE, lenMsgBodyCipher));
                }
                byte[] plainText = RSASecurityUtils.Decrypt(msgBytes, MsgHeader.MSG_HEADER_SIZE, lenMsgBodyCipher);
                if (DBG) {
                    MyLog.d(TAG, "plainText = " + CodecUtils.bytesToHexString(plainText));
                }
                int lenAutoLinkMsgBytes = MsgHeader.MSG_HEADER_SIZE + plainText.length;
                byte[] autoLinkMsgBytes = new byte[lenAutoLinkMsgBytes];
                System.arraycopy(msgBytes, 0, autoLinkMsgBytes, 0, MsgHeader.MSG_HEADER_SIZE);
                System.arraycopy(plainText, 0, autoLinkMsgBytes, MsgHeader.MSG_HEADER_SIZE, plainText.length);
                MyLog.i(TAG, "autoLinkMsg = " + CodecUtils.bytesToHexString(autoLinkMsgBytes));

                int checkSum = CodecUtils.crcCheckSum(autoLinkMsgBytes, lenAutoLinkMsgBytes);
                int desiredCheckSum = (msgBytes[lenMsgBytes - 2] & 0xFF) << 8 | msgBytes[lenMsgBytes - 1] & 0xFF;
                if (checkSum != desiredCheckSum) {
                    byte[] crcBytes = new byte[2];
                    crcBytes[0] = (byte) ((checkSum & (0xFF << 8)) >> 8);
                    crcBytes[1] = (byte) (checkSum & 0xFF);
                    MyLog.e(TAG, "invalid frame, crc checksum error, "
                            + CodecUtils.bytesToHexString(crcBytes) + "!=" + CodecUtils.bytesToHexString(msgBytes, lenMsgBytes - 2, 2));
                    try {
                        autoLinkMsg = fromByteArray(autoLinkMsgBytes);
                    } catch (AutoLinkMsgException exception) {
                        autoLinkMsg = (AutoLinkMsg) (exception.getBaseMsg());
                    }
                    autoLinkMsg.setResultCode(ResultCode.CHECKSUM_ERROR);
                } else {
                    MyLog.e(TAG, "valid frame");
                    try {
                        autoLinkMsg = fromByteArray(autoLinkMsgBytes);
                    } catch (AutoLinkMsgException exception) {
                        autoLinkMsg = (AutoLinkMsg) (exception.getBaseMsg());
                        autoLinkMsg.setResultCode(exception.getResultCode());
                    }
                }
            }
        }

        return autoLinkMsg;
    }

    @Override
    public String toString() {
        return "{"
                + "MsgHeader=" + msgHeader
                + ", msgBody=" + msgBody
                + ", resultCode=" + resultCode
                + "}";
    }
}
