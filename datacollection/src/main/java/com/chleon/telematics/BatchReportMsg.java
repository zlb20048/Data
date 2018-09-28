package com.chleon.telematics;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ryan Fan on 2016/2/26.
 */
public class BatchReportMsg extends IndicatorMsg implements BaseMsg {
    private static final String TAG = BatchReportMsg.class.getSimpleName();
    private static final int MAX_COUNT = 255;

    private short flag;
    private short count;
    private ReportReason reportReason;
    private List<Long> timestamps;
    private List<LocationMsg> locationMsgs;
    private List<StatusMsg> statusMsgs;
    private List<AdditionMsg> additionMsgs;
    private List<BaseStationMsg> baseStationMsgs;
    private List<BatteryMsg> batteryMsgs;
    private List<GSensorMsg> gsensorMsgs;
    private FaultMsg faultMsg;

    private List<Long> ids;

    public BatchReportMsg() {
        ids = new ArrayList<Long>();
        timestamps = new ArrayList<Long>();
        locationMsgs = new ArrayList<LocationMsg>();
        statusMsgs = new ArrayList<StatusMsg>();
        additionMsgs = new ArrayList<AdditionMsg>();
        baseStationMsgs = new ArrayList<BaseStationMsg>();
        batteryMsgs = new ArrayList<BatteryMsg>();
        gsensorMsgs = new ArrayList<GSensorMsg>();
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

    public short getFlag() {
        return flag;
    }

    public void setFlag(short flag) {
        this.flag = flag;
    }

    public boolean reachMaxCount() {
        return count >= MAX_COUNT;
    }

    public short getCount() {
        return count;
    }

    public void setCount(short count) {
        this.count = count;
    }

    public void inCreaseCount() {
        count++;
    }

    public ReportReason getReportReason() {
        return reportReason;
    }

    public void setReportReason(ReportReason reportReason) {
        this.reportReason = reportReason;
    }

    public List<Long> getTimestamps() {
        return timestamps;
    }

    public void addTimestamp(long timestamp) {
        timestamps.add(timestamp);
    }

    public List<LocationMsg> getLocationMsgs() {
        return locationMsgs;
    }

    public void addLocationMsg(LocationMsg locationMsg) {
        locationMsgs.add(locationMsg);
    }

    public List<StatusMsg> getStatusMsgs() {
        return statusMsgs;
    }

    public void addStatusMsg(StatusMsg statusMsg) {
        statusMsgs.add(statusMsg);
    }

    public List<AdditionMsg> getAdditionMsgs() {
        return additionMsgs;
    }

    public void addAdditionMsg(AdditionMsg additionMsg) {
        additionMsgs.add(additionMsg);
    }

    public List<BaseStationMsg> getBaseStationMsgs() {
        return baseStationMsgs;
    }

    public void addBaseStationMsg(BaseStationMsg baseStationMsg) {
        baseStationMsgs.add(baseStationMsg);
    }

    public List<BatteryMsg> getBatteryMsgs() {
        return batteryMsgs;
    }

    public void addBatteryMsg(BatteryMsg batteryMsg) {
        batteryMsgs.add(batteryMsg);
    }

    public List<GSensorMsg> getGSensorMsgs() {
        return gsensorMsgs;
    }

    public void addGSensorMsg(GSensorMsg gsensorMsg) {
        gsensorMsgs.add(gsensorMsg);
    }

    public FaultMsg getFaultMsg() {
        return faultMsg;
    }

    public void setFaultMsg(FaultMsg faultMsg) {
        this.faultMsg = faultMsg;
    }

    public List<Long> getIds() {
        return ids;
    }

    public void addId(long id) {
        ids.add(id);
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(14032);

        outStream.write(flag & 0xFF);
        outStream.write(count & 0xFF);
        outStream.write(reportReason.getValue() & 0xFF);
        outStream.write((indicator & (0xFF << 8)) >> 8);
        outStream.write(indicator & 0xFF);

        for (int i = 0; i < count; i++) {
            String strTimestamp = DateUtils.getTimeStampFromMillis(timestamps.get(i));
            byte[] timestampBytes = CodecUtils.hexStringToBytes(strTimestamp);
            int lenTimestampMsg = timestampBytes.length;
            if (lenTimestampMsg != 6) {
                MyLog.e(TAG, "lenTimestampMsg error");
            }
            try {
                outStream.write(timestampBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (containLocationMsg()) {
                LocationMsg locationMsg = locationMsgs.get(i);
                try {
                    outStream.write(locationMsg.toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (containBaseStationMsg()) {
                BaseStationMsg baseStationMsg = baseStationMsgs.get(i);
                try {
                    outStream.write(baseStationMsg.toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (containStatusMsg()) {
                StatusMsg statusMsg = statusMsgs.get(i);
                try {
                    outStream.write(statusMsg.toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (containAdditionMsg()) {
                AdditionMsg additionMsg = additionMsgs.get(i);
                try {
                    outStream.write(additionMsg.toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (containBatteryMsg()) {
                BatteryMsg batteryMsg = batteryMsgs.get(i);
                try {
                    outStream.write(batteryMsg.toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (containGSensorMsg()) {
                GSensorMsg gsensorMsg = gsensorMsgs.get(i);
                try {
                    outStream.write(gsensorMsg.toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
        String str = "{"
                + "flag=" + flag
                + ", count=" + count
                + ", reportReason=" + reportReason
                + ", indicator=" + indicator;
        for (int i = 0; i < count; i++) {
            str += ", timestamps[" + i + "]=" + DateUtils.getTimeStampFromMillis(timestamps.get(i));
            if (containLocationMsg()) {
                str += ", locationMsgs[" + i + "]=" + locationMsgs.get(i);
            }
            if (containBaseStationMsg()) {
                str += ", baseStationMsgs[" + i + "]=" + baseStationMsgs.get(i);
            }
            if (containStatusMsg()) {
                str += ", statusMsgs[" + i + "]=" + statusMsgs.get(i);
            }
            if (containAdditionMsg()) {
                str += ", additionMsgs[" + i + "]=" + additionMsgs.get(i);
            }
            if (containBatteryMsg()) {
                str += ", batteryMsgs[" + i + "]=" + batteryMsgs.get(i);
            }
            if (containGSensorMsg()) {
                str += ", gsensorMsgs[" + i + "]=" + gsensorMsgs.get(i);
            }
        }
        if (containFaultMsg()) {
            str += ", faultMsg=" + faultMsg;
        }
        return str;

    }
}
