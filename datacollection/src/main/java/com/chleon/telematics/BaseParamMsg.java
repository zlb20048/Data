package com.chleon.telematics;

import android.text.TextUtils;

import com.chleon.datacollection.BuildConfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Ryan Fan on 2016/2/25.
 */
public abstract interface BaseParamMsg {
    public static final String TAG = BaseParamMsg.class.getSimpleName();

    public static class ChannelIdMsg implements BaseMsg {
        public static final int CHANNEL_ID_MSG_SIZE = 1;

        private static final int DEFAULT_CHANNEL_ID = 0x02;

        private int channelId = DEFAULT_CHANNEL_ID;

        public ChannelIdMsg() {
        }

        public ChannelIdMsg(int channelId) {
            this.channelId = channelId;
        }

        public int getChannelId() {
            return channelId;
        }

        public void setChannelId(int channelId) {
            this.channelId = channelId;
        }

        @Override
        public byte[] toByteArray() {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream(CHANNEL_ID_MSG_SIZE);

            outStream.write(channelId & 0xFF);

            return outStream.toByteArray();
        }

        public static ChannelIdMsg fromByteArray(byte[] data) throws AutoLinkMsgException {
            ChannelIdMsg channelIdMsg = null;

            if (data.length != CHANNEL_ID_MSG_SIZE) {
                MyLog.e(TAG, "ChannelIdMsg invalid");
                throw new AutoLinkMsgException(channelIdMsg, ResultCode.UNKNOWN_ERROR);
            } else {
                channelIdMsg = new ChannelIdMsg();
                channelIdMsg.channelId = (data[0] & 0xFF);
            }

            return channelIdMsg;
        }

        @Override
        public String toString() {
            return "{"
                    + "channelId=" + channelId
                    + "}";
        }
    }

    public static class NetworkParamMsg implements BaseMsg {
        public static final int NETWORK_PARAM_MIN_SIZE = 12;

        private static final String DEFAULT_IP = BuildConfig.SERVER_IP;
        private static final int DEFAULT_PORT = BuildConfig.SERVER_PORT;
        private static final int DEFAULT_HANDSHAKE_INTERVAL = 10 * 60;

        private int port;
        private int handshakeInterval;
        private String ip;

        private void init() {
            ip = DEFAULT_IP;
            port = DEFAULT_PORT;
            handshakeInterval = DEFAULT_HANDSHAKE_INTERVAL;
        }

        public NetworkParamMsg() {
            init();
        }

        public NetworkParamMsg(String ip, int port, int handshakeInterval) {
            if (!checkIP(ip)) {
                MyLog.e(TAG, "NetworkParamMsg constructor error, ip = " + ip);
                ip = DEFAULT_IP;
            }
            this.ip = ip;
            if (!checkPort(port)) {
                MyLog.e(TAG, "NetworkParamMsg constructor error, port = " + port);
                port = DEFAULT_PORT;
            }
            this.port = port;
            this.handshakeInterval = handshakeInterval;
        }

        public String getIp() {
            return ip;
        }

        private static boolean checkIP(String str) {
/*            Pattern pattern = Pattern
                    .compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]"
                            + "|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");
            return pattern.matcher(str).matches();*/
            return true;
        }

        public void setIp(String ip) {
            if (!checkIP(ip)) {
                MyLog.e(TAG, "NetworkParamMsg setIp error, ip = " + ip);
                ip = DEFAULT_IP;
            }
            this.ip = ip;
        }

        public int getPort() {
            return port;
        }

        private static boolean checkPort(int port) {
            return port <= 65535 && port >= 0;
        }

        public void setPort(int port) {
            if (!checkPort(port)) {
                MyLog.e(TAG, "NetworkParamMsg setPort error, port = " + port);
                port = DEFAULT_PORT;
            }
            this.port = port;
        }

        public int getHandshakeInterval() {
            return handshakeInterval;
        }

        public void setHandshakeInterval(int handshakeInterval) {
            this.handshakeInterval = handshakeInterval;
        }

