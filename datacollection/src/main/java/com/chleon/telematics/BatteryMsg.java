package com.chleon.telematics;

import java.io.ByteArrayOutputStream;

/**
 * Created by Ryan Fan on 2016/2/26.
 */
public class BatteryMsg implements BaseMsg {

    public static final int BATTERY_MSG_SIZE = 8;

    public static enum BatChargeState {
        DISCHARGE,
        AC_CHARGE,
        DC_CHARGE,
        VOID
    }

    public static enum BatHeatingState {
        NOT_HEATING,
        HEATING,
        HEATING_FAULT,
        VOID
    }

    private int motorPower;
    private int soc;
    private BatChargeState batChargeState = BatChargeState.DISCHARGE;
    private BatHeatingState batHeatingState = BatHeatingState.NOT_HEATING;

    private void init() {
        motorPower = 0xFFFF;
        soc = 0xFFFF;
        batChargeState = BatChargeState.VOID;
        batHeatingState = BatHeatingState.VOID;
    }

    public BatteryMsg() {
        init();
    }

    public BatteryMsg(BatteryMsg batteryMsg) {
        this.motorPower = batteryMsg.motorPower;
        this.soc = batteryMsg.soc;
        this.batChargeState = batteryMsg.batChargeState;
        this.batHeatingState = batteryMsg.batHeatingState;
    }

    public BatteryMsg(int motorPower, int soc,
                      BatChargeState batChargeState, BatHeatingState batHeatingState) {
        this.motorPower = motorPower;
        this.soc = soc;
        this.batChargeState = batChargeState;
        this.batHeatingState = batHeatingState;
    }

    public int getMotorPower() {
        return motorPower;
    }

    public void setMotorPower(int motorPower) {
        this.motorPower = motorPower;
    }

    public int getSoc() {
        return soc;
    }

    public void setSoc(int soc) {
        this.soc = soc;
    }

    public BatChargeState getBatChargeState() {
        return batChargeState;
    }

    public void setBatChargeState(BatChargeState batChargeState) {
        this.batChargeState = batChargeState;
    }

    public BatHeatingState getBatHeatingState() {
        return batHeatingState;
    }

    public void setBatHeatingState(BatHeatingState batHeatingState) {
        this.batHeatingState = batHeatingState;
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(BATTERY_MSG_SIZE);

        int dummyInt = BATTERY_MSG_SIZE;
        outStream.write((dummyInt & (0xFF << 8)) >> 8);
        outStream.write(dummyInt & 0xFF);

        dummyInt = motorPower;
        outStream.write((dummyInt & (0xFF << 8)) >> 8);
        outStream.write(dummyInt & 0xFF);

        dummyInt = soc;
        outStream.write((dummyInt & (0xFF << 8)) >> 8);
        outStream.write(dummyInt & 0xFF);

        switch (batChargeState) {
            case VOID:
                dummyInt = 0xFF;
                break;
            default:
                dummyInt = batChargeState.ordinal();
                break;
        }
        outStream.write(dummyInt & 0xFF);

        switch (batHeatingState) {
            case VOID:
                dummyInt = 0xFF;
                break;
            default:
                dummyInt = batChargeState.ordinal();
                break;
        }
        outStream.write(dummyInt & 0xFF);

        return outStream.toByteArray();
    }

    @Override
    public String toString() {
        return "{"
                + "motorPower=" + motorPower
                + ", soc=" + soc
                + ", batChargeState=" + batChargeState
                + ", batHeatingState=" + batHeatingState
                + "}";
    }
}
