package com.chleon.datacollection.autolink;

import android.text.StaticLayout;

public interface Constants {

    public final static String PROPERTY_WAKE_LOCK_TIMEOUT = "";

    public final static int SERVER_NOT_AVAILABLE = -2;              /* If Server did not start or is resetting */
    public final static int INVALID_RESPONSE = -1;
    public final static int SUCCESS = 0;
    public final static int RESPONSE_TIMEOUT = 1;
    public final static int GENERIC_FAILURE = 2;

    //Request Definition
    public final static int REQUEST_LOGIN = 1;
    public final static int REQUEST_HEARTBEAT = 2;
    public final static int REQUEST_LOGOUT = 3;
    public final static int REQUEST_ACK_SET_PARAM = 4;
    public final static int REQUEST_ACK_GET_PARAM = 5;
    public final static int REQUEST_SINGLE_REPORT = 6;
    public final static int REQUEST_BATCH_REPORT = 7;
    public final static int REQUEST_CAN_REPORT = 8;

    //Unsolicited Response Definition
    public final static int UNSOL_RESPONSE_BASE = 1000;
    public final static int UNSOL_RESPONSE_SET_PARAM = UNSOL_RESPONSE_BASE + 1;
    public final static int UNSOL_RESPONSE_GET_PARAM = UNSOL_RESPONSE_BASE + 2;

    //public final static int MAX_FRAME_SIZE = 1452;
    public final static int MAX_FRAME_SIZE = 27588;//(13777 + 14 + 2) * 2 + 2;
    public final static int MIN_FRAME_SIZE = 18;

    public final static int SERVER_DETECT_DELAY = 5000;

    //Server Address & Port & Delay Definitions
    public final static String HOST_ADDR = "36.110.75.21";
    public final static int HOST_PORT = 8807;
    public final static int HOST_DELAY = 3000;

    //For Tester
    public final static int FAKE_MSG_DELAY = 3 * 60 * 1000;
}