        @Override
        public byte[] toByteArray() {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream(32);

            outStream.write((port & (0xFF << 8)) >> 8);
            outStream.write(port & 0xFF);

            outStream.write((handshakeInterval & (0xFF << 8)) >> 8);
            outStream.write(handshakeInterval & 0xFF);

            if (!TextUtils.isEmpty(ip)) {
                try {
                    outStream.write(ip.getBytes());
                    outStream.write(0x00);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // TODO dummy 0x00 should be added?
            }

            return outStream.toByteArray();
        }

        public static NetworkParamMsg fromByteArray(byte[] data) throws AutoLinkMsgException {
            NetworkParamMsg networkParamMsg = null;

            if (data.length < NETWORK_PARAM_MIN_SIZE) {
                MyLog.e(TAG, "NetworkParamMsg invalid");
                throw new AutoLinkMsgException(networkParamMsg, ResultCode.UNKNOWN_ERROR);
            } else {
                networkParamMsg = new NetworkParamMsg();
                networkParamMsg.port = (data[0] & 0xFF) << 8 | data[1] & 0xFF;
                if (!checkPort(networkParamMsg.port)) {
                    MyLog.e(TAG, "NetworkParamMsg error, port = " + networkParamMsg.port);
                }
                networkParamMsg.handshakeInterval = (data[2] & 0xFF) << 8 | data[3] & 0xFF;
                int lenIp = data.length - 4 - 1;
                byte[] ip = new byte[lenIp];
                System.arraycopy(data, 4, ip, 0, lenIp);
                networkParamMsg.ip = new String(ip);
                if (!checkIP(networkParamMsg.ip)) {
                    MyLog.e(TAG, "NetworkParamMsg error, ip = " + networkParamMsg.ip);
                }
            }

            return networkParamMsg;
        }

        @Override
        public String toString() {
            return "{"
                    + "ip=" + ip
                    + ", port=" + port
                    + ", handshakeInterval=" + handshakeInterval
                    + "}";
        }
    }

    public static class TimerMsg implements BaseMsg {
        public static final int TIMER_MSG_SIZE = 4;
        private static final int DEFAULT_INTERVAL = 5;

        private int interval;
        private int count;

        public TimerMsg() {
            this.interval = DEFAULT_INTERVAL;
            this.count = 0xFFFF;
        }

        public TimerMsg(int interval, int count) {
            this.interval = interval;
            this.count = count;
        }

        public int getInterval() {
            return interval;
        }

        public void setInterval(int interval) {
            this.interval = interval;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public boolean isTimerOn() {
            return interval != 0;
        }

        @Override
        public byte[] toByteArray() {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream(TIMER_MSG_SIZE);

            outStream.write((interval & (0xFF << 8)) >> 8);
            outStream.write(interval & 0xFF);

            outStream.write((count & (0xFF << 8)) >> 8);
            outStream.write(count & 0xFF);

            return outStream.toByteArray();
        }

        public static TimerMsg fromByteArray(byte[] data) throws AutoLinkMsgException {
            TimerMsg timerMsg = null;

            if (data.length != TIMER_MSG_SIZE) {
                MyLog.e(TAG, "TimerMsg invalid");
                throw new AutoLinkMsgException(timerMsg, ResultCode.UNKNOWN_ERROR);
            } else {
                timerMsg = new TimerMsg();
                timerMsg.interval = (data[0] & 0xFF) << 8 | data[1] & 0xFF;
                timerMsg.count = (data[2] & 0xFF) << 8 | data[3] & 0xFF;
            }

            return timerMsg;
        }

        @Override
        public String toString() {
            return "{"
                    + "interval=" + interval
                    + ", count=" + count
                    + "}";
        }
    }

    public static class ReportMsg implements BaseMsg {
        public static final int REPORT_MSG_SIZE = 1;

        private static final short REPORT_OFF = 0;
        private static final short REPORT_ON = 1;

        private boolean reportState = true;

