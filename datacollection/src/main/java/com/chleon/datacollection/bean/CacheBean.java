package com.chleon.datacollection.bean;

import android.location.Location;
import android.telephony.CellLocation;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.text.TextUtils;

import com.chleon.telematics.*;

import kallaite.com.util.VehicleInfo;

import java.util.Map;

/**
 * Created by Ryan Fan on 2016/3/16.
 */
public class CacheBean {
    private static final boolean DBG = false;
    private static final boolean ENABLE_MULTI_WRITE_ON_DB = false;

    private int mAccStatus;

    private LocationMsg mLocationMsg;
    private BaseStationMsg mBaseStationMsg;
    private StatusMsg mStatusMsg;
    private AdditionMsg mAdditionMsg;
    private BatteryMsg mBatteryMsg;
    private GSensorMsg mGSensorMsg;
    private FaultMsg mFaultMsg;

    public CacheBean() {
        mAccStatus = 0;

        mLocationMsg = new LocationMsg();
        mBaseStationMsg = new BaseStationMsg();
        mStatusMsg = new StatusMsg();
        mAdditionMsg = new AdditionMsg();
        mAdditionMsg.setFuelTrim(-1);
        mAdditionMsg.setThrottlePos(-1);
        mBatteryMsg = new BatteryMsg();
        mGSensorMsg = new GSensorMsg();
        mFaultMsg = new FaultMsg();

        if (DBG) {
            mLocationMsg.setFixed(true);
            mLocationMsg.setTimestamp(1507424123862l);
            mLocationMsg.setBearing(200);
            mLocationMsg.setSpeed(20);
            mLocationMsg.setLatitude(39.5500000);
            mLocationMsg.setLongitude(116.2400000);
            mLocationMsg.setAltitude(100);

            mBaseStationMsg.setAttatched(true);
            mBaseStationMsg.setMnc(00);
            mBaseStationMsg.setSid(0000);
            mBaseStationMsg.setLac(02);
            mBaseStationMsg.setCid(65535);
            mBaseStationMsg.setSignal(31);

            mStatusMsg.setTheftAlarm(true);
            mStatusMsg.setSosAlarm(true);
            mStatusMsg.setCollisionAlarm(true);
            mStatusMsg.setVibrationAlarm(true);
            mStatusMsg.setFatigueDrivingAlarm(true);
            mStatusMsg.setOvertimeParkingAlarm(true);
            mStatusMsg.setOverSpeedAlarm(true);
            mStatusMsg.setDecelerationXAlarm(true);
            mStatusMsg.setAccelerationXAlarm(true);

            mAdditionMsg.setRpm(1000);
            mAdditionMsg.setSpeed(72);
            mAdditionMsg.setInstFuel(9);
            mAdditionMsg.setAvgFuel(20);
            mAdditionMsg.setInletTemp(20);
            mAdditionMsg.setCoolantTemp(40);
            mAdditionMsg.setFuelTrim(0);
            mAdditionMsg.setThrottlePos(0);
            mAdditionMsg.setRemainingFuel(20);
            mAdditionMsg.setEnMileage(500);
            mAdditionMsg.setTotalFuel(800);
            mAdditionMsg.setTotalMileage(1000);

            mBatteryMsg.setMotorPower(100);
            mBatteryMsg.setSoc(50);
            mBatteryMsg.setBatChargeState(BatteryMsg.BatChargeState.DISCHARGE);
            mBatteryMsg.setBatHeatingState(BatteryMsg.BatHeatingState.NOT_HEATING);

            mGSensorMsg.setAccuracy(2);
            mGSensorMsg.setxValue(10.123f);
            mGSensorMsg.setyValue(20.345f);
            mGSensorMsg.setzValue(30.456f);

            mFaultMsg.addFaultCode("A001");
        }
    }

    public int getAccStatus() {
        return mAccStatus;
    }

    public boolean updateAccStatus(int accStatus) {
        if (mAccStatus != accStatus) {
            mAccStatus = accStatus;
            return true;
        } else {
            return false;
        }
    }

    public void setGpsFixed(boolean gpsFixed) {
        mLocationMsg.setFixed(gpsFixed);
    }

    public LocationMsg getLocationMsg() {
        if (ENABLE_MULTI_WRITE_ON_DB) {
            return mLocationMsg;
        } else {
            return new LocationMsg(mLocationMsg);
        }
    }

    public void updateLocationMsg(Location location) {
        if (!DBG) {
            this.mLocationMsg.setLocation(location);
        }
    }

