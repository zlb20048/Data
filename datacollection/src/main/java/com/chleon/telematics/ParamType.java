package com.chleon.telematics;

/**
 * Created by Ryan Fan on 2016/2/25.
 */
public enum ParamType {
    CHANNEL_ID_PARAM(0x00),
    NETWORK_PARAM(0x01),
    TIMER_PARAM(0x10),
    ACC_REPORT_PARAM(0x11),
    STATUS_REPORT_PARAM(0x12),
    FAULT_REPORT_PARAM(0x13),
    ALARM_REPORT_PARAM(0x14),
    RESTRICTION_PARAM(0x15),
    FAULT_CODE(0x20),
    FIND_CAR(0x21),
    INVALID_PARAM(0xFF);

    private int value;

    private int invalidValue;

    private ParamType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public int getInvalidValue() {
        return invalidValue;
    }

    public void setInvalidValue(int invalidValue) {
        this.invalidValue = invalidValue;
    }

    public static ParamType getParamType(int value) {
        ParamType paramType;
        switch (value) {
            case 0x00:
                paramType = CHANNEL_ID_PARAM;
                break;
            case 0x01:
                paramType = NETWORK_PARAM;
                break;
            case 0x10:
                paramType = TIMER_PARAM;
                break;
            case 0x11:
                paramType = ACC_REPORT_PARAM;
                break;
            case 0x12:
                paramType = STATUS_REPORT_PARAM;
                break;
            case 0x13:
                paramType = FAULT_REPORT_PARAM;
                break;
            case 0x14:
                paramType = ALARM_REPORT_PARAM;
                break;
            case 0x15:
                paramType = RESTRICTION_PARAM;
                break;
            case 0x20:
                paramType = FAULT_CODE;
                break;
            case 0x21:
                paramType = FIND_CAR;
                break;
            default:
                paramType = INVALID_PARAM;
                paramType.setInvalidValue(value);
                break;
        }
        return paramType;
    }
}
