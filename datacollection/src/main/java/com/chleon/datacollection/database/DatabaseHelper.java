package com.chleon.datacollection.database;

import android.content.*;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import com.chleon.telematics.MyLog;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "datacollection.db";
    private static final int DATABASE_VERSION = 3;

    public static final class Config implements BaseColumns {
        private Config() {
        }

        public static final String TABLE_NAME = "config";

        public static final String NANE = "name";
        public static final String VALUE = "value";

        private static final String SQL_CREATE_TABLE =
                "create table " + TABLE_NAME + " (_id integer primary key autoincrement, "
                        + NANE + " text unique on conflict replace, "
                        + VALUE + " text);";

        private static final String SQL_DROP_TABLE =
                "drop table if exists " + TABLE_NAME;
    }

    public static final class Msg implements BaseColumns {
        private Msg() {
        }

        public static final String TABLE_NAME = "msg";

        public static final String ISNEW = "isnew";
        public static final String FLAG = "flag";
        public static final String REASON = "reason";
        public static final String INDICATOR = "indicator";
        public static final String TIMESTAMP = "timestamp";
        public static final String ELAPSED = "elapsed";

        public static final String COLUMNS = TABLE_NAME + ".*";

        private static final String SQL_CREATE_TABLE =
                "create table " + TABLE_NAME + " (_id integer primary key autoincrement, "
                        + ISNEW + " integer, "
                        + FLAG + " integer, "
                        + REASON + " integer, "
                        + INDICATOR + " integer, "
                        + TIMESTAMP + " integer, "
                        + ELAPSED + " integer);";

        private static final int MAX_COUNT = 5000;
        private static final String SQL_CREATE_TRIGGER =
                "CREATE TRIGGER delete_till_" + MAX_COUNT + " AFTER INSERT"
                        + " ON " + TABLE_NAME + " WHEN (select count(*) from " + TABLE_NAME + ") > " + MAX_COUNT
                        + " BEGIN"
                        + " DELETE FROM " + TABLE_NAME + " WHERE _id IN (SELECT _id FROM " + TABLE_NAME + " ORDER BY _id limit (select count(*) - " + MAX_COUNT + " from " + TABLE_NAME + "));"
                        + " END;";

        private static final String SQL_DROP_TABLE =
                "drop table if exists " + TABLE_NAME;

        private static final String SQL_DROP_TRIGGER =
                "DROP TRIGGER delete_till_" + MAX_COUNT;
    }

    public static final class CanMsg implements BaseColumns {
        private CanMsg() {
        }

        public static final String TABLE_NAME = "can_msg";

        public static final String ID = "_id";

        public static final String TIMESTAMP = "timestamp";

        public static final String ELAPSED = "elapsed";

        public static final String DATA = "data";

        public static final String FIXED = "fixed";

        public static final String LATITUDE = "latitude";

        public static final String LONGITUDE = "longitude";

        public static final String ALTITUDE = "altitude";

        public static final String BEARING = "bearing";

        public static final String SPEED = "speed";

        public static final String HDOP = "hdop";

        public static final String VDOP = "vdop";

        public static final String TDOP = "tdop";

        public static final String COLUMNS = TABLE_NAME + ".*";

        private static final String SQL_CREATE_TABLE =
                "create table " + TABLE_NAME + " (_id integer primary key autoincrement, "
                        + TIMESTAMP + " integer, "
                        + DATA + " text, "
                        + FIXED + " integer, "
                        + LATITUDE + " real, "
                        + LONGITUDE + " real, "
                        + ALTITUDE + " real, "
                        + BEARING + " real, "
                        + SPEED + " real, "
                        + HDOP + " real, "
                        + VDOP + " real, "
                        + TDOP + " real, "
                        + ELAPSED + " integer);";

        private static final int MAX_COUNT = 500;

        private static final String SQL_CREATE_TRIGGER =
                "CREATE TRIGGER delete_till_" + MAX_COUNT + " AFTER INSERT"
                        + " ON " + TABLE_NAME + " WHEN (select count(*) from " + TABLE_NAME + ") > " + MAX_COUNT
                        + " BEGIN"
                        + " DELETE FROM " + TABLE_NAME + " WHERE _id IN (SELECT _id FROM " + TABLE_NAME + " ORDER BY _id limit (select count(*) - " + MAX_COUNT + " from " + TABLE_NAME + "));"
                        + " END;";

        private static final String SQL_DROP_TABLE =
                "drop table if exists " + TABLE_NAME;

        private static final String SQL_DROP_TRIGGER =
                "DROP TRIGGER delete_till_" + MAX_COUNT;
    }

    public static class CustomBaseColumns implements BaseColumns {
        private CustomBaseColumns() {
        }

        public static final String MSGID = "msgId";
    }

    public static final class Location extends CustomBaseColumns {
        private Location() {
        }

        public static final String TABLE_NAME = "location";

        public static final String FIXED = "fixed";
        public static final String TIMESTAMP = "timestamp";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String ALTITUDE = "altitude";
        public static final String BEARING = "bearing";
        public static final String SPEED = "speed";
        public static final String HDOP = "hdop";
        public static final String VDOP = "vdop";
        public static final String TDOP = "tdop";

        public static final String TABLE_NAME_TIMESTAMP = TABLE_NAME + TIMESTAMP;
        public static final String TABLE_NAME_FIXED = TABLE_NAME + FIXED;
        public static final String TABLE_NAME_LATITUDE = TABLE_NAME + LATITUDE;
        public static final String TABLE_NAME_LONGITUDE = TABLE_NAME + LONGITUDE;
        public static final String TABLE_NAME_ALTITUDE = TABLE_NAME + ALTITUDE;
        public static final String TABLE_NAME_BEARING = TABLE_NAME + BEARING;
        public static final String TABLE_NAME_SPEED = TABLE_NAME + SPEED;
        public static final String TABLE_NAME_HDOP = TABLE_NAME + HDOP;
        public static final String TABLE_NAME_VDOP = TABLE_NAME + VDOP;
        public static final String TABLE_NAME_TDOP = TABLE_NAME + TDOP;

        public static final String COLUMNS = TABLE_NAME + "." + FIXED + " as " + TABLE_NAME_FIXED + ", "
                + TABLE_NAME + "." + TIMESTAMP + " as " + TABLE_NAME_TIMESTAMP + ", "
                + TABLE_NAME + "." + LATITUDE + " as " + TABLE_NAME_LATITUDE + ", "
                + TABLE_NAME + "." + LONGITUDE + " as " + TABLE_NAME_LONGITUDE + ", "
                + TABLE_NAME + "." + ALTITUDE + " as " + TABLE_NAME_ALTITUDE + ", "
                + TABLE_NAME + "." + BEARING + " as " + TABLE_NAME_BEARING + ", "
                + TABLE_NAME + "." + SPEED + " as " + TABLE_NAME_SPEED + ", "
                + TABLE_NAME + "." + HDOP + " as " + TABLE_NAME_HDOP + ", "
                + TABLE_NAME + "." + VDOP + " as " + TABLE_NAME_VDOP + ", "
                + TABLE_NAME + "." + TDOP + " as " + TABLE_NAME_TDOP;

        private static final String SQL_CREATE_TABLE =
                "create table " + TABLE_NAME + " (_id integer primary key autoincrement, "
                        + MSGID + " integer, "
                        + FIXED + " integer, "
                        + TIMESTAMP + " integer, "
                        + LATITUDE + " real, "
                        + LONGITUDE + " real, "
                        + ALTITUDE + " real, "
                        + BEARING + " real, "
                        + SPEED + " real, "
                        + HDOP + " real, "
                        + VDOP + " real, "
                        + TDOP + " real, "
                        + "FOREIGN KEY(" + MSGID + ") REFERENCES " + Msg.TABLE_NAME + "(_id));";

        private static final String SQL_CREATE_TRIGGER =
                "CREATE TRIGGER delete_" + TABLE_NAME + "item BEFORE DELETE"
                        + " ON " + Msg.TABLE_NAME
                        + " BEGIN"
                        + " DELETE FROM " + TABLE_NAME + " WHERE " + MSGID + " = old._id;"
                        + " END;";

        private static final String SQL_DROP_TABLE =
                "drop table if exists " + TABLE_NAME;

        private static final String SQL_DROP_TRIGGER =
                "DROP TRIGGER delete_" + TABLE_NAME + "item";
    }

    public static final class BaseStation extends CustomBaseColumns {
        private BaseStation() {
        }

        public static final String TABLE_NAME = "baseStation";

        public static final String ATTACHED = "attached";
        public static final String MNC = "mnc";
        public static final String SID = "sid";
        public static final String LAC = "lac";
        public static final String CID = "cid";
        public static final String SIGNAL = "signal";

        public static final String TABLE_NAME_ATTACHED = TABLE_NAME + ATTACHED;
        public static final String TABLE_NAME_MNC = TABLE_NAME + MNC;
        public static final String TABLE_NAME_SID = TABLE_NAME + SID;
        public static final String TABLE_NAME_LAC = TABLE_NAME + LAC;
        public static final String TABLE_NAME_CID = TABLE_NAME + CID;
        public static final String TABLE_NAME_SIGNAL = TABLE_NAME + SIGNAL;

        public static final String COLUMNS = TABLE_NAME + "." + ATTACHED + " as " + TABLE_NAME_ATTACHED + ", "
                + TABLE_NAME + "." + MNC + " as " + TABLE_NAME_MNC + ", "
                + TABLE_NAME + "." + SID + " as " + TABLE_NAME_SID + ", "
                + TABLE_NAME + "." + LAC + " as " + TABLE_NAME_LAC + ", "
                + TABLE_NAME + "." + CID + " as " + TABLE_NAME_CID + ", "
                + TABLE_NAME + "." + SIGNAL + " as " + TABLE_NAME_SIGNAL;

        private static final String SQL_CREATE_TABLE =
                "create table " + TABLE_NAME + " (_id integer primary key autoincrement, "
                        + MSGID + " integer, "
                        + ATTACHED + " integer, "
                        + MNC + " integer, "
                        + SID + " integer, "
                        + LAC + " integer, "
                        + CID + " integer, "
                        + SIGNAL + " integer, "
                        + "FOREIGN KEY(" + MSGID + ") REFERENCES " + Msg.TABLE_NAME + "(_id));";

        private static final String SQL_CREATE_TRIGGER =
                "CREATE TRIGGER delete_" + TABLE_NAME + "item BEFORE DELETE"
                        + " ON " + Msg.TABLE_NAME
                        + " BEGIN"
                        + " DELETE FROM " + TABLE_NAME + " WHERE " + MSGID + " = old._id;"
                        + " END;";

        private static final String SQL_DROP_TABLE =
                "drop table if exists " + TABLE_NAME;

        private static final String SQL_DROP_TRIGGER =
                "DROP TRIGGER delete_" + TABLE_NAME + "item";
    }

    public static final class Alarm extends CustomBaseColumns {
        private Alarm() {
        }

        public static final String TABLE_NAME = "alarm";

        public static final String THEFT = "theft";
        public static final String SOS = "sos";
        public static final String COLLISION = "collision";
        public static final String VIBRATION = "vibration";
        public static final String OVERSPEED = "overspeed";
        public static final String FATIGUE_DRIVING = "fatigueDriving";
        public static final String OVERTIME_PARKING = "overtimeParking";
        public static final String DECELERATION_X = "decelerationX";
        public static final String ACCELERATION_X = "accelerationX";
        public static final String DECELERATION_Y = "decelerationY";
        public static final String ACCELERATION_Y = "accelerationY";
        public static final String DECELERATION_Z = "decelerationZ";
        public static final String ACCELERATION_Z = "accelerationZ";

        public static final String TABLE_NAME_THEFT = TABLE_NAME + THEFT;
        public static final String TABLE_NAME_SOS = TABLE_NAME + SOS;
        public static final String TABLE_NAME_COLLISION = TABLE_NAME + COLLISION;
        public static final String TABLE_NAME_VIBRATION = TABLE_NAME + VIBRATION;
        public static final String TABLE_NAME_OVERSPEED = TABLE_NAME + OVERSPEED;
        public static final String TABLE_NAME_FATIGUE_DRIVING = TABLE_NAME + FATIGUE_DRIVING;
        public static final String TABLE_NAME_OVERTIME_PARKING = TABLE_NAME + OVERTIME_PARKING;
        public static final String TABLE_NAME_DECELERATION_X = TABLE_NAME + DECELERATION_X;
        public static final String TABLE_NAME_ACCELERATION_X = TABLE_NAME + ACCELERATION_X;
        public static final String TABLE_NAME_DECELERATION_Y = TABLE_NAME + DECELERATION_Y;
        public static final String TABLE_NAME_ACCELERATION_Y = TABLE_NAME + ACCELERATION_Y;
        public static final String TABLE_NAME_DECELERATION_Z = TABLE_NAME + DECELERATION_Z;
        public static final String TABLE_NAME_ACCELERATION_Z = TABLE_NAME + ACCELERATION_Z;

        public static final String COLUMNS = TABLE_NAME + "." + THEFT + " as " + TABLE_NAME_THEFT + ", "
                + TABLE_NAME + "." + SOS + " as " + TABLE_NAME_SOS + ", "
                + TABLE_NAME + "." + COLLISION + " as " + TABLE_NAME_COLLISION + ", "
                + TABLE_NAME + "." + VIBRATION + " as " + TABLE_NAME_VIBRATION + ", "
                + TABLE_NAME + "." + OVERSPEED + " as " + TABLE_NAME_OVERSPEED + ", "
                + TABLE_NAME + "." + FATIGUE_DRIVING + " as " + TABLE_NAME_FATIGUE_DRIVING + ", "
                + TABLE_NAME + "." + OVERTIME_PARKING + " as " + TABLE_NAME_OVERTIME_PARKING + ", "
                + TABLE_NAME + "." + DECELERATION_X + " as " + TABLE_NAME_DECELERATION_X + ", "
                + TABLE_NAME + "." + ACCELERATION_X + " as " + TABLE_NAME_ACCELERATION_X + ", "
                + TABLE_NAME + "." + DECELERATION_Y + " as " + TABLE_NAME_DECELERATION_Y + ", "
                + TABLE_NAME + "." + ACCELERATION_Y + " as " + TABLE_NAME_ACCELERATION_Y + ", "
                + TABLE_NAME + "." + DECELERATION_Z + " as " + TABLE_NAME_DECELERATION_Z + ", "
                + TABLE_NAME + "." + ACCELERATION_Z + " as " + TABLE_NAME_ACCELERATION_Z;

        private static final String SQL_CREATE_TABLE =
                "create table " + TABLE_NAME + " (_id integer primary key autoincrement, "
                        + MSGID + " integer, "
                        + THEFT + " integer, "
                        + SOS + " integer, "
                        + COLLISION + " integer, "
                        + VIBRATION + " integer, "
                        + OVERSPEED + " integer, "
                        + FATIGUE_DRIVING + " integer, "
                        + OVERTIME_PARKING + " integer, "
                        + DECELERATION_X + " integer, "
                        + ACCELERATION_X + " integer, "
                        + DECELERATION_Y + " integer, "
                        + ACCELERATION_Y + " integer, "
                        + DECELERATION_Z + " integer, "
                        + ACCELERATION_Z + " integer, "
                        + "FOREIGN KEY(" + MSGID + ") REFERENCES " + Msg.TABLE_NAME + "(_id));";

        private static final String SQL_CREATE_TRIGGER =
                "CREATE TRIGGER delete_" + TABLE_NAME + "item BEFORE DELETE"
                        + " ON " + Msg.TABLE_NAME
                        + " BEGIN"
                        + " DELETE FROM " + TABLE_NAME + " WHERE " + MSGID + " = old._id;"
                        + " END;";

        private static final String SQL_DROP_TABLE =
                "drop table if exists " + TABLE_NAME;

        private static final String SQL_DROP_TRIGGER =
                "DROP TRIGGER delete_" + TABLE_NAME + "item";
    }

    public static final class Status extends CustomBaseColumns {
        private Status() {
        }

        public static final String TABLE_NAME = "status";

        public static final String RPM = "rpm";
        public static final String SPEED = "speed";
        public static final String INST_FUEL = "instFuel";
        public static final String AVG_FUEL = "avgFuel";
        public static final String INLET_TEMP = "inletTemp";
        public static final String COOLANT_TEMP = "coolantTemp";
        public static final String FUEL_TRIM = "fuelTrim";
        public static final String THROTTLE_POS = "throttlePos";
        public static final String REMAINING_FUEL = "remainingFuel";
        public static final String EN_MILEAGE = "enMileage";
        public static final String TOTAL_FUEL = "totalFuel";
        public static final String TOTAL_MILEAGE = "totalMileage";

        public static final String TABLE_NAME_RPM = TABLE_NAME + RPM;
        public static final String TABLE_NAME_SPEED = TABLE_NAME + SPEED;
        public static final String TABLE_NAME_INST_FUEL = TABLE_NAME + INST_FUEL;
        public static final String TABLE_NAME_AVG_FUEL = TABLE_NAME + AVG_FUEL;
        public static final String TABLE_NAME_INLET_TEMP = TABLE_NAME + INLET_TEMP;
        public static final String TABLE_NAME_COOLANT_TEMP = TABLE_NAME + COOLANT_TEMP;
        public static final String TABLE_NAME_FUEL_TRIM = TABLE_NAME + FUEL_TRIM;
        public static final String TABLE_NAME_THROTTLE_POS = TABLE_NAME + THROTTLE_POS;
        public static final String TABLE_NAME_REMAINING_FUEL = TABLE_NAME + REMAINING_FUEL;
        public static final String TABLE_NAME_EN_MILEAGE = TABLE_NAME + EN_MILEAGE;
        public static final String TABLE_NAME_TOTAL_FUEL = TABLE_NAME + TOTAL_FUEL;
        public static final String TABLE_NAME_TOTAL_MILEAGE = TABLE_NAME + TOTAL_MILEAGE;

        public static final String COLUMNS = TABLE_NAME + "." + RPM + " as " + TABLE_NAME_RPM + ", "
                + TABLE_NAME + "." + SPEED + " as " + TABLE_NAME_SPEED + ", "
                + TABLE_NAME + "." + INST_FUEL + " as " + TABLE_NAME_INST_FUEL + ", "
                + TABLE_NAME + "." + AVG_FUEL + " as " + TABLE_NAME_AVG_FUEL + ", "
                + TABLE_NAME + "." + INLET_TEMP + " as " + TABLE_NAME_INLET_TEMP + ", "
                + TABLE_NAME + "." + COOLANT_TEMP + " as " + TABLE_NAME_COOLANT_TEMP + ", "
                + TABLE_NAME + "." + FUEL_TRIM + " as " + TABLE_NAME_FUEL_TRIM + ", "
                + TABLE_NAME + "." + THROTTLE_POS + " as " + TABLE_NAME_THROTTLE_POS + ", "
                + TABLE_NAME + "." + REMAINING_FUEL + " as " + TABLE_NAME_REMAINING_FUEL + ", "
                + TABLE_NAME + "." + EN_MILEAGE + " as " + TABLE_NAME_EN_MILEAGE + ", "
                + TABLE_NAME + "." + TOTAL_FUEL + " as " + TABLE_NAME_TOTAL_FUEL + ", "
                + TABLE_NAME + "." + TOTAL_MILEAGE + " as " + TABLE_NAME_TOTAL_MILEAGE;

        private static final String SQL_CREATE_TABLE =
                "create table " + TABLE_NAME + " (_id integer primary key autoincrement, "
                        + MSGID + " integer, "
                        + RPM + " integer, "
                        + SPEED + " integer, "
                        + INST_FUEL + " real, "
                        + AVG_FUEL + " real, "
                        + INLET_TEMP + " real, "
                        + COOLANT_TEMP + " real, "
                        + FUEL_TRIM + " integer, "
                        + THROTTLE_POS + " integer, "
                        + REMAINING_FUEL + " integer, "
                        + EN_MILEAGE + " integer, "
                        + TOTAL_FUEL + " integer, "
                        + TOTAL_MILEAGE + " integer, "
                        + "FOREIGN KEY(" + MSGID + ") REFERENCES " + Msg.TABLE_NAME + "(_id));";

        private static final String SQL_CREATE_TRIGGER =
                "CREATE TRIGGER delete_" + TABLE_NAME + "item BEFORE DELETE"
                        + " ON " + Msg.TABLE_NAME
                        + " BEGIN"
                        + " DELETE FROM " + TABLE_NAME + " WHERE " + MSGID + " = old._id;"
                        + " END;";

        private static final String SQL_DROP_TABLE =
                "drop table if exists " + TABLE_NAME;

        private static final String SQL_DROP_TRIGGER =
                "DROP TRIGGER delete_" + TABLE_NAME + "item";
    }

    public static final class Fault extends CustomBaseColumns {
        private Fault() {
        }

        public static final String TABLE_NAME = "fault";

        public static final String FAULT_CODE = "faultCode";

        public static final String TABLE_NAME_FAULT_CODE = TABLE_NAME + FAULT_CODE;

        public static final String COLUMNS = TABLE_NAME + "." + FAULT_CODE + " as " + TABLE_NAME_FAULT_CODE;

        private static final String SQL_CREATE_TABLE =
                "create table " + TABLE_NAME + " (_id integer primary key autoincrement, "
                        + MSGID + " integer, "
                        + FAULT_CODE + " text, "
                        + "FOREIGN KEY(" + MSGID + ") REFERENCES " + Msg.TABLE_NAME + "(_id));";

        private static final String SQL_CREATE_TRIGGER =
                "CREATE TRIGGER delete_" + TABLE_NAME + "item BEFORE DELETE"
                        + " ON " + Msg.TABLE_NAME
                        + " BEGIN"
                        + " DELETE FROM " + TABLE_NAME + " WHERE " + MSGID + " = old._id;"
                        + " END;";

        private static final String SQL_DROP_TABLE =
                "drop table if exists " + TABLE_NAME;

        private static final String SQL_DROP_TRIGGER =
                "DROP TRIGGER delete_" + TABLE_NAME + "item";
    }

    public static final class Electric extends CustomBaseColumns {
        private Electric() {
        }

        public static final String TABLE_NAME = "electric";

        public static final String MOTOR_POWER = "motorPower";
        public static final String SOC = "soc";//State Of Charge
        public static final String BAT_CHARGE_STATE = "batChargeState";
        public static final String BAT_HEATING_STATE = "batHeatingState";

        public static final String TABLE_NAME_MOTOR_POWER = TABLE_NAME + MOTOR_POWER;
        public static final String TABLE_NAME_SOC = TABLE_NAME + SOC;
        public static final String TABLE_NAME_BAT_CHARGE_STATE = TABLE_NAME + BAT_CHARGE_STATE;
        public static final String TABLE_NAME_BAT_HEATING_STATE = TABLE_NAME + BAT_HEATING_STATE;

        public static final String COLUMNS = TABLE_NAME + "." + MOTOR_POWER + " as " + TABLE_NAME_MOTOR_POWER + ", "
                + TABLE_NAME + "." + SOC + " as " + TABLE_NAME_SOC + ", "
                + TABLE_NAME + "." + BAT_CHARGE_STATE + " as " + TABLE_NAME_BAT_CHARGE_STATE + ", "
                + TABLE_NAME + "." + BAT_HEATING_STATE + " as " + TABLE_NAME_BAT_HEATING_STATE;

        private static final String SQL_CREATE_TABLE =
                "create table " + TABLE_NAME + " (_id integer primary key autoincrement, "
                        + MSGID + " integer, "
                        + MOTOR_POWER + " integer, "
                        + SOC + " integer, "
                        + BAT_CHARGE_STATE + " integer, "
                        + BAT_HEATING_STATE + " integer, "
                        + "FOREIGN KEY(" + MSGID + ") REFERENCES " + Msg.TABLE_NAME + "(_id));";

        private static final String SQL_CREATE_TRIGGER =
                "CREATE TRIGGER delete_" + TABLE_NAME + "item BEFORE DELETE"
                        + " ON " + Msg.TABLE_NAME
                        + " BEGIN"
                        + " DELETE FROM " + TABLE_NAME + " WHERE " + MSGID + " = old._id;"
                        + " END;";

        private static final String SQL_DROP_TABLE =
                "drop table if exists " + TABLE_NAME;

        private static final String SQL_DROP_TRIGGER =
                "DROP TRIGGER delete_" + TABLE_NAME + "item";
    }

    public static final class GSensor extends CustomBaseColumns {
        private GSensor() {
        }

        public static final String TABLE_NAME = "gsensor";

        public static final String ACCURACY = "accuracy";
        public static final String X_VALUE = "xValue";
        public static final String Y_VALUE = "yValue";
        public static final String Z_VALUE = "zValue";

        public static final String TABLE_NAME_ACCURACY = TABLE_NAME + ACCURACY;
        public static final String TABLE_NAME_X_VALUE = TABLE_NAME + X_VALUE;
        public static final String TABLE_NAME_Y_VALUE = TABLE_NAME + Y_VALUE;
        public static final String TABLE_NAME_Z_VALUE = TABLE_NAME + Z_VALUE;

        public static final String COLUMNS = TABLE_NAME + "." + ACCURACY + " as " + TABLE_NAME_ACCURACY + ", "
                + TABLE_NAME + "." + X_VALUE + " as " + TABLE_NAME_X_VALUE + ", "
                + TABLE_NAME + "." + Y_VALUE + " as " + TABLE_NAME_Y_VALUE + ", "
                + TABLE_NAME + "." + Z_VALUE + " as " + TABLE_NAME_Z_VALUE;

        private static final String SQL_CREATE_TABLE =
                "create table " + TABLE_NAME + " (_id integer primary key autoincrement, "
                        + MSGID + " integer, "
                        + ACCURACY + " integer, "
                        + X_VALUE + " real, "
                        + Y_VALUE + " real, "
                        + Z_VALUE + " real, "
                        + "FOREIGN KEY(" + MSGID + ") REFERENCES " + Msg.TABLE_NAME + "(_id));";

        private static final String SQL_CREATE_TRIGGER =
                "CREATE TRIGGER delete_" + TABLE_NAME + "item BEFORE DELETE"
                        + " ON " + Msg.TABLE_NAME
                        + " BEGIN"
                        + " DELETE FROM " + TABLE_NAME + " WHERE " + MSGID + " = old._id;"
                        + " END;";

        private static final String SQL_DROP_TABLE =
                "drop table if exists " + TABLE_NAME;

        private static final String SQL_DROP_TRIGGER =
                "DROP TRIGGER delete_" + TABLE_NAME + "item";
    }

    public DatabaseHelper(Context context) {
        super(new DatabaseContext(context), DATABASE_NAME, null, DATABASE_VERSION);
        setWriteAheadLoggingEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        MyLog.d(TAG, "onCreate...");
        db.execSQL(Config.SQL_CREATE_TABLE);

        db.execSQL(Msg.SQL_CREATE_TABLE);
        db.execSQL(Location.SQL_CREATE_TABLE);
        db.execSQL(BaseStation.SQL_CREATE_TABLE);
        db.execSQL(Alarm.SQL_CREATE_TABLE);
        db.execSQL(Status.SQL_CREATE_TABLE);
        db.execSQL(Fault.SQL_CREATE_TABLE);
        db.execSQL(Electric.SQL_CREATE_TABLE);
        db.execSQL(GSensor.SQL_CREATE_TABLE);
        db.execSQL(CanMsg.SQL_CREATE_TABLE);

        db.execSQL(Msg.SQL_CREATE_TRIGGER);
        db.execSQL(CanMsg.SQL_CREATE_TRIGGER);
        db.execSQL(Location.SQL_CREATE_TRIGGER);
        db.execSQL(BaseStation.SQL_CREATE_TRIGGER);
        db.execSQL(Alarm.SQL_CREATE_TRIGGER);
        db.execSQL(Status.SQL_CREATE_TRIGGER);
        db.execSQL(Fault.SQL_CREATE_TRIGGER);
        db.execSQL(Electric.SQL_CREATE_TRIGGER);
        db.execSQL(GSensor.SQL_CREATE_TRIGGER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion == newVersion) {
            db.execSQL(Config.SQL_CREATE_TABLE);
        } else {
            recreate(db);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        recreate(db);
    }

    private void recreate(SQLiteDatabase db) {
        db.execSQL(Msg.SQL_DROP_TRIGGER);
        db.execSQL(Location.SQL_DROP_TRIGGER);
        db.execSQL(BaseStation.SQL_DROP_TRIGGER);
        db.execSQL(Alarm.SQL_DROP_TRIGGER);
        db.execSQL(Status.SQL_DROP_TRIGGER);
        db.execSQL(Fault.SQL_DROP_TRIGGER);
        db.execSQL(Electric.SQL_DROP_TRIGGER);
        db.execSQL(GSensor.SQL_DROP_TRIGGER);

        db.execSQL(Config.SQL_DROP_TABLE);

        db.execSQL(Msg.SQL_DROP_TABLE);
        db.execSQL(Location.SQL_DROP_TABLE);
        db.execSQL(BaseStation.SQL_DROP_TABLE);
        db.execSQL(Alarm.SQL_DROP_TABLE);
        db.execSQL(Status.SQL_DROP_TABLE);
        db.execSQL(Fault.SQL_DROP_TABLE);
        db.execSQL(Electric.SQL_DROP_TABLE);
        db.execSQL(GSensor.SQL_DROP_TABLE);

        try {
            db.execSQL(CanMsg.SQL_DROP_TRIGGER);
            db.execSQL(CanMsg.SQL_DROP_TABLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        onCreate(db);
    }
}
