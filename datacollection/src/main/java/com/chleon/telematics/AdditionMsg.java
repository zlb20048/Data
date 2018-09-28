package com.chleon.telematics;

import java.io.ByteArrayOutputStream;

/**
 * Created by Ryan Fan on 2016/2/26.
 */
public class AdditionMsg implements BaseMsg {

    public static final int ADDITION_MSG_SIZE = 21;

    private int rpm;
    private int speed;
    private float instFuel;
    private float avgFuel;
    private float inletTemp;
    private float coolantTemp;
    private int fuelTrim;
    private int throttlePos;
    private int remainingFuel;
    private int enMileage;
    private int totalFuel;
    private int totalMileage;

    public AdditionMsg() {
    }

    public AdditionMsg(AdditionMsg additionMsg) {
        this.rpm = additionMsg.rpm;
        this.speed = additionMsg.speed;
        this.instFuel = additionMsg.instFuel;
        this.avgFuel = additionMsg.avgFuel;
        this.inletTemp = additionMsg.inletTemp;
        this.coolantTemp = additionMsg.coolantTemp;
        this.fuelTrim = additionMsg.fuelTrim;
        this.throttlePos = additionMsg.throttlePos;
        this.remainingFuel = additionMsg.remainingFuel;
        this.enMileage = additionMsg.enMileage;
        this.totalFuel = additionMsg.totalFuel;
        this.totalMileage = additionMsg.totalMileage;
    }

    public AdditionMsg(int rpm, int speed, float instFuel, float avgFuel, float inletTemp, float coolantTemp, int fuelTrim, int throttlePos, int remainingFuel, int enMileage, int totalFuel, int totalMileage) {
        this.rpm = rpm;
        this.speed = speed;
        this.instFuel = instFuel;
        this.avgFuel = avgFuel;
        this.inletTemp = inletTemp;
        this.coolantTemp = coolantTemp;
        this.fuelTrim = fuelTrim;
        this.throttlePos = throttlePos;
        this.remainingFuel = remainingFuel;
        this.enMileage = enMileage;
        this.totalFuel = totalFuel;
        this.totalMileage = totalMileage;
    }

    public int getRpm() {
        return rpm;
    }

    public void setRpm(int rpm) {
        this.rpm = rpm;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public float getInstFuel() {
        return instFuel;
    }

    public void setInstFuel(float instFuel) {
        this.instFuel = instFuel;
    }

    public float getAvgFuel() {
        return avgFuel;
    }

    public void setAvgFuel(float avgFuel) {
        this.avgFuel = avgFuel;
    }

    public float getInletTemp() {
        return inletTemp;
    }

    public void setInletTemp(float inletTemp) {
        this.inletTemp = inletTemp;
    }

    public float getCoolantTemp() {
        return coolantTemp;
    }

    public void setCoolantTemp(float coolantTemp) {
        this.coolantTemp = coolantTemp;
    }

    public int getFuelTrim() {
        return fuelTrim;
    }

    public void setFuelTrim(int fuelTrim) {
        this.fuelTrim = fuelTrim;
    }

    public int getThrottlePos() {
        return throttlePos;
    }

    public void setThrottlePos(int throttlePos) {
        this.throttlePos = throttlePos;
    }

    public int getRemainingFuel() {
        return remainingFuel;
    }

    public void setRemainingFuel(int remainingFuel) {
        this.remainingFuel = remainingFuel;
    }

    public int getEnMileage() {
        return enMileage;
    }

    public void setEnMileage(int enMileage) {
        this.enMileage = enMileage;
    }

    public int getTotalFuel() {
        return totalFuel;
    }

    public void setTotalFuel(int totalFuel) {
        this.totalFuel = totalFuel;
    }

    public int getTotalMileage() {
        return totalMileage;
    }

    public void setTotalMileage(int totalMileage) {
        this.totalMileage = totalMileage;
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(ADDITION_MSG_SIZE);

        int dummyInt = ADDITION_MSG_SIZE;
        outStream.write((dummyInt & (0xFF << 8)) >> 8);
        outStream.write(dummyInt & 0xFF);

        if(instFuel < 0) {
            dummyInt = 0xFF;
        } else {
            dummyInt = Math.round(instFuel * 10);
        }
        outStream.write(dummyInt & 0xFF);

        if (avgFuel < 0) {
            dummyInt = 0xFF;
        } else {
            dummyInt = Math.round(avgFuel * 10);
        }
        outStream.write(dummyInt & 0xFF);

        if (remainingFuel < 0) {
            dummyInt = 0xFFFF;
        } else {
            dummyInt = remainingFuel * 10;
        }
        outStream.write((dummyInt & (0xFF << 8)) >> 8);
        outStream.write(dummyInt & 0xFF);

        if (inletTemp <= -0xFF) {
            dummyInt = 0xFF;
        } else {
            dummyInt = Math.round(inletTemp + 48);
        }
        outStream.write(dummyInt & 0xFF);

        if (totalMileage < 0) {
            dummyInt = 0xFFFFFF;
        } else {
            dummyInt = totalMileage * 10;
        }
        outStream.write((dummyInt & (0xFF << 16)) >> 16);
        outStream.write((dummyInt & (0xFF << 8)) >> 8);
        outStream.write(dummyInt & 0xFF);

        if (coolantTemp <= -0xFF) {
            dummyInt = 0xFF;
        } else {
            dummyInt = Math.round(coolantTemp + 50);
        }
        outStream.write(dummyInt & 0xFF);

        if (fuelTrim < 0) {
            dummyInt = 0xFF;
        } else {
            dummyInt = fuelTrim;
        }
        outStream.write(dummyInt & 0xFF);

        if (rpm < 0) {
            dummyInt = 0xFFFF;
        } else {
            dummyInt = rpm;
        }
        outStream.write((dummyInt & (0xFF << 8)) >> 8);
        outStream.write(dummyInt & 0xFF);

        if (speed < 0) {
            dummyInt = 0xFFFF;
        } else {
            dummyInt = speed * 10;
        }
        outStream.write((dummyInt & (0xFF << 8)) >> 8);
        outStream.write(dummyInt & 0xFF);

        if (enMileage < 0) {
            dummyInt = 0xFFFF;
        } else {
            dummyInt = enMileage * 10;
        }
        outStream.write((dummyInt & (0xFF << 8)) >> 8);
        outStream.write(dummyInt & 0xFF);

        if (throttlePos < 0) {
            dummyInt = 0xFF;
        } else {
            dummyInt = throttlePos;
        }
        outStream.write(dummyInt & 0xFF);

        if (totalFuel < 0) {
            dummyInt = 0xFFFF;
        } else {
            dummyInt = totalFuel;
        }
        outStream.write((dummyInt & (0xFF << 8)) >> 8);
        outStream.write(dummyInt & 0xFF);

        return outStream.toByteArray();
    }

    @Override
    public String toString() {

        return "{"
                + "rpm=" + rpm
                + ", speed=" + speed
                + ", instFuel=" + instFuel
                + ", avgFuel=" + avgFuel
                + ", inletTemp=" + inletTemp
                + ", coolantTemp=" + coolantTemp
                + ", fuelTrim=" + fuelTrim
                + ", throttlePos=" + throttlePos
                + ", remainingFuel=" + remainingFuel
                + ", enMileage=" + enMileage
                + ", totalFuel=" + totalFuel
                + ", totalMileage=" + totalMileage
                + "}";
    }
}
