package com.chleon.telematics;

/**
 * Created by Ryan Fan on 2016/2/27.
 */
public enum ReportReason {
    UNKNOWN(0x00),
    TIMER(0x20),
    ACC_ON(0x21),
    ACC_OFF(0x22),
    SLEEP(0x23),
    POWER_OFF(0x24),
    FAULT(0x25),
    STATUS_CHANGE(0x26),
    NAVIGATION(0x27),
    SOS(0x28),
    BREAK_DOWN(0x29),
    ALARM(0x2A),
    ACC_OFF_LIGHTS_ON(0x2B),
    ACC_OFF_DOORS_OPEN(0x2C),
    ACC_OFF_UNLOCKED(0x2D),
    ENGINE_START(0x2E),
    ENGINE_STOP(0x2F),
    DIAGNOSE(0x30),
    CHARGE_FINISH(0x31);

    private int value;

    private ReportReason(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ReportReason getReportReason(int value) {
        switch (value) {
            case 0x20:
                return TIMER;
            case 0x21:
                return ACC_ON;
            case 0x22:
                return ACC_OFF;
            case 0x23:
                return SLEEP;
            case 0x24:
                return POWER_OFF;
            case 0x25:
                return FAULT;
            case 0x26:
                return STATUS_CHANGE;
            case 0x27:
                return NAVIGATION;
            case 0x28:
                return SOS;
            case 0x29:
                return BREAK_DOWN;
            case 0x2A:
                return ALARM;
            case 0x2B:
                return ACC_OFF_LIGHTS_ON;
            case 0x2C:
                return ACC_OFF_DOORS_OPEN;
            case 0x2D:
                return ACC_OFF_UNLOCKED;
            case 0x2E:
                return ENGINE_START;
            case 0x2F:
                return ENGINE_STOP;
            case 0x30:
                return DIAGNOSE;
            case 0x31:
                return CHARGE_FINISH;
            default:
                return UNKNOWN;
        }
    }
}
