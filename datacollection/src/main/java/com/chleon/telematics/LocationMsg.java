package com.chleon.telematics;

import android.location.Location;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Created by Ryan Fan on 2016/2/26.
 */
public class LocationMsg implements BaseMsg {

    public static final int LOCATION_MSG_SIZE = 26;

    private boolean fixed;
    private long timestamp;
    private double latitude;
    private double longitude;
    private double altitude;
    private float bearing;
    private float speed;
    private float hdop;
    private float vdop;
    private float tdop;

    public LocationMsg() {
    }

    public LocationMsg(LocationMsg locationMsg) {
        this.fixed = locationMsg.fixed;
        this.timestamp = locationMsg.timestamp;
        this.latitude = locationMsg.latitude;
        this.longitude = locationMsg.longitude;
        this.altitude = locationMsg.altitude;
        this.bearing = locationMsg.bearing;
        this.speed = locationMsg.speed;
        this.hdop = locationMsg.hdop;
        this.vdop = locationMsg.vdop;
        this.tdop = locationMsg.tdop;
    }

    public LocationMsg(Location location) {
        setLocation(location);
    }

    public void setLocation(Location location) {
        if (location != null) {
            fixed = true;
            timestamp = location.getTime();
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            altitude = location.getAltitude();
            bearing = location.getBearing();
            speed = location.getSpeed();
        } else {
            fixed = false;
        }
    }

    public void setNmea(float hdop, float vdop, float tdop) {
        this.hdop = hdop;
        this.vdop = vdop;
        this.tdop = tdop;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getHdop() {
        return hdop;
    }

    public void setHdop(float hdop) {
        this.hdop = hdop;
    }

    public float getVdop() {
        return vdop;
    }

    public void setVdop(float vdop) {
        this.vdop = vdop;
    }

    public float getTdop() {
        return tdop;
    }

    public void setTdop(float tdop) {
        this.tdop = tdop;
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(LOCATION_MSG_SIZE);

        int dummyInt = 0;
        if (fixed) {
            dummyInt = dummyInt | 0x80;
        }
        if (altitude < 0) {
            dummyInt = dummyInt | 0x40;
        }
        outStream.write(dummyInt & 0xFF);

        dummyInt = Math.round(bearing / 10);
        outStream.write(dummyInt & 0xFF);

        BigDecimal bigDecimal = new BigDecimal(latitude * 10000000);
        long dummyLong = bigDecimal.setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
        int flag = 0x00;
        if (dummyLong < 0) {
            flag = 0x80;
            dummyLong = -dummyLong;
        }
        outStream.write((int) (((dummyLong & (0xFF << 24)) >> 24) | flag));
        outStream.write((int) ((dummyLong & (0xFF << 16)) >> 16));
        outStream.write((int) ((dummyLong & (0xFF << 8)) >> 8));
        outStream.write((int) (dummyLong & 0xFF));

        bigDecimal = new BigDecimal(longitude * 10000000);
        dummyLong = bigDecimal.setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
        flag = 0x00;
        if (dummyLong < 0) {
            flag = 0x80;
            dummyLong = -dummyLong;
        }
        outStream.write((int) (((dummyLong & (0xFF << 24)) >> 24) | flag));
        outStream.write((int) ((dummyLong & (0xFF << 16)) >> 16));
        outStream.write((int) ((dummyLong & (0xFF << 8)) >> 8));
        outStream.write((int) (dummyLong & 0xFF));

        dummyInt = Math.round(speed * 36);
        outStream.write((dummyInt & (0xFF << 8)) >> 8);
        outStream.write(dummyInt & 0xFF);

        dummyInt = (int) Math.round(Math.abs(altitude));
        outStream.write((dummyInt & (0xFF << 8)) >> 8);
        outStream.write(dummyInt & 0xFF);

        String timestampStr = DateUtils.getGMTTimeStampFromMillis(timestamp);
        byte[] timestamp = CodecUtils.hexStringToBytes(timestampStr);
        try {
            outStream.write(timestamp);
        } catch (IOException e) {
            e.printStackTrace();
        }

        dummyInt = Math.round(hdop * 100);
        outStream.write((dummyInt & (0xFF << 8)) >> 8);
        outStream.write(dummyInt & 0xFF);

        dummyInt = Math.round(vdop * 100);
        outStream.write((dummyInt & (0xFF << 8)) >> 8);
        outStream.write(dummyInt & 0xFF);

        dummyInt = Math.round(tdop * 100);
        outStream.write((dummyInt & (0xFF << 8)) >> 8);
        outStream.write(dummyInt & 0xFF);

        return outStream.toByteArray();
    }

    @Override
    public String toString() {
        return "{" +
                "fixed=" + fixed
                + ", timestamp=" + DateUtils.getGMTTimeStampFromMillis(timestamp)
                + ", latitude=" + latitude
                + ", longitude=" + longitude
                + ", altitude=" + altitude
                + ", bearing=" + bearing
                + ", speed=" + speed
                + ", hdop=" + hdop
                + ", vdop=" + vdop
                + ", tdop=" + tdop
                + "}";
    }
}