        public ReportMsg() {
        }

        public ReportMsg(boolean reportState) {
            this.reportState = reportState;
        }

        public boolean getReportState() {
            return reportState;
        }

        public void setReportState(boolean reportState) {
            this.reportState = reportState;
        }

        @Override
        public byte[] toByteArray() {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream(REPORT_MSG_SIZE);

            short reportState = this.reportState ? REPORT_ON : REPORT_OFF;
            outStream.write(reportState & 0xFF);

            return outStream.toByteArray();
        }

        public static ReportMsg fromByteArray(byte[] data) throws AutoLinkMsgException {
            ReportMsg reportMsg = null;

            if (data.length != REPORT_MSG_SIZE) {
                MyLog.e(TAG, "ReportMsg invalid");
                throw new AutoLinkMsgException(reportMsg, ResultCode.UNKNOWN_ERROR);
            } else {
                reportMsg = new ReportMsg();
                short reportState = (short) (data[0] & 0xFF);
                reportMsg.reportState = (reportState == REPORT_ON) ? true : false;
            }

            return reportMsg;
        }

        @Override
        public String toString() {
            return "{"
                    + "reportState=" + reportState
                    + "}";
        }
    }

    public static class RestrictionMsg implements BaseMsg {
        public static final int RESTRICTION_MSG_MIN_SIZE = 2;
        public static final int RESTRICTION_MSG_MAX_SIZE = 11;

        private static final int DEFAULT_ATTRIBUTE = 0x3F;
        private static final int DEFAULT_MAX_SPEED = 160;
        private static final int DEFAULT_OVERSPEED_DURATION = 60;
        private static final int DEFAULT_PARKING_DURATION = 10080;
        private static final int DEFAULT_FATIGUE_DRIVING_DURATION = 240;
        private static final int DEFAULT_DECELERATION_X = 6000; //0.6~0.8g
        private static final int DEFAULT_ACCELERATION_X = 4500; //0.5g
        private static final int DEFAULT_DECELERATION_Y = 5000; //0.2g=10*10/50
        private static final int DEFAULT_ACCELERATION_Y = 5000; //0.2g=10*10/50
        private static final int DEFAULT_DECELERATION_Z = 20000; //2g
        private static final int DEFAULT_ACCELERATION_Z = 20000; //2g

        private int attribute;
        private int maxSpeed;
        private int overspeedDuration;
        private int parkingDuration;
        private int fatigueDrivingDuration;
        private int decelerationX;
        private int accelerationX;
        private int decelerationY;
        private int accelerationY;
        private int decelerationZ;
        private int accelerationZ;

        private void init() {
            attribute = DEFAULT_ATTRIBUTE;
            maxSpeed = DEFAULT_MAX_SPEED;
            overspeedDuration = DEFAULT_OVERSPEED_DURATION;
            parkingDuration = DEFAULT_PARKING_DURATION;
            fatigueDrivingDuration = DEFAULT_FATIGUE_DRIVING_DURATION;
            decelerationX = DEFAULT_DECELERATION_X;
            accelerationX = DEFAULT_ACCELERATION_X;
            decelerationY = DEFAULT_DECELERATION_Y;
            accelerationY = DEFAULT_ACCELERATION_Y;
            decelerationZ = DEFAULT_DECELERATION_Z;
            accelerationZ = DEFAULT_ACCELERATION_Z;
        }

        public RestrictionMsg() {
            init();
        }

        public int getAttribute() {
            return attribute;
        }

        public void setAttribute(int attribute) {
            this.attribute = attribute;
        }

        public int getMaxSpeed() {
            return maxSpeed;
        }

        public void setMaxSpeed(int maxSpeed) {
            this.maxSpeed = maxSpeed;
        }

        public int getOverspeedDuration() {
            return overspeedDuration;
        }

        public void setOverspeedDuration(int overspeedDuration) {
            this.overspeedDuration = overspeedDuration;
        }

        public int getParkingDuration() {
            return parkingDuration;
        }

