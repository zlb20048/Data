package com.chleon.telematics;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CanReqMsg implements BaseMsg {

    private final static String TAG = CanReqMsg.class.getSimpleName();

    private long id;

    private short flag;

    private long timestamp;

    private byte[] msg;

    private long elapsed;

    private LocationMsg locationMsg;

    public boolean shouldAck() {
        return (flag & 0x80) != 0;
    }

    public void setShouldAck(boolean shouldAck) {
        if (shouldAck) {
            flag = (short) (flag | 0x80);
        } else {
            flag = (short) (flag & 0x7F);
        }
    }

    public short getFlag() {
        return flag;
    }

    public void setFlag(short flag) {
        this.flag = flag;
    }

    public byte[] getMsg() {
        return msg;
    }

    public void setMsg(byte[] msg) {
        this.msg = msg;
    }

    public boolean isDelayReport() {
        return (flag & 0x01) != 0;
    }

    public void setDelayReport(boolean delayReport) {
        if (delayReport) {
            flag = (short) (flag | 0x01);
        } else {
            flag = (short) (flag & 0xFE);
        }
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(msg.length + 33);

        outStream.write(flag & 0xFF);

        String strTimestamp = DateUtils.getTimeStampFromMillis(timestamp);
        byte[] timestamp = CodecUtils.hexStringToBytes(strTimestamp);
        int lenTimestamp = timestamp.length;
        if (lenTimestamp != 6) {
            MyLog.e(TAG, "lenTimestamp error");
        }
        try {
            outStream.write(timestamp);
            outStream.write(locationMsg.toByteArray());
            outStream.write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outStream.toByteArray();
    }

    public long getElapsed() {
        return elapsed;
    }

    public void setElapsed(long elapsed) {
        this.elapsed = elapsed;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocationMsg getLocationMsg() {
        return locationMsg;
    }

    public void setLocationMsg(LocationMsg locationMsg) {
        this.locationMsg = locationMsg;
    }
}
