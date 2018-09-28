package com.chleon.datacollection.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;
import com.chleon.datacollection.database.DatabaseHelper.*;
import com.chleon.telematics.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ryan Fan on 2016/3/8.
 */
public class DatabaseService {

    private static final String TAG = DatabaseService.class.getSimpleName();

    private static final boolean DBG = true;

    private DatabaseHelper mDbHelper;

    public DatabaseService(Context context) {
        mDbHelper = new DatabaseHelper(context);
    }

    public long insertCanMsgToDataBase(CanReqMsg canReqMsg) {
        long canMsgId = -1;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(CanMsg.DATA, CodecUtils.bytesToHexString(canReqMsg.getMsg()));
            values.put(CanMsg.TIMESTAMP, canReqMsg.getTimestamp());
            values.put(CanMsg.ELAPSED, canReqMsg.getElapsed());

            LocationMsg location = canReqMsg.getLocationMsg();

            values.put(Location.FIXED, location.isFixed() ? 1 : 0);
            values.put(Location.TIMESTAMP, location.getTimestamp());
            values.put(Location.LATITUDE, location.getLatitude());
            values.put(Location.LONGITUDE, location.getLongitude());
            values.put(Location.ALTITUDE, location.getAltitude());
            values.put(Location.BEARING, location.getBearing());
            values.put(Location.SPEED, location.getSpeed());
            values.put(Location.HDOP, location.getHdop());
            values.put(Location.VDOP, location.getVdop());
            values.put(Location.TDOP, location.getTdop());

            canMsgId = db.insert(CanMsg.TABLE_NAME, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        return canMsgId;
    }

    public long insertMsgToDataBase(SingleReportMsg msg, int isNew) {
        long msgId = -1;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            ContentValues values = new ContentValues();
            values.put(Msg.ISNEW, isNew);
            values.put(Msg.FLAG, msg.getFlag());
            values.put(Msg.REASON, msg.getReportReason().getValue());
            values.put(Msg.INDICATOR, msg.getIndicator());
            values.put(Msg.TIMESTAMP, msg.getTimestamp());
            values.put(Msg.ELAPSED, msg.getElapsed());
            long id = db.insert(Msg.TABLE_NAME, null, values);

            if (msg.containLocationMsg()) {
                values.clear();
                LocationMsg location = msg.getLocationMsg();
                values.put(Location.MSGID, id);
                values.put(Location.FIXED, location.isFixed() ? 1 : 0);
                values.put(Location.TIMESTAMP, location.getTimestamp());
                values.put(Location.LATITUDE, location.getLatitude());
                values.put(Location.LONGITUDE, location.getLongitude());
                values.put(Location.ALTITUDE, location.getAltitude());
                values.put(Location.BEARING, location.getBearing());
                values.put(Location.SPEED, location.getSpeed());
                values.put(Location.HDOP, location.getHdop());
                values.put(Location.VDOP, location.getVdop());
                values.put(Location.TDOP, location.getTdop());
                db.insert(Location.TABLE_NAME, null, values);
            }

            if (msg.containBaseStationMsg()) {
                values.clear();
                BaseStationMsg baseStationMsg = msg.getBaseStationMsg();
                values.put(BaseStation.MSGID, id);
                values.put(BaseStation.ATTACHED, baseStationMsg.isAttatched() ? 1 : 0);
                values.put(BaseStation.MNC, baseStationMsg.getMnc());
                values.put(BaseStation.SID, baseStationMsg.getSid());
                values.put(BaseStation.LAC, baseStationMsg.getLac());
                values.put(BaseStation.CID, baseStationMsg.getCid());
                values.put(BaseStation.SIGNAL, baseStationMsg.getSignal());
                db.insert(BaseStation.TABLE_NAME, null, values);
            }

            if (msg.containStatusMsg()) {
                values.clear();
                StatusMsg statusMsg = msg.getStatusMsg();
                values.put(Location.MSGID, id);
                values.put(Alarm.THEFT, statusMsg.isTheftAlarm() ? 1 : 0);
                values.put(Alarm.SOS, statusMsg.isSosAlarm() ? 1 : 0);
                values.put(Alarm.COLLISION, statusMsg.isCollisionAlarm() ? 1 : 0);
                values.put(Alarm.VIBRATION, statusMsg.isVibrationAlarm() ? 1 : 0);
                values.put(Alarm.FATIGUE_DRIVING, statusMsg.isFatigueDrivingAlarm() ? 1 : 0);
                values.put(Alarm.OVERTIME_PARKING, statusMsg.isOvertimeParkingAlarm() ? 1 : 0);
                values.put(Alarm.OVERSPEED, statusMsg.isOverSpeedAlarm() ? 1 : 0);
                values.put(Alarm.DECELERATION_X, statusMsg.isDecelerationXAlarm() ? 1 : 0);
                values.put(Alarm.ACCELERATION_X, statusMsg.isAccelerationXAlarm() ? 1 : 0);
                values.put(Alarm.DECELERATION_Y, statusMsg.isDecelerationYAlarm() ? 1 : 0);
                values.put(Alarm.ACCELERATION_Y, statusMsg.isAccelerationYAlarm() ? 1 : 0);
                values.put(Alarm.DECELERATION_Z, statusMsg.isDecelerationZAlarm() ? 1 : 0);
                values.put(Alarm.ACCELERATION_Z, statusMsg.isAccelerationZAlarm() ? 1 : 0);
                db.insert(Alarm.TABLE_NAME, null, values);
            }

            if (msg.containAdditionMsg()) {
                values.clear();
                AdditionMsg additionMsg = msg.getAdditionMsg();
                values.put(Location.MSGID, id);
                values.put(Status.RPM, additionMsg.getRpm());
                values.put(Status.SPEED, additionMsg.getSpeed());
                values.put(Status.INST_FUEL, additionMsg.getInstFuel());
                values.put(Status.AVG_FUEL, additionMsg.getAvgFuel());
                values.put(Status.INLET_TEMP, additionMsg.getInletTemp());
                values.put(Status.COOLANT_TEMP, additionMsg.getCoolantTemp());
                values.put(Status.FUEL_TRIM, additionMsg.getFuelTrim());
                values.put(Status.THROTTLE_POS, additionMsg.getThrottlePos());
                values.put(Status.REMAINING_FUEL, additionMsg.getRemainingFuel());
                values.put(Status.EN_MILEAGE, additionMsg.getEnMileage());
                values.put(Status.TOTAL_FUEL, additionMsg.getTotalFuel());
                values.put(Status.TOTAL_MILEAGE, additionMsg.getTotalMileage());
                db.insert(Status.TABLE_NAME, null, values);
            }

            if (msg.containFaultMsg()) {
                values.clear();
                FaultMsg faultMsg = msg.getFaultMsg();
                values.put(Location.MSGID, id);
                values.put(Fault.FAULT_CODE, faultMsg.toStrFaultCode());
                db.insert(Fault.TABLE_NAME, null, values);
            }

            if (msg.containBatteryMsg()) {
                values.clear();
                BatteryMsg batteryMsg = msg.getBatteryMsg();
                values.put(Location.MSGID, id);
                values.put(Electric.MOTOR_POWER, batteryMsg.getMotorPower());
                values.put(Electric.SOC, batteryMsg.getSoc());
                values.put(Electric.BAT_CHARGE_STATE, batteryMsg.getBatChargeState().ordinal());
                values.put(Electric.BAT_HEATING_STATE, batteryMsg.getBatHeatingState().ordinal());
                db.insert(Electric.TABLE_NAME, null, values);
            }

            if (msg.containGSensorMsg()) {
                values.clear();
                GSensorMsg gsensorMsg = msg.getGSensorMsg();
                values.put(GSensor.MSGID, id);
                values.put(GSensor.ACCURACY, gsensorMsg.getAccuracy());
                values.put(GSensor.X_VALUE, gsensorMsg.getxValue());
                values.put(GSensor.Y_VALUE, gsensorMsg.getyValue());
                values.put(GSensor.Z_VALUE, gsensorMsg.getzValue());
                db.insert(GSensor.TABLE_NAME, null, values);
            }

            db.setTransactionSuccessful();
            msgId = id;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        return msgId;
    }

    public void updateMsgsInDataBase(int isNew) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Msg.ISNEW, isNew);
        String whereClause = Msg.ISNEW + "<>" + isNew;
        db.update(Msg.TABLE_NAME, values, whereClause, null);
    }

    public void updateNewMsgsTimestampInDataBase(long timeInMillis) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long elapsed = SystemClock.elapsedRealtime();
        String sql = "update " + Msg.TABLE_NAME
                + " set " + Msg.TIMESTAMP + " = " + (timeInMillis - elapsed) + " + " + Msg.ELAPSED
                + " where " + Msg.ISNEW + " = 2";
        db.execSQL(sql);
    }

    public void deleteMsgFromDataBase() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(Msg.TABLE_NAME, null, null);
    }

    public void deleteMsgFromDataBase(long id) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String whereClause = "_id = " + id;
        db.delete(Msg.TABLE_NAME, whereClause, null);
    }

    public void deleteCanMsgFromDataBase(long id) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String whereClause = "_id = " + id;
        db.delete(CanMsg.TABLE_NAME, whereClause, null);
    }

    public void deleteMsgFromDataBase(List<Long> ids) {
        if (DBG) {
            MyLog.d(TAG, "deleteMsgFromDataBase begin");
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String whereClause = "_id IN (" + argsListToString(ids) + ")";
        db.delete(Msg.TABLE_NAME, whereClause, null);
        if (DBG) {
            MyLog.d(TAG, "deleteMsgFromDataBase end");
        }
    }

    private String argsListToString(List<Long> args) {
        StringBuilder argsBuilder = new StringBuilder();
        final int argsCount = args.size();
        for (int i = 0; i < argsCount; i++) {
            argsBuilder.append(args.get(i));
            if (i < argsCount - 1) {
                argsBuilder.append(",");
            }
        }
        return argsBuilder.toString();
    }

    public List<CanReqMsg> getNextReportCanMsg(List<Long> waitingIds) {
        MyLog.d(TAG, "getNextReportCanMsg...");
        List<CanReqMsg> canReqMsgList = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String whereClause = "_id NOT IN (" + argsListToString(waitingIds) + ")";
        Cursor cursor = db.query(CanMsg.TABLE_NAME, null, whereClause, null, null, null, null);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    CanReqMsg canReqMsg = new CanReqMsg();
                    long id = cursor.getLong(cursor.getColumnIndex(CanMsg.ID));
                    String data = cursor.getString(cursor.getColumnIndex(CanMsg.DATA));
                    long timestamp = cursor.getLong(cursor.getColumnIndex(CanMsg.TIMESTAMP));
                    canReqMsg.setMsg(CodecUtils.hexStringToBytes(data));
                    canReqMsg.setId(id);
                    canReqMsg.setTimestamp(timestamp);
                    canReqMsg.setShouldAck(false);

                    LocationMsg locationMsg = new LocationMsg();
                    int locationFixed = cursor.getInt(cursor.getColumnIndex(CanMsg.FIXED));
                    double locationLatitude = cursor.getDouble(cursor.getColumnIndex(CanMsg.LATITUDE));
                    double locationLongitude = cursor.getDouble(cursor.getColumnIndex(CanMsg.LONGITUDE));
                    double locationAltitude = cursor.getDouble(cursor.getColumnIndex(CanMsg.ALTITUDE));
                    float locationBearing = cursor.getFloat(cursor.getColumnIndex(CanMsg.BEARING));
                    float locationSpeed = cursor.getFloat(cursor.getColumnIndex(CanMsg.SPEED));
                    float locationHdop = cursor.getFloat(cursor.getColumnIndex(CanMsg.HDOP));
                    float locationVdop = cursor.getFloat(cursor.getColumnIndex(CanMsg.VDOP));
                    float locationTdop = cursor.getFloat(cursor.getColumnIndex(CanMsg.TDOP));

                    locationMsg.setFixed(locationFixed == 1);
                    locationMsg.setTimestamp(timestamp);
                    locationMsg.setLatitude(locationLatitude);
                    locationMsg.setLongitude(locationLongitude);
                    locationMsg.setAltitude(locationAltitude);
                    locationMsg.setBearing(locationBearing);
                    locationMsg.setSpeed(locationSpeed);
                    locationMsg.setHdop(locationHdop);
                    locationMsg.setVdop(locationVdop);
                    locationMsg.setTdop(locationTdop);

                    canReqMsg.setLocationMsg(locationMsg);

                    canReqMsgList.add(canReqMsg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }
        return canReqMsgList;
    }

    public IndicatorMsg getNextReportMsg(List<Long> waitingIds) {
        IndicatorMsg reportMsg = null;
        SingleReportMsg singleReportMsg = null;
        BatchReportMsg batchReportMsg = null;
        FaultMsg allFaultMsg = null;
        boolean jumpOutLoop = false;

        IndicatorMsg indicatorMsg = new IndicatorMsg();

        LocationMsg locationMsg = null;
        BaseStationMsg baseStationMsg = null;
        StatusMsg statusMsg = null;
        AdditionMsg additionMsg = null;
        BatteryMsg batteryMsg = null;
        FaultMsg faultMsg = null;
        GSensorMsg gsensorMsg = null;
        if (DBG) {
            MyLog.d(TAG, "getNextReportMsg begin");
        }

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String sql = "select temp.*, " + Location.COLUMNS + ", " + BaseStation.COLUMNS + ", " + Alarm.COLUMNS + ", " + Status.COLUMNS + ", " + Electric.COLUMNS + ", " + GSensor.COLUMNS + ", " + Fault.COLUMNS
                + " from ((select " + Msg.COLUMNS + " from " + Msg.TABLE_NAME + " where _id not in (" + argsListToString(waitingIds) + ") order by _id asc limit 0,255) as temp) "
                + " left join " + Location.TABLE_NAME + " on " + "temp._id = " + Location.TABLE_NAME + "." + Location.MSGID
                + " left join " + BaseStation.TABLE_NAME + " on " + "temp._id = " + BaseStation.TABLE_NAME + "." + BaseStation.MSGID
                + " left join " + Alarm.TABLE_NAME + " on " + "temp._id = " + Alarm.TABLE_NAME + "." + Alarm.MSGID
                + " left join " + Status.TABLE_NAME + " on " + "temp._id = " + Status.TABLE_NAME + "." + Status.MSGID
                + " left join " + Electric.TABLE_NAME + " on " + "temp._id = " + Electric.TABLE_NAME + "." + Electric.MSGID
                + " left join " + GSensor.TABLE_NAME + " on " + "temp._id = " + GSensor.TABLE_NAME + "." + GSensor.MSGID
                + " left join " + Fault.TABLE_NAME + " on " + "temp._id = " + Fault.TABLE_NAME + "." + Fault.MSGID;

        Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(cursor.getColumnIndex(Msg._ID));
                    int isNew = cursor.getInt(cursor.getColumnIndex(Msg.ISNEW));
                    short flag = cursor.getShort(cursor.getColumnIndex(Msg.FLAG));
                    short reason = cursor.getShort(cursor.getColumnIndex(Msg.REASON));
                    int indicator = cursor.getInt(cursor.getColumnIndex(Msg.INDICATOR));
                    long timestamp = cursor.getLong(cursor.getColumnIndex(Msg.TIMESTAMP));

                    indicatorMsg.setIndicator(indicator);

                    if (indicatorMsg.containLocationMsg()) {
                        int locationFixed = cursor.getInt(cursor.getColumnIndex(Location.TABLE_NAME_FIXED));
                        long locationTimestamp = cursor.getLong(cursor.getColumnIndex(Location.TABLE_NAME_TIMESTAMP));
                        double locationLatitude = cursor.getDouble(cursor.getColumnIndex(Location.TABLE_NAME_LATITUDE));
                        double locationLongitude = cursor.getDouble(cursor.getColumnIndex(Location.TABLE_NAME_LONGITUDE));
                        double locationAltitude = cursor.getDouble(cursor.getColumnIndex(Location.TABLE_NAME_ALTITUDE));
                        float locationBearing = cursor.getFloat(cursor.getColumnIndex(Location.TABLE_NAME_BEARING));
                        float locationSpeed = cursor.getFloat(cursor.getColumnIndex(Location.TABLE_NAME_SPEED));
                        float locationHdop = cursor.getFloat(cursor.getColumnIndex(Location.TABLE_NAME_HDOP));
                        float locationVdop = cursor.getFloat(cursor.getColumnIndex(Location.TABLE_NAME_VDOP));
                        float locationTdop = cursor.getFloat(cursor.getColumnIndex(Location.TABLE_NAME_TDOP));
                        locationMsg = new LocationMsg();
                        locationMsg.setFixed(locationFixed == 1);
                        locationMsg.setTimestamp(locationTimestamp);
                        locationMsg.setLatitude(locationLatitude);
                        locationMsg.setLongitude(locationLongitude);
                        locationMsg.setAltitude(locationAltitude);
                        locationMsg.setBearing(locationBearing);
                        locationMsg.setSpeed(locationSpeed);
                        locationMsg.setHdop(locationHdop);
                        locationMsg.setVdop(locationVdop);
                        locationMsg.setTdop(locationTdop);
                    }

                    if (indicatorMsg.containBaseStationMsg()) {
                        int baseStationAttached = cursor.getInt(cursor.getColumnIndex(BaseStation.TABLE_NAME_ATTACHED));
                        int baseStationMnc = cursor.getInt(cursor.getColumnIndex(BaseStation.TABLE_NAME_MNC));
                        int baseStationSid = cursor.getInt(cursor.getColumnIndex(BaseStation.TABLE_NAME_SID));
                        int baseStationLac = cursor.getInt(cursor.getColumnIndex(BaseStation.TABLE_NAME_LAC));
                        int baseStationCid = cursor.getInt(cursor.getColumnIndex(BaseStation.TABLE_NAME_CID));
                        int baseStationSignal = cursor.getInt(cursor.getColumnIndex(BaseStation.TABLE_NAME_SIGNAL));
                        baseStationMsg = new BaseStationMsg();
                        baseStationMsg.setAttatched(baseStationAttached == 1);
                        baseStationMsg.setMnc(baseStationMnc);
                        baseStationMsg.setSid(baseStationSid);
                        baseStationMsg.setLac(baseStationLac);
                        baseStationMsg.setCid(baseStationCid);
                        baseStationMsg.setSignal(baseStationSignal);
                    }

                    if (indicatorMsg.containStatusMsg()) {
                        int alarmTheftAlarm = cursor.getInt(cursor.getColumnIndex(Alarm.TABLE_NAME_THEFT));
                        int alarmSosAlarm = cursor.getInt(cursor.getColumnIndex(Alarm.TABLE_NAME_SOS));
                        int alarmCollisionAlarm = cursor.getInt(cursor.getColumnIndex(Alarm.TABLE_NAME_COLLISION));
                        int alarmVibrationAlarm = cursor.getInt(cursor.getColumnIndex(Alarm.TABLE_NAME_VIBRATION));
                        int alarmFatigueDrivingAlarm = cursor.getInt(cursor.getColumnIndex(Alarm.TABLE_NAME_FATIGUE_DRIVING));
                        int alarmOvertimeParkingAlarm = cursor.getInt(cursor.getColumnIndex(Alarm.TABLE_NAME_OVERTIME_PARKING));
                        int alarmOverspeedAlarm = cursor.getInt(cursor.getColumnIndex(Alarm.TABLE_NAME_OVERSPEED));
                        int alarmDecelerationXAlarm = cursor.getInt(cursor.getColumnIndex(Alarm.TABLE_NAME_DECELERATION_X));
                        int alarmAccelerationXAlarm = cursor.getInt(cursor.getColumnIndex(Alarm.TABLE_NAME_ACCELERATION_X));
                        int alarmDecelerationYAlarm = cursor.getInt(cursor.getColumnIndex(Alarm.TABLE_NAME_DECELERATION_Y));
                        int alarmAccelerationYAlarm = cursor.getInt(cursor.getColumnIndex(Alarm.TABLE_NAME_ACCELERATION_Y));
                        int alarmDecelerationZAlarm = cursor.getInt(cursor.getColumnIndex(Alarm.TABLE_NAME_DECELERATION_Z));
                        int alarmAccelerationZAlarm = cursor.getInt(cursor.getColumnIndex(Alarm.TABLE_NAME_ACCELERATION_Z));
                        statusMsg = new StatusMsg();
                        statusMsg.setTheftAlarm(alarmTheftAlarm == 1);
                        statusMsg.setSosAlarm(alarmSosAlarm == 1);
                        statusMsg.setCollisionAlarm(alarmCollisionAlarm == 1);
                        statusMsg.setVibrationAlarm(alarmVibrationAlarm == 1);
                        statusMsg.setFatigueDrivingAlarm(alarmFatigueDrivingAlarm == 1);
                        statusMsg.setOvertimeParkingAlarm(alarmOvertimeParkingAlarm == 1);
                        statusMsg.setOverSpeedAlarm(alarmOverspeedAlarm == 1);
                        statusMsg.setDecelerationXAlarm(alarmDecelerationXAlarm == 1);
                        statusMsg.setAccelerationXAlarm(alarmAccelerationXAlarm == 1);
                        statusMsg.setDecelerationYAlarm(alarmDecelerationYAlarm == 1);
                        statusMsg.setAccelerationYAlarm(alarmAccelerationYAlarm == 1);
                        statusMsg.setDecelerationZAlarm(alarmDecelerationZAlarm == 1);
                        statusMsg.setAccelerationZAlarm(alarmAccelerationZAlarm == 1);
                    }

                    if (indicatorMsg.containAdditionMsg()) {
                        int statusRpm = cursor.getInt(cursor.getColumnIndex(Status.TABLE_NAME_RPM));
                        int statusSpeed = cursor.getInt(cursor.getColumnIndex(Status.TABLE_NAME_SPEED));
                        float statusInstFuel = cursor.getFloat(cursor.getColumnIndex(Status.TABLE_NAME_INST_FUEL));
                        float statusAvgFuel = cursor.getFloat(cursor.getColumnIndex(Status.TABLE_NAME_AVG_FUEL));
                        float statusInletTemp = cursor.getFloat(cursor.getColumnIndex(Status.TABLE_NAME_INLET_TEMP));
                        float statusCoolantTemp = cursor.getFloat(cursor.getColumnIndex(Status.TABLE_NAME_COOLANT_TEMP));
                        int statusFuelTrim = cursor.getInt(cursor.getColumnIndex(Status.TABLE_NAME_FUEL_TRIM));
                        int statusThrottlePos = cursor.getInt(cursor.getColumnIndex(Status.TABLE_NAME_THROTTLE_POS));
                        int statusRemainingFuel = cursor.getInt(cursor.getColumnIndex(Status.TABLE_NAME_REMAINING_FUEL));
                        int statusEnMileage = cursor.getInt(cursor.getColumnIndex(Status.TABLE_NAME_EN_MILEAGE));
                        int statusTotalFuel = cursor.getInt(cursor.getColumnIndex(Status.TABLE_NAME_TOTAL_FUEL));
                        int statusTotalMileage = cursor.getInt(cursor.getColumnIndex(Status.TABLE_NAME_TOTAL_MILEAGE));
                        additionMsg = new AdditionMsg();
                        additionMsg.setRpm(statusRpm);
                        additionMsg.setSpeed(statusSpeed);
                        additionMsg.setInstFuel(statusInstFuel);
                        additionMsg.setAvgFuel(statusAvgFuel);
                        additionMsg.setInletTemp(statusInletTemp);
                        additionMsg.setCoolantTemp(statusCoolantTemp);
                        additionMsg.setFuelTrim(statusFuelTrim);
                        additionMsg.setThrottlePos(statusThrottlePos);
                        additionMsg.setRemainingFuel(statusRemainingFuel);
                        additionMsg.setEnMileage(statusEnMileage);
                        additionMsg.setTotalFuel(statusTotalFuel);
                        additionMsg.setTotalMileage(statusTotalMileage);
                    }

                    if (indicatorMsg.containBatteryMsg()) {
                        int electricMotorPower = cursor.getInt(cursor.getColumnIndex(Electric.TABLE_NAME_MOTOR_POWER));
                        int electricSoc = cursor.getInt(cursor.getColumnIndex(Electric.TABLE_NAME_SOC));
                        int electricBatChargeState = cursor.getInt(cursor.getColumnIndex(Electric.TABLE_NAME_BAT_CHARGE_STATE));
                        int electricBatHeatingState = cursor.getInt(cursor.getColumnIndex(Electric.TABLE_NAME_BAT_HEATING_STATE));
                        batteryMsg = new BatteryMsg();
                        batteryMsg.setMotorPower(electricMotorPower);
                        batteryMsg.setSoc(electricSoc);
                        if (electricBatChargeState < 0 || electricBatChargeState >= BatteryMsg.BatChargeState.values().length) {
                            electricBatChargeState = BatteryMsg.BatChargeState.VOID.ordinal();
                        }
                        if (electricBatHeatingState < 0 || electricBatHeatingState >= BatteryMsg.BatHeatingState.values().length) {
                            electricBatHeatingState = BatteryMsg.BatHeatingState.VOID.ordinal();
                        }
                        batteryMsg.setBatChargeState(BatteryMsg.BatChargeState.values()[electricBatChargeState]);
                        batteryMsg.setBatHeatingState(BatteryMsg.BatHeatingState.values()[electricBatHeatingState]);
                    }

                    if (indicatorMsg.containFaultMsg()) {
                        String strfaultCodes = cursor.getString(cursor.getColumnIndex(Fault.TABLE_NAME_FAULT_CODE));
                        faultMsg = new FaultMsg();
                        faultMsg.fromStrFaultCode(strfaultCodes);
                    }

                    if (indicatorMsg.containGSensorMsg()) {
                        int gsensorAccuracy = cursor.getInt(cursor.getColumnIndex(GSensor.TABLE_NAME_ACCURACY));
                        float gsensorXValue = cursor.getFloat(cursor.getColumnIndex(GSensor.TABLE_NAME_X_VALUE));
                        float gsensorYValue = cursor.getFloat(cursor.getColumnIndex(GSensor.TABLE_NAME_Y_VALUE));
                        float gsensorZValue = cursor.getFloat(cursor.getColumnIndex(GSensor.TABLE_NAME_Z_VALUE));
                        gsensorMsg = new GSensorMsg();
                        gsensorMsg.setAccuracy(gsensorAccuracy);
                        gsensorMsg.setxValue(gsensorXValue);
                        gsensorMsg.setyValue(gsensorYValue);
                        gsensorMsg.setzValue(gsensorZValue);
                    }

                    ReportReason reportReason = ReportReason.getReportReason(reason);

                    if (isNew != 1) {
                        switch (reportReason) {
                            case TIMER:
                                if (batchReportMsg == null) {
                                    batchReportMsg = new BatchReportMsg();
                                    batchReportMsg.setShouldAck(true);
                                    batchReportMsg.setReportReason(reportReason);
                                    batchReportMsg.setIndicator(indicator);

                                    allFaultMsg = new FaultMsg();
                                }
                                batchReportMsg.inCreaseCount();
                                batchReportMsg.addId(id);

                                batchReportMsg.addTimestamp(timestamp);
                                if (batchReportMsg.containLocationMsg()) {
                                    batchReportMsg.addLocationMsg(locationMsg);
                                }
                                if (batchReportMsg.containBaseStationMsg()) {
                                    batchReportMsg.addBaseStationMsg(baseStationMsg);
                                }
                                if (batchReportMsg.containStatusMsg()) {
                                    batchReportMsg.addStatusMsg(statusMsg);
                                }
                                if (batchReportMsg.containAdditionMsg()) {
                                    batchReportMsg.addAdditionMsg(additionMsg);
                                }
                                if (batchReportMsg.containBatteryMsg()) {
                                    batchReportMsg.addBatteryMsg(batteryMsg);
                                }
                                if (batchReportMsg.containFaultMsg()) {
                                    allFaultMsg.merge(faultMsg);
                                }

                                if (batchReportMsg.reachMaxCount() || cursor.isLast()) {
                                    jumpOutLoop = true;
                                    batchReportMsg.setFaultMsg(faultMsg);
                                    reportMsg = batchReportMsg;
                                }
                                break;
                            default:
                                jumpOutLoop = true;
                                if (batchReportMsg != null) {
                                    batchReportMsg.setFaultMsg(faultMsg);
                                    reportMsg = batchReportMsg;
                                } else {
                                    singleReportMsg = new SingleReportMsg();
                                    singleReportMsg.setId(id);
                                    singleReportMsg.setFlag(flag);
                                    singleReportMsg.setDelayReport(isNew == 1 ? false : true);
                                    singleReportMsg.setReportReason(reportReason);
                                    singleReportMsg.setTimestamp(timestamp);
                                    singleReportMsg.setIndicator(indicator);
                                    if (singleReportMsg.containLocationMsg()) {
                                        singleReportMsg.setLocationMsg(locationMsg);
                                    }
                                    if (singleReportMsg.containBaseStationMsg()) {
                                        singleReportMsg.setBaseStationMsg(baseStationMsg);
                                    }
                                    if (singleReportMsg.containStatusMsg()) {
                                        singleReportMsg.setStatusMsg(statusMsg);
                                    }
                                    if (singleReportMsg.containAdditionMsg()) {
                                        singleReportMsg.setAdditionMsg(additionMsg);
                                    }
                                    if (singleReportMsg.containFaultMsg()) {
                                        singleReportMsg.setFaultMsg(faultMsg);
                                    }
                                    if (singleReportMsg.containBatteryMsg()) {
                                        singleReportMsg.setBatteryMsg(batteryMsg);
                                    }
                                    if (singleReportMsg.containGSensorMsg()) {
                                        singleReportMsg.setGSensorMsg(gsensorMsg);
                                    }

                                    reportMsg = singleReportMsg;
                                }
                                break;
                        }
                    } else {
                        jumpOutLoop = true;
                        if (batchReportMsg != null) {
                            batchReportMsg.setFaultMsg(faultMsg);
                            reportMsg = batchReportMsg;
                        } else {
                            singleReportMsg = new SingleReportMsg();
                            singleReportMsg.setId(id);
                            singleReportMsg.setFlag(flag);
                            singleReportMsg.setDelayReport(isNew == 1 ? false : true);
                            singleReportMsg.setReportReason(reportReason);
                            singleReportMsg.setTimestamp(timestamp);
                            singleReportMsg.setIndicator(indicator);
                            if (singleReportMsg.containLocationMsg()) {
                                singleReportMsg.setLocationMsg(locationMsg);
                            }
                            if (singleReportMsg.containBaseStationMsg()) {
                                singleReportMsg.setBaseStationMsg(baseStationMsg);
                            }
                            if (singleReportMsg.containStatusMsg()) {
                                singleReportMsg.setStatusMsg(statusMsg);
                            }
                            if (singleReportMsg.containAdditionMsg()) {
                                singleReportMsg.setAdditionMsg(additionMsg);
                            }
                            if (singleReportMsg.containFaultMsg()) {
                                singleReportMsg.setFaultMsg(faultMsg);
                            }
                            if (singleReportMsg.containBatteryMsg()) {
                                singleReportMsg.setBatteryMsg(batteryMsg);
                            }
                            if (singleReportMsg.containGSensorMsg()) {
                                singleReportMsg.setGSensorMsg(gsensorMsg);
                            }

                            reportMsg = singleReportMsg;
                        }
                    }
                    if (jumpOutLoop) {
                        break;
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }

        if (DBG) {
            MyLog.d(TAG, "getNextReportMsg end");
        }

        return reportMsg;
    }

    public int countCanMsg(List<Long> waitingIds) {
        int count = 0;
        if (DBG) {
            MyLog.d(TAG, "countCanMsgs begin");
        }
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String whereClause = "_id NOT IN (" + argsListToString(waitingIds) + ")";
        Cursor cursor = db.query(CanMsg.TABLE_NAME, new String[]{"count(*)"}, whereClause, null, null, null, null);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    count = cursor.getInt(0);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }
        if (DBG) {
            MyLog.d(TAG, "countCanMsgs end count = " + count);
        }
        return count;
    }

    public int countMsgs(List<Long> waitingIds) {
        int count = 0;
        if (DBG) {
            MyLog.d(TAG, "countMsgs begin");
        }
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String whereClause = "_id NOT IN (" + argsListToString(waitingIds) + ")";
        Cursor cursor = db.query(Msg.TABLE_NAME, new String[]{"count(*)"}, whereClause, null, null, null, null);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    count = cursor.getInt(0);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }
        if (DBG) {
            MyLog.d(TAG, "countMsgs end count = " + count);
        }
        return count;
    }

    public void putString(String name, String value) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Config.NANE, name);
        values.put(Config.VALUE, value);
        db.insert(Config.TABLE_NAME, null, values);
    }

    public String getString(String name) {
        String value = null;
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String whereClause = Config.NANE + "='" + name + "'";
        Cursor cursor = db.query(Config.TABLE_NAME, new String[]{Config.VALUE}, whereClause, null, null, null, null);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    value = cursor.getString(cursor.getColumnIndex(Config.VALUE));
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }
        return value;
    }

    public void putInt(String name, int value) {
        putString(name, String.valueOf(value));
    }

    public int getInt(String name, int defValue) {
        String value = getString(name);
        try {
            return value != null ? Integer.parseInt(value) : defValue;
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    public void close() {
        mDbHelper.close();
    }

}
