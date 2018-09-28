package com.chleon.telematics;

public class CanRspMsg implements BaseMsg {

    private final static String TAG = CanRspMsg.class.getSimpleName();

    private static final int TIMESTAMP_SIZE = 6;

    private String timestamp;

    private byte[] canData;

    @Override
    public byte[] toByteArray() {
        return new byte[0];
    }

    public static CanRspMsg fromByteArray(byte[] data) {
        CanRspMsg canRspMsg = new CanRspMsg();
        byte[] timestamp = new byte[TIMESTAMP_SIZE];
        System.arraycopy(data, 0, timestamp, 0, TIMESTAMP_SIZE);
        canRspMsg.setTimestamp(CodecUtils.bytesToHexString(timestamp));
        int canLen = data.length - 6;
        byte[] canByte = new byte[canLen];
        System.arraycopy(data, 6, canByte, 0, canLen);
        canRspMsg.setCanData(canByte);
        MyLog.d(TAG, "can data = " + CodecUtils.bytesToHexString(canByte));
        return canRspMsg;
    }

    public byte[] getCanData() {
        return canData;
    }

    public void setCanData(byte[] canData) {
        this.canData = canData;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
