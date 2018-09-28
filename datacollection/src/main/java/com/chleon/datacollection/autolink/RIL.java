package com.chleon.datacollection.autolink;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.*;
import android.os.SystemProperties;

import com.chleon.telematics.*;
import com.chleon.telematics.MsgHeader.MsgType;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@hide}
 */
class RILRequest {
    private static final String LOG_TAG = "DataCollectionRILRequest";

    //***** Class Variables
    static int sNextSerial = 1;
    static Object sSerialMonitor = new Object();
    private static Object sPoolSync = new Object();
    private static RILRequest sPool = null;
    private static int sPoolSize = 0;
    private static final int MAX_POOL_SIZE = 4;

    //***** Instance Variables
    int mSerial;
    MsgHeader.MsgType msgType;
    Object msg;
    int mRequest;
    boolean shouldAck;
    Message mResult;
    byte[] data;
    RILRequest mNext;

    /**
     * Retrieves a new RILRequest instance from the pool.
     *
     * @param request RIL_REQUEST_*
     * @param result  sent when operation completes
     * @return a RILRequest instance from the pool.
     */
    static RILRequest obtain(int request, Message result) {
        RILRequest rr = null;

        synchronized (sPoolSync) {
            if (sPool != null) {
                rr = sPool;
                sPool = rr.mNext;
                rr.mNext = null;
                sPoolSize--;
            }
        }

        if (rr == null) {
            rr = new RILRequest();
        }

        synchronized (sSerialMonitor) {
            rr.mSerial = sNextSerial++;
        }
        rr.mRequest = request;
        rr.mResult = result;
        rr.data = null;

        if (result != null && result.getTarget() == null) {
            throw new NullPointerException("Message target must not be null");
        }

        return rr;
    }

    /**
     * Returns a RILRequest instance to the pool.
     * <p/>
     * Note: This should only be called once per use.
     */
    void release() {
        synchronized (sPoolSync) {
            if (sPoolSize < MAX_POOL_SIZE) {
                this.mNext = sPool;
                sPool = this;
                sPoolSize++;
                mResult = null;
                data = null;
            }
        }
    }

    private RILRequest() {
    }

    static void
    resetSerial() {
        synchronized (sSerialMonitor) {
            sNextSerial = 1;
        }
    }

    String
    serialString() {
        //Cheesy way to do %04d
        StringBuilder sb = new StringBuilder(8);
        String sn;

        sn = Integer.toString(mSerial);

        //sb.append("J[");
        sb.append('[');
        for (int i = 0, s = sn.length(); i < 4 - s; i++) {
            sb.append('0');
        }

        sb.append(sn);
        sb.append(']');
        return sb.toString();
    }

    void
    onError(int error, Object ret) {
        CommandException ex;

        ex = CommandException.fromErrno(error);

        MyLog.d(LOG_TAG, serialString() + "< "
                + RIL.requestToString(mRequest)
                + " error: " + ex);

        if (mResult != null) {
            AsyncResult.forMessage(mResult, ret, ex);
            mResult.sendToTarget();
        }
    }
}

public class RIL extends BaseCommands implements CommandsInterface {
    private static final String LOG_TAG = "DataCollectionRIL";
    private static final Boolean RILJ_LOGD = true;

    private static final boolean DBG_SERVER = false;

    Socket mSocket;
    HandlerThread mSenderThread;
    RILSender mSender;
    Thread mReceiverThread;
    RILReceiver mReceiver;
    // mtk not support WakeLock define by apps, use atomic
    //WakeLock mWakeLock;
    AtomicBoolean mWakeLock;
    int mWakeLockTimeout;
    boolean mClearTimeout = true;
    // The number of requests pending to be sent out, it increases before calling
    // EVENT_SEND and decreases while handling EVENT_SEND. It gets cleared while
    // WAKE_LOCK_TIMEOUT occurs.
    int mRequestMessagesPending;
    // The number of requests sent out but waiting for response. It increases while
    // sending request and decreases while handling response. It should match
    // mRequestList.size() unless there are requests no replied while
    // WAKE_LOCK_TIMEOUT occurs.
    int mRequestMessagesWaiting;

    //I'd rather this be LinkedList or something
    ArrayList<RILRequest> mRequestsList = new ArrayList<RILRequest>();

    //***** Events
    static final int EVENT_SEND = 1;
    static final int EVENT_WAKE_LOCK_TIMEOUT = 2;