        public void setParkingDuration(int parkingDuration) {
            this.parkingDuration = parkingDuration;
        }

        public int getFatigueDrivingDuration() {
            return fatigueDrivingDuration;
        }

        public void setFatigueDrivingDuration(int fatigueDrivingDuration) {
            fatigueDrivingDuration = fatigueDrivingDuration;
        }

        public int getDecelerationX() {
            return decelerationX;
        }

        public void setDecelerationX(int decelerationX) {
            this.decelerationX = decelerationX;
        }

        public int getAccelerationX() {
            return accelerationX;
        }

        public void setAccelerationX(int accelerationX) {
            this.accelerationX = accelerationX;
        }

        public int getDecelerationY() {
            return decelerationY;
        }

        public void setDecelerationY(int decelerationY) {
            this.decelerationY = decelerationY;
        }

        public int getAccelerationY() {
            return accelerationY;
        }

        public void setAccelerationY(int accelerationY) {
            this.accelerationY = accelerationY;
        }

        public int getDecelerationZ() {
            return decelerationZ;
        }

        public void setDecelerationZ(int decelerationZ) {
            this.decelerationZ = decelerationZ;
        }

        public int getAccelerationZ() {
            return accelerationZ;
        }

        public void setAccelerationZ(int accelerationZ) {
            this.accelerationZ = accelerationZ;
        }

        public void setRestrictMaxSpeed(boolean restrict) {
            if (restrict) {
                attribute = attribute | 0x0001;
            } else {
                attribute = attribute & 0xFFFE;
            }
        }

        public boolean isRestrictMaxSpeed() {
            return (attribute & 0x0001) != 0;
        }

        public void setRestrictOverSpeedDuration(boolean restrict) {
            if (restrict) {
                attribute = attribute | 0x0002;
            } else {
                attribute = attribute & 0xFFFD;
            }
        }

        public boolean isRestrictOverSpeedDuration() {
            return (attribute & 0x0002) != 0;
        }

        public void setRestrictParkingDuration(boolean restrict) {
            if (restrict) {
                attribute = attribute | 0x0004;
            } else {
                attribute = attribute & 0xFFFB;
            }
        }

        public boolean isRestrictParkingDuration() {
            return (attribute & 0x0004) != 0;
        }

        public void setRestrictFatigueDrivingDuration(boolean restrict) {
            if (restrict) {
                attribute = attribute | 0x0008;
            } else {
                attribute = attribute & 0xFFF7;
            }
        }

        public boolean isRestrictFatigueDrivingDuration() {
            return (attribute & 0x0008) != 0;
        }

        public void setRestrictDecelerationX(boolean deceleration) {
            if (deceleration) {
                attribute = attribute | 0x0010;
            } else {
                attribute = attribute & 0xFFEF;
            }
        }

        public boolean isRestrictDecelerationX() {
            return (attribute & 0x0010) != 0;
        }

        public void setRestrictAccelerationX(boolean acceleration) {
            if (acceleration) {
                attribute = attribute | 0x0020;
            } else {
                attribute = attribute & 0xFFDF;
            }
        }

        public boolean isRestrictAccelerationX() {
            return (attribute & 0x0020) != 0;
        }

        public void setRestrictDecelerationY(boolean deceleration) {
            if (deceleration) {
                attribute = attribute | 0x0080;
            } else {
                attribute = attribute & 0xFF7F;
            }
        }

        public boolean isRestrictDecelerationY() {
            return (attribute & 0x0080) != 0;
        }

        public void setRestrictAccelerationY(boolean acceleration) {
            if (acceleration) {
                attribute = attribute | 0x0040;
            } else {
                attribute = attribute & 0xFFBF;
            }
        }

        public boolean isRestrictAccelerationY() {
            return (attribute & 0x0040) != 0;
        }

        public void setRestrictDecelerationZ(boolean deceleration) {
            if (deceleration) {
                attribute = attribute | 0x0200;
            } else {
                attribute = attribute & 0xFDFF;
            }
        }

