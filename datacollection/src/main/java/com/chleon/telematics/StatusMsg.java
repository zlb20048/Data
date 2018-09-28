package com.chleon.telematics;

import java.io.ByteArrayOutputStream;

/**
 * Created by Ryan Fan on 2016/2/26.
 */
public class StatusMsg implements BaseMsg {

    public static final int STATUS_MSG_SIZE = 2;

    private boolean theftAlarm;
    private boolean sosAlarm;
    private boolean collisionAlarm;
    private boolean vibrationAlarm;
    private boolean fatigueDrivingAlarm;
    private boolean overtimeParkingAlarm;
    private boolean overSpeedAlarm;
    private boolean decelerationXAlarm;
    private boolean accelerationXAlarm;
    private boolean decelerationYAlarm;
    private boolean accelerationYAlarm;
    private boolean decelerationZAlarm;
    private boolean accelerationZAlarm;

    public StatusMsg() {
    }

    public StatusMsg(StatusMsg statusMsg) {
        this.theftAlarm = statusMsg.theftAlarm;
        this.sosAlarm = statusMsg.sosAlarm;
        this.collisionAlarm = statusMsg.collisionAlarm;
        this.vibrationAlarm = statusMsg.vibrationAlarm;
        this.fatigueDrivingAlarm = statusMsg.fatigueDrivingAlarm;
        this.overtimeParkingAlarm = statusMsg.overtimeParkingAlarm;
        this.overSpeedAlarm = statusMsg.overSpeedAlarm;
        this.decelerationXAlarm = statusMsg.decelerationXAlarm;
        this.accelerationXAlarm = statusMsg.accelerationXAlarm;
        this.decelerationYAlarm = statusMsg.decelerationYAlarm;
        this.accelerationYAlarm = statusMsg.accelerationYAlarm;
        this.decelerationZAlarm = statusMsg.decelerationZAlarm;
        this.accelerationZAlarm = statusMsg.accelerationZAlarm;
    }

    public boolean isTheftAlarm() {
        return theftAlarm;
    }

    public void setTheftAlarm(boolean theftAlarm) {
        this.theftAlarm = theftAlarm;
    }

    public boolean isSosAlarm() {
        return sosAlarm;
    }

    public void setSosAlarm(boolean sosAlarm) {
        this.sosAlarm = sosAlarm;
    }

    public boolean isCollisionAlarm() {
        return collisionAlarm;
    }

    public void setCollisionAlarm(boolean collisionAlarm) {
        this.collisionAlarm = collisionAlarm;
    }

    public boolean isVibrationAlarm() {
        return vibrationAlarm;
    }

    public void setVibrationAlarm(boolean vibrationAlarm) {
        this.vibrationAlarm = vibrationAlarm;
    }

    public boolean isFatigueDrivingAlarm() {
        return fatigueDrivingAlarm;
    }

    public void setFatigueDrivingAlarm(boolean fatigueDrivingAlarm) {
        this.fatigueDrivingAlarm = fatigueDrivingAlarm;
    }

    public boolean isOvertimeParkingAlarm() {
        return overtimeParkingAlarm;
    }

    public void setOvertimeParkingAlarm(boolean overtimeParkingAlarm) {
        this.overtimeParkingAlarm = overtimeParkingAlarm;
    }

    public boolean isOverSpeedAlarm() {
        return overSpeedAlarm;
    }

    public void setOverSpeedAlarm(boolean overSpeedAlarm) {
        this.overSpeedAlarm = overSpeedAlarm;
    }

    public boolean isDecelerationXAlarm() {
        return decelerationXAlarm;
    }

    public void setDecelerationXAlarm(boolean decelerationXAlarm) {
        this.decelerationXAlarm = decelerationXAlarm;
    }

    public boolean isAccelerationXAlarm() {
        return accelerationXAlarm;
    }

    public void setAccelerationXAlarm(boolean accelerationXAlarm) {
        this.accelerationXAlarm = accelerationXAlarm;
    }

    public boolean isDecelerationYAlarm() {
        return decelerationYAlarm;
    }

    public void setDecelerationYAlarm(boolean decelerationYAlarm) {
        this.decelerationYAlarm = decelerationYAlarm;
    }

    public boolean isAccelerationYAlarm() {
        return accelerationYAlarm;
    }

    public void setAccelerationYAlarm(boolean accelerationYAlarm) {
        this.accelerationYAlarm = accelerationYAlarm;
    }

    public boolean isDecelerationZAlarm() {
        return decelerationZAlarm;
    }

    public void setDecelerationZAlarm(boolean decelerationZAlarm) {
        this.decelerationZAlarm = decelerationZAlarm;
    }

    public boolean isAccelerationZAlarm() {
        return accelerationZAlarm;
    }

    public void setAccelerationZAlarm(boolean accelerationZAlarm) {
        this.accelerationZAlarm = accelerationZAlarm;
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(STATUS_MSG_SIZE);

        int dummy = 0;
        if (theftAlarm) {
            dummy = dummy | 0x40;
        }
        if (sosAlarm) {
            dummy = dummy | 0x10;
        }
        if (fatigueDrivingAlarm) {
            dummy = dummy | 0x08;
        }
        if (overtimeParkingAlarm) {
            dummy = dummy | 0x04;
        }
        if (overSpeedAlarm) {
            dummy = dummy | 0x02;
        }
        outStream.write(dummy & 0xFF);

        dummy = 0;
        if (decelerationXAlarm) {
            dummy = dummy | 0x80;
        }
        if (accelerationXAlarm) {
            dummy = dummy | 0x40;
        }
        if (decelerationYAlarm) {
            dummy = dummy | 0x08;
        }
        if (accelerationYAlarm) {
            dummy = dummy | 0x20;
        }
        if (decelerationZAlarm) {
            dummy = dummy | 0x01;
        }
        if (accelerationZAlarm) {
            dummy = dummy | 0x02;
        }
        if (collisionAlarm) {
            dummy = dummy | 0x10;
        }
        if (vibrationAlarm) {
            dummy = dummy | 0x04;
        }
        outStream.write(dummy & 0xFF);

        return outStream.toByteArray();
    }

    @Override
    public String toString() {
        return "{"
                + "theftAlarm=" + theftAlarm
                + ", sosAlarm=" + sosAlarm
                + ", collisionAlarm=" + collisionAlarm
                + ", vibrationAlarm=" + vibrationAlarm
                + ", fatigueDrivingAlarm=" + fatigueDrivingAlarm
                + ", overtimeParkingAlarm=" + overtimeParkingAlarm
                + ", overSpeedAlarm=" + overSpeedAlarm
                + ", decelerationXAlarm=" + decelerationXAlarm
                + ", accelerationXAlarm=" + accelerationXAlarm
                + ", decelerationYAlarm=" + decelerationYAlarm
                + ", accelerationYAlarm=" + accelerationYAlarm
                + ", decelerationZAlarm=" + decelerationZAlarm
                + ", accelerationZAlarm=" + accelerationZAlarm
                + "}";
    }
}
