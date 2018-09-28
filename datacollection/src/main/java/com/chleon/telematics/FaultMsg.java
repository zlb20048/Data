package com.chleon.telematics;

import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ryan Fan on 2016/2/26.
 */
public class FaultMsg implements BaseMsg {

    public static final int FAULT_MSG_MIN_SIZE = 2;

    private static final int FAULT_CODE_SIZE = 8;

    private Map<String, Boolean> faultCodes;

    private void init() {
        faultCodes = new HashMap<String, Boolean>();
    }

    public FaultMsg() {
        init();
    }

    public FaultMsg(FaultMsg faultMsg) {
        init();
        faultCodes.putAll(faultMsg.faultCodes);
    }

    public Map<String, Boolean> getFaultCodes() {
        return faultCodes;
    }

    public void addFaultCode(String faultCode) {
        faultCodes.put(faultCode, true);
    }

    public void merge(FaultMsg faultMsg) {
        if (faultMsg != null) {
            faultCodes.putAll(faultMsg.getFaultCodes());
        }
    }

    public void fromStrFaultCode(String strFaultCode) {
        if (!TextUtils.isEmpty(strFaultCode)) {
            String[] strFaultCodes = strFaultCode.split(" ");
            for (String faultCode : strFaultCodes) {
                if (!TextUtils.isEmpty(faultCode)) {
                    faultCodes.put(faultCode, true);
                }
            }
        }
    }

    public String toStrFaultCode() {
        String strFaultCode = "";
        for (String faultCode : faultCodes.keySet()) {
            strFaultCode += " " + faultCode;
        }
        return strFaultCode.trim();
    }

    public void clearFaultCodes() {
        faultCodes.clear();
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(32);

        int faultCount = faultCodes.size();
        outStream.write((faultCount & (0xFF << 8)) >> 8);
        outStream.write(faultCount & 0xFF);
        for (String faultCode : faultCodes.keySet()) {
            byte[] tempFaultCode = faultCode.getBytes();
            int lenTempFault = tempFaultCode.length;
            byte[] fault;
            if (lenTempFault < FAULT_CODE_SIZE) {
                fault = new byte[FAULT_CODE_SIZE];
                System.arraycopy(tempFaultCode, 0, fault, 0, lenTempFault);
            } else {
                fault = tempFaultCode;
            }
            try {
                outStream.write(fault);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return outStream.toByteArray();
    }

    @Override
    public String toString() {
        String str = "{"
                + "faultCount=" + faultCodes.size();
        for (String faultCode : faultCodes.keySet()) {
            str += ", " + faultCode;
        }
        str += "}";
        return str;
    }
}
