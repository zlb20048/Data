package com.chleon.datacollection;

import android.app.Application;
import android.content.Intent;

import com.chleon.telematics.*;

/**
 * Created by Ryan Fan on 2016/2/27.
 */
public class DataCollectionApp extends Application {
    private static final String TAG = DataCollectionApp.class.getSimpleName();
    private static final boolean DBG_MSG = true;

    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent(getApplicationContext(), DataCollectionService.class);
        startService(intent);

        if (DBG_MSG) {
//            testLogin();
//            testSingleReport();
//            testBatchReport();
//            testRsp();
        }
    }

    private void testLogin() {
        ConnectionMsg.LoginReqMsg loginReqMsg = new ConnectionMsg.LoginReqMsg("1234", 4);
        ConnectionMsg connectionMsg = new ConnectionMsg(ConnectionMsg.MsgType.LOGIN, loginReqMsg);
        MsgHeader msgHeader = new MsgHeader();
        MsgHeader.MsgType msgType = MsgHeader.MsgType.CONNECTION_MSG;
        msgHeader.setMsgType(msgType);
        msgHeader.setTerminalId("86130402042171");
        msgHeader.setSerialNumber(1);
        AutoLinkMsg autoLinkMsg = new AutoLinkMsg(msgHeader, connectionMsg);
        MyLog.e(TAG, "autoLinkMsg = " +
                CodecUtils.bytesToHexString(autoLinkMsg.toSendFrame()));
    }

    private void testSingleReport() {
        SingleReportMsg singleReportMsg = new SingleReportMsg();

        singleReportMsg.setShouldAck(true);
        singleReportMsg.setDelayReport(false);

        singleReportMsg.setReportReason(ReportReason.ACC_ON);
        singleReportMsg.setContainLocationMsg(true);
        singleReportMsg.setContainBaseStationMsg(true);
        singleReportMsg.setContainStatusMsg(true);
        singleReportMsg.setContainAdditionMsg(true);
        singleReportMsg.setContainBatteryMsg(true);
        singleReportMsg.setContainFaultMsg(true);
        singleReportMsg.setTimestamp(System.currentTimeMillis());

        if (singleReportMsg.containLocationMsg()) {
            singleReportMsg.setLocationMsg(new LocationMsg());
        }
        if (singleReportMsg.containBaseStationMsg()) {
            singleReportMsg.setBaseStationMsg(new BaseStationMsg());
        }
        if (singleReportMsg.containStatusMsg()) {
            singleReportMsg.setStatusMsg(new StatusMsg());
        }
        if (singleReportMsg.containAdditionMsg()) {
            singleReportMsg.setAdditionMsg(new AdditionMsg());
        }
        if (singleReportMsg.containBatteryMsg()) {
            singleReportMsg.setBatteryMsg(new BatteryMsg());
        }
        if (singleReportMsg.containFaultMsg()) {
            singleReportMsg.setFaultMsg(new FaultMsg());
        }

        MsgHeader.MsgType msgType = MsgHeader.MsgType.SINGLE_REPORT_MSG;

        MsgHeader msgHeader = new MsgHeader();
        msgHeader.setMsgType(msgType);
        msgHeader.setTerminalId("86130402042171");
        msgHeader.setSerialNumber(2);
        AutoLinkMsg autoLinkMsg = new AutoLinkMsg(msgHeader, singleReportMsg);
        MyLog.e(TAG, "autoLinkMsg = " +
                CodecUtils.bytesToHexString(autoLinkMsg.toSendFrame()));
    }

    private void testBatchReport() {
        BatchReportMsg batchReportMsg = new BatchReportMsg();
        batchReportMsg.setShouldAck(true);
        batchReportMsg.setReportReason(ReportReason.TIMER);
        batchReportMsg.setContainLocationMsg(true);
        batchReportMsg.setContainBaseStationMsg(true);
        batchReportMsg.setContainStatusMsg(true);
        batchReportMsg.setContainAdditionMsg(true);
        batchReportMsg.setContainBatteryMsg(true);
        batchReportMsg.setContainFaultMsg(true);
        batchReportMsg.inCreaseCount();
        batchReportMsg.inCreaseCount();

        batchReportMsg.addTimestamp(System.currentTimeMillis());
        batchReportMsg.addTimestamp(System.currentTimeMillis());

        batchReportMsg.addLocationMsg(new LocationMsg());
        batchReportMsg.addLocationMsg(new LocationMsg());

        batchReportMsg.addBaseStationMsg(new BaseStationMsg());
        batchReportMsg.addBaseStationMsg(new BaseStationMsg());

        batchReportMsg.addStatusMsg(new StatusMsg());
        batchReportMsg.addStatusMsg(new StatusMsg());

        batchReportMsg.addAdditionMsg(new AdditionMsg());
        batchReportMsg.addAdditionMsg(new AdditionMsg());

        batchReportMsg.addBatteryMsg(new BatteryMsg());
        batchReportMsg.addBatteryMsg(new BatteryMsg());

        batchReportMsg.setFaultMsg(new FaultMsg());

        MsgHeader.MsgType msgType = MsgHeader.MsgType.BATCH_REPORT_MSG;

        MsgHeader msgHeader = new MsgHeader();
        msgHeader.setMsgType(msgType);
        msgHeader.setTerminalId("86130402042171");
        msgHeader.setSerialNumber(2);
        AutoLinkMsg autoLinkMsg = new AutoLinkMsg(msgHeader, batchReportMsg);
        MyLog.e(TAG, "autoLinkMsg = " +
                CodecUtils.bytesToHexString(autoLinkMsg.toSendFrame()));
    }

    private void testRsp() {
        MyLog.i(TAG, "testRsp...");
//        String rsp = "7D1002861304020421714100020003E7C47790AF8EFE504840C8A4BAE7962AC39C7D";
//        String rsp = "7D220231323334353637383931323334353637384101660003AB82C88157D449BD55323E69A80F350FF6C17D";
        String rsp = "7D10221234567890234567020014000F689E35D5935BD906E1EB763F78582633D4527D";
        byte[] rspBytes = CodecUtils.hexStringToBytes(rsp);
        for (byte b : rspBytes) {
            MyLog.i(TAG, "b = " + b);
        }
        AutoLinkMsg autoLinkMsg = AutoLinkMsg.fromRcvFrame(rspBytes, rspBytes.length);
        MyLog.d(TAG, "autoLinkMsg=" + autoLinkMsg);
    }
}
