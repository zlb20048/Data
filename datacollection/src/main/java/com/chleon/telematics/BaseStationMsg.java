package com.chleon.telematics;

import android.telephony.CellLocation;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

import java.io.ByteArrayOutputStream;

/**
 * Created by Ryan Fan on 2016/2/26.
 */
public class BaseStationMsg implements BaseMsg {

    public static final int BASE_STATION_MSG_SIZE = 10;

    private boolean attatched;
    private int mnc = 0xFF;
    private int sid;
    private int lac;
    private int cid;
    private int signal = 99;

    private boolean hasIccCard;
    private boolean hasCellLocation;

    public BaseStationMsg() {
    }

    public BaseStationMsg(BaseStationMsg baseStationMsg) {
        this.attatched = baseStationMsg.attatched;
        this.mnc = baseStationMsg.mnc;
        this.sid = baseStationMsg.sid;
        this.lac = baseStationMsg.lac;
        this.cid = baseStationMsg.cid;
        this.signal = baseStationMsg.signal;
    }

    public BaseStationMsg(ServiceState serviceState, CellLocation cellLocation, SignalStrength signalStrength) {
        setServiceState(serviceState);
        setCellLocation(cellLocation);
        setSignalStrength(signalStrength);
    }

    private void updateAttched() {
        //MyLog.d("============", "mnc = " + mnc);
        //MyLog.d("============", "hasCellLocation = " + hasCellLocation);
        //MyLog.d("============", "signal = " + signal);
        //MyLog.d("============", "hasIccCard = " + hasIccCard);
        attatched = ((mnc != 0xFF)
                && hasCellLocation
                && (signal != 99)
                && hasIccCard);
    }

    public void setServiceState(ServiceState serviceState) {
        if (serviceState != null) {
            String operator = serviceState.getOperatorNumeric();
            if (operator != null && operator.length() >= 5) {
                mnc = Integer.valueOf(operator.substring(3, 5));
            }
        } else {
            mnc = 0xFF;
        }
        updateAttched();
    }

    public void setCellLocation(CellLocation cellLocation) {
        if (cellLocation != null) {
            hasCellLocation = true;
            if (cellLocation instanceof GsmCellLocation) {
                GsmCellLocation gsm = (GsmCellLocation) cellLocation;
                sid = 0;
                lac = gsm.getLac();
                cid = gsm.getCid();
            } else if (cellLocation instanceof CdmaCellLocation) {
                CdmaCellLocation cdma = (CdmaCellLocation) cellLocation;
                sid = cdma.getSystemId();
                lac = cdma.getNetworkId();
                cid = cdma.getBaseStationId();
            } else {
            }
        } else {
            hasCellLocation = false;
            sid = 0;
            lac = 0;
            cid = 0;
        }
        updateAttched();
    }

/*    public void setSignalStrength(SignalStrength signalStrength) {
        int dBm;
        if (signalStrength != null) {
            if (signalStrength.isGsm()) {
                dBm = signalStrength.getGsmSignalStrength();
            } else {
                dBm = signalStrength.getCdmaDbm();
            }
        } else {
            dBm = 0;
        }
        signal = dBm;
        updateAttched();
    }*/

    public void setSignalStrength(SignalStrength signalStrength) {
        int asu;
        if (signalStrength != null) {
            asu = signalStrength.getAsuLevel();
        } else {
            asu = 99;
        }
        signal = asu;
        updateAttched();
    }

    public boolean isAttatched() {
        return attatched;
    }

    public void setAttatched(boolean attatched) {
        this.attatched = attatched;
    }

    public int getMnc() {
        return mnc;
    }

    public void setMnc(int mnc) {
        this.mnc = mnc;
    }

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public int getLac() {
        return lac;
    }

    public void setLac(int lac) {
        this.lac = lac;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public int getSignal() {
        return signal;
    }

    public void setSignal(int signal) {
        this.signal = signal;
    }

    public boolean hasIccCard() {
        return hasIccCard;
    }

    public void setHasIccCard(boolean hasIccCard) {
        this.hasIccCard = hasIccCard;
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(BASE_STATION_MSG_SIZE);

        int dummyInt = 0;
        if (attatched) {
            dummyInt = dummyInt | 0x80;
        }

        dummyInt = dummyInt | (mnc & 0x7F);
        outStream.write(dummyInt & 0xFF);

        dummyInt = sid;
        outStream.write((dummyInt & (0xFF << 8)) >> 8);
        outStream.write(dummyInt & 0xFF);

        dummyInt = lac;
        outStream.write((dummyInt & (0xFF << 8)) >> 8);
        outStream.write(dummyInt & 0xFF);

        dummyInt = cid;
        outStream.write((dummyInt & (0xFF << 24)) >> 24);
        outStream.write((dummyInt & (0xFF << 16)) >> 16);
        outStream.write((dummyInt & (0xFF << 8)) >> 8);
        outStream.write(dummyInt & 0xFF);

        dummyInt = signal;
        outStream.write(dummyInt & 0xFF);

        return outStream.toByteArray();
    }

    @Override
    public String toString() {
        return "{" +
                "attatched=" + attatched
                + ", mnc=" + mnc
                + ", sid=" + sid
                + ", lac=" + lac
                + ", cid=" + cid
                + ", signal=" + signal
                + "}";
    }
}