        public boolean isRestrictDecelerationZ() {
            return (attribute & 0x0200) != 0;
        }

        public void setRestrictAccelerationZ(boolean acceleration) {
            if (acceleration) {
                attribute = attribute | 0x0100;
            } else {
                attribute = attribute & 0xFEFF;
            }
        }

        public boolean isRestrictAccelerationZ() {
            return (attribute & 0x0100) != 0;
        }

        @Override
        public byte[] toByteArray() {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream(RESTRICTION_MSG_MAX_SIZE);

            outStream.write((attribute & (0xFF << 8)) >> 8);
            outStream.write(attribute & 0xFF);

            if (isRestrictMaxSpeed()) {
                outStream.write((maxSpeed & (0xFF << 8)) >> 8);
                outStream.write(maxSpeed & 0xFF);
            }
            if (isRestrictOverSpeedDuration()) {
                outStream.write(overspeedDuration & 0xFF);
            }
            if (isRestrictParkingDuration()) {
                outStream.write((parkingDuration & (0xFF << 8)) >> 8);
                outStream.write(parkingDuration & 0xFF);
            }
            if (isRestrictFatigueDrivingDuration()) {
                outStream.write((fatigueDrivingDuration & (0xFF << 8)) >> 8);
                outStream.write(fatigueDrivingDuration & 0xFF);
            }
            if (isRestrictDecelerationX()) {
                outStream.write((decelerationX & (0xFF << 8)) >> 8);
                outStream.write(decelerationX & 0xFF);
            }
            if (isRestrictAccelerationX()) {
                outStream.write((accelerationX & (0xFF << 8)) >> 8);
                outStream.write(accelerationX & 0xFF);
            }
            if (isRestrictAccelerationY()) {
                outStream.write((accelerationY & (0xFF << 8)) >> 8);
                outStream.write(accelerationY & 0xFF);
            }
            if (isRestrictDecelerationY()) {
                outStream.write((decelerationY & (0xFF << 8)) >> 8);
                outStream.write(decelerationY & 0xFF);
            }
            if (isRestrictAccelerationZ()) {
                outStream.write((accelerationZ & (0xFF << 8)) >> 8);
                outStream.write(accelerationZ & 0xFF);
            }
            if (isRestrictDecelerationZ()) {
                outStream.write((decelerationZ & (0xFF << 8)) >> 8);
                outStream.write(decelerationZ & 0xFF);
            }

            return outStream.toByteArray();
        }

