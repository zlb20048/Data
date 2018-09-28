package com.chleon.telematics;

/**
 * Created by Ryan Fan on 2016/2/27.
 */
public enum AlarmType {
    UNKNOWN(0x0000),
    THEFT(0x4000),
    SOS(0x0100),
    FATIGUE_DRIVING(0x0800),
    OVERTIME_PARKING(0x0400),
    OVERSPEED(0x0200),
    DECELERATION_X(0x0080),
    ACCELERATION_X(0x0040),
    DECELERATION_Y(0x0008),
    ACCELERATION_Y(0x0020),
    DECELERATION_Z(0x0001),
    ACCELERATION_Z(0x0002),
    COLLISION(0x0010),
    VIBRATION(0x0004);

    private int value;

    private AlarmType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AlarmType getAlarmType(int value) {
        switch (value) {
            case 0x4000:
                return THEFT;
            case 0x0100:
                return SOS;
            case 0x0800:
                return FATIGUE_DRIVING;
            case 0x0400:
                return OVERTIME_PARKING;
            case 0x0200:
                return OVERSPEED;
            case 0x0080:
                return DECELERATION_X;
            case 0x0040:
                return ACCELERATION_X;
            case 0x0008:
                return DECELERATION_Y;
            case 0x0020:
                return ACCELERATION_Y;
            case 0x0001:
                return DECELERATION_Z;
            case 0x0002:
                return ACCELERATION_Z;
            case 0x0010:
                return COLLISION;
            case 0x0004:
                return VIBRATION;
            default:
                return UNKNOWN;
        }
    }
}
