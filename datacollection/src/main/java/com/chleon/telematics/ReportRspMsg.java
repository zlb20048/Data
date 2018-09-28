package com.chleon.telematics;

import java.io.ByteArrayOutputStream;

/**
 * Created by Ryan Fan on 2016/2/27.
 */
public class ReportRspMsg implements BaseMsg {
    private static final String TAG = ReportRspMsg.class.getSimpleName();

    private static final int REPORT_RSP_MSG_SIZE = 3;

    private ReportReason reportReason;
    private ResultCode resultCode;

    public ReportRspMsg() {
    }

    public ReportRspMsg(ReportReason reportReason, ResultCode resultCode) {
        this.reportReason = reportReason;
        this.resultCode = resultCode;
    }

    public ReportReason getReportReason() {
        return reportReason;
    }

    public void setReportReason(ReportReason reportReason) {
        this.reportReason = reportReason;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }

    public void setResultCode(ResultCode resultCode) {
        this.resultCode = resultCode;
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(REPORT_RSP_MSG_SIZE);

        outStream.write(reportReason.getValue());

        outStream.write((resultCode.getValue() & (0xFF << 8)) >> 8);
        outStream.write(resultCode.getValue() & 0xFF);

        return outStream.toByteArray();
    }

    public static ReportRspMsg fromByteArray(byte[] data) throws AutoLinkMsgException {
        ReportRspMsg reportRspMsg = null;
        if (data.length != REPORT_RSP_MSG_SIZE) {
            MyLog.e(TAG, "ReportRspMsg invalid");
            throw new AutoLinkMsgException(reportRspMsg, ResultCode.UNKNOWN_ERROR);
        } else {
            reportRspMsg = new ReportRspMsg();
            reportRspMsg.reportReason = ReportReason.getReportReason(data[0] & 0xFF);
            reportRspMsg.resultCode = ResultCode.getResultCode((data[1] & 0xFF) << 8 | data[2] & 0xFF);
        }
        return reportRspMsg;
    }

    @Override
    public String toString() {
        return "{"
                + "reportReason=" + reportReason
                + ", resultCode=" + resultCode
                + "}";
    }
}