    public void updateNmea(float hdop, float vdop, float tdop) {
        if (!DBG) {
            this.mLocationMsg.setNmea(hdop, vdop, tdop);
        }
    }

    public void setHasIccCard(boolean hasIccCard) {
        mBaseStationMsg.setHasIccCard(hasIccCard);
    }

    public BaseStationMsg getBaseStationMsg() {
        if (ENABLE_MULTI_WRITE_ON_DB) {
            return mBaseStationMsg;
        } else {
            return new BaseStationMsg(mBaseStationMsg);
        }
    }

    public void updateBaseStationMsg(ServiceState serviceState, CellLocation cellLocation, SignalStrength signalStrength) {
        if (serviceState != null) {
            mBaseStationMsg.setServiceState(serviceState);
        }
        if (cellLocation != null) {
            mBaseStationMsg.setCellLocation(cellLocation);
        }
        if (signalStrength != null) {
            mBaseStationMsg.setSignalStrength(signalStrength);
        }
    }

    public StatusMsg getStatusMsg() {
        if (ENABLE_MULTI_WRITE_ON_DB) {
            return mStatusMsg;
        } else {
            return new StatusMsg(mStatusMsg);
        }
    }

    public boolean updateStatusMsg(AlarmType alarmType, boolean alarmValue) {
        boolean updated = false;
        switch (alarmType) {
            case THEFT:
                if (mStatusMsg.isTheftAlarm() != alarmValue) {
                    mStatusMsg.setTheftAlarm(alarmValue);
                    updated = true;
                }
                break;
            case SOS:
                if (mStatusMsg.isSosAlarm() != alarmValue) {
                    mStatusMsg.setSosAlarm(alarmValue);
                    updated = true;
                }
                break;
            case COLLISION:
                if (mStatusMsg.isCollisionAlarm() != alarmValue) {
                    mStatusMsg.setCollisionAlarm(alarmValue);
                    updated = true;
                }
                break;
            case VIBRATION:
                if (mStatusMsg.isVibrationAlarm() != alarmValue) {
                    mStatusMsg.setVibrationAlarm(alarmValue);
                    updated = true;
                }
                break;
            case FATIGUE_DRIVING:
                if (mStatusMsg.isFatigueDrivingAlarm() != alarmValue) {
                    mStatusMsg.setFatigueDrivingAlarm(alarmValue);
                    updated = true;
                }
                break;
            case OVERTIME_PARKING:
                if (mStatusMsg.isOvertimeParkingAlarm() != alarmValue) {
                    mStatusMsg.setOvertimeParkingAlarm(alarmValue);
                    updated = true;
                }
                break;
            case OVERSPEED:
                if (mStatusMsg.isOverSpeedAlarm() != alarmValue) {
                    mStatusMsg.setOverSpeedAlarm(alarmValue);
                    updated = true;
                }
                break;
            case DECELERATION_X:
                if (mStatusMsg.isDecelerationXAlarm() != alarmValue) {
                    mStatusMsg.setDecelerationXAlarm(alarmValue);
                    updated = true;
                }
                break;
            case ACCELERATION_X:
                if (mStatusMsg.isAccelerationXAlarm() != alarmValue) {
                    mStatusMsg.setAccelerationXAlarm(alarmValue);
                    updated = true;
                }
                break;
            case DECELERATION_Y:
                if (mStatusMsg.isDecelerationYAlarm() != alarmValue) {
                    mStatusMsg.setDecelerationYAlarm(alarmValue);
                    updated = true;
                }
                break;
            case ACCELERATION_Y:
                if (mStatusMsg.isAccelerationYAlarm() != alarmValue) {
                    mStatusMsg.setAccelerationYAlarm(alarmValue);
                    updated = true;
                }
                break;
            case DECELERATION_Z:
                if (mStatusMsg.isDecelerationZAlarm() != alarmValue) {
                    mStatusMsg.setDecelerationZAlarm(alarmValue);
                    updated = true;
                }
                break;
            case ACCELERATION_Z:
                if (mStatusMsg.isAccelerationZAlarm() != alarmValue) {
                    mStatusMsg.setAccelerationZAlarm(alarmValue);
                    updated = true;
                }
                break;
            default:
                break;
        }
        return updated;
    }

    public AdditionMsg getAdditionMsg() {
        if (ENABLE_MULTI_WRITE_ON_DB) {
            return mAdditionMsg;
        } else {
            return new AdditionMsg(mAdditionMsg);
        }
    }