        public static RestrictionMsg fromByteArray(byte[] data) throws AutoLinkMsgException {
            RestrictionMsg restrictionMsg = null;

            int msgSize = RESTRICTION_MSG_MIN_SIZE;
            if (data.length < msgSize) {
                MyLog.e(TAG, "RestrictionMsg invalid");
                throw new AutoLinkMsgException(restrictionMsg, ResultCode.UNKNOWN_ERROR);
            } else {
                restrictionMsg = new RestrictionMsg();
                restrictionMsg.attribute = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
                if (restrictionMsg.isRestrictMaxSpeed()) {
                    msgSize += 2;
                    if (data.length >= msgSize) {
                        restrictionMsg.maxSpeed = ((data[msgSize - 2] & 0xFF) << 8) | (data[msgSize - 1] & 0xFF);
                    } else {
                        throw new AutoLinkMsgException(restrictionMsg, ResultCode.UNKNOWN_ERROR);
                    }
                }
                if (restrictionMsg.isRestrictOverSpeedDuration()) {
                    msgSize += 1;
                    if (data.length >= msgSize) {
                        restrictionMsg.overspeedDuration = data[msgSize - 1] & 0xFF;
                    } else {
                        throw new AutoLinkMsgException(restrictionMsg, ResultCode.UNKNOWN_ERROR);
                    }
                }
                if (restrictionMsg.isRestrictParkingDuration()) {
                    msgSize += 2;
                    if (data.length >= msgSize) {
                        restrictionMsg.parkingDuration = ((data[msgSize - 2] & 0xFF) << 8) | (data[msgSize - 1] & 0xFF);
                    } else {
                        throw new AutoLinkMsgException(restrictionMsg, ResultCode.UNKNOWN_ERROR);
                    }
                }
                if (restrictionMsg.isRestrictFatigueDrivingDuration()) {
                    msgSize += 2;
                    if (data.length >= msgSize) {
                        restrictionMsg.fatigueDrivingDuration = ((data[msgSize - 2] & 0xFF) << 8) | (data[msgSize - 1] & 0xFF);
                    } else {
                        throw new AutoLinkMsgException(restrictionMsg, ResultCode.UNKNOWN_ERROR);
                    }
                }
                if (restrictionMsg.isRestrictDecelerationX()) {
                    msgSize += 2;
                    if (data.length >= msgSize) {
                        restrictionMsg.decelerationX = ((data[msgSize - 2] & 0xFF) << 8) | (data[msgSize - 1] & 0xFF);
                    } else {
                        throw new AutoLinkMsgException(restrictionMsg, ResultCode.UNKNOWN_ERROR);
                    }
                }
                if (restrictionMsg.isRestrictAccelerationX()) {
                    msgSize += 2;
                    if (data.length >= msgSize) {
                        restrictionMsg.accelerationX = ((data[msgSize - 2] & 0xFF) << 8) | (data[msgSize - 1] & 0xFF);
                    } else {
                        throw new AutoLinkMsgException(restrictionMsg, ResultCode.UNKNOWN_ERROR);
                    }
                }
                if (restrictionMsg.isRestrictAccelerationY()) {
                    msgSize += 2;
                    if (data.length >= msgSize) {
                        restrictionMsg.accelerationY = ((data[msgSize - 2] & 0xFF) << 8) | (data[msgSize - 1] & 0xFF);
                    } else {
                        throw new AutoLinkMsgException(restrictionMsg, ResultCode.UNKNOWN_ERROR);
                    }
                }
                if (restrictionMsg.isRestrictDecelerationY()) {
                    msgSize += 2;
                    if (data.length >= msgSize) {
                        restrictionMsg.decelerationY = ((data[msgSize - 2] & 0xFF) << 8) | (data[msgSize - 1] & 0xFF);
                    } else {
                        throw new AutoLinkMsgException(restrictionMsg, ResultCode.UNKNOWN_ERROR);
                    }
                }
                if (restrictionMsg.isRestrictAccelerationZ()) {
                    msgSize += 2;
                    if (data.length >= msgSize) {
                        restrictionMsg.accelerationZ = ((data[msgSize - 2] & 0xFF) << 8) | (data[msgSize - 1] & 0xFF);
                    } else {
                        throw new AutoLinkMsgException(restrictionMsg, ResultCode.UNKNOWN_ERROR);
                    }
                }
                if (restrictionMsg.isRestrictDecelerationZ()) {
                    msgSize += 2;
                    if (data.length >= msgSize) {
                        restrictionMsg.decelerationZ = ((data[msgSize - 2] & 0xFF) << 8) | (data[msgSize - 1] & 0xFF);
                    } else {
                        throw new AutoLinkMsgException(restrictionMsg, ResultCode.UNKNOWN_ERROR);
                    }
                }
            }
            return restrictionMsg;
        }

        @Override
        public String toString() {
            return "{"
                    + "attribute=" + attribute
                    + ", maxSpeed=" + maxSpeed
                    + ", overspeedDuration=" + overspeedDuration
                    + ", parkingDuration=" + parkingDuration
                    + ", fatigueDrivingDuration=" + fatigueDrivingDuration
                    + ", decelerationX=" + decelerationX
                    + ", accelerationX=" + accelerationX
                    + ", decelerationY=" + decelerationY
                    + ", accelerationY=" + accelerationY
                    + ", decelerationZ=" + decelerationZ
                    + ", accelerationZ=" + accelerationZ
                    + "}";
        }
    }
}
