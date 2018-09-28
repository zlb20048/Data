package com.chleon.telematics;

/**
 * Created by Ryan Fan on 2016/3/12.
 */
public class IndicatorMsg {

    protected int indicator;

    public boolean containLocationMsg() {
        return (indicator & 0x8000) != 0;
    }

    public void setContainLocationMsg(boolean containLocationMsg) {
        if (containLocationMsg) {
            indicator = indicator | 0x8000;
        } else {
            indicator = indicator & 0x7FFF;
        }
    }

    public boolean containStatusMsg() {
        return (indicator & 0x4000) != 0;
    }

    public void setContainStatusMsg(boolean containStatusMsg) {
        if (containStatusMsg) {
            indicator = indicator | 0x4000;
        } else {
            indicator = indicator & 0xBFFF;
        }
    }

    public boolean containAdditionMsg() {
        return (indicator & 0x2000) != 0;
    }

    public void setContainAdditionMsg(boolean containAdditionMsg) {
        if (containAdditionMsg) {
            indicator = indicator | 0x2000;
        } else {
            indicator = indicator & 0xDFFF;
        }
    }

    public boolean containBaseStationMsg() {
        return (indicator & 0x1000) != 0;
    }

    public void setContainBaseStationMsg(boolean containBaseStationMsg) {
        if (containBaseStationMsg) {
            indicator = indicator | 0x1000;
        } else {
            indicator = indicator & 0xEFFF;
        }
    }

    public boolean containFaultMsg() {
        return (indicator & 0x0800) != 0;
    }

    public void setContainFaultMsg(boolean containFaultMsg) {
        if (containFaultMsg) {
            indicator = indicator | 0x0800;
        } else {
            indicator = indicator & 0xF7FF;
        }
    }

    public boolean containBatteryMsg() {
        return (indicator & 0x0400) != 0;
    }

    public void setContainBatteryMsg(boolean containBatteryMsg) {
        if (containBatteryMsg) {
            indicator = indicator | 0x0400;
        } else {
            indicator = indicator & 0xFBFF;
        }
    }

    public boolean containGSensorMsg() {
        return (indicator & 0x0200) != 0;
    }

    public void setContainGSensorMsg(boolean containGSensorMsg) {
        if (containGSensorMsg) {
            indicator = indicator | 0x0200;
        } else {
            indicator = indicator & 0xFDFF;
        }
    }

    public int getIndicator() {
        return indicator;
    }

    public void setIndicator(int indicator) {
        this.indicator = indicator;
    }
}
