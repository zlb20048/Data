package com.chleon.telematics;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Ryan Fan on 2016/2/26.
 */
public class SingleReportMsg extends IndicatorMsg implements BaseMsg {
    private static final String TAG = SingleReportMsg.class.getSimpleName();

    private short flag;
    private ReportReason reportReason;
    private long timestamp;
    private LocationMsg locationMsg;
    private StatusMsg statusMsg;
    private AdditionMsg additionMsg;
    private BaseStationMsg baseStationMsg;
    private BatteryMsg batteryMsg;
    private GSensorMsg gsensorMsg;
    private FaultMsg faultMsg;

    private long id;
    private long elapsed;

    public SingleReportMsg() {
        super();
    }

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

    public short getFlag() {
        return flag;
    }

    public void setFlag(short flag) {
        this.flag = flag;
    }

    public ReportReason getReportReason() {
        return reportReason;
    }

    public void setReportReason(ReportReason reportReason) {
        this.reportReason = reportReason;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public LocationMsg getLocationMsg() {
        return locationMsg;
    }

    public void setLocationMsg(LocationMsg locationMsg) {
        this.locationMsg = locationMsg;
    }

    public StatusMsg getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(StatusMsg statusMsg) {
        this.statusMsg = statusMsg;
    }

    public AdditionMsg getAdditionMsg() {
        return additionMsg;
    }

    public void setAdditionMsg(AdditionMsg additionMsg) {
        this.additionMsg = additionMsg;
    }

    public BaseStationMsg getBaseStationMsg() {
        return baseStationMsg;
    }

    public void setBaseStationMsg(BaseStationMsg baseStationMsg) {
        this.baseStationMsg = baseStationMsg;
    }

    public FaultMsg getFaultMsg() {
        return faultMsg;
    }

    public void setFaultMsg(FaultMsg faultMsg) {
        this.faultMsg = faultMsg;
    }

    public BatteryMsg getBatteryMsg() {
        return batteryMsg;
    }

    public void setBatteryMsg(BatteryMsg batteryMsg) {
        this.batteryMsg = batteryMsg;
    }

    public GSensorMsg getGSensorMsg() {
        return gsensorMsg;
    }

    public void setGSensorMsg(GSensorMsg gsensorMsg) {
        this.gsensorMsg = gsensorMsg;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getElapsed() {
        return elapsed;
    }

    public void setElapsed(long elapsed) {
        this.elapsed = elapsed;
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(256);

        outStream.write(flag & 0xFF);
        outStream.write(reportReason.getValue() & 0xFF);
        outStream.write((indicator & (0xFF << 8)) >> 8);
        outStream.write(indicator & 0xFF);

        String strTimestamp = DateUtils.getTimeStampFromMillis(timestamp);
        byte[] timestamp = CodecUtils.hexStringToBytes(strTimestamp);
        int lenTimestamp = timestamp.length;
        if (lenTimestamp != 6) {
            MyLog.e(TAG, "lenTimestamp error");
        }
        try {
            outStream.write(timestamp);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (containLocationMsg()) {
            try {
                outStream.write(locationMsg.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (containBaseStationMsg()) {
            try {
                outStream.write(baseStationMsg.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (containStatusMsg()) {
            try {
                outStream.write(statusMsg.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (containAdditionMsg()) {
            try {
                outStream.write(additionMsg.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (containBatteryMsg()) {
            try {
                outStream.write(batteryMsg.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (containGSensorMsg()) {
            try {
                outStream.write(gsensorMsg.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (containFaultMsg()) {
            try {
                outStream.write(faultMsg.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return outStream.toByteArray();
    }

    @Override
    public String toString() {
        return "{"
                + "flag=" + flag
                + ", reportReason=" + reportReason
                + ", indicator=" + indicator
                + ", timestamp=" + DateUtils.getTimeStampFromMillis(timestamp)
                + ", locationMsg=" + locationMsg
                + ", baseStationMsg=" + baseStationMsg
                + ", statusMsg=" + statusMsg
                + ", additionMsg=" + additionMsg
                + ", batteryMsg=" + batteryMsg
                + ", gsensorMsg=" + gsensorMsg
                + ", faultMsg=" + faultMsg
                + "}";
    }
}