    //***** Constants
    static final int DEFAULT_WAKE_LOCK_TIMEOUT = 1 * 60 * 1000;

    String mTerminalId;
    BaseParamMsg.NetworkParamMsg mNetworkParamMsg;
    BaseParamMsg.NetworkParamMsg mBackupNetworkParamMsg;
    RILTester mRILTester;
    Thread mTesterThread;

    private final BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                ConnectivityManager connManager = (ConnectivityManager)
                        context.getSystemService(Context.CONNECTIVITY_SERVICE);
                //NetworkInfo info = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                NetworkInfo info = connManager.getActiveNetworkInfo();
                if (info != null) {
                    if (!info.isConnected() && mSocket != null) {
                        try {
                            TrafficStats.untagSocket(mSocket);
                            mSocket.close();
                        } catch (Exception ex) {
                        }
                    }
                }
            }
        }
    };

    class RILSender extends Handler implements Runnable {
        public RILSender(Looper looper) {
            super(looper);
        }

        //***** Runnable implementation
        public void run() {
            //setup if needed
        }


        //***** Handler implementation
        @Override
        public void handleMessage(Message msg) {
            RILRequest rr = (RILRequest) (msg.obj);
            RILRequest req = null;

            switch (msg.what) {
                case EVENT_SEND:
                    /**
                     * mRequestMessagePending++ already happened for every
                     * EVENT_SEND, thus we must make sure
                     * mRequestMessagePending-- happens once and only once
                     */
                    boolean alreadySubtracted = false;
                    boolean shouldAck = rr.shouldAck;
                    try {
                        Socket s;

                        s = mSocket;

                        if (s == null) {
                            rr.onError(Constants.SERVER_NOT_AVAILABLE, null);
                            rr.release();
                            // TODO ryan:
                            // add shouldAck
                            if(shouldAck) {
                                if (mRequestMessagesPending > 0)
                                    mRequestMessagesPending--;
                                alreadySubtracted = true;
                            }
                            return;
                        }

                        // TODO ryan:
                        // should only add rr which needs ack, otherwise don't add to list,
                        // as acks for server, just ignore error case, as server should resend rr if it's ack missed
                        if(shouldAck) {
                            synchronized (mRequestsList) {
                                mRequestsList.add(rr);
                                mRequestMessagesWaiting++;
                            }

                            if (mRequestMessagesPending > 0)
                                mRequestMessagesPending--;
                            alreadySubtracted = true;
                        }

                        byte[] data = rr.data;

                        // TODO ryan:
                        // MAX_FRAME_SIZE should be considered by network, for example mtu = 1500, use ppp, max = 1452
                        if (data.length > Constants.MAX_FRAME_SIZE) {
                            throw new RuntimeException(
                                    "Frame larger than max bytes allowed! "
                                            + data.length);
                        }

                        MyLog.d(LOG_TAG, "writing packet: " + data.length + " bytes, data=" + CodecUtils.bytesToHexString(data));
                        s.getOutputStream().write(data);
                        // TODO ryan:
                        // add shouldAck
                        if(!shouldAck) {
                            if (rr.mResult != null) {
                                AsyncResult.forMessage(rr.mResult, rr.msg, null);
                                rr.mResult.sendToTarget();
                            }
                            rr.release();
                        }
                    } catch (IOException ex) {
                        MyLog.e(LOG_TAG, "IOException", ex);
                        // TODO ryan:
                        // acks for server, just ignore error case, as server should resend rr if it's ack missed
                        if(shouldAck) {
                            req = findAndRemoveRequestFromList(rr.mSerial);
                            // make sure this request has not already been handled,
                            // eg, if RILReceiver cleared the list.
                            if (req != null || !alreadySubtracted) {
                                rr.onError(Constants.SERVER_NOT_AVAILABLE, null);
                                rr.release();
                            }
                        }
                    } catch (RuntimeException exc) {
                        MyLog.e(LOG_TAG, "Uncaught exception ", exc);
                        // TODO ryan:
                        // acks for server, just ignore error case, as server should resend rr if it's ack missed
                        if(shouldAck) {
                            req = findAndRemoveRequestFromList(rr.mSerial);
                            // make sure this request has not already been handled,
                            // eg, if RILReceiver cleared the list.
                            if (req != null || !alreadySubtracted) {
                                rr.onError(Constants.GENERIC_FAILURE, null);
                                rr.release();
                            }
                        }
                    } finally {
                        // Note: We are "Done" only if there are no outstanding
                        // requests or replies. Thus this code path will only release
                        // the wake lock on errors.

                        // TODO ryan:
                        // add shouldAck
                        if(shouldAck) {
                            releaseWakeLockIfDone();
                        }
                    }

                    // TODO ryan:
                    // add shouldAck
                    if(shouldAck) {
                        if (!alreadySubtracted && mRequestMessagesPending > 0) {
                            mRequestMessagesPending--;
                        }
                    }

                    break;

                case EVENT_WAKE_LOCK_TIMEOUT:
                    // Haven't heard back from the last request.  Assume we're
                    // not getting a response and  release the wake lock.
                    synchronized (mWakeLock) {
                        //if (mWakeLock.isHeld()) {
                        if (mWakeLock.get()) {
                            // The timer of WAKE_LOCK_TIMEOUT is reset with each
                            // new send request. So when WAKE_LOCK_TIMEOUT occurs
                            // all requests in mRequestList already waited at
                            // least DEFAULT_WAKE_LOCK_TIMEOUT but no response.
                            // Reset mRequestMessagesWaiting to enable
                            // releaseWakeLockIfDone().
                            //
                            // Note: Keep mRequestList so that delayed response
                            // can still be handled when response finally comes.
                            if (mRequestMessagesWaiting != 0) {
                                MyLog.d(LOG_TAG, "NOTE: mReqWaiting is NOT 0 but"
                                        + mRequestMessagesWaiting + " at TIMEOUT, reset!"
                                        + " There still msg waitng for response");

                                mRequestMessagesWaiting = 0;

                                if (RILJ_LOGD) {
                                    synchronized (mRequestsList) {
                                        int count = mRequestsList.size();
                                        MyLog.d(LOG_TAG, "WAKE_LOCK_TIMEOUT " +
                                                " mRequestList=" + count);

                                        for (int i = 0; i < count; i++) {
                                            rr = mRequestsList.get(i);
                                            MyLog.d(LOG_TAG, i + ": [" + rr.mSerial + "] "
                                                    + requestToString(rr.mRequest));
                                        }
                                    }
                                }

                                // TODO ryan:
                                // if network is bad, list will increase quickly, thus clear will be necessary
                                if (mClearTimeout) {
                                    clearRequestsList(Constants.RESPONSE_TIMEOUT, false);
                                }
                            }
                            // mRequestMessagesPending shows how many
                            // requests are waiting to be sent (and before
                            // to be added in request list) since star the
                            // WAKE_LOCK_TIMEOUT timer. Since WAKE_LOCK_TIMEOUT
                            // is the expected time to get response, all requests
                            // should already sent out (i.e.
                            // mRequestMessagesPending is 0 )while TIMEOUT occurs.
                            if (mRequestMessagesPending != 0) {
                                MyLog.e(LOG_TAG, "ERROR: mReqPending is NOT 0 but"
                                        + mRequestMessagesPending + " at TIMEOUT, reset!");
                                mRequestMessagesPending = 0;

                            }
                            //mWakeLock.release();
                            mWakeLock.set(false);
                        }
                    }
                    break;
            }
        }
    }

    class RILReceiver implements Runnable {
        byte[] buffer;
        byte[] frame;

        RILReceiver() {
            buffer = new byte[Constants.MAX_FRAME_SIZE];
        }

        public void run() {
            int retryCount = 0;
            TrafficStats.setThreadStatsTag(0xFFFFF000);

            try {
                while (!mReceiverThread.isInterrupted()){
                    Socket s = null;
                    InetSocketAddress l;
                    boolean isConnected = false;

                    try {
                        ConnectivityManager connectivityMgr = (ConnectivityManager)
                                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                        if (connectivityMgr != null) {
                            //NetworkInfo networkInfo = connectivityMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                            NetworkInfo networkInfo = connectivityMgr.getActiveNetworkInfo();
                            if (networkInfo != null && networkInfo.isConnected()) {
                                isConnected = true;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (!isConnected) {
                        try {
                            Thread.sleep(Constants.SERVER_DETECT_DELAY);
                        } catch (InterruptedException er) {
                            MyLog.i(LOG_TAG, "InterruptedException");
                            break;
                        }

                        retryCount++;
                        continue;
                    }

                    try {
                        s = new Socket();
                        l = new InetSocketAddress(mNetworkParamMsg.getIp(), mNetworkParamMsg.getPort());
                        s.connect(l, mNetworkParamMsg.getHandshakeInterval());
                    } catch (IOException ex) {
                        try {
                            if (s != null) {
                                s.close();
                            }
                        } catch (IOException ex2) {
                            //ignore failure to close after failure to connect
                        }

                        // don't print an error message after the the first time
                        // or after the 8th time

                        if (retryCount == 8) {
                            if(mBackupNetworkParamMsg != null) {
                                MyLog.e(LOG_TAG,
                                        "Couldn't find '" + mNetworkParamMsg.getIp()
                                                + "' socket after " + retryCount
                                                + " times, switch to backup '" + mBackupNetworkParamMsg.getIp() + "'");
                                mNetworkParamMsg = mBackupNetworkParamMsg;
                                mBackupNetworkParamMsg = null;
                                mNetworkParamChangeRegistrants.notifyRegistrants(new AsyncResult(null, mNetworkParamMsg, null));
                                retryCount = 0;

                            } else {
                                MyLog.e(LOG_TAG,
                                        "Couldn't find '" + mNetworkParamMsg.getIp()
                                                + "' socket after " + retryCount
                                                + " times, continuing to retry silently");
                            }
                        } else if (retryCount > 0 && retryCount < 8) {
                            MyLog.i(LOG_TAG,
                                    "Couldn't find '" + mNetworkParamMsg.getIp()
                                            + "' socket; retrying after timeout");
                        }

                        try {
                            Thread.sleep(Constants.SERVER_DETECT_DELAY);
                        } catch (InterruptedException er) {
                            MyLog.i(LOG_TAG, "InterruptedException");
                            break;
                        }

                        retryCount++;
                        continue;
                    }

                    retryCount = 0;
                    mSocket = s;
                    TrafficStats.tagSocket(mSocket);
                    MyLog.i(LOG_TAG, "Connected to '" + mNetworkParamMsg.getIp() + "' socket");
                    setServerAvailable(true);

                    int bufferLength = 0;
                    try {
                        InputStream is = mSocket.getInputStream();

                        while (!mReceiverThread.isInterrupted()) {

                            bufferLength = is.read(buffer);

                            if (bufferLength < 0) {
                                // End-of-stream reached
                                break;
                            }

                            if (bufferLength < Constants.MIN_FRAME_SIZE) {
                                MyLog.e(LOG_TAG, "invalid frame, raw frame size error");
                                continue;
                            } else if (buffer[0] != 0x7D || buffer[bufferLength - 1] != 0x7D) {
                                MyLog.e(LOG_TAG, "invalid frame, start or end error");
                                continue;
                            } else {
                                MyLog.d(LOG_TAG, "simple check, valid frame");
                            }

                            MyLog.d(LOG_TAG, "Read packet: " + bufferLength + " bytes, data = " +
                                    CodecUtils.bytesToHexString(buffer, 0, bufferLength));
                            try {
                                int beginIndex = 0;
                                int endIndex = 0;
                                boolean end = false;
                                while(endIndex < bufferLength - 1) {
                                    endIndex ++;
                                    if(buffer[endIndex] == 0x7D) {
                                        end = !end;
                                        if(end) {
                                            //MyLog.w(LOG_TAG, "==================(" + beginIndex + "," + endIndex + ")");
                                            int frameLength = endIndex - beginIndex + 1;
                                            frame = new byte[frameLength];
                                            System.arraycopy(buffer, beginIndex, frame, 0, frameLength);
                                            processResponse(frame, frameLength);
                                        } else {
                                            beginIndex = endIndex;
                                        }
                                    }
                                }
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    } catch (IOException ex) {
                        MyLog.i(LOG_TAG, "'" + mNetworkParamMsg.getIp() + "' socket closed" +
                                ex.toString());
                    } catch (Throwable tr) {
                        MyLog.e(LOG_TAG, "Uncaught exception read length=" + bufferLength +
                               "Exception:" + tr.toString());
                    }

                    MyLog.i(LOG_TAG, "Disconnected from '" + mNetworkParamMsg.getIp()
                            + "' socket");

                    setServerAvailable(false);

                    try {
                        TrafficStats.untagSocket(mSocket);
                        mSocket.close();
                    } catch (IOException ex) {
                    }

                    mSocket = null;
                    RILRequest.resetSerial();

                    // Clear request list on close
                    clearRequestsList(Constants.SERVER_NOT_AVAILABLE, false);
                }
                MyLog.i(LOG_TAG, "ReceiverThread exit");
            } catch (Throwable tr) {
                MyLog.e(LOG_TAG, "Uncaught exception", tr);
            }
        }

    }

    class RILTester implements Runnable {

        RILTester() {
        }

        public void run() {
            try {
                for (; ; ) {
                    try {
                        Thread.sleep(Constants.FAKE_MSG_DELAY);
                    } catch (InterruptedException er) {
                    }

                    try {
                        GetParamReqMsg getParamReqMsg = new GetParamReqMsg();
                        getParamReqMsg.setTimestamp("171102050000");
                        getParamReqMsg.setParamType(ParamType.NETWORK_PARAM);
                        MsgHeader msgHeader = new MsgHeader();
                        MsgHeader.MsgType msgType = MsgHeader.MsgType.GET_PARAM_MSG;
                        msgHeader.setMsgType(msgType);
                        msgHeader.setTerminalId("867223027378686");
                        msgHeader.setSerialNumber(1);
                        AutoLinkMsg autoLinkMsg = new AutoLinkMsg(msgHeader, getParamReqMsg);
                        byte[] frame = autoLinkMsg.toSendFrame();
                        processResponse(frame, frame.length);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(Constants.FAKE_MSG_DELAY);
                    } catch (InterruptedException er) {
                    }

                    try {
                        SetParamReqMsg setParamReqMsg = new SetParamReqMsg();
                        setParamReqMsg.setTimestamp("171102050000");
                        setParamReqMsg.setParamType(ParamType.NETWORK_PARAM);
                        setParamReqMsg.setMsgBody(new BaseParamMsg.NetworkParamMsg());
                        MsgHeader msgHeader = new MsgHeader();
                        MsgHeader.MsgType msgType = MsgHeader.MsgType.SET_PARAM_MSG;
                        msgHeader.setMsgType(msgType);
                        msgHeader.setTerminalId("867223027378686");
                        msgHeader.setSerialNumber(2);
                        AutoLinkMsg autoLinkMsg = new AutoLinkMsg(msgHeader, setParamReqMsg);
                        byte[] frame = autoLinkMsg.toSendFrame();
                        processResponse(frame, frame.length);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Throwable tr) {
                MyLog.e(LOG_TAG, "Uncaught exception", tr);
            }
        }

    }

    private String getTerminalId() {
        return mTerminalId;
    }

    private void processResponse(byte[] frame, int frameLength) {
        AutoLinkMsg autoLinkMsg = AutoLinkMsg.fromRcvFrame(frame, frameLength);
        if (autoLinkMsg != null) {
            MsgHeader msgHeader = autoLinkMsg.getMsgHeader();
            if(getTerminalId().equalsIgnoreCase(msgHeader.getTerminalId())) {
                int serial = msgHeader.getSerialNumber();
                MsgHeader.MsgType msgType = msgHeader.getMsgType();
                if (!isInRequestList(serial, msgType)) {
                    processUnsolicited(autoLinkMsg);
                } else {
                    processSolicited(autoLinkMsg);
                }
            } else {
                MyLog.e(LOG_TAG, "terminalId mismatch");
            }
            releaseWakeLockIfDone();
        }
    }

    /**
     * Release each request in mReqeustsList then clear the list
     *
     * @param error    is the RIL_Errno sent back
     * @param loggable true means to print all requests in mRequestslist
     */
    private void clearRequestsList(int error, boolean loggable) {
        RILRequest rr;
        synchronized (mRequestsList) {
            int count = mRequestsList.size();
            if (RILJ_LOGD && loggable) {
                MyLog.d(LOG_TAG, "WAKE_LOCK_TIMEOUT " +
                        " mReqPending=" + mRequestMessagesPending +
                        " mRequestList=" + count);
            }

            for (int i = 0; i < count; i++) {
                rr = mRequestsList.get(i);
                if (RILJ_LOGD && loggable) {
                    MyLog.d(LOG_TAG, i + ": [" + rr.mSerial + "] " +
                            requestToString(rr.mRequest));
                }
                rr.onError(error, null);
                rr.release();
            }
            mRequestsList.clear();
            mRequestMessagesWaiting = 0;
        }
    }

    private RILRequest findAndRemoveRequestFromList(int serial) {
        synchronized (mRequestsList) {
            for (int i = 0, s = mRequestsList.size(); i < s; i++) {
                RILRequest rr = mRequestsList.get(i);

                if (rr.mSerial == serial) {
                    mRequestsList.remove(i);
                    if (mRequestMessagesWaiting > 0)
                        mRequestMessagesWaiting--;
                    return rr;
                }
            }
        }

        return null;
    }


    private void processUnsolicited(AutoLinkMsg autoLinkMsg) {
        MyLog.d(LOG_TAG, "processUnsolicited...");
        MsgHeader msgHeader = autoLinkMsg.getMsgHeader();
        switch (msgHeader.getMsgType()) {
            case SET_PARAM_MSG: {
                SetParamReqMsg setParamReqMsg = (SetParamReqMsg)autoLinkMsg.getMsgBody();
                mSetParamRegistrants.notifyRegistrants(new AsyncResult(null, autoLinkMsg, null));
                break;
            }
            case GET_PARAM_MSG: {
                GetParamReqMsg getParamReqMsg = (GetParamReqMsg)autoLinkMsg.getMsgBody();
                mGetParamRegistrants.notifyRegistrants(new AsyncResult(null, autoLinkMsg, null));
                break;
            }
            case CAN_MSG:
                CanRspMsg canRspMsg = (CanRspMsg) autoLinkMsg.getMsgBody();
                mCanMessageRegistrants.notifyRegistrants(new AsyncResult(null, canRspMsg, null));
                break;
            default: {
                // TODO ryan
                // have no idea how to handle this
                break;
            }
        }
    }

    private boolean isInRequestList(int serial, MsgHeader.MsgType msgType) {
        MyLog.d(LOG_TAG, "serial = " + serial + " msgType = " + msgType);
        synchronized (mRequestsList) {
            for (int i = 0, s = mRequestsList.size(); i < s; i++) {
                RILRequest rr = mRequestsList.get(i);
                MyLog.d(LOG_TAG, "rr.mSerial = " + rr.mSerial + " rr.msgType = " + rr.msgType);
                if (rr.mSerial == serial && rr.msgType == msgType) {
                    return true;
                }
            }
        }

        return false;
    }

    private void processSolicited(AutoLinkMsg autoLinkMsg) {
        MyLog.d(LOG_TAG, "processSolicited...");
        MsgHeader msgHeader = autoLinkMsg.getMsgHeader();
        int serial = msgHeader.getSerialNumber();

        RILRequest rr = findAndRemoveRequestFromList(serial);
        if (rr == null) {
            MyLog.w(LOG_TAG, "Unexpected solicited response! sn: "
                    + serial);
            return;
        }

        Object ret = null;

        // either command succeeds or command fails but with data payload
        try {
            switch (rr.mRequest) {
                case Constants.REQUEST_LOGIN: {
                    break;
                }
                case Constants.REQUEST_HEARTBEAT: {
                    break;
                }
                case Constants.REQUEST_SINGLE_REPORT: {
                    break;
                }
                case Constants.REQUEST_BATCH_REPORT: {
                    break;
                }
                case Constants.REQUEST_CAN_REPORT: {
                    break;
                }
                default: {
                    MyLog.e(LOG_TAG, "Unrecognized solicited response: " + rr.mRequest);
                    break;
                }
            }
        } catch (Throwable tr) {
            // Exceptions here usually mean invalid RIL responses

            MyLog.w(LOG_TAG, rr.serialString() + "< "
                    + requestToString(rr.mRequest)
                    + " exception, possible invalid RIL response", tr);

            if (rr.mResult != null) {
                AsyncResult.forMessage(rr.mResult, null, tr);
                rr.mResult.sendToTarget();
            }
            rr.release();
            return;
        }

        ret = autoLinkMsg.getMsgBody();

        if (RILJ_LOGD)
            MyLog.d(LOG_TAG, rr.serialString() + "< " + requestToString(rr.mRequest)
                    + " " + retToString(rr.mRequest, ret));

        if (rr.mResult != null) {
            AsyncResult.forMessage(rr.mResult, ret, null);
            rr.mResult.sendToTarget();
        }

        rr.release();
    }

    //***** Constructors
    public RIL(Context context, String terminalId, BaseParamMsg.NetworkParamMsg networkParamMsg, BaseParamMsg.NetworkParamMsg backupNetworkParamMsg) {
        super(context);

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOG_TAG);
        //mWakeLock.setReferenceCounted(false);
        mWakeLock = new AtomicBoolean(false);
        mWakeLockTimeout = SystemProperties.getInt(Constants.PROPERTY_WAKE_LOCK_TIMEOUT,
                DEFAULT_WAKE_LOCK_TIMEOUT);
        mRequestMessagesPending = 0;
        mRequestMessagesWaiting = 0;

        mContext = context;
        mTerminalId = terminalId;
        mNetworkParamMsg = networkParamMsg;
        mBackupNetworkParamMsg =  backupNetworkParamMsg;

        mSenderThread = new HandlerThread("RILSender");
        mSenderThread.start();

        Looper looper = mSenderThread.getLooper();
        mSender = new RILSender(looper);

        mReceiver = new RILReceiver();
        mReceiverThread = new Thread(mReceiver, "RILReceiver");
        mReceiverThread.start();

        if (DBG_SERVER) {
            mRILTester = new RILTester();
            mTesterThread = new Thread(mRILTester, "RILTester");
            mTesterThread.start();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(mConnectivityReceiver, filter);
    }

    private void sendRILRequest(RILRequest rr, MsgHeader.MsgType msgType, BaseMsg msgBody, int serial) {
        MsgHeader msgHeader = new MsgHeader();
        msgHeader.setMsgType(msgType);
        rr.msgType = msgType;
        msgHeader.setTerminalId(getTerminalId());
        if(serial < 0) {
            // client request
            msgHeader.setSerialNumber(rr.mSerial);
        } else {
            // client ack server request
            msgHeader.setSerialNumber(serial);
        }
        AutoLinkMsg autoLinkMsg = new AutoLinkMsg(msgHeader, msgBody);

        switch (msgType) {
            case CONNECTION_MSG:
                MyLog.i(LOG_TAG, "" + autoLinkMsg);
                break;
            default:
                //MyLog.d(LOG_TAG, "" + autoLinkMsg);
                break;
        }

        rr.data = autoLinkMsg.toSendFrame();

        if (RILJ_LOGD) MyLog.d(LOG_TAG, rr.serialString() + "> " + requestToString(rr.mRequest));

        send(rr);
    }

    @Override
    public void login(ConnectionMsg.LoginReqMsg loginReqMsg, Message result) {
        RILRequest rr = RILRequest.obtain(Constants.REQUEST_LOGIN, result);
        rr.shouldAck = true;
        ConnectionMsg connectionMsg = new ConnectionMsg(ConnectionMsg.MsgType.LOGIN, loginReqMsg);
        sendRILRequest(rr, MsgHeader.MsgType.CONNECTION_MSG, connectionMsg, -1);
    }

    @Override
    public void heartbeat(Message result) {
        RILRequest rr = RILRequest.obtain(Constants.REQUEST_HEARTBEAT, result);
        rr.shouldAck = true;
        ConnectionMsg connectionMsg = new ConnectionMsg(ConnectionMsg.MsgType.HEARTBEAT, null);
        sendRILRequest(rr, MsgHeader.MsgType.CONNECTION_MSG, connectionMsg, -1);
    }

    @Override
    public void logout(Message result) {
        RILRequest rr = RILRequest.obtain(Constants.REQUEST_LOGOUT, result);
        rr.shouldAck = false;
        ConnectionMsg connectionMsg = new ConnectionMsg(ConnectionMsg.MsgType.LOGOUT, null);
        sendRILRequest(rr, MsgHeader.MsgType.CONNECTION_MSG, connectionMsg, -1);
    }

    @Override
    public void ackSetParam(SetParamRspMsg setParamRspMsg, Message result) {
        RILRequest rr = RILRequest.obtain(Constants.REQUEST_ACK_SET_PARAM, result);
        rr.shouldAck = false;
        sendRILRequest(rr, MsgHeader.MsgType.SET_PARAM_MSG, setParamRspMsg, setParamRspMsg.getReqSerial());
    }

    @Override
    public void ackGetParam(GetParamRspMsg getParamRspMsg, Message result) {
        RILRequest rr = RILRequest.obtain(Constants.REQUEST_ACK_GET_PARAM, result);
        rr.shouldAck = false;
        sendRILRequest(rr, MsgHeader.MsgType.GET_PARAM_MSG, getParamRspMsg, getParamRspMsg.getReqSerial());
    }

    @Override
    public void singleReport(SingleReportMsg singleReportMsg, Message result) {
        RILRequest rr = RILRequest.obtain(Constants.REQUEST_SINGLE_REPORT, result);
        rr.shouldAck = singleReportMsg.shouldAck();
        sendRILRequest(rr, MsgHeader.MsgType.SINGLE_REPORT_MSG, singleReportMsg, -1);
    }

    @Override
    public void batchReport(BatchReportMsg batchReportMsg, Message result) {
        RILRequest rr = RILRequest.obtain(Constants.REQUEST_BATCH_REPORT, result);
        rr.shouldAck = batchReportMsg.shouldAck();
        sendRILRequest(rr, MsgHeader.MsgType.BATCH_REPORT_MSG, batchReportMsg, -1);
    }

    @Override
    public void canReport(CanReqMsg canReportMsg, Message result) {
        RILRequest rr = RILRequest.obtain(Constants.REQUEST_CAN_REPORT, result);
        rr.shouldAck = canReportMsg.shouldAck();
        sendRILRequest(rr, MsgType.CONTROL_MSG, canReportMsg, -1);
    }

    @Override
    public void resetSocket(BaseParamMsg.NetworkParamMsg networkParamMsg) {
        mBackupNetworkParamMsg = mNetworkParamMsg;
        mNetworkParamMsg = networkParamMsg;
        if(mSocket != null) {
            try {
                TrafficStats.untagSocket(mSocket);
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void reset() {
        mSenderThread.quit();
        mReceiverThread.interrupt();
        if(mSocket != null) {
            try {
                TrafficStats.untagSocket(mSocket);
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mContext.unregisterReceiver(mConnectivityReceiver);
    }

    /**
     * Holds a PARTIAL_WAKE_LOCK whenever
     * a) There is outstanding RIL request sent to RIL deamon and no replied
     * b) There is a request pending to be sent out.
     * <p/>
     * There is a WAKE_LOCK_TIMEOUT to release the lock, though it shouldn't
     * happen often.
     */

    private void acquireWakeLock() {
        synchronized (mWakeLock) {
            //mWakeLock.acquire();
            mWakeLock.set(true);
            mRequestMessagesPending++;

            mSender.removeMessages(EVENT_WAKE_LOCK_TIMEOUT);
            Message msg = mSender.obtainMessage(EVENT_WAKE_LOCK_TIMEOUT);
            mSender.sendMessageDelayed(msg, mWakeLockTimeout);
        }
    }

    private void releaseWakeLockIfDone() {
        synchronized (mWakeLock) {
            //if (mWakeLock.isHeld() &&
            if (mWakeLock.get() &&
                    (mRequestMessagesPending == 0) &&
                    (mRequestMessagesWaiting == 0)) {
                mSender.removeMessages(EVENT_WAKE_LOCK_TIMEOUT);
                //mWakeLock.release();
                mWakeLock.set(false);
            }
        }
    }

    private void send(RILRequest rr) {
        Message msg;

        msg = mSender.obtainMessage(EVENT_SEND, rr);

        if(rr.shouldAck) {
            acquireWakeLock();
        }

        msg.sendToTarget();
    }

    private String retToString(int req, Object ret) {
        if (ret == null) return "";
        switch (req) {
            // Don't log these return values, for privacy's sake.
            case Constants.REQUEST_LOGIN:
                return "";
            default:
                break;
        }
        return ret.toString();
    }

    static String requestToString(int request) {
        switch (request) {
            case Constants.REQUEST_LOGIN:
                return "LOGIN";
            case Constants.REQUEST_HEARTBEAT:
                return "HEARTBEAT";
            case Constants.REQUEST_LOGOUT:
                return "LOGOUT";
            case Constants.REQUEST_ACK_SET_PARAM:
                return "ACK_SET_PARAM";
            case Constants.REQUEST_ACK_GET_PARAM:
                return "ACK_GET_PARAM";
            case Constants.REQUEST_SINGLE_REPORT:
                return "SINGLE_REPORT";
            case Constants.REQUEST_BATCH_REPORT:
                return "BATCH_REPORT";
            case Constants.REQUEST_CAN_REPORT:
                return "CAN_REPORT";
            default:
                return "<unknown request>";
        }
    }
}
