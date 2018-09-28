package com.chleon.telematics;

/**
 * Created by Ryan Fan on 2016/2/26.
 */
public enum ResultCode {
    OK(0x0000),
    FORMAT_ERROR(0x0001),
    MSG_TYPE_ERROR(0x0002),
    SUBMSG_TYPE_ERROR(0x0003),
    CMD_EXPIRE(0x0004),
    CHECKSUM_ERROR(0x0005),
    ACC_OFF(0x0006),
    DOOR_OPEN(0x0007),
    CAR_TYPE_ERROR(0x0008),
    CTRL_UNLOCK(0x0009),
    LF_DOOR_OPEN(0x000A),
    RF_DOOR_OPEN(0x000B),
    LR_DOOR_OPEN(0x000C),
    RR_DOOR_OPEN(0x000D),
    TRUNK_OPEN(0x000E),
    OVERSPEED(0x000F),
    PHONE_UNPAIR(0x0010),
    DECRYPT_ERROR(0X0011),
    VIN_INVALID(0X0100),
    TERMINAL_INVALID(0X0101),
    UNKNOWN_ERROR(0xFFFF);

    private int value;

    private ResultCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ResultCode getResultCode(int value) {
        switch (value) {
            case 0x0000:
                return OK;
            case 0x0001:
                return FORMAT_ERROR;
            case 0x0002:
                return MSG_TYPE_ERROR;
            case 0x0003:
                return SUBMSG_TYPE_ERROR;
            case 0x0004:
                return CMD_EXPIRE;
            case 0x0005:
                return CHECKSUM_ERROR;
            case 0x0006:
                return ACC_OFF;
            case 0x0007:
                return DOOR_OPEN;
            case 0x0008:
                return CAR_TYPE_ERROR;
            case 0x0009:
                return CTRL_UNLOCK;
            case 0x000A:
                return LF_DOOR_OPEN;
            case 0x000B:
                return RF_DOOR_OPEN;
            case 0x000C:
                return LR_DOOR_OPEN;
            case 0x000D:
                return RR_DOOR_OPEN;
            case 0x000E:
                return TRUNK_OPEN;
            case 0x000F:
                return OVERSPEED;
            case 0x0010:
                return PHONE_UNPAIR;
            case 0x0011:
                return DECRYPT_ERROR;
            case 0x0100:
                return VIN_INVALID;
            case 0x0101:
                return TERMINAL_INVALID;
            case 0xFFFF:
                return UNKNOWN_ERROR;
            default:
                return UNKNOWN_ERROR;
        }
    }
}
