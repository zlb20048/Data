package com.chleon.datacollection;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.telephony.*;
import android.text.TextUtils;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.TelephonyIntents;
import com.chleon.datacollection.autolink.CommandException;
import com.chleon.datacollection.autolink.CommandsInterface;
import com.chleon.datacollection.autolink.RIL;
import com.chleon.datacollection.bean.CacheBean;
import com.chleon.datacollection.bean.ConfigBean;
import com.chleon.datacollection.database.DatabaseService;
import com.chleon.datacollection.utils.StorageUtils;
import com.chleon.telematics.*;
import com.mapbar.android.location.LocationClient;
import com.mapbar.android.location.LocationClientOption;
import com.mapbar.android.location.QFAuthResultListener;
import kallaite.com.util.VehicleInfo;
import kallaite.com.util.VehicleInfo.VehicleInfoCallBack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataCollectionService extends Service {

    private static final String TAG = DataCollectionService.class.getSimpleName();

    // switch for dbg
    private static final boolean DBG = true;

    private static final boolean DBG_BATCH = false;

    private static final boolean DBG_GPS = false;

    private static final boolean DBG_MODEM = false;

    // feature definition
    private static final boolean FORBIDDEN_NON_ACTIVATED_DEVICE = true;

    private static final boolean UNPUBLISH_IF_VEHICLESVC_DISCONNECTED = false;

    private static final boolean ADJUST_AXIS_ALPHA_ANGLE = false;

    private static final boolean LOW_LATENCY_HAS_PRIORITY = false;

    private static final boolean COMBINE_SPEED_WITH_GSENSOR = true;

    private static final boolean IGNORE_GSENSOR_IF_PARKING = false;

    private static final boolean IS_CAN_SPEED_AVAILABLE = true;

    private static final double ALPHA = Math.PI / 12;

    private static final double COS_ALPHA = Math.cos(ALPHA);

    private static final double SIN_ALPHA = Math.sin(ALPHA);

    private static final double COS2_ALPHA_SIN2_ALPHA = (COS_ALPHA + SIN_ALPHA) * (COS_ALPHA - SIN_ALPHA);

    // key in setting.db write by register apps
    private static final String KEY_VEHICLE_VIN = "vehicle_vin";

    private static final String KEY_VEHICLE_ACTIVED = "vehicle_actived";

    // key in setting.db write by driving monitor
    private static final String KEY_MILEAGE_TRIGGER = "drivingMonitorDistance";

    private static final String ACTION_DRIVING_MONITOR = "driving.monitor.start";

    private static final String EXTRA_CATEGORY = "category";

    // constants for publish retry in case of sdcard mount latency or sdcard file access failure
    private static final int MSG_RETRY = 0x200;

    private static final int RETRY_INTERVAL = 1000;

    private static final int MAX_RETRY_TIMES = 3;

    // Message codes used with mWorkThreadHandler
    private static final int WORK_CMD_BASE = 0x00;

    private static final int CMD_HANDLE_LOGIN_DONE = WORK_CMD_BASE + 1;

    private static final int CMD_HANDLE_HEARTBEAT_DONE = CMD_HANDLE_LOGIN_DONE + 1;

    private static final int CMD_HANDLE_ACK_SET_PARAM_DONE = CMD_HANDLE_HEARTBEAT_DONE + 1;

    private static final int CMD_HANDLE_ACK_GET_PARAM_DONE = CMD_HANDLE_ACK_SET_PARAM_DONE + 1;

    private static final int CMD_HANDLE_SINGLE_REPORT_DONE = CMD_HANDLE_ACK_GET_PARAM_DONE + 1;

    private static final int CMD_HANDLE_CAN_REPORT_DONE = CMD_HANDLE_SINGLE_REPORT_DONE + 1;

    private static final int CMD_HANDLE_BATCH_REPORT_DONE = CMD_HANDLE_CAN_REPORT_DONE + 1;

    private static final int CMD_HANDLE_LOGOUT_DONE = CMD_HANDLE_BATCH_REPORT_DONE + 1;

    private static final int WORK_EVENT_BASE = 0x100;

    private static final int EVENT_SERVER_UNAVAILABLE = WORK_EVENT_BASE + 1;

    private static final int EVENT_SERVER_AVAILABLE = EVENT_SERVER_UNAVAILABLE + 1;

    private static final int EVENT_LOGIN = EVENT_SERVER_AVAILABLE + 1;

    private static final int EVENT_HEARTBEAT = EVENT_LOGIN + 1;

    private static final int EVENT_SET_PARAM = EVENT_HEARTBEAT + 1;

    private static final int EVENT_GET_PARAM = EVENT_SET_PARAM + 1;

    private static final int EVENT_NETWORK_PARAM_CHANGE = EVENT_GET_PARAM + 1;

    private static final int EVENT_CAN_PARAM = EVENT_NETWORK_PARAM_CHANGE + 1;

    // Message codes used with mDatabaseThreadHandler
    private static final int DATABASE_EVENT_BASE = 0x00;

    private static final int EVENT_INSERT_MSG_DATABASE = DATABASE_EVENT_BASE + 1;

    private static final int EVENT_INSERT_CAN_MSG_DATABASE = EVENT_INSERT_MSG_DATABASE + 1;

    private static final int EVENT_TIME_CHANGED = EVENT_INSERT_CAN_MSG_DATABASE + 1;

    private static final int EVENT_LOGIN_DONE = EVENT_TIME_CHANGED + 1;

    private static final int EVENT_SINGLE_REPORT_DONE = EVENT_LOGIN_DONE + 1;

    private static final int EVENT_CAN_REPORT_DONE = EVENT_SINGLE_REPORT_DONE + 1;

    private static final int EVENT_BATCH_REPORT_DONE = EVENT_CAN_REPORT_DONE + 1;

    // constants for login retry, add max retry time? retry forever for now
    private static final int LOGIN_INTERVAL = 60 * 1000;

    // constants for heartbeat retry, add retry interval? retry immediately for now
    private static final int HEARTBEAT_MAX_RETRY_TIMES = 3;

    private static final int HEARTBEAT_INTERVAL = 10 * 60 * 1000;

    private static final String ACTION_LOGIN_RETRY = "com.chleon.datacollection.LOGIN_RETRY";

    private static final String ACTION_HEART_BEAT = "com.chleon.datacollection.HEART_BEAT";

    private static final String ACTION_TIMER_REPORT = "com.chleon.datacollection.TIMER_REPORT";

    private Context mContext;

    private boolean mActivated = false;

    // ----------------------------------- begin ----------------------------------
    // |                                                                          |
    // |   variables init/set/used after activated                                |
    // |                                                                          |
    // ----------------------------------------------------------------------------

    private VehicleInfo mVehicleInfo;

    private boolean mSDCardReceiverRegistered = false;

    private boolean mVehicleSvcConnected = false;

    // ----------------------------------- begin ----------------------------------
    // |                                                                          |
    // |   variables init/set/used after vehicleSvc connected                     |
    // |                                                                          |
    // ----------------------------------------------------------------------------

    // variables for device
    private String mVin;

    private String mIMEI;

    // bean to cache all
    private CacheBean mCacheBean;

    // ----------------------------------- begin ----------------------------------
    // |                                                                          |
    // |   variables init/set/used after sdcard mounted & vehicleSvc connected    |
    // |                                                                          |
    // ----------------------------------------------------------------------------

    private boolean mPublished = false;

    // variables for retry in case of sdcard mount latency or sdcard file access failure
    private int mRetryTime = 0;

    // ----------------------------------- begin ----------------------------------
    // |                                                                          |
    // |   variables init/set/used after service published                        |
    // |                                                                          |
    // ----------------------------------------------------------------------------

    // bean for config
    private ConfigBean mConfigBean;

    // service to access db
    private DatabaseService mDbService;

    // variables for server accessibility
    private boolean mServerConnected;

    // variables for login
    private boolean mLogined;

    private int mLoginId;

    // variables for heartbeat
    private int mHeartBeatId;

    private int mHeartbeatRetryTimes;

    private long mLastHeartBeatTimeStamp;

    // variables to maintenance server time
    private AtomicBoolean mTcpServerTimeInitByServer = new AtomicBoolean();

    private long mTcpServerTime;

    private long mTcpElapsedRealtime;

    // manager to access device info
    private LocationManager mLocationMgr;

    private TelephonyManager mTelephonyMgr;

    private SensorManager mSensorMgr;

    //
    private ConnectivityManager mConnectivityMgr;

    // intents for login/heartbeat/timer
    private AlarmManager mAlarmMgr;

    private PendingIntent mLoginRetryPendingIntent;

    private PendingIntent mHeartBeatPendingIntent;

    private PendingIntent mTimerReportPendingIntent;

    // work thread
    private HandlerThread mWorkThread;

    private Handler mWorkThreadHandler;

    // database thread
    private HandlerThread mDatabaseThread;

    private Handler mDatabaseThreadHandler;

    // interface to communicate with socket
    private CommandsInterface mCi;

    // upload thread
    private Uploader mUploader;

    private Thread mUploadThread;

    // class and variables for speed check
    private enum AccelerationState {
        NORMAL,
        ACCELERATION,
        DECELERATION
    }

    private AccelerationState mSpeedState;

    private int mLastSpeed;

    private long mLastSpeedTimestamp;

    private class GSensorEventBean {

        float[] values;

        int accuracy;

        long timestamp;

        public GSensorEventBean(int valueSize) {
            values = new float[valueSize];
        }
    }

    private class WorkThreadHandler extends Handler {

        public WorkThreadHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Message onCompleted;
            AsyncResult ar;
            MyLog.d(TAG + "-WorkThreadHandler", "handleMessage begin, msg.what = " + msg.what);
            switch (msg.what) {
                case EVENT_SERVER_UNAVAILABLE:
                    mServerConnected = false;
                    if (mLogined) {
                        stopHeartBeat();
                        notifyLoginState(false);
                        mUploader.clearWaitingIds();
                    } else {
                        stopLoginRetry();
                    }
                    break;

                case EVENT_SERVER_AVAILABLE:
                    mServerConnected = true;
                case EVENT_LOGIN:
                    mLoginId++;
                    onCompleted = obtainMessage(CMD_HANDLE_LOGIN_DONE, mLoginId);
                    ConnectionMsg.LoginReqMsg loginReqMsg = new ConnectionMsg.LoginReqMsg(mVin, mConfigBean.getChannelId().getChannelId());
                    MyLog.i(TAG, "call ril login");
                    mCi.login(loginReqMsg, onCompleted);
                    break;

                case CMD_HANDLE_LOGIN_DONE:
                    ar = (AsyncResult) msg.obj;
                    int loginId = (Integer) ar.userObj;
                    MyLog.d(TAG, "loginId = " + loginId + " mLoginId = " + mLoginId);
                    if (loginId == mLoginId) {
                        if (null == ar.exception) {
                            boolean success = false;
                            ConnectionMsg connectionMsg = (ConnectionMsg) ar.result;
                            MyLog.d(TAG, "connectionMsg = " + connectionMsg);
                            if (connectionMsg != null) {
                                if (connectionMsg.getMsgType() == ConnectionMsg.MsgType.LOGIN) {
                                    ConnectionMsg.LoginRspMsg loginRspMsg = (ConnectionMsg.LoginRspMsg) connectionMsg.getMsgBody();
                                    if (loginRspMsg != null) {
                                        ResultCode resultCode = loginRspMsg.getResultCode();
                                        if (resultCode == ResultCode.OK) {
                                            success = true;
                                            String timestamp = loginRspMsg.getTimestamp();
                                            MyLog.i(TAG, "ril login success, adjust server time, " + timestamp);
                                            adjustTcpServerTime(timestamp);
                                            fireLoginDone();
                                            notifyLoginState(true);
                                            mUploader.notifyMe();
                                            stopLoginRetry();
                                            mHeartBeatId = 0;
                                            mLastHeartBeatTimeStamp = SystemClock.elapsedRealtime();
                                            startHeartBeat();
                                        } else {
                                            MyLog.e(TAG, "ril login fail, resultCode = " + resultCode);
                                        }
                                    } else {
                                        MyLog.e(TAG, "ril login fail, dummy loginRspMsg");
                                    }
                                } else {
                                    MyLog.e(TAG, "ril login fail, rsp mismatch");
                                }
                            } else {
                                MyLog.e(TAG, "ril login fail, dummy connectionMsg");
                            }
                            // if login fail, retry later
                            if (!success) {
                                startLoginRetry();
                            }
                        } else {
                            MyLog.e(TAG, "ril login fail");
                            if (ar.exception instanceof CommandException) {
                                CommandException.Error err = ((CommandException) (ar.exception)).getCommandError();
                                // if login fail, retry later, but there is no need if SERVER_NOT_AVAILABLE,
                                // as retry will trigger by EVENT_SERVER_AVAILABLE
                                if (err == CommandException.Error.GENERIC_FAILURE
                                        || err == CommandException.Error.RESPONSE_TIMEOUT) {
                                    startLoginRetry();
                                }
                            }
                        }
                    } else {
                        MyLog.i(TAG, "stale ril login rsp");
                    }
                    break;

                case EVENT_HEARTBEAT:
                    mHeartBeatId++;
                    onCompleted = obtainMessage(CMD_HANDLE_HEARTBEAT_DONE, mHeartBeatId);
                    MyLog.i(TAG, "call ril heartbeat");
                    mCi.heartbeat(onCompleted);
                    break;

                case CMD_HANDLE_HEARTBEAT_DONE:
                    ar = (AsyncResult) msg.obj;
                    int heartbeatId = (Integer) ar.userObj;
                    if (heartbeatId == mHeartBeatId) {
                        if (null == ar.exception) {
                            boolean success = false;
                            ConnectionMsg connectionMsg = (ConnectionMsg) ar.result;
                            if (connectionMsg != null) {
                                if (connectionMsg.getMsgType() == ConnectionMsg.MsgType.HEARTBEAT) {
                                    MyLog.i(TAG, "ril heartbeat success");
                                    success = true;
                                    mHeartbeatRetryTimes = 0;
                                    mLastHeartBeatTimeStamp = SystemClock.elapsedRealtime();
                                } else {
                                    MyLog.e(TAG, "ril heartbeat fail, rsp mismatch");
                                }
                            } else {
                                MyLog.e(TAG, "ril heartbeat fail, dummy connectionMsg");
                            }
                            if (!success) {
                                if (mHeartbeatRetryTimes < HEARTBEAT_MAX_RETRY_TIMES) {
                                    mHeartbeatRetryTimes++;
                                    fireNextHeartBeat();
                                }
                            }
                        } else {
                            MyLog.e(TAG, "ril heartbeat fail");
                            if (ar.exception instanceof CommandException) {
                                // if heartbeat fail, retry later, but there is no need if SERVER_NOT_AVAILABLE,
                                // as login retry will trigger by EVENT_SERVER_AVAILABLE
                                CommandException.Error err = ((CommandException) (ar.exception)).getCommandError();
                                if (err == CommandException.Error.GENERIC_FAILURE
                                        || err == CommandException.Error.RESPONSE_TIMEOUT) {
                                    if (DateUtils.isHeartBeatExpired(mLastHeartBeatTimeStamp)) {
                                        // TODO maybe should logout first
                                        stopHeartBeat();
                                        notifyLoginState(false);
                                        mUploader.clearWaitingIds();
                                        startLoginRetry();
                                    } else {
                                        if (mHeartbeatRetryTimes < HEARTBEAT_MAX_RETRY_TIMES) {
                                            mHeartbeatRetryTimes++;
                                            fireNextHeartBeat();
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        MyLog.i(TAG, "stale ril heartbeat rsp");
                    }
                    break;

                case CMD_HANDLE_ACK_SET_PARAM_DONE:
                    ar = (AsyncResult) msg.obj;
                    ParamType type = (ParamType) ar.userObj;
                    if (null == ar.exception) {
                        MyLog.i(TAG, "ril ackSetParam success");
                        if (type == ParamType.NETWORK_PARAM || type == ParamType.CHANNEL_ID_PARAM) {
                            stopHeartBeat();
                            //as mapbar not support logout, so just resetSocket
                            //onCompleted = obtainMessage(CMD_HANDLE_LOGOUT_DONE, mLoginId);
                            //MyLog.i(TAG, "call ril logout");
                            //mCi.logout(onCompleted);
                            MyLog.i(TAG, "call ril resetSocket");
                            mCi.resetSocket(mConfigBean.getNetworkParam());
                        }
                    } else {
                        // there is no need to do anything, as server should retry by protocol
                        MyLog.e(TAG, "ril ackSetParam fail");
                    }
                    break;

                case CMD_HANDLE_ACK_GET_PARAM_DONE:
                    ar = (AsyncResult) msg.obj;
                    if (null == ar.exception) {
                        MyLog.i(TAG, "ril ackGetParam success");
                    } else {
                        // there is no need to do anything, as server should retry by protocol
                        MyLog.e(TAG, "ril ackGetParam fail");
                    }
                    break;

                case CMD_HANDLE_LOGOUT_DONE:
                    ar = (AsyncResult) msg.obj;
                    int logoutId = (Integer) ar.userObj;
                    if (logoutId == mLoginId) {
                        if (null == ar.exception) {
                            // there is no need to judge the rsp by protocol
                            // just think logout will always success by default
                            ConnectionMsg connectionMsg = (ConnectionMsg) ar.result;
                            if (connectionMsg != null) {
                                if (connectionMsg.getMsgType() == ConnectionMsg.MsgType.LOGOUT) {
                                    MyLog.i(TAG, "ril logout success");
                                } else {
                                    MyLog.e(TAG, "ril logout fail, rsp mismatch");
                                }
                            } else {
                                MyLog.e(TAG, "ril logout fail, dummy connectionMsg");
                            }
                        } else {
                            MyLog.e(TAG, "ril logout fail");
                            if (ar.exception instanceof CommandException) {
                                // if logout fail, retry later, but there is no need if SERVER_NOT_AVAILABLE,
                                // as login retry will trigger by EVENT_SERVER_AVAILABLE
                                CommandException.Error err = ((CommandException) (ar.exception)).getCommandError();
                                if (err == CommandException.Error.GENERIC_FAILURE
                                        || err == CommandException.Error.RESPONSE_TIMEOUT) {
                                    // TODO maybe should retry logout is this case
                                }
                            }
                        }
                        notifyLoginState(false);
                        mUploader.clearWaitingIds();

                        // TODO maybe should judge logout reason someday, as resetSocket is not required by logout
                        MyLog.e(TAG, "call ril resetSocket");
                        mCi.resetSocket(mConfigBean.getNetworkParam());
                    } else {
                        MyLog.i(TAG, "stale ril logout rsp");
                    }
                    break;

                case CMD_HANDLE_SINGLE_REPORT_DONE:
                    ar = (AsyncResult) msg.obj;
                    SingleReportMsg singleReportMsg = (SingleReportMsg) ar.userObj;
                    boolean singleReportSuccess = false;
                    if (null == ar.exception) {
                        resetHeartBeat();
                        if (singleReportMsg.shouldAck()) {
                            ReportRspMsg reportRspMsg = (ReportRspMsg) ar.result;
                            if (reportRspMsg != null) {
                                ResultCode resultCode = reportRspMsg.getResultCode();
                                if (resultCode == ResultCode.OK) {
                                    MyLog.i(TAG, "ril single report success");
                                    singleReportSuccess = true;
                                } else {
                                    MyLog.e(TAG, "ril single report fail, resultCode = " + resultCode);
                                }
                            } else {
                                MyLog.e(TAG, "ril single report fail, dummy reportRspMsg");
                            }
                        } else {
                            MyLog.i(TAG, "ril single report success");
                            singleReportSuccess = true;
                        }
                    } else {
                        MyLog.e(TAG, "ril single report fail");
                    }
                    long id = singleReportMsg.getId();
                    if (singleReportSuccess) {
                        fireSingleReportDone(id);
                    } else {
                        // will try later
                        mUploader.removeIdFromWaitingIds(id);
                    }
                    break;
                case CMD_HANDLE_CAN_REPORT_DONE:
                    ar = (AsyncResult) msg.obj;
                    CanReqMsg canReqMsg = (CanReqMsg) ar.userObj;
                    id = canReqMsg.getId();
                    fireCanReportDone(id);
                    mUploader.removeIdFromWaitingCanIds(id);
                    break;
                case CMD_HANDLE_BATCH_REPORT_DONE:
                    ar = (AsyncResult) msg.obj;
                    BatchReportMsg batchReportMsg = (BatchReportMsg) ar.userObj;
                    boolean batchReportSuccess = false;
                    if (null == ar.exception) {
                        resetHeartBeat();
                        if (batchReportMsg.shouldAck()) {
                            ReportRspMsg reportRspMsg = (ReportRspMsg) ar.result;
                            if (reportRspMsg != null) {
                                ResultCode resultCode = reportRspMsg.getResultCode();
                                if (resultCode == ResultCode.OK) {
                                    MyLog.i(TAG, "ril batch report success");
                                    batchReportSuccess = true;
                                } else {
                                    MyLog.e(TAG, "ril batch report fail, resultCode = " + resultCode);
                                }
                            } else {
                                MyLog.e(TAG, "ril batch report fail, dummy reportRspMsg");
                            }
                        } else {
                            MyLog.i(TAG, "ril batch report success");
                            batchReportSuccess = true;
                        }
                    } else {
                        MyLog.e(TAG, "ril batch report fail");
                    }
                    List<Long> ids = batchReportMsg.getIds();
                    if (batchReportSuccess) {
                        fireBatchReportDone(ids);
                    } else {
                        // will try later
                        mUploader.removeIdsFromWaitingIds(ids);
                    }
                    break;

                case EVENT_SET_PARAM:
                    ar = (AsyncResult) msg.obj;
                    if (null == ar.exception) {
                        AutoLinkMsg autoLinkMsg = (AutoLinkMsg) ar.result;
                        MsgHeader msgHeader = autoLinkMsg.getMsgHeader();
                        SetParamReqMsg setParamReqMsg = (SetParamReqMsg) autoLinkMsg.getMsgBody();

                        if (setParamReqMsg != null) {
                            MyLog.i(TAG, "ril set param success");
                            ParamType paramType = setParamReqMsg.getParamType();
                            onCompleted = obtainMessage(CMD_HANDLE_ACK_SET_PARAM_DONE, paramType);
                            SetParamRspMsg setParamRspMsg = new SetParamRspMsg(msgHeader.getSerialNumber());
                            setParamRspMsg.setParamType(paramType);

                            if (DateUtils.isMsgExpired(setParamReqMsg.getTimestamp(), getTcpServerTime())) {
                                autoLinkMsg.setResultCode(ResultCode.CMD_EXPIRE);
                            }

                            ResultCode resultCode = autoLinkMsg.getResultCode();
                            if (resultCode == ResultCode.OK) {
                                switch (paramType) {
                                    case CHANNEL_ID_PARAM: {
                                        BaseParamMsg.ChannelIdMsg channelIdMsg = (BaseParamMsg.ChannelIdMsg) setParamReqMsg.getMsgBody();
                                        mConfigBean.setChannelId(channelIdMsg);
                                        StorageUtils.saveConfigBean(mContext, mConfigBean);
                                        setParamRspMsg.setResultCode(ResultCode.OK);
                                        break;
                                    }
                                    case NETWORK_PARAM: {
                                        BaseParamMsg.NetworkParamMsg networkParamMsg = (BaseParamMsg.NetworkParamMsg) setParamReqMsg.getMsgBody();
                                        mConfigBean.setBackupNetworkParam(mConfigBean.getNetworkParam());
                                        mConfigBean.setNetworkParam(networkParamMsg);
                                        StorageUtils.saveConfigBean(mContext, mConfigBean);
                                        setParamRspMsg.setResultCode(ResultCode.OK);
                                        break;
                                    }
                                    case TIMER_PARAM: {
                                        BaseParamMsg.TimerMsg timerMsg = (BaseParamMsg.TimerMsg) setParamReqMsg.getMsgBody();
                                        mConfigBean.setTimer(timerMsg);
                                        StorageUtils.saveConfigBean(mContext, mConfigBean);
                                        if (mCacheBean.getAccStatus() == 1) {
                                            if (timerMsg.isTimerOn()) {
                                                resetTimerReport();
                                            } else {
                                                stopTimerReport();
                                            }
                                        }
                                        setParamRspMsg.setResultCode(ResultCode.OK);
                                        break;
                                    }
                                    case ACC_REPORT_PARAM: {
                                        BaseParamMsg.ReportMsg reportMsg = (BaseParamMsg.ReportMsg) setParamReqMsg.getMsgBody();
                                        mConfigBean.setAccReport(reportMsg);
                                        StorageUtils.saveConfigBean(mContext, mConfigBean);
                                        setParamRspMsg.setResultCode(ResultCode.OK);
                                        break;
                                    }
                                    case STATUS_REPORT_PARAM: {
                                        BaseParamMsg.ReportMsg reportMsg = (BaseParamMsg.ReportMsg) setParamReqMsg.getMsgBody();
                                        mConfigBean.setStatusReport(reportMsg);
                                        StorageUtils.saveConfigBean(mContext, mConfigBean);
                                        setParamRspMsg.setResultCode(ResultCode.OK);
                                        break;
                                    }
                                    case FAULT_REPORT_PARAM: {
                                        BaseParamMsg.ReportMsg reportMsg = (BaseParamMsg.ReportMsg) setParamReqMsg.getMsgBody();
                                        mConfigBean.setFaultReport(reportMsg);
                                        StorageUtils.saveConfigBean(mContext, mConfigBean);
                                        setParamRspMsg.setResultCode(ResultCode.OK);
                                        break;
                                    }
                                    case ALARM_REPORT_PARAM: {
                                        BaseParamMsg.ReportMsg reportMsg = (BaseParamMsg.ReportMsg) setParamReqMsg.getMsgBody();
                                        mConfigBean.setAlarmReport(reportMsg);
                                        StorageUtils.saveConfigBean(mContext, mConfigBean);
                                        setParamRspMsg.setResultCode(ResultCode.OK);
                                        break;
                                    }
                                    case RESTRICTION_PARAM: {
                                        BaseParamMsg.RestrictionMsg restrictionMsg = (BaseParamMsg.RestrictionMsg) setParamReqMsg.getMsgBody();
                                        mConfigBean.updateRestriction(restrictionMsg);
                                        StorageUtils.saveConfigBean(mContext, mConfigBean);
                                        setParamRspMsg.setResultCode(ResultCode.OK);
                                        break;
                                    }
                                    default: {
                                        MyLog.w(TAG, "not supported param");
                                        setParamRspMsg.setResultCode(ResultCode.SUBMSG_TYPE_ERROR);
                                        break;
                                    }
                                }
                            } else {
                                setParamRspMsg.setResultCode(resultCode);
                            }
                            mCi.ackSetParam(setParamRspMsg, onCompleted);
                        } else {
                            MyLog.e(TAG, "ril set param fail, dummy setParamReqMsg");
                        }
                    } else {
                        MyLog.e(TAG, "ril set param fail");
                    }
                    break;

                case EVENT_GET_PARAM:
                    ar = (AsyncResult) msg.obj;
                    if (null == ar.exception) {
                        AutoLinkMsg autoLinkMsg = (AutoLinkMsg) ar.result;
                        MsgHeader msgHeader = autoLinkMsg.getMsgHeader();
                        GetParamReqMsg getParamReqMsg = (GetParamReqMsg) autoLinkMsg.getMsgBody();

                        if (getParamReqMsg != null) {
                            MyLog.i(TAG, "ril get param success");
                            ParamType paramType = getParamReqMsg.getParamType();
                            onCompleted = obtainMessage(CMD_HANDLE_ACK_GET_PARAM_DONE, paramType);
                            GetParamRspMsg getParamRspMsg = new GetParamRspMsg(msgHeader.getSerialNumber());
                            getParamRspMsg.setParamType(paramType);

                            if (DateUtils.isMsgExpired(getParamReqMsg.getTimestamp(), getTcpServerTime())) {
                                autoLinkMsg.setResultCode(ResultCode.CMD_EXPIRE);
                            }

                            ResultCode resultCode = autoLinkMsg.getResultCode();
                            if (resultCode == ResultCode.OK) {
                                switch (getParamReqMsg.getParamType()) {
                                    case CHANNEL_ID_PARAM: {
                                        getParamRspMsg.setMsgBody(mConfigBean.getChannelId());
                                        getParamRspMsg.setResultCode(ResultCode.OK);
                                        break;
                                    }
                                    case NETWORK_PARAM: {
                                        getParamRspMsg.setMsgBody(mConfigBean.getNetworkParam());
                                        getParamRspMsg.setResultCode(ResultCode.OK);
                                        break;
                                    }
                                    case TIMER_PARAM: {
                                        getParamRspMsg.setMsgBody(mConfigBean.getTimer());
                                        getParamRspMsg.setResultCode(ResultCode.OK);
                                        break;
                                    }
                                    case ACC_REPORT_PARAM: {
                                        getParamRspMsg.setMsgBody(mConfigBean.getAccReport());
                                        getParamRspMsg.setResultCode(ResultCode.OK);
                                        break;
                                    }
                                    case STATUS_REPORT_PARAM: {
                                        getParamRspMsg.setMsgBody(mConfigBean.getStatusReport());
                                        getParamRspMsg.setResultCode(ResultCode.OK);
                                        break;
                                    }
                                    case FAULT_REPORT_PARAM: {
                                        getParamRspMsg.setMsgBody(mConfigBean.getFaultReport());
                                        getParamRspMsg.setResultCode(ResultCode.OK);
                                        break;
                                    }
                                    case ALARM_REPORT_PARAM: {
                                        getParamRspMsg.setMsgBody(mConfigBean.getAlarmReport());
                                        getParamRspMsg.setResultCode(ResultCode.OK);
                                        break;
                                    }
                                    case RESTRICTION_PARAM: {
                                        getParamRspMsg.setMsgBody(mConfigBean.getRestriction());
                                        getParamRspMsg.setResultCode(ResultCode.OK);
                                        break;
                                    }
                                    case FAULT_CODE: {
                                        getParamRspMsg.setMsgBody(mCacheBean.getFaultMsg());
                                        getParamRspMsg.setResultCode(ResultCode.OK);
                                        break;
                                    }
                                    case FIND_CAR: {
                                        FindCarMsg findCarMsg = new FindCarMsg();
                                        findCarMsg.setContainLocationMsg(true);
                                        findCarMsg.setContainStatusMsg(true);
                                        findCarMsg.setContainAdditionMsg(true);
                                        findCarMsg.setContainBaseStationMsg(true);
                                        findCarMsg.setContainFaultMsg(true);
                                        findCarMsg.setContainBatteryMsg(true);
                                        findCarMsg.setContainGSensorMsg(true);
                                        findCarMsg.setTimestamp(getTcpServerTime());
                                        Location location = mLocationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                        if (mMapbarSDKAuthSuccess) {
                                            location = mLocation;
                                        }
                                        mCacheBean.updateLocationMsg(location);
                                        findCarMsg.setLocationMsg(mCacheBean.getLocationMsg());
                                        findCarMsg.setStatusMsg(mCacheBean.getStatusMsg());
                                        findCarMsg.setAdditionMsg(mCacheBean.getAdditionMsg());
                                        findCarMsg.setBaseStationMsg(mCacheBean.getBaseStationMsg());
                                        findCarMsg.setFaultMsg(mCacheBean.getFaultMsg());
                                        findCarMsg.setBatteryMsg(mCacheBean.getBatteryMsg());
                                        findCarMsg.setGSensorMsg(mCacheBean.getGSensorMsg());
                                        getParamRspMsg.setMsgBody(findCarMsg);
                                        getParamRspMsg.setResultCode(ResultCode.OK);
                                        break;
                                    }
                                    default: {
                                        MyLog.w(TAG, "not supported param");
                                        getParamRspMsg.setResultCode(ResultCode.SUBMSG_TYPE_ERROR);
                                        break;
                                    }
                                }
                            } else {
                                getParamRspMsg.setResultCode(resultCode);
                            }
                            mCi.ackGetParam(getParamRspMsg, onCompleted);
                        } else {
                            MyLog.e(TAG, "ril get param fail, dummy getParamReqMsg");
                        }
                    } else {
                        MyLog.e(TAG, "ril get param fail");
                    }
                    break;

                case EVENT_NETWORK_PARAM_CHANGE:
                    ar = (AsyncResult) msg.obj;
                    BaseParamMsg.NetworkParamMsg networkParamMsg = (BaseParamMsg.NetworkParamMsg) ar.result;
                    if (networkParamMsg != null) {
                        mConfigBean.setNetworkParam(networkParamMsg);
                        mConfigBean.setBackupNetworkParam(null);
                        StorageUtils.saveConfigBean(mContext, mConfigBean);
                    }
                    break;
                case EVENT_CAN_PARAM:
                    ar = (AsyncResult) msg.obj;
                    CanRspMsg canRspMsg = (CanRspMsg) ar.result;
                    if (null != canRspMsg) {
                        MyLog.i(TAG, "msg = " + canRspMsg.getCanData());
                        mVehicleInfo.sendCanDataChange(canRspMsg.getCanData());
                    }
                    break;
                default:
                    MyLog.w(TAG + "-WorkThreadHandler", "unexpected message code: " + msg.what);
                    break;
            }

            MyLog.d(TAG + "-WorkThreadHandler", "handleMessage end");
        }
    }

    private void fireCanReportDone(long id) {
        try {
            Message msg = mDatabaseThreadHandler.obtainMessage(EVENT_CAN_REPORT_DONE, id);
            mDatabaseThreadHandler.sendMessage(msg);
        } catch (Exception e) {
            Message msg = new Message();
            msg.what = EVENT_CAN_REPORT_DONE;
            msg.obj = id;
            mDatabaseThreadHandler.sendMessage(msg);
            e.printStackTrace();
        }
    }

    private class DatabaseThreadHandler extends Handler {

        public DatabaseThreadHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            MyLog.d(TAG + "-DatabaseThreadHandler", "handleMessage begin, msg.what = " + msg.what);

            switch (msg.what) {
                case EVENT_INSERT_MSG_DATABASE:
                    if (msg.obj != null) {
                        SingleReportMsg singleReportMsg = (SingleReportMsg) msg.obj;
                        long id = mDbService.insertMsgToDataBase(singleReportMsg, mLogined ? 1 : 2);
                        if (id > 0) {
                            MyLog.i(TAG, "insertMsgToDataBase success");
                            mUploader.notifyMe();
                        } else {
                            MyLog.e(TAG, "insertMsgToDataBase error");
                            if (mUploader.isReadyToUpload()) {
                                mUploader.notifyMe();
                            }
                        }
                    }
                    break;
                case EVENT_INSERT_CAN_MSG_DATABASE:
                    if (msg.obj != null) {
                        CanReqMsg canReqMsg = (CanReqMsg) msg.obj;
                        long id = mDbService.insertCanMsgToDataBase(canReqMsg);
                        if (id > 0) {
                            MyLog.i(TAG, "insertCanMsgToDataBase success id ===> " + id);
                        } else {
                            MyLog.i(TAG, "insertCanMsgToDataBase error");
                        }
                        mUploader.notifyMe();
                    }
                    break;

                case EVENT_TIME_CHANGED:
                    if (!mTcpServerTimeInitByServer.get()) {
                        mTcpServerTime = DateUtils.getMillisFromLocalDate();
                        mTcpElapsedRealtime = SystemClock.elapsedRealtime();
                        MyLog.i(TAG, "updateNewMsgsTimestamp localTime = " + mTcpServerTime);
                        mDbService.updateNewMsgsTimestampInDataBase(mTcpServerTime);
                    }
                    break;

                case EVENT_LOGIN_DONE:
                    MyLog.i(TAG, "updateNewMsgsTimestamp mTcpServerTime = " + mTcpServerTime);
                    mDbService.updateNewMsgsTimestampInDataBase(mTcpServerTime);
                    mDbService.updateMsgsInDataBase(0);
                    break;

                case EVENT_SINGLE_REPORT_DONE:
                    if (msg.obj != null) {
                        long id = (Long) msg.obj;
                        MyLog.i(TAG, "del msg, id = " + id);
                        mDbService.deleteMsgFromDataBase(id);
                        mUploader.removeIdFromWaitingIds(id);
                    }
                    break;
                case EVENT_CAN_REPORT_DONE:
                    if (msg.obj != null) {
                        long id = (Long) msg.obj;
                        MyLog.i(TAG, "del can msg , id = " + id);
                        mDbService.deleteCanMsgFromDataBase(id);
                        mUploader.removeIdFromWaitingCanIds(id);
                    }
                    break;
                case EVENT_BATCH_REPORT_DONE:
                    if (msg.obj != null) {
                        List<Long> ids = (List<Long>) msg.obj;
                        MyLog.i(TAG, "del msgs, ids = " + ids.toString());
                        mDbService.deleteMsgFromDataBase(ids);
                        mUploader.removeIdsFromWaitingIds(ids);
                    }
                    break;

                default:
                    MyLog.w(TAG + "-DatabaseThreadHandler", "unexpected message code: " + msg.what);
                    break;
            }

            MyLog.d(TAG + "-DatabaseThreadHandler", "handleMessage end");
        }
    }

    private void adjustTcpServerTime(String timestamp) {
        mTcpServerTimeInitByServer.set(true);
        mTcpServerTime = DateUtils.getMillisFromTimestamp(timestamp);
        mTcpElapsedRealtime = SystemClock.elapsedRealtime();
    }

    public long getTcpServerTime() {
        return mTcpServerTime + SystemClock.elapsedRealtime() - mTcpElapsedRealtime;
    }

    private void notifyLoginState(boolean state) {
        mLogined = state;
    }

    private void fireNextLogin() {
        try {
            Message msg = mWorkThreadHandler.obtainMessage(EVENT_LOGIN);
            mWorkThreadHandler.sendMessage(msg);
        } catch (Exception e) {
            Message msg = new Message();
            msg.what = EVENT_LOGIN;
            mWorkThreadHandler.sendMessage(msg);
            e.printStackTrace();
        }
    }

    private void fireNextHeartBeat() {
        try {
            Message msg = mWorkThreadHandler.obtainMessage(EVENT_HEARTBEAT);
            mWorkThreadHandler.sendMessage(msg);
        } catch (Exception e) {
            Message msg = new Message();
            msg.what = EVENT_HEARTBEAT;
            mWorkThreadHandler.sendMessage(msg);
            e.printStackTrace();
        }
    }

    private void fireInsertMsgToDataBase(SingleReportMsg singleReportMsg) {
        singleReportMsg.setElapsed(SystemClock.elapsedRealtime());
        try {
            Message msg = mDatabaseThreadHandler.obtainMessage(EVENT_INSERT_MSG_DATABASE, singleReportMsg);
            mDatabaseThreadHandler.sendMessage(msg);
        } catch (Exception e) {
            Message msg = new Message();
            msg.what = EVENT_INSERT_MSG_DATABASE;
            msg.obj = singleReportMsg;
            mDatabaseThreadHandler.sendMessage(msg);
            e.printStackTrace();
        }
    }

    private void fireTimeChanged() {
        try {
            Message msg = mDatabaseThreadHandler.obtainMessage(EVENT_TIME_CHANGED);
            mDatabaseThreadHandler.sendMessage(msg);
        } catch (Exception e) {
            Message msg = new Message();
            msg.what = EVENT_TIME_CHANGED;
            mDatabaseThreadHandler.sendMessage(msg);
            e.printStackTrace();
        }
    }

    private void fireLoginDone() {
        try {
            Message msg = mDatabaseThreadHandler.obtainMessage(EVENT_LOGIN_DONE);
            mDatabaseThreadHandler.sendMessage(msg);
        } catch (Exception e) {
            Message msg = new Message();
            msg.what = EVENT_LOGIN_DONE;
            mDatabaseThreadHandler.sendMessage(msg);
            e.printStackTrace();
        }
    }

    private void fireSingleReportDone(long id) {
        try {
            Message msg = mDatabaseThreadHandler.obtainMessage(EVENT_SINGLE_REPORT_DONE, id);
            mDatabaseThreadHandler.sendMessage(msg);
        } catch (Exception e) {
            Message msg = new Message();
            msg.what = EVENT_SINGLE_REPORT_DONE;
            msg.obj = id;
            mDatabaseThreadHandler.sendMessage(msg);
            e.printStackTrace();
        }
    }

    private void fireBatchReportDone(List<Long> ids) {
        try {
            Message msg = mDatabaseThreadHandler.obtainMessage(EVENT_BATCH_REPORT_DONE, ids);
            mDatabaseThreadHandler.sendMessage(msg);
        } catch (Exception e) {
            Message msg = new Message();
            msg.what = EVENT_BATCH_REPORT_DONE;
            msg.obj = ids;
            mDatabaseThreadHandler.sendMessage(msg);
            e.printStackTrace();
        }
    }

    private void handleAccReport() {
        MyLog.i(TAG, "handleAccReport");
        boolean accReport = mConfigBean.getAccReport().getReportState();
        MyLog.i(TAG, "accReport = " + accReport);
        if (accReport) {
            SingleReportMsg singleReportMsg;
            switch (mCacheBean.getAccStatus()) {
                case 0: //off
                    singleReportMsg = createSingleReportMsg(ReportReason.ACC_OFF);
                    singleReportMsg.setContainLocationMsg(true);
                    singleReportMsg.setContainBaseStationMsg(true);
                    singleReportMsg.setContainStatusMsg(true);
                    singleReportMsg.setContainAdditionMsg(true);
                    singleReportMsg.setContainBatteryMsg(true);
                    singleReportMsg.setContainFaultMsg(true);
                    break;
                default: //on
                    singleReportMsg = createSingleReportMsg(ReportReason.ACC_ON);
                    singleReportMsg.setContainLocationMsg(true);
                    singleReportMsg.setContainBaseStationMsg(true);
                    singleReportMsg.setContainStatusMsg(true);
                    singleReportMsg.setContainAdditionMsg(true);
                    singleReportMsg.setContainBatteryMsg(true);
                    singleReportMsg.setContainFaultMsg(true);
                    break;
            }
            buildSingleReportMsg(singleReportMsg);
            fireInsertMsgToDataBase(singleReportMsg);
        }

        boolean timerReport = mConfigBean.getTimer().isTimerOn();

        if (timerReport) {
            switch (mCacheBean.getAccStatus()) {
                case 0: //off
                    stopTimerReport();
                    break;
                case 1: //on
                    startTimerReport();
                    break;
            }
        }
    }

    private void handleTimerReport() {
        MyLog.i(TAG, "handleTimerReport");
        SingleReportMsg singleReportMsg = createSingleReportMsg(ReportReason.TIMER);
        singleReportMsg.setContainLocationMsg(true);
        singleReportMsg.setContainBaseStationMsg(true);
        singleReportMsg.setContainStatusMsg(true);
        singleReportMsg.setContainAdditionMsg(true);
        singleReportMsg.setContainBatteryMsg(true);
        singleReportMsg.setContainFaultMsg(true);
        buildSingleReportMsg(singleReportMsg);
        fireInsertMsgToDataBase(singleReportMsg);
    }

    private void handleDrivingMonitor() {
        int lastMileage = mDbService.getInt("lastMileage", -1);
        int curMileage = mVehicleInfo.getTotalMileage();
        if (lastMileage == -1) {
            if (curMileage >= 0) {
                mDbService.putInt("lastMileage", curMileage);
            }
        } else {
            int mileageTrigger = Settings.System.getInt(getContentResolver(), KEY_MILEAGE_TRIGGER, 0);
            MyLog.i(TAG, "handleDrivingMonitor, curMileage:lastMileage:mileageTrigger = " + curMileage + ":" + lastMileage + ":" + mileageTrigger);
            if (mileageTrigger != 0) {
                if (curMileage - lastMileage >= mileageTrigger) {
                    mDbService.putInt("lastMileage", curMileage);
                    Intent intent = new Intent(ACTION_DRIVING_MONITOR);
                    intent.putExtra(EXTRA_CATEGORY, 1);
                    mContext.sendBroadcast(intent);
                }
            }
        }
    }

    private void handleAlarmReport(AlarmType alarmType) {
        MyLog.i(TAG, "handleAlarmReport, alarmType = " + alarmType);
        SingleReportMsg singleReportMsg = createSingleReportMsg(ReportReason.ALARM);
        singleReportMsg.setContainLocationMsg(true);
        singleReportMsg.setContainBaseStationMsg(true);
        singleReportMsg.setContainStatusMsg(true);
        singleReportMsg.setContainAdditionMsg(true);
        singleReportMsg.setContainBatteryMsg(true);
        switch (alarmType) {
            case COLLISION:
            case DECELERATION_X:
            case ACCELERATION_X:
            case DECELERATION_Y:
            case ACCELERATION_Y:
            case DECELERATION_Z:
            case ACCELERATION_Z:
                singleReportMsg.setContainGSensorMsg(true);
                break;
            default:
                break;
        }
        singleReportMsg.setContainFaultMsg(true);
        buildSingleReportMsg(singleReportMsg);
        fireInsertMsgToDataBase(singleReportMsg);
    }

    private void handleStatusReport() {
        MyLog.i(TAG, "handleStatusReport");
        if (mCacheBean.getAccStatus() != 1) { // enable status report when timer report is off
            boolean statusReport = mConfigBean.getStatusReport().getReportState();
            if (statusReport) {
                SingleReportMsg singleReportMsg = createSingleReportMsg(ReportReason.STATUS_CHANGE);
                singleReportMsg.setContainLocationMsg(true);
                singleReportMsg.setContainBaseStationMsg(true);
                singleReportMsg.setContainStatusMsg(true);
                singleReportMsg.setContainAdditionMsg(true);
                singleReportMsg.setContainBatteryMsg(true);
                singleReportMsg.setContainFaultMsg(true);
                buildSingleReportMsg(singleReportMsg);
                fireInsertMsgToDataBase(singleReportMsg);
            }
        }
    }

    private void handleFaultReport() {
        MyLog.i(TAG, "handleFaultReport");
        boolean report = mConfigBean.getFaultReport().getReportState();
        if (report) {
            SingleReportMsg singleReportMsg = createSingleReportMsg(ReportReason.FAULT);
            singleReportMsg.setContainLocationMsg(true);
            singleReportMsg.setContainBaseStationMsg(true);
            singleReportMsg.setContainStatusMsg(true);
            singleReportMsg.setContainAdditionMsg(true);
            singleReportMsg.setContainBatteryMsg(true);
            singleReportMsg.setContainFaultMsg(true);
            buildSingleReportMsg(singleReportMsg);
            fireInsertMsgToDataBase(singleReportMsg);
        }
    }

    private SingleReportMsg createSingleReportMsg(ReportReason reportReason) {
        SingleReportMsg singleReportMsg = new SingleReportMsg();
        singleReportMsg.setShouldAck(true);
        singleReportMsg.setDelayReport(false);
        singleReportMsg.setReportReason(reportReason);
        singleReportMsg.setTimestamp(getTcpServerTime());
        return singleReportMsg;
    }

    private void buildSingleReportMsg(SingleReportMsg singleReportMsg) {
        if (singleReportMsg.containLocationMsg()) {
            Location location = mLocationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (mMapbarSDKAuthSuccess) {
                location = mLocation;
            }
            mCacheBean.updateLocationMsg(location);
            singleReportMsg.setLocationMsg(mCacheBean.getLocationMsg());
        }

        if (singleReportMsg.containBaseStationMsg()) {
            singleReportMsg.setBaseStationMsg(mCacheBean.getBaseStationMsg());
        }

        if (singleReportMsg.containStatusMsg()) {
            singleReportMsg.setStatusMsg(mCacheBean.getStatusMsg());
        }

        if (singleReportMsg.containAdditionMsg()) {
            mCacheBean.updateAdditionMsg(mVehicleInfo);
            singleReportMsg.setAdditionMsg(mCacheBean.getAdditionMsg());
        }

        if (singleReportMsg.containBatteryMsg()) {
            singleReportMsg.setBatteryMsg(mCacheBean.getBatteryMsg());
        }

        if (singleReportMsg.containGSensorMsg()) {
            singleReportMsg.setGSensorMsg(mCacheBean.getGSensorMsg());
        }

        if (singleReportMsg.containFaultMsg()) {
            singleReportMsg.setFaultMsg(mCacheBean.getFaultMsg());
        }
    }

    private void startLoginRetry() {
        Intent intent = new Intent(ACTION_LOGIN_RETRY);
        mLoginRetryPendingIntent = PendingIntent.getBroadcast(mContext, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        mAlarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + LOGIN_INTERVAL, mLoginRetryPendingIntent);
    }

    private void stopLoginRetry() {
        mAlarmMgr.cancel(mLoginRetryPendingIntent);
    }

    private void startHeartBeat() {
        mHeartbeatRetryTimes = 0;
        Intent intent = new Intent(ACTION_HEART_BEAT);
        mHeartBeatPendingIntent = PendingIntent.getBroadcast(mContext, 1,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mAlarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, mHeartBeatPendingIntent);
    }

    private void stopHeartBeat() {
        mAlarmMgr.cancel(mHeartBeatPendingIntent);
    }

    private void resetHeartBeat() {
        stopHeartBeat();
        startHeartBeat();
    }

    private void startTimerReport() {
        Intent intent = new Intent(ACTION_TIMER_REPORT);
        mTimerReportPendingIntent = PendingIntent.getBroadcast(mContext, 2,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        int interval = mConfigBean.getTimer().getInterval() * 1000;
        if (DBG_BATCH) {
            interval = mDbgInterval;
        }
        mAlarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + interval, interval, mTimerReportPendingIntent);
    }

    private void stopTimerReport() {
        mAlarmMgr.cancel(mTimerReportPendingIntent);
    }

    private void resetTimerReport() {
        stopTimerReport();
        startTimerReport();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MyLog.d(TAG, "onCreate");

        mContext = getApplicationContext();

        if (FORBIDDEN_NON_ACTIVATED_DEVICE) {
            mActivated = isActivated();
            getContentResolver().registerContentObserver(
                    Settings.System.getUriFor(KEY_VEHICLE_ACTIVED), false, mActivateStateObserver);
        } else {
            mActivated = true;
        }

        if (DBG) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.chleon.accstate.changed");
            filter.addAction("com.chleon.alarm.changed");
            filter.addAction("com.chleon.fault.changed");
            filter.addAction("com.chleon.activated.changed");
            filter.addAction("com.chleon.vehicle.connected");
            filter.addAction("com.chleon.dbg.interval");
            filter.addAction("com.chleon.can.changed");
            registerReceiver(mDbgReceiver, filter);
        }

        handleActivatedChanged(mActivated);
    }

    private void tryPublishService(String reason) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && mVehicleSvcConnected) {
            // simple param check
            if (mVin == null || mVin.length() != 17) {
                MyLog.e(TAG, "Error, mVin = " + mVin);
                return;
            }
            if (mIMEI == null || mIMEI.length() != 15) {
                MyLog.e(TAG, "Error, mIMEI = " + mIMEI);
                return;
            }

            if (StorageUtils.configFileExists(mContext)) {
                if (!mPublished) {
                    mPublished = true;
                    publish();
                }
            } else {
                // max retry times reached means config file is absent, generate it from asset file
                if (mRetryTime >= MAX_RETRY_TIMES) {
                    //StorageUtils.generateConfigFile(mContext);
                    if (!mPublished) {
                        mPublished = true;
                        publish();
                    }
                } else {
                    mRetryHandler.sendEmptyMessageDelayed(MSG_RETRY, RETRY_INTERVAL);
                    mRetryTime++;
                }
            }
        } else {
            // will try again when SDCARD mounted or VehicleSvc connected
        }
    }

    private void tryUnpublishService(String reason) {
        if (mPublished) {
            unpublish();
            mPublished = false;
        } else {
            mRetryHandler.removeMessages(MSG_RETRY);
            mRetryTime = 0;
        }
    }

    private void trigger() {
        if (mCacheBean.getAccStatus() == 1) {
            handleAccReport();
        }

        //TODO check all alarm ?
        if (mCacheBean.getStatusMsg().isVibrationAlarm()
                || mCacheBean.getStatusMsg().isCollisionAlarm()) {
            // no need to report twice or more
            //handleAlarmReport(AlarmType.VIBRATION);
            handleAlarmReport(AlarmType.COLLISION);
        }

        if (!mCacheBean.getFaultMsg().getFaultCodes().isEmpty()) {
            handleFaultReport();
        }
    }

    private void publish() {
        MyLog.d(TAG, "publish");

        mConfigBean = StorageUtils.getConfigBean(mContext);
        mDbService.updateMsgsInDataBase(0);

        mServerConnected = false;
        mLogined = false;
        mLoginId = 0;

        mHeartBeatId = 0;
        mHeartbeatRetryTimes = 0;
        mLastHeartBeatTimeStamp = 0;

        mTcpServerTimeInitByServer.set(false);
        mTcpServerTime = DateUtils.getMillisFromLocalDate();
        mTcpElapsedRealtime = SystemClock.elapsedRealtime();

        mLocationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60 * 1000, 0, mLocationListener);
        mLocationMgr.addNmeaListener(mNmeaListener);

        mConnectivityMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mConnectivityReceiver, intentFilter);
        mConnectivityReceiverRegistered = true;
        NetworkInfo info = mConnectivityMgr.getActiveNetworkInfo();
        if (info != null) {
            if (info.isConnected()) {
                mNetworkConnected = true;
                deinitLocation();
                initLocation();
            }
        }

        mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyMgr.listen(mPhoneStateListener,
                PhoneStateListener.LISTEN_SERVICE_STATE
                        | PhoneStateListener.LISTEN_CELL_LOCATION
                        | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        boolean hasIccCard = mTelephonyMgr.hasIccCard();
        mCacheBean.setHasIccCard(hasIccCard);

        mSensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorMgr.registerListener(mSensorEventListener, sensor, SensorManager.SENSOR_DELAY_GAME);

        mAlarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(ACTION_LOGIN_RETRY);
        filter.addAction(ACTION_HEART_BEAT);
        filter.addAction(ACTION_TIMER_REPORT);
        filter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        registerReceiver(mWorkReceiver, filter);

        mWorkThread = new HandlerThread("workThread");
        mWorkThread.start();
        mWorkThreadHandler = new WorkThreadHandler(mWorkThread.getLooper());

        mDatabaseThread = new HandlerThread("databaseThread");
        mDatabaseThread.start();
        mDatabaseThreadHandler = new DatabaseThreadHandler(mDatabaseThread.getLooper());

        String terminalId;
        if (BuildConfig.PROTOCOL_VERSION == 0x10) {
            terminalId = mIMEI;
        } else {
            terminalId = mVin;
        }
        mCi = new RIL(mContext, terminalId, mConfigBean.getNetworkParam(), mConfigBean.getBackupNetworkParam());
        mCi.registerForAvailable(mWorkThreadHandler, EVENT_SERVER_AVAILABLE, null);
        mCi.registerForNotAvailable(mWorkThreadHandler, EVENT_SERVER_UNAVAILABLE, null);
        mCi.registerForSetParamMsg(mWorkThreadHandler, EVENT_SET_PARAM, null);
        mCi.registerForGetParamMsg(mWorkThreadHandler, EVENT_GET_PARAM, null);
        mCi.registerForNetworkParamChange(mWorkThreadHandler, EVENT_NETWORK_PARAM_CHANGE, null);
        mCi.registerCanMessageChange(mWorkThreadHandler, EVENT_CAN_PARAM, null);

        mUploader = new Uploader();
        mUploadThread = new Thread(mUploader, "uploadThread");
        mUploadThread.start();

        trigger();
    }

    public void unpublish() {
        mLocationMgr.removeUpdates(mLocationListener);
        mLocationMgr.removeNmeaListener(mNmeaListener);

        deinitLocation();

        mTelephonyMgr.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);

        mSensorMgr.unregisterListener(mSensorEventListener);

        unregisterReceiver(mWorkReceiver);

        if (mConnectivityReceiverRegistered) {
            unregisterReceiver(mConnectivityReceiver);
            mConnectivityReceiverRegistered = false;
        }

        // stop all ignore current context ?
        if (mLogined) {
            if (mConfigBean.getTimer().isTimerOn()) {
                stopTimerReport();
            }
            stopHeartBeat();
        } else if (mServerConnected) {
            stopLoginRetry();
        }

        mCi.unregisterForAvailable(mWorkThreadHandler);
        mCi.unregisterForNotAvailable(mWorkThreadHandler);
        mCi.unregisterForSetParamMsg(mWorkThreadHandler);
        mCi.unregisterForGetParamMsg(mWorkThreadHandler);
        mCi.unregisterForNetworkParamChange(mWorkThreadHandler);
        mCi.unregisterCanMessageChange(mWorkThreadHandler);
        // TODO how to destroy mCi ?
        mCi.reset();

        mWorkThread.quit();
        mDatabaseThread.quit();
        mUploadThread.interrupt();

        mConfigBean = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        MyLog.d(TAG, "onBind");
        return null;
    }

    @Override
    public void onDestroy() {
        MyLog.d(TAG, "onDestroy");
        super.onDestroy();

        getContentResolver().unregisterContentObserver(mActivateStateObserver);

        if (DBG) {
            unregisterReceiver(mDbgReceiver);
        }

        handleActivatedChanged(false);
    }

    private class Uploader implements Runnable {

        private List<Long> mWaitingIds = new ArrayList<Long>();

        private List<Long> mWaitingCanIds = new ArrayList<>();

        private synchronized void addIdToWaitingIds(long id) {
            mWaitingIds.add(id);
        }

        private synchronized void addIdToWaitingCanIds(long id) {
            mWaitingCanIds.add(id);
        }

        private synchronized void addIdsToWaitingIds(List<Long> ids) {
            mWaitingIds.addAll(ids);
        }

        private synchronized void addIdsToWaitingCanIds(List<Long> ids) {
            mWaitingCanIds.addAll(ids);
        }

        private synchronized void clearWaitingIds() {
            mWaitingIds.clear();
        }

        private synchronized void clearWaitingCanIds() {
            mWaitingCanIds.clear();
        }

        private synchronized void removeIdFromWaitingIds(long id) {
            Long objId = id;
            mWaitingIds.remove(objId);
        }

        private synchronized void removeIdsFromWaitingIds(List<Long> ids) {
            mWaitingIds.removeAll(ids);
        }

        private synchronized void removeIdFromWaitingCanIds(long id) {
            mWaitingCanIds.remove(id);
        }

        private boolean isDataBaseEmpty() {
            List<Long> copiedWaitingIds = new ArrayList<>();
            synchronized (this) {
                copiedWaitingIds.addAll(mWaitingIds);
            }
            return mDbService.countMsgs(copiedWaitingIds) == 0;
        }

        private boolean isCanDataBaseEmpty() {
            List<Long> copiedWaitingIds = new ArrayList<>();
            synchronized (this) {
                copiedWaitingIds.addAll(mWaitingCanIds);
            }
            return mDbService.countCanMsg(copiedWaitingIds) == 0;
        }

        private boolean isReadyToUpload() {
            MyLog.i(TAG, "mLogined = " + mLogined);
            return mLogined && (!isDataBaseEmpty() || !isCanDataBaseEmpty());
        }

        private void notifyMe() {
            boolean readyToUpload = isReadyToUpload();
            MyLog.d(TAG, "notifyMe, readyToUpload = " + readyToUpload);
            if (readyToUpload) {
                synchronized (this) {
                    this.notify();
                }
            }
        }

        @Override
        public void run() {
            while (!mUploadThread.isInterrupted()) {
                if (!isReadyToUpload()) {
                    MyLog.d(TAG, "run enter wait");
                    synchronized (this) {
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            MyLog.i(TAG, "upload thread exit");
                            break;
                        }
                    }
                    MyLog.d(TAG, "run exit wait");
                }


                IndicatorMsg reportMsg;
                List<Long> copiedWaitingIds = new ArrayList<>();
                synchronized (this) {
                    copiedWaitingIds.addAll(mWaitingIds);
                }
                reportMsg = mDbService.getNextReportMsg(copiedWaitingIds);
                if (reportMsg != null) {
                    if (reportMsg instanceof SingleReportMsg) {
                        SingleReportMsg singleReportMsg = (SingleReportMsg) reportMsg;
                        addIdToWaitingIds(singleReportMsg.getId());
                        Message onCompleted = mWorkThreadHandler.obtainMessage(CMD_HANDLE_SINGLE_REPORT_DONE, singleReportMsg);
                        MyLog.i(TAG, "call ril singleReport");
                        mCi.singleReport(singleReportMsg, onCompleted);
                    } else if (reportMsg instanceof BatchReportMsg) {
                        BatchReportMsg batchReportMsg = (BatchReportMsg) reportMsg;
                        addIdsToWaitingIds(batchReportMsg.getIds());
                        Message onCompleted = mWorkThreadHandler.obtainMessage(CMD_HANDLE_BATCH_REPORT_DONE, batchReportMsg);
                        MyLog.i(TAG, "call ril batchReport");
                        mCi.batchReport(batchReportMsg, onCompleted);
                    } else {
                        //TODO
                    }
                }
                // give other thread a chance to execute
                // TODO 新增Can消息的检测发送
                copiedWaitingIds = new ArrayList<>();
                synchronized (this) {
                    copiedWaitingIds.addAll(mWaitingCanIds);
                }
                List<CanReqMsg> canReqMsgList = mDbService.getNextReportCanMsg(copiedWaitingIds);
                for (CanReqMsg canReqMsg : canReqMsgList) {
                    MyLog.i(TAG, "call ril can report");
                    byte[] data = canReqMsg.getMsg();
                    MyLog.i(TAG, "b string = " + new String(data));
                    for (byte b : data) {
                        MyLog.i(TAG, "b ===> " + b);
                    }

                    addIdToWaitingCanIds(canReqMsg.getId());
                    Message onCompleted = mWorkThreadHandler.obtainMessage(CMD_HANDLE_CAN_REPORT_DONE, canReqMsg);
                    mCi.canReport(canReqMsg, onCompleted);
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    MyLog.i(TAG, "upload thread exit");
                    break;
                }
            }
        }
    }

    // ----------------------------------- begin ----------------------------------
    // |                                                                          |
    // |   add for dbg logic                                                      |
    // |                                                                          |
    // ----------------------------------------------------------------------------

    private int mDbgInterval = 50;

    private BroadcastReceiver mDbgReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            MyLog.d(TAG, "action = " + action);
            if ("com.chleon.accstate.changed".equals(action)) {
                int accStatus = intent.getIntExtra("state", 0);
                boolean updated = mCacheBean.updateAccStatus(accStatus);
                if (updated) {
                    handleAccReport();
                }
            } else if ("com.chleon.alarm.changed".equals(action)) {
                int type = intent.getIntExtra("type", 0);
                AlarmType alarmType = AlarmType.getAlarmType(type);
                handleAlarmReport(alarmType);
            } else if ("com.chleon.fault.changed".equals(action)) {
                handleFaultReport();
            } else if ("com.chleon.activated.changed".equals(action)) {
                boolean activated = intent.getIntExtra("activated", 0) == 1;
                if (mActivated != activated) {
                    mActivated = activated;
                    handleActivatedChanged(activated);
                }
            } else if ("com.chleon.vehicle.connected".equals(action)) {
                boolean connected = intent.getIntExtra("connected", 0) == 1;
                if (mVehicleSvcConnected != connected) {
                    mVehicleSvcConnected = connected;
                    handleVehicleSvcConnected(connected);
                }
            } else if ("com.chleon.dbg.interval".equals(action)) {
                mDbgInterval = intent.getIntExtra("interval", 50);
            } else if ("com.chleon.can.changed".equals(action)) {
                byte[] b = new byte[]{1, 2, 2, 2, 2, 2, 2, 2, 22, 2, 22, 22};
                MyLog.i(TAG, " b = " + CodecUtils.bytesToHexString(b));
                MyLog.i(TAG, "b string = " + new String(b));
                handleCanReport(b);
            } else {
                // TODO
            }
        }
    };

    // ----------------------------------- begin ----------------------------------
    // |                                                                          |
    // |   add for activate logic                                                 |
    // |                                                                          |
    // ----------------------------------------------------------------------------

    private void handleActivatedChanged(boolean activated) {
        if (activated) {
            mDbService = new DatabaseService(mContext);

            mVehicleInfo = new VehicleInfo(mContext, mVehicleInfoCallBack);

            if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                IntentFilter sdcardFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
                sdcardFilter.addDataScheme("file");
                registerReceiver(mSDCardReceiver, sdcardFilter);
                mSDCardReceiverRegistered = true;
            }

            tryPublishService("activated");
        } else {
            if (mVehicleInfo != null) {
                mVehicleInfo.unregisterVehicleInfoReceiver();
                mVehicleInfo = null;
            }

            if (mSDCardReceiverRegistered) {
                unregisterReceiver(mSDCardReceiver);
            }

            tryUnpublishService("deactivated");

            if (mDbService != null) {
                mDbService.close();
                mDbService = null;
            }
        }
    }

    private boolean isActivated() {
        int activateState = Settings.System.getInt(getContentResolver(), KEY_VEHICLE_ACTIVED, 0);
        return activateState == 1;
    }

    private ContentObserver mActivateStateObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            boolean activated = isActivated();
            if (mActivated != activated) {
                mActivated = activated;
                handleActivatedChanged(activated);
            }
        }
    };

    // ----------------------------------- begin ----------------------------------
    // |                                                                          |
    // |   add for vehicleSvc logic                                               |
    // |                                                                          |
    // ----------------------------------------------------------------------------

    private void handleVehicleSvcConnected(boolean isConnected) {
        if (isConnected) {
            // if feature is false, only do this once
            if (UNPUBLISH_IF_VEHICLESVC_DISCONNECTED || !mPublished) {
                mVin = mVehicleInfo.getVinCode();
                if (mVin == null || mVin.length() != 17) {
                    mVin = Settings.System.getString(getContentResolver(), KEY_VEHICLE_VIN);
                }
                mIMEI = mVehicleInfo.getIMEI();

                if (DBG) {
                    mVin = "TESTZHOU000000001";
                    mIMEI = "861304020421719";
                }

                if (mVin != null && mVin.length() == 17) {
                    String vin = mDbService.getString("vin");
                    if (!mVin.equals(vin)) {
                        mDbService.putString("vin", mVin);
                        mDbService.deleteMsgFromDataBase();
                    }
                }

                mCacheBean = new CacheBean();
                mCacheBean.updateAccStatus(mVehicleInfo.getAccState());
                mCacheBean.updateStatusMsg(AlarmType.COLLISION, mVehicleInfo.getCollisionWarning() == 1);
                mCacheBean.updateStatusMsg(AlarmType.VIBRATION, mVehicleInfo.getVibrationWarning() == 1);
                mCacheBean.updateAdditionMsg(mVehicleInfo);
                mCacheBean.updateFaultMsg(mVehicleInfo.getFaultCode());

                if (LOW_LATENCY_HAS_PRIORITY) {
                    if (COMBINE_SPEED_WITH_GSENSOR) {
                        if (IS_CAN_SPEED_AVAILABLE) {
                            mSpeedState = AccelerationState.NORMAL;
                            mLastSpeed = mVehicleInfo.getCarSpeed();
                            mLastSpeedTimestamp = SystemClock.elapsedRealtime();
                        }
                    }
                }

                tryPublishService("vehicleSvcConnected");
            }
        } else {
            if (UNPUBLISH_IF_VEHICLESVC_DISCONNECTED) {
                tryUnpublishService("vehicleSvcDisconnected");
            }
        }
    }

    private VehicleInfoCallBack mVehicleInfoCallBack = new VehicleInfoCallBack() {

        @Override
        public void onAccState(int state) {
            MyLog.i(TAG, "onAccState, " + state);
            boolean updated = mCacheBean.updateAccStatus(state);
            if (mPublished) {
                if (updated) {
                    handleAccReport();
                }
            }
        }

        @Override
        public void onVibrationWarning() {
            boolean vibration = mVehicleInfo.getVibrationWarning() == 1;
            MyLog.i(TAG, "onVibrationWarning, " + vibration);
            if (vibration) {
                boolean updated = mCacheBean.updateStatusMsg(AlarmType.VIBRATION, true);
                if (mPublished) {
                    if (updated) {
                        handleAlarmReport(AlarmType.VIBRATION);
                    }
                }
            } else {
                mCacheBean.updateStatusMsg(AlarmType.VIBRATION, false);
            }
        }

        @Override
        public void onInstantaneousOilConsumption(float value) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onFaultCode(String value) {
            MyLog.i(TAG, "onFaultCode, " + value);
            boolean updated = mCacheBean.updateFaultMsg(value);
            if (mPublished) {
                if (updated) {
                    handleFaultReport();
                }
            }
        }

        @Override
        public void onEngineWheel(int value) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onCollisionWarning() {
            boolean collision = mVehicleInfo.getCollisionWarning() == 1;
            MyLog.i(TAG, "onCollisionWarning, " + collision);
            if (collision) {
                boolean updated = mCacheBean.updateStatusMsg(AlarmType.COLLISION, true);
                if (mPublished) {
                    if (updated) {
                        handleAlarmReport(AlarmType.COLLISION);
                    }
                }
            } else {
                mCacheBean.updateStatusMsg(AlarmType.COLLISION, false);
            }
        }

        @Override
        public void onCarSpeed(int speed) {
            MyLog.i(TAG, "onCarSpeed, " + speed);
            if (mPublished) {
                if (LOW_LATENCY_HAS_PRIORITY) {
                    if (COMBINE_SPEED_WITH_GSENSOR) {
                        if (IS_CAN_SPEED_AVAILABLE) {
                            if (speed != -1) {
                                long now = SystemClock.elapsedRealtime();
                                long interval = now - mLastSpeedTimestamp;
                                float delta = ((speed - mLastSpeed) * 1000) / (interval * 3.6f);
                                float accelerationTrigger = mConfigBean.getRestriction().getAccelerationX() / 1000f;
                                float decelerationTrigger = mConfigBean.getRestriction().getDecelerationX() / 1000f;
                                if (delta > accelerationTrigger / 2) {
                                    mSpeedState = AccelerationState.ACCELERATION;
                                } else if (delta < -decelerationTrigger / 2) {
                                    mSpeedState = AccelerationState.DECELERATION;
                                } else {
                                    mSpeedState = AccelerationState.NORMAL;
                                }
                                mLastSpeed = speed;
                                mLastSpeedTimestamp = now;
                            }
                        }
                    }
                }
                int triggerSpeed = mConfigBean.getRestriction().getMaxSpeed();
                if (speed > triggerSpeed) {
                    boolean updated = mCacheBean.updateStatusMsg(AlarmType.OVERSPEED, true);
                    if (updated) {
                        boolean alarmReport = mConfigBean.getAlarmReport().getReportState();
                        if (alarmReport) {
                            handleAlarmReport(AlarmType.OVERSPEED);
                        }
                    }
                } else {
                    mCacheBean.updateStatusMsg(AlarmType.OVERSPEED, false);
                }
            }
        }

        @Override
        public void onICCID(String iccid) {
            //TODO 获得ICCID
        }

        @Override
        public void onIMEI(String imei) {
            if (!DBG) {
                mIMEI = imei;
                tryPublishService("imei");
            }
        }

        @Override
        public void isVehicleInfoConnect(boolean connect) {
            MyLog.i(TAG, "isVehicleInfoConnect, " + connect);
            if (mVehicleSvcConnected != connect) {
                mVehicleSvcConnected = connect;
                dumpVehicleInfo(mVehicleInfo);
                handleVehicleSvcConnected(connect);
            }
        }

        @Override
        public void onCanDataChanged(byte data[]) {
            MyLog.i(TAG, "onCanDataChanged...");
            handleCanReport(data);
        }

        private void dumpVehicleInfo(VehicleInfo vehicleInfo) {
            MyLog.d(TAG, "getAccState:" + vehicleInfo.getAccState());
            MyLog.d(TAG, "getAverageOilConsumption:" + vehicleInfo.getAverageOilConsumption());
            MyLog.d(TAG, "getCarOnlyCode:" + vehicleInfo.getCarOnlyCode());
            MyLog.d(TAG, "getCarSpeed:" + vehicleInfo.getCarSpeed());
            MyLog.d(TAG, "getCollisionWarning:" + vehicleInfo.getCollisionWarning());
            MyLog.d(TAG, "getEngineWheel:" + vehicleInfo.getEngineWheel());
            MyLog.d(TAG, "getFaultCode:" + vehicleInfo.getFaultCode());
            MyLog.d(TAG, "getFuelLevel:" + vehicleInfo.getFuelLevel());
            MyLog.d(TAG, "getICCID:" + vehicleInfo.getICCID());
            MyLog.d(TAG, "getIMEI:" + vehicleInfo.getIMEI());
            MyLog.d(TAG, "getInletTemperature:" + vehicleInfo.getInletTemperature());
            MyLog.d(TAG, "getInstantaneousOilConsumption:" + vehicleInfo.getInstantaneousOilConsumption());
            MyLog.d(TAG, "getTotalFuelConsumption:" + vehicleInfo.getTotalFuelConsumption());
            MyLog.d(TAG, "getTotalMileage:" + vehicleInfo.getTotalMileage());
            MyLog.d(TAG, "getTravelMileage:" + vehicleInfo.getTravelMileage());
            MyLog.d(TAG, "getVibrationWarning:" + vehicleInfo.getVibrationWarning());
            MyLog.d(TAG, "getVinCode:" + vehicleInfo.getVinCode());
            MyLog.d(TAG, "getWaterTemperature:" + vehicleInfo.getWaterTemperature());
        }
    };

    /**
     * 上报当前的Can的消息
     *
     * @param data can消息数据
     */
    private void handleCanReport(byte[] data) {
        CanReqMsg canReqMsg = new CanReqMsg();
        canReqMsg.setTimestamp(getTcpServerTime());
        canReqMsg.setMsg(data);
        canReqMsg.setShouldAck(false);
        Location location = mLocationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (mMapbarSDKAuthSuccess) {
            location = mLocation;
        }
        mCacheBean.updateLocationMsg(location);
        canReqMsg.setLocationMsg(mCacheBean.getLocationMsg());
        fireInsertCanMsgToDataBase(canReqMsg);
    }

    private void fireInsertCanMsgToDataBase(CanReqMsg canReqMsg) {
        canReqMsg.setElapsed(SystemClock.elapsedRealtime());
        try {
            Message msg = mDatabaseThreadHandler.obtainMessage(EVENT_INSERT_CAN_MSG_DATABASE, canReqMsg);
            mDatabaseThreadHandler.sendMessage(msg);
        } catch (Exception e) {
            Message msg = new Message();
            msg.what = EVENT_INSERT_CAN_MSG_DATABASE;
            msg.obj = canReqMsg;
            mDatabaseThreadHandler.sendMessage(msg);
            e.printStackTrace();
        }
    }

    // ----------------------------------- begin ----------------------------------
    // |                                                                          |
    // |   add for sdcard logic                                                   |
    // |                                                                          |
    // ----------------------------------------------------------------------------

    private BroadcastReceiver mSDCardReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            MyLog.i(TAG, "action = " + action);
            if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
                Uri uri = intent.getData();
                if (uri != null) {
                    String path = uri.getLastPathSegment();
                    if (TextUtils.equals(path, "sdcard0")) {
                        tryPublishService("sdcardMounted");
                    }
                }
            }
        }
    };

    // ----------------------------------- begin ----------------------------------
    // |                                                                          |
    // |   add for publish retry logic                                            |
    // |                                                                          |
    // ----------------------------------------------------------------------------

    private Handler mRetryHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RETRY: {
                    tryPublishService("retry");
                    break;
                }
            }
        }
    };

    // ----------------------------------- begin ----------------------------------
    // |                                                                          |
    // |   add for publish logic                                                  |
    // |                                                                          |
    // ----------------------------------------------------------------------------

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    if (DBG_GPS) {
                        MyLog.d(TAG, "AVAILABLE");
                    }
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    if (DBG_GPS) {
                        MyLog.d(TAG, "OUT_OF_SERVICE");
                    }
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    if (DBG_GPS) {
                        MyLog.d(TAG, "TEMPORARILY_UNAVAILABLE");
                    }
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }
    };

    private GpsStatus.NmeaListener mNmeaListener = new GpsStatus.NmeaListener() {
        @Override
        public void onNmeaReceived(long timestamp, String nmea) {
            //$GPGSA,A,3,193,25,12,24,15,14,32,18,,,,,1.42,1.11,0.88*34
            if (nmea != null && nmea.startsWith("$GPGSA")) {
                if (DBG_GPS) {
                    MyLog.d(TAG, "nmea = " + nmea);
                }
                String[] strs = nmea.split("\\*");
                if (strs != null && strs.length == 2) {
                    strs = strs[0].split(",");
                    if (strs != null && strs.length == 18) {
                        if (DBG_GPS) {
                            MyLog.d(TAG, "nmea = " + strs[15] + "," + strs[16] + "," + strs[17]);
                        }
                        mCacheBean.updateNmea(Float.valueOf(strs[16]), Float.valueOf(strs[17]), 0.0f);
                    } else {
                        //TODO
                    }
                } else {
                    //TODO
                }
            }
        }
    };

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            if (DBG_MODEM) {
                MyLog.d(TAG, "onServiceStateChanged, " + serviceState);
            }
            mCacheBean.updateBaseStationMsg(serviceState, null, null);
        }

        @Override
        public void onCellLocationChanged(CellLocation location) {
            if (DBG_MODEM) {
                MyLog.d(TAG, "onCellLocationChanged, " + location);
            }
            //mCacheBean.updateBaseStationMsg(null, location, null);
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            if (DBG_MODEM) {
                MyLog.d(TAG, "onSignalStrengthsChanged, " + signalStrength);
            }
            mCacheBean.updateBaseStationMsg(null, null, signalStrength);
        }
    };

    private class GSensorEventListener implements SensorEventListener {

        private static final int DIMENSIONS = 3;

        private static final int INDEX_X = 2;

        private static final int INDEX_Y = 0;

        private static final int INDEX_Z = 1;

        private static final int FAST_TRIGGER_COUNT = 3;

        private static final int ACCURATE_TRIGGER_COUNT = 10;

        private AccelerationState[] mGSensorState = new AccelerationState[]{
                AccelerationState.NORMAL, AccelerationState.NORMAL, AccelerationState.NORMAL
        };

        private int[] mAccelerationCounter = new int[]{
                0, 0, 0
        };

        private int[] mDecelerationCounter = new int[]{
                0, 0, 0
        };

        private boolean mNeedReport = false;

        private float mLastGpsSpeed;

        private GSensorEventBean[] mGSensorBeans = new GSensorEventBean[DIMENSIONS];

        public GSensorEventListener() {
            if (!LOW_LATENCY_HAS_PRIORITY) {
                for (int i = 0; i < DIMENSIONS; i++) {
                    mGSensorBeans[i] = new GSensorEventBean(DIMENSIONS);
                }
            }
        }

        private AlarmType getAlarmType(int axis, boolean acceleration) {
            AlarmType alarmType = AlarmType.UNKNOWN;
            if (acceleration) {
                switch (axis) {
                    case INDEX_X:
                        alarmType = AlarmType.ACCELERATION_X;
                        break;
                    case INDEX_Y:
                        alarmType = AlarmType.ACCELERATION_Y;
                        break;
                    case INDEX_Z:
                        alarmType = AlarmType.ACCELERATION_Z;
                        break;
                    default:
                        break;
                }
            } else {
                switch (axis) {
                    case INDEX_X:
                        alarmType = AlarmType.DECELERATION_X;
                        break;
                    case INDEX_Y:
                        alarmType = AlarmType.DECELERATION_Y;
                        break;
                    case INDEX_Z:
                        alarmType = AlarmType.DECELERATION_Z;
                        break;
                    default:
                        break;
                }
            }
            return alarmType;
        }

        private void handleAcceleration(int axis, boolean acceleration) {
            mCacheBean.updateStatusMsg(getAlarmType(axis, !acceleration), false);
            AlarmType alarmType = getAlarmType(axis, acceleration);
            boolean updated = mCacheBean.updateStatusMsg(alarmType, true);
            if (updated) {
                boolean alarmReport = mConfigBean.getAlarmReport().getReportState();
                if (alarmReport) {
                    handleAlarmReport(alarmType);
                }
            }
        }

        private void handleNormal(int axis) {
            mCacheBean.updateStatusMsg(getAlarmType(axis, true), false);
            mCacheBean.updateStatusMsg(getAlarmType(axis, false), false);
        }

        private void handleSensorChangeAsFastAsPossible(GSensorEventBean event, float[] accelerationTriggers, float[] decelerationTriggers) {
            for (int i = 0; i < DIMENSIONS; i++) {
                if (event.values[i] > accelerationTriggers[i] / 2) {
                    if (COMBINE_SPEED_WITH_GSENSOR) {
                        if (i == INDEX_X) {
                            if (mNeedReport) {
                                if (IS_CAN_SPEED_AVAILABLE) {
                                    if (mGSensorState[i] == AccelerationState.ACCELERATION
                                            && mSpeedState == AccelerationState.ACCELERATION) {
                                        handleAcceleration(i, true);
                                        mNeedReport = false;
                                    } else if (mGSensorState[i] == AccelerationState.DECELERATION
                                            && mSpeedState == AccelerationState.DECELERATION) {
                                        handleAcceleration(i, false);
                                    } else {
                                        //
                                    }
                                } else {
                                    if (mGSensorState[i] != AccelerationState.NORMAL) {
                                        Location location = mLocationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                        if (mMapbarSDKAuthSuccess) {
                                            location = mLocation;
                                        }
                                        if (location != null) {
                                            long interval = SystemClock.elapsedRealtime() - mLastSpeedTimestamp;
                                            float delta = (location.getSpeed() - mLastGpsSpeed) * 1000 / interval;
                                            if (mGSensorState[i] == AccelerationState.ACCELERATION
                                                    && delta > accelerationTriggers[INDEX_X] / 2) {
                                                handleAcceleration(i, true);
                                                mNeedReport = false;
                                            } else if (mGSensorState[i] == AccelerationState.DECELERATION
                                                    && delta < -decelerationTriggers[INDEX_X] / 2) {
                                                handleAcceleration(i, false);
                                            } else {
                                                //
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (mGSensorState[i] == AccelerationState.DECELERATION) {
                        mGSensorState[i] = AccelerationState.NORMAL;
                        if (COMBINE_SPEED_WITH_GSENSOR) {
                            if (i == INDEX_X) {
                                mNeedReport = false;
                            }
                        }
                        handleNormal(i);
                    }
                    mDecelerationCounter[i] = 0;
                    mAccelerationCounter[i]++;
                    if (event.values[i] > accelerationTriggers[i]) {
                        if (mAccelerationCounter[i] > FAST_TRIGGER_COUNT && mGSensorState[i] != AccelerationState.ACCELERATION) {
                            mGSensorState[i] = AccelerationState.ACCELERATION;
                            MyLog.d(TAG, "onSensorChanged, " + event.values[INDEX_X] + ", " + event.values[INDEX_Y] + ", " + event.values[INDEX_Z]);
                            mCacheBean.updateGSensorMsg(event.accuracy, event.values[INDEX_X], event.values[INDEX_Y], event.values[INDEX_Z]);
                            if (COMBINE_SPEED_WITH_GSENSOR) {
                                if (i == INDEX_X) {
                                    if (IS_CAN_SPEED_AVAILABLE) {
                                        if (mSpeedState != AccelerationState.ACCELERATION) {
                                            mNeedReport = true;
                                            continue;
                                        } else {
                                            mNeedReport = false;
                                        }
                                    } else {
                                        Location location = mLocationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                        if (mMapbarSDKAuthSuccess) {
                                            location = mLocation;
                                        }
                                        if (location != null) {
                                            mLastGpsSpeed = location.getSpeed();
                                            mLastSpeedTimestamp = SystemClock.elapsedRealtime();
                                            mNeedReport = true;
                                            continue;
                                        } else {
                                            mNeedReport = false;
                                        }
                                    }
                                }
                            }
                            handleAcceleration(i, true);
                        }
                    }
                } else if (event.values[i] < -decelerationTriggers[i] / 2) {
                    if (COMBINE_SPEED_WITH_GSENSOR) {
                        if (i == INDEX_X) {
                            if (mNeedReport) {
                                if (IS_CAN_SPEED_AVAILABLE) {
                                    if (mGSensorState[i] == AccelerationState.DECELERATION
                                            && mSpeedState == AccelerationState.DECELERATION) {
                                        handleAcceleration(i, false);
                                        mNeedReport = false;
                                    } else if (mGSensorState[i] == AccelerationState.ACCELERATION
                                            && mSpeedState == AccelerationState.ACCELERATION) {
                                        handleAcceleration(i, true);
                                    } else {
                                        //
                                    }
                                } else {
                                    if (mGSensorState[i] != AccelerationState.NORMAL) {
                                        Location location = mLocationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                        if (mMapbarSDKAuthSuccess) {
                                            location = mLocation;
                                        }
                                        if (location != null) {
                                            long interval = SystemClock.elapsedRealtime() - mLastSpeedTimestamp;
                                            float delta = (location.getSpeed() - mLastGpsSpeed) * 1000 / interval;
                                            if (mGSensorState[i] == AccelerationState.DECELERATION
                                                    && delta < -decelerationTriggers[INDEX_X] / 2) {
                                                handleAcceleration(i, false);
                                                mNeedReport = false;
                                            } else if (mGSensorState[i] == AccelerationState.ACCELERATION
                                                    && delta > accelerationTriggers[INDEX_X] / 2) {
                                                handleAcceleration(i, true);
                                            } else {
                                                //
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (mGSensorState[i] == AccelerationState.ACCELERATION) {
                        mGSensorState[i] = AccelerationState.NORMAL;
                        if (COMBINE_SPEED_WITH_GSENSOR) {
                            if (i == INDEX_X) {
                                mNeedReport = false;
                            }
                        }
                        handleNormal(i);
                    }
                    mAccelerationCounter[i] = 0;
                    mDecelerationCounter[i]++;
                    if (event.values[i] < -decelerationTriggers[i]) {
                        if (mDecelerationCounter[i] > FAST_TRIGGER_COUNT && mGSensorState[i] != AccelerationState.DECELERATION) {
                            mGSensorState[i] = AccelerationState.DECELERATION;
                            MyLog.d(TAG, "onSensorChanged, " + event.values[INDEX_X] + ", " + event.values[INDEX_Y] + ", " + event.values[INDEX_Z]);
                            mCacheBean.updateGSensorMsg(event.accuracy, event.values[INDEX_X], event.values[INDEX_Y], event.values[INDEX_Z]);
                            if (COMBINE_SPEED_WITH_GSENSOR) {
                                if (i == INDEX_X) {
                                    if (IS_CAN_SPEED_AVAILABLE) {
                                        if (mSpeedState != AccelerationState.DECELERATION) {
                                            mNeedReport = true;
                                            continue;
                                        } else {
                                            mNeedReport = false;
                                        }
                                    } else {
                                        Location location = mLocationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                        if (mMapbarSDKAuthSuccess) {
                                            location = mLocation;
                                        }
                                        if (location != null) {
                                            mLastGpsSpeed = location.getSpeed();
                                            mLastSpeedTimestamp = SystemClock.elapsedRealtime();
                                            mNeedReport = true;
                                            continue;
                                        } else {
                                            mNeedReport = false;
                                        }
                                    }
                                }
                            }
                            handleAcceleration(i, false);
                        }
                    }
                } else {
                    if (COMBINE_SPEED_WITH_GSENSOR) {
                        if (i == INDEX_X) {
                            if (mNeedReport) {
                                if (IS_CAN_SPEED_AVAILABLE) {
                                    if (mSpeedState == AccelerationState.ACCELERATION
                                            && mGSensorState[i] == AccelerationState.ACCELERATION) {
                                        handleAcceleration(i, true);
                                    } else if (mSpeedState == AccelerationState.DECELERATION
                                            && mGSensorState[i] == AccelerationState.DECELERATION) {
                                        handleAcceleration(i, false);
                                    } else {
                                        //
                                    }
                                } else {
                                    if (mGSensorState[i] != AccelerationState.NORMAL) {
                                        Location location = mLocationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                        if (mMapbarSDKAuthSuccess) {
                                            location = mLocation;
                                        }
                                        if (location != null) {
                                            long interval = SystemClock.elapsedRealtime() - mLastSpeedTimestamp;
                                            float delta = (location.getSpeed() - mLastGpsSpeed) * 1000 / interval;
                                            if (mGSensorState[i] == AccelerationState.ACCELERATION
                                                    && delta > accelerationTriggers[INDEX_X] / 2) {
                                                handleAcceleration(i, true);
                                            } else if (mGSensorState[i] == AccelerationState.DECELERATION
                                                    && delta < -decelerationTriggers[INDEX_X] / 2) {
                                                handleAcceleration(i, false);
                                            } else {
                                                //
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (mGSensorState[i] != AccelerationState.NORMAL) {
                        mGSensorState[i] = AccelerationState.NORMAL;
                        if (COMBINE_SPEED_WITH_GSENSOR) {
                            if (i == INDEX_X) {
                                mNeedReport = false;
                            }
                        }
                        handleNormal(i);
                    }
                    mAccelerationCounter[i] = 0;
                    mDecelerationCounter[i] = 0;
                }
            }
        }

        private void handleSensorChangeAsAccurateAsPossible(GSensorEventBean event, float[] accelerationTriggers, float[] decelerationTriggers) {
            for (int i = 0; i < DIMENSIONS; i++) {
                if (event.values[i] > accelerationTriggers[i] / 3) {
                    if (mDecelerationCounter[i] > ACCURATE_TRIGGER_COUNT && mGSensorBeans[i].values[i] < -decelerationTriggers[i]) {
                        MyLog.d(TAG, "onSensorChanged, " + mGSensorBeans[i].values[INDEX_X] + ", " + mGSensorBeans[i].values[INDEX_Y] + ", " + mGSensorBeans[i].values[INDEX_Z]);
                        mCacheBean.updateGSensorMsg(mGSensorBeans[i].accuracy, mGSensorBeans[i].values[INDEX_X], mGSensorBeans[i].values[INDEX_Y], mGSensorBeans[i].values[INDEX_Z]);
                        handleAcceleration(i, false);
                        for (int j = 0; j < DIMENSIONS; j++) {
                            mGSensorBeans[i].values[j] = 0;
                        }
                        handleNormal(i);
                    }
                    mDecelerationCounter[i] = 0;
                    mAccelerationCounter[i]++;
                    if (mGSensorBeans[i].values[i] < event.values[i]) {
                        mGSensorBeans[i].accuracy = event.accuracy;
                        mGSensorBeans[i].timestamp = event.timestamp;
                        for (int j = 0; j < DIMENSIONS; j++) {
                            mGSensorBeans[i].values[j] = event.values[j];
                        }
                    }
                } else if (event.values[i] < -decelerationTriggers[i] / 3) {
                    if (mAccelerationCounter[i] > ACCURATE_TRIGGER_COUNT && mGSensorBeans[i].values[i] > accelerationTriggers[i]) {
                        MyLog.d(TAG, "onSensorChanged, " + mGSensorBeans[i].values[INDEX_X] + ", " + mGSensorBeans[i].values[INDEX_Y] + ", " + mGSensorBeans[i].values[INDEX_Z]);
                        mCacheBean.updateGSensorMsg(mGSensorBeans[i].accuracy, mGSensorBeans[i].values[INDEX_X], mGSensorBeans[i].values[INDEX_Y], mGSensorBeans[i].values[INDEX_Z]);
                        handleAcceleration(i, true);
                        for (int j = 0; j < DIMENSIONS; j++) {
                            mGSensorBeans[i].values[j] = 0;
                        }
                        handleNormal(i);
                    }
                    mAccelerationCounter[i] = 0;
                    mDecelerationCounter[i]++;
                    if (mGSensorBeans[i].values[i] > event.values[i]) {
                        mGSensorBeans[i].accuracy = event.accuracy;
                        mGSensorBeans[i].timestamp = event.timestamp;
                        for (int j = 0; j < DIMENSIONS; j++) {
                            mGSensorBeans[i].values[j] = event.values[j];
                        }
                    }
                } else {
                    if (mDecelerationCounter[i] > ACCURATE_TRIGGER_COUNT && mGSensorBeans[i].values[i] < -decelerationTriggers[i]) {
                        MyLog.d(TAG, "onSensorChanged, " + mGSensorBeans[i].values[INDEX_X] + ", " + mGSensorBeans[i].values[INDEX_Y] + ", " + mGSensorBeans[i].values[INDEX_Z]);
                        mCacheBean.updateGSensorMsg(mGSensorBeans[i].accuracy, mGSensorBeans[i].values[INDEX_X], mGSensorBeans[i].values[INDEX_Y], mGSensorBeans[i].values[INDEX_Z]);
                        handleAcceleration(i, false);
                        for (int j = 0; j < DIMENSIONS; j++) {
                            mGSensorBeans[i].values[j] = 0;
                        }
                        handleNormal(i);
                    } else if (mAccelerationCounter[i] > ACCURATE_TRIGGER_COUNT && mGSensorBeans[i].values[i] > accelerationTriggers[i]) {
                        MyLog.d(TAG, "onSensorChanged, " + mGSensorBeans[i].values[INDEX_X] + ", " + mGSensorBeans[i].values[INDEX_Y] + ", " + mGSensorBeans[i].values[INDEX_Z]);
                        mCacheBean.updateGSensorMsg(mGSensorBeans[i].accuracy, mGSensorBeans[i].values[INDEX_X], mGSensorBeans[i].values[INDEX_Y], mGSensorBeans[i].values[INDEX_Z]);
                        handleAcceleration(i, true);
                        for (int j = 0; j < DIMENSIONS; j++) {
                            mGSensorBeans[i].values[j] = 0;
                        }
                        handleNormal(i);
                    }
                    mAccelerationCounter[i] = 0;
                    mDecelerationCounter[i] = 0;
                }
            }
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (!mPublished) {
                return;
            }

            if (event == null || event.values == null || event.values.length < DIMENSIONS) {
                return;
            }

            if (LOW_LATENCY_HAS_PRIORITY) {
                if (COMBINE_SPEED_WITH_GSENSOR) {
                    if (IGNORE_GSENSOR_IF_PARKING) {
                        if (IS_CAN_SPEED_AVAILABLE) {
                            if (mVehicleInfo.getCarSpeed() == 0) {
                                return;
                            }
                        } else {
                            Location location = mLocationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (mMapbarSDKAuthSuccess) {
                                location = mLocation;
                            }
                            if (location != null) {
                                if (location.getSpeed() < 0.1) {
                                    return;
                                }
                            }
                        }
                    }
                }
            }

            GSensorEventBean eventBean = new GSensorEventBean(DIMENSIONS);
            float[] accelerationTriggers = new float[DIMENSIONS];
            float[] decelerationTriggers = new float[DIMENSIONS];

            // adjust axis direction and angle
            eventBean.accuracy = event.accuracy;
            eventBean.timestamp = event.timestamp;
            eventBean.values[INDEX_Y] = -event.values[INDEX_Y];
            if (ADJUST_AXIS_ALPHA_ANGLE) {
                eventBean.values[INDEX_X] = (float) ((event.values[INDEX_Z] * SIN_ALPHA - event.values[INDEX_X] * COS_ALPHA) / COS2_ALPHA_SIN2_ALPHA);
                eventBean.values[INDEX_Z] = (float) ((event.values[INDEX_X] * SIN_ALPHA - event.values[INDEX_Z] * COS_ALPHA) / COS2_ALPHA_SIN2_ALPHA);
            } else {
                eventBean.values[INDEX_X] = -event.values[INDEX_X];
                eventBean.values[INDEX_Z] = event.values[INDEX_Z];
            }

            // init triggers
            accelerationTriggers[INDEX_X] = mConfigBean.getRestriction().getAccelerationX() / 1000f;
            accelerationTriggers[INDEX_Y] = mConfigBean.getRestriction().getAccelerationY() / 1000f;
            accelerationTriggers[INDEX_Z] = mConfigBean.getRestriction().getAccelerationZ() / 1000f;
            decelerationTriggers[INDEX_X] = mConfigBean.getRestriction().getDecelerationX() / 1000f;
            decelerationTriggers[INDEX_Y] = mConfigBean.getRestriction().getDecelerationY() / 1000f;
            decelerationTriggers[INDEX_Z] = mConfigBean.getRestriction().getDecelerationZ() / 1000f;

            if (LOW_LATENCY_HAS_PRIORITY) {
                handleSensorChangeAsFastAsPossible(eventBean, accelerationTriggers, decelerationTriggers);
            } else {
                handleSensorChangeAsAccurateAsPossible(eventBean, accelerationTriggers, decelerationTriggers);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub
        }
    }

    private SensorEventListener mSensorEventListener = new GSensorEventListener();

    private BroadcastReceiver mWorkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            MyLog.i(TAG, "action = " + action);
            if (Intent.ACTION_TIME_CHANGED.equals(action)) {
                fireTimeChanged();
            } else if (ACTION_LOGIN_RETRY.equals(action)) {
                fireNextLogin();
            } else if (ACTION_HEART_BEAT.equals(action)) {
                fireNextHeartBeat();
            } else if (ACTION_TIMER_REPORT.equals(action)) {
                handleTimerReport();
                handleDrivingMonitor();
            } else if (TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(action)) {
                String iccState = intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE);
                MyLog.d(TAG, "iccState = " + iccState);
                boolean hasIccCard = mTelephonyMgr.hasIccCard();
                mCacheBean.setHasIccCard(hasIccCard);
            } else {
                // TODO
            }
        }
    };

    //private static final String KEY = "huangh20180109-01-L-T-A00010";
    private static final String KEY = "fyl200180209-01-Z-F-A00010";

    private LocationClient mLocationClient;

    private Location mLocation;

    private int mCount;

    private boolean mMapbarSDKAuthSuccess = false;

    /**
     * 初始化定位
     */
    private void initLocation() {
        try {
            mLocationClient = new LocationClient(this, KEY, new QFAuthResultListener() {
                @Override
                public void onAuthResult(boolean b, String s) {
                    mMapbarSDKAuthSuccess = b;
                    MyLog.i(TAG, "onAuthResult, b = " + b + ", s = " + s);
                    if (mConnectivityReceiverRegistered) {
                        unregisterReceiver(mConnectivityReceiver);
                        mConnectivityReceiverRegistered = false;
                    }
                }
            });
            mLocationClient.enableDebug(false, "/mnt/sdcard/mapbar/log/");
            LocationClientOption option = new LocationClientOption();
            option.setPriority(LocationClientOption.LocationMode.GPS_FIRST);
            option.setScanSpanGPS(5000);// 设置GPS定位最小间隔时间
            option.setGpsExpire(1500);// 设置GPS定位失效时间 毫秒
            option.setGPSCoorType(LocationClientOption.COORTYPE_WD); //坐标类型:84坐标
            option.setScanSpanNetWork(5000);// 设置基站定位最小间隔时间
            option.setResultType(0);// 默认返回逆地理信息
            mLocationClient.setOption(option);
            mLocationClient.addListener(new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    mCount++;
                    mLocation = location;
                    if (mLocation != null) {
                        MyLog.i(TAG, "mCount " + mCount + " " + mLocation.getLatitude() + ",  " + mLocation.getLongitude());
                    } else {
                        MyLog.i(TAG, "mCount " + mCount);
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
            mLocationClient.start();
            MyLog.i(TAG, "-------开始定位---");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deinitLocation() {
        try {
            if (mLocationClient != null) {
                mLocationClient.stop();
                mLocationClient.removeAllListener();
                MyLog.i(TAG, "-------结束定位---");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean mConnectivityReceiverRegistered = false;

    private boolean mNetworkConnected = false;

    private final BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                NetworkInfo info = mConnectivityMgr.getActiveNetworkInfo();
                if (info != null) {
                    if (info.isConnected()) {
                        if (!mNetworkConnected) {
                            mNetworkConnected = true;
                            deinitLocation();
                            initLocation();
                        }
                    } else {
                        mNetworkConnected = false;
                    }
                }
            }
        }
    };
}
