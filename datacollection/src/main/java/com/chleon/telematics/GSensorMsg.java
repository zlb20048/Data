package com.chleon.telematics;

import java.io.ByteArrayOutputStream;

/**
 * Created by Ryan Fan on 2016/2/26.
 */
public class GSensorMsg implements BaseMsg {

    public static final int GSENSOR_MSG_SIZE = 7;

    private int accuracy = 0xFF;
    private float xValue;
    private float yValue;
    private float zValue;

    public GSensorMsg() {
    }

    public GSensorMsg(GSensorMsg gsensorMsg) {
        this.accuracy = gsensorMsg.accuracy;
        this.xValue = gsensorMsg.xValue;
        this.yValue = gsensorMsg.yValue;
        this.zValue = gsensorMsg.zValue;
    }

    public GSensorMsg(int accuracy, float xValue, float yValue, float zValue) {
        this.accuracy = accuracy;
        this.xValue = xValue;
        this.yValue = yValue;
        this.zValue = zValue;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    public float getxValue() {
        return xValue;
    }

    public void setxValue(float xValue) {
        this.xValue = xValue;
    }

    public float getyValue() {
        return yValue;
    }

    public void setyValue(float yValue) {
        this.yValue = yValue;
    }

    public float getzValue() {
        return zValue;
    }

    public void setzValue(float zValue) {
        this.zValue = zValue;
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(GSENSOR_MSG_SIZE);

        outStream.write(accuracy & 0xFF);

        int dummyInt = 0;
        int valueInt = Math.round(xValue * 1000);
        if (valueInt < 0) {
            dummyInt = 0x8000;
        }
        dummyInt = dummyInt | (Math.abs(valueInt) & 0x7FFF);
        outStream.write((dummyInt & (0xFF << 8)) >> 8);
        outStream.write(dummyInt & 0xFF);

        dummyInt = 0;
        valueInt = Math.round(yValue * 1000);
        if (valueInt < 0) {
            dummyInt = 0x8000;
        }
        dummyInt = dummyInt | (Math.abs(valueInt) & 0x7FFF);
        outStream.write((dummyInt & (0xFF << 8)) >> 8);
        outStream.write(dummyInt & 0xFF);

        dummyInt = 0;
        valueInt = Math.round(zValue * 1000);
        if (valueInt < 0) {
            dummyInt = 0x8000;
        }
        dummyInt = dummyInt | (Math.abs(valueInt) & 0x7FFF);
        outStream.write((dummyInt & (0xFF << 8)) >> 8);
        outStream.write(dummyInt & 0xFF);

        return outStream.toByteArray();
    }

    @Override
    public String toString() {
        return "{" +
                "accuracy=" + accuracy
                + ", xValue=" + xValue
                + ", yValue=" + yValue
                + ", zValue=" + zValue
                + "}";
    }
}
