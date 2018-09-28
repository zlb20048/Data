package com.chleon.telematics;

import android.os.SystemClock;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Ryan Fan on 2016/3/3.
 */
public class DateUtils {
    public static final SimpleDateFormat SHORT_DATE_FORMAT = new SimpleDateFormat(
            "yyMMddHHmmss");
    public static final SimpleDateFormat GMT_SHORT_DATE_FORMAT = new SimpleDateFormat(
            "yyMMddHHmmss");
    private static final SimpleDateFormat LONG_DATE_FORMAT = new SimpleDateFormat(
            "yyyyMMddHHmmss");
    private static final SimpleDateFormat GMT_LONG_DATE_FORMAT = new SimpleDateFormat(
            "yyyyMMddHHmmss");

    private static final long MSG_EXPIRED_TIME = 3 * 60 * 60 * 1000;
    private static final long HEARTBEAT_EXPIRED_TIME = 15 * 60 * 1000;

    static {
        GMT_SHORT_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        GMT_LONG_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT+0"));
    }

    public static String getTimeStampFromMillis(long millis) {
        Date date = new Date();
        date.setTime(millis);
        return SHORT_DATE_FORMAT.format(date);
    }

    public static String getGMTTimeStampFromMillis(long millis) {
        Date date = new Date();
        date.setTime(millis);
        return GMT_SHORT_DATE_FORMAT.format(date);
    }

    public static long getMillisFromLocalDate() {
        Date date = new Date();
        try {
            String strTimestamp = GMT_LONG_DATE_FORMAT.format(date);
            date = LONG_DATE_FORMAT.parse(strTimestamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    public static long getMillisFromTimestamp(String timestamp) {
        Date date;
        try {
            date = LONG_DATE_FORMAT.parse("20" + timestamp);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return getMillisFromLocalDate();
        }
    }

    public static boolean isMsgExpired(String timestamp, long millis) {
        long milliseconds = 0;

        try {
            Date date = LONG_DATE_FORMAT.parse("20" + timestamp);
            milliseconds = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (Math.abs(millis - milliseconds) > MSG_EXPIRED_TIME) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isHeartBeatExpired(long timestamp) {
        if (Math.abs(SystemClock.elapsedRealtime() - timestamp) > HEARTBEAT_EXPIRED_TIME) {
            return true;
        } else {
            return false;
        }
    }
}