    public boolean updateAdditionMsg(VehicleInfo vehicleInfo) {
        boolean updated = false;
        if (!DBG) {
            float avgOilConsumption = vehicleInfo.getAverageOilConsumption();
            if (avgOilConsumption != mAdditionMsg.getAvgFuel()) {
                mAdditionMsg.setAvgFuel(avgOilConsumption);
                updated = true;
            }
            int carSpeed = vehicleInfo.getCarSpeed();
            if (carSpeed != mAdditionMsg.getSpeed()) {
                mAdditionMsg.setSpeed(carSpeed);
                updated = true;
            }
            int engineWheel = vehicleInfo.getEngineWheel();
            if (engineWheel != mAdditionMsg.getRpm()) {
                mAdditionMsg.setRpm(engineWheel);
                updated = true;
            }
            int fuelLevel = vehicleInfo.getFuelLevel();
            if (fuelLevel != mAdditionMsg.getRemainingFuel()) {
                mAdditionMsg.setRemainingFuel(fuelLevel);
                updated = true;
            }
            float inletTemp = vehicleInfo.getInletTemperature();
            if (inletTemp != mAdditionMsg.getInletTemp()) {
                mAdditionMsg.setInletTemp(inletTemp);
                updated = true;
            }
            float instOilConsumption = vehicleInfo.getInstantaneousOilConsumption();
            if (instOilConsumption != mAdditionMsg.getInstFuel()) {
                mAdditionMsg.setInstFuel(instOilConsumption);
                updated = true;
            }
            int totalOilConsumption = (int) vehicleInfo.getTotalFuelConsumption();
            if (totalOilConsumption != mAdditionMsg.getTotalFuel()) {
                mAdditionMsg.setTotalFuel(totalOilConsumption);
                updated = true;
            }
            int totalMileage = vehicleInfo.getTotalMileage();
            if (totalMileage != mAdditionMsg.getTotalMileage()) {
                mAdditionMsg.setTotalMileage(totalMileage);
                updated = true;
            }
            int travelMileage = vehicleInfo.getTravelMileage();
            if (travelMileage != mAdditionMsg.getEnMileage()) {
                mAdditionMsg.setEnMileage(travelMileage);
                updated = true;
            }
            float waterTemp = vehicleInfo.getWaterTemperature();
            if (waterTemp != mAdditionMsg.getCoolantTemp()) {
                mAdditionMsg.setCoolantTemp(waterTemp);
                updated = true;
            }
        }
        return updated;
    }

    public BatteryMsg getBatteryMsg() {
        if (ENABLE_MULTI_WRITE_ON_DB) {
            return mBatteryMsg;
        } else {
            return new BatteryMsg(mBatteryMsg);
        }
    }

    public boolean updateBatteryMsg() {
        return false;
    }

    public GSensorMsg getGSensorMsg() {
        if (ENABLE_MULTI_WRITE_ON_DB) {
            return mGSensorMsg;
        } else {
            return new GSensorMsg(mGSensorMsg);
        }
    }

    public void updateGSensorMsg(int accuracy, float xValue, float yValue, float zValue) {
        if (!DBG) {
            mGSensorMsg.setAccuracy(accuracy);
            mGSensorMsg.setxValue(xValue);
            mGSensorMsg.setyValue(yValue);
            mGSensorMsg.setzValue(zValue);
        }
    }

    public FaultMsg getFaultMsg() {
        if (ENABLE_MULTI_WRITE_ON_DB) {
            return mFaultMsg;
        } else {
            return new FaultMsg(mFaultMsg);
        }
    }

    public boolean updateFaultMsg(String strFaultCode) {
        boolean updated = false;
        if (!TextUtils.isEmpty(strFaultCode)) {
            String[] strFaultCodes = strFaultCode.split(" ");
            Map<String, Boolean> faultCodes = mFaultMsg.getFaultCodes();
            boolean found;
            for (String curFaultCode : faultCodes.keySet()) {
                found = false;
                for (String faultCode : strFaultCodes) {
                    if (TextUtils.equals(curFaultCode, faultCode)) {
                        found = true;
                    }
                }
                if (!found) {
                    faultCodes.remove(curFaultCode);
                }
            }
            for (String faultCode : strFaultCodes) {
                if (!faultCodes.containsKey(faultCode)) {
                    faultCodes.put(faultCode, true);
                    updated = true;
                }
            }
        } else {
            mFaultMsg.clearFaultCodes();
        }
        return updated;
    }
}
