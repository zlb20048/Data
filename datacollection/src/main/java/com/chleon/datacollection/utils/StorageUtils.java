package com.chleon.datacollection.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Xml;
import com.chleon.datacollection.bean.ConfigBean;
import com.chleon.telematics.BaseParamMsg;
import com.chleon.telematics.MyLog;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.*;

public class StorageUtils {
    private static final String TAG = StorageUtils.class.getSimpleName();
    private static final String CONFIG_FILE = "config.xml";
    private static final String FILE_DIR = Environment.getExternalStorageDirectory() + File.separator + Constants.APP_DIR_PATH;

    private static ConfigBean createDefaultConfigBean() {
        BaseParamMsg.ChannelIdMsg channelIdMsg = new BaseParamMsg.ChannelIdMsg();
        BaseParamMsg.NetworkParamMsg networkParamMsg = new BaseParamMsg.NetworkParamMsg();
        BaseParamMsg.TimerMsg timerMsg = new BaseParamMsg.TimerMsg();
        BaseParamMsg.ReportMsg accReportMsg = new BaseParamMsg.ReportMsg();
        BaseParamMsg.ReportMsg statusReportMsg = new BaseParamMsg.ReportMsg();
        BaseParamMsg.ReportMsg faultReportMsg = new BaseParamMsg.ReportMsg();
        BaseParamMsg.ReportMsg alarmReportMsg = new BaseParamMsg.ReportMsg();
        BaseParamMsg.RestrictionMsg restrictionMsg = new BaseParamMsg.RestrictionMsg();

        return new ConfigBean(channelIdMsg, networkParamMsg, timerMsg,
                accReportMsg, statusReportMsg, faultReportMsg, alarmReportMsg, restrictionMsg);
    }

    public static ConfigBean getConfigBean(Context context) {

        ConfigBean configBean = createDefaultConfigBean();

        File file = new File(FILE_DIR, CONFIG_FILE);
        InputStream xml = null;
        try {
            xml = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            //fall back to default
        }
        if (xml != null) {
            try {
                XmlPullParser pullParser = Xml.newPullParser();
                pullParser.setInput(xml, "UTF-8");
                int event = pullParser.getEventType();
                while (event != XmlPullParser.END_DOCUMENT) {
                    switch (event) {
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            if ("channelIdMsg".equals(pullParser.getName())) {
                                // channelIdMsg =========================================
                            } else if ("channelId".equals(pullParser.getName())) {
                                String value = pullParser.nextText();
                                int channelId = 0;
                                try {
                                    channelId = Integer.parseInt(value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                configBean.getChannelId().setChannelId(channelId);
                            } else if ("networkParamMsg".equals(pullParser.getName())) {
                                // networkParamMsg =========================================
                            } else if ("ip".equals(pullParser.getName())) {
                                String value = pullParser.nextText();
                                configBean.getNetworkParam().setIp(value);
                            } else if ("port".equals(pullParser.getName())) {
                                String value = pullParser.nextText();
                                int port = 0;
                                try {
                                    port = Integer.parseInt(value);
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                                configBean.getNetworkParam().setPort(port);
                            } else if ("handshakeInterval".equals(pullParser.getName())) {
                                String value = pullParser.nextText();
                                int interval = 0;
                                try {
                                    interval = Integer.parseInt(value);
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                                configBean.getNetworkParam().setHandshakeInterval(interval);
                            } else if ("backupNetworkParamMsg".equals(pullParser.getName())) {
                                // backupNetworkParamMsg =========================================
                                configBean.setBackupNetworkParam(new BaseParamMsg.NetworkParamMsg());
                            } else if ("ip".equals(pullParser.getName())) {
                                String value = pullParser.nextText();
                                configBean.getBackupNetworkParam().setIp(value);
                            } else if ("port".equals(pullParser.getName())) {
                                String value = pullParser.nextText();
                                int port = 0;
                                try {
                                    port = Integer.parseInt(value);
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                                configBean.getBackupNetworkParam().setPort(port);
                            } else if ("handshakeInterval".equals(pullParser.getName())) {
                                String value = pullParser.nextText();
                                int interval = 0;
                                try {
                                    interval = Integer.parseInt(value);
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                                configBean.getBackupNetworkParam().setHandshakeInterval(interval);
                            } else if ("timerMsg".equals(pullParser.getName())) {
                                // timerMsg =========================================
                            } else if ("interval".equals(pullParser.getName())) {
                                String value = pullParser.nextText();
                                int interval = 0;
                                try {
                                    interval = Integer.parseInt(value);
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                                configBean.getTimer().setInterval(interval);
                            } else if ("count".equals(pullParser.getName())) {
                                String value = pullParser.nextText();
                                int count = 0;
                                try {
                                    count = Integer.parseInt(value);
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                                configBean.getTimer().setCount(count);
                            } else if ("accReportMsg".equals(pullParser.getName())) {
                                // accReportMsg =========================================
                            } else if ("accReport".equals(pullParser.getName())) {
                                String value = pullParser.nextText();
                                boolean reportState = false;
                                try {
                                    reportState = Boolean.parseBoolean(value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                configBean.getAccReport().setReportState(reportState);
                            } else if ("statusReportMsg".equals(pullParser.getName())) {
                                // statusReportMsg =========================================
                            } else if ("statusReport".equals(pullParser.getName())) {
                                String value = pullParser.nextText();
                                boolean reportState = false;
                                try {
                                    reportState = Boolean.parseBoolean(value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                configBean.getStatusReport().setReportState(reportState);
                            } else if ("faultReportMsg".equals(pullParser.getName())) {
                                // faultReportMsg =========================================
                            } else if ("faultReport".equals(pullParser.getName())) {
                                String value = pullParser.nextText();
                                boolean reportState = false;
                                try {
                                    reportState = Boolean.parseBoolean(value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                configBean.getFaultReport().setReportState(reportState);
                            } else if ("alarmReportMsg".equals(pullParser.getName())) {
                                // alarmReportMsg =========================================
                            } else if ("alarmReport".equals(pullParser.getName())) {
                                String value = pullParser.nextText();
                                boolean reportState = false;
                                try {
                                    reportState = Boolean.parseBoolean(value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                configBean.getAlarmReport().setReportState(reportState);
                            } else if ("restrictionMsg".equals(pullParser.getName())) {
                                // restrictionMsg =========================================
                            } else if ("maxSpeed".equals(pullParser.getName())) {
                                String value = pullParser.nextText();
                                int maxSpeed = 0;
                                try {
                                    maxSpeed = Integer.parseInt(value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                configBean.getRestriction().setMaxSpeed(maxSpeed);
                            } else if ("overspeedDuration".equals(pullParser.getName())) {
                                String value = pullParser.nextText();
                                int overspeedDuration = 0;
                                try {
                                    overspeedDuration = Integer.parseInt(value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                configBean.getRestriction().setOverspeedDuration(overspeedDuration);
                            } else if ("parkingDuration".equals(pullParser.getName())) {
                                String value = pullParser.nextText();
                                int parkingDuration = 0;
                                try {
                                    parkingDuration = Integer.parseInt(value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                configBean.getRestriction().setParkingDuration(parkingDuration);
                            } else if ("fatigueDrivingDuration".equals(pullParser.getName())) {
                                String value = pullParser.nextText();
                                int fatigueDrivingDuration = 0;
                                try {
                                    fatigueDrivingDuration = Integer.parseInt(value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                configBean.getRestriction().setFatigueDrivingDuration(fatigueDrivingDuration);
                            } else if ("decelerationX".equals(pullParser.getName())) {
                                String value = pullParser.nextText();
                                int deceleration = 0;
                                try {
                                    deceleration = Integer.parseInt(value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                configBean.getRestriction().setDecelerationX(deceleration);
                            } else if ("accelerationX".equals(pullParser.getName())) {
                                String value = pullParser.nextText();
                                int acceleration = 0;
                                try {
                                    acceleration = Integer.parseInt(value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                configBean.getRestriction().setAccelerationX(acceleration);
                            } else if ("decelerationY".equals(pullParser.getName())) {
                                String value = pullParser.nextText();
                                int deceleration = 0;
                                try {
                                    deceleration = Integer.parseInt(value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                configBean.getRestriction().setDecelerationY(deceleration);
                            } else if ("accelerationY".equals(pullParser.getName())) {
                                String value = pullParser.nextText();
                                int acceleration = 0;
                                try {
                                    acceleration = Integer.parseInt(value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                configBean.getRestriction().setAccelerationY(acceleration);
                            } else if ("decelerationZ".equals(pullParser.getName())) {
                                String value = pullParser.nextText();
                                int deceleration = 0;
                                try {
                                    deceleration = Integer.parseInt(value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                configBean.getRestriction().setDecelerationZ(deceleration);
                            } else if ("accelerationZ".equals(pullParser.getName())) {
                                String value = pullParser.nextText();
                                int acceleration = 0;
                                try {
                                    acceleration = Integer.parseInt(value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                configBean.getRestriction().setAccelerationZ(acceleration);
                            } else {
                                //TODO
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            break;
                        default:
                            break;
                    }
                    event = pullParser.next();
                }
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    xml.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return configBean;
    }

    public static void saveConfigBean(Context context, ConfigBean configBean) {
        File dir = new File(FILE_DIR);
        if(!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(FILE_DIR, CONFIG_FILE);
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            MyLog.w(TAG, "saveConfigBean, config.xml not found");
        }
        if (out != null) {
            try {
                XmlSerializer serializer = Xml.newSerializer();
                serializer.setOutput(out, "UTF-8");
                serializer.startDocument("UTF-8", true);
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

                serializer.startTag(null, "channelIdMsg");
                // channelIdMsg =========================================
                BaseParamMsg.ChannelIdMsg channelIdMsg = configBean.getChannelId();
                serializer.startTag(null, "channelId");
                serializer.text(String.valueOf(channelIdMsg.getChannelId()));
                serializer.endTag(null, "channelId");
                serializer.endTag(null, "channelIdMsg");

                serializer.startTag(null, "networkParamMsg");
                // networkParamMsg =========================================
                BaseParamMsg.NetworkParamMsg networkParamMsg = configBean.getNetworkParam();
                serializer.startTag(null, "ip");
                serializer.text(networkParamMsg.getIp());
                serializer.endTag(null, "ip");
                serializer.startTag(null, "port");
                serializer.text(String.valueOf(networkParamMsg.getPort()));
                serializer.endTag(null, "port");
                serializer.startTag(null, "handshakeInterval");
                serializer.text(String.valueOf(networkParamMsg.getHandshakeInterval()));
                serializer.endTag(null, "handshakeInterval");
                serializer.endTag(null, "networkParamMsg");

                if (configBean.getBackupNetworkParam() != null) {
                    serializer.startTag(null, "backupNetworkParamMsg");
                    // backupNetworkParamMsg =========================================
                    BaseParamMsg.NetworkParamMsg backupNetworkParamMsg = configBean.getBackupNetworkParam();
                    serializer.startTag(null, "ip");
                    serializer.text(backupNetworkParamMsg.getIp());
                    serializer.endTag(null, "ip");
                    serializer.startTag(null, "port");
                    serializer.text(String.valueOf(backupNetworkParamMsg.getPort()));
                    serializer.endTag(null, "port");
                    serializer.startTag(null, "handshakeInterval");
                    serializer.text(String.valueOf(backupNetworkParamMsg.getHandshakeInterval()));
                    serializer.endTag(null, "handshakeInterval");
                    serializer.endTag(null, "backupNetworkParamMsg");
                }

                serializer.startTag(null, "timerMsg");
                // timerMsg =========================================
                BaseParamMsg.TimerMsg timerMsg = configBean.getTimer();
                serializer.startTag(null, "interval");
                serializer.text(String.valueOf(timerMsg.getInterval()));
                serializer.endTag(null, "interval");
                serializer.startTag(null, "count");
                serializer.text(String.valueOf(timerMsg.getCount()));
                serializer.endTag(null, "count");
                serializer.endTag(null, "timerMsg");

                serializer.startTag(null, "accReportMsg");
                // accReportMsg =========================================
                BaseParamMsg.ReportMsg accReportMsg = configBean.getAccReport();
                serializer.startTag(null, "accReport");
                serializer.text(String.valueOf(accReportMsg.getReportState()));
                serializer.endTag(null, "accReport");
                serializer.endTag(null, "accReportMsg");

                serializer.startTag(null, "statusReportMsg");
                // statusReportMsg =========================================
                BaseParamMsg.ReportMsg statusReportMsg = configBean.getStatusReport();
                serializer.startTag(null, "statusReport");
                serializer.text(String.valueOf(statusReportMsg.getReportState()));
                serializer.endTag(null, "statusReport");
                serializer.endTag(null, "statusReportMsg");

                serializer.startTag(null, "faultReportMsg");
                // faultReportMsg =========================================
                BaseParamMsg.ReportMsg faultReportMsg = configBean.getFaultReport();
                serializer.startTag(null, "faultReport");
                serializer.text(String.valueOf(faultReportMsg.getReportState()));
                serializer.endTag(null, "faultReport");
                serializer.endTag(null, "faultReportMsg");

                serializer.startTag(null, "alarmReportMsg");
                // alarmReportMsg =========================================
                BaseParamMsg.ReportMsg alarmReportMsg = configBean.getAlarmReport();
                serializer.startTag(null, "alarmReport");
                serializer.text(String.valueOf(alarmReportMsg.getReportState()));
                serializer.endTag(null, "alarmReport");
                serializer.endTag(null, "alarmReportMsg");

                serializer.startTag(null, "restrictionMsg");
                // restrictionMsg =========================================
                BaseParamMsg.RestrictionMsg restrictionMsg = configBean.getRestriction();
                serializer.startTag(null, "maxSpeed");
                serializer.text(String.valueOf(restrictionMsg.getMaxSpeed()));
                serializer.endTag(null, "maxSpeed");
                serializer.startTag(null, "overspeedDuration");
                serializer.text(String.valueOf(restrictionMsg.getOverspeedDuration()));
                serializer.endTag(null, "overspeedDuration");
                serializer.startTag(null, "parkingDuration");
                serializer.text(String.valueOf(restrictionMsg.getParkingDuration()));
                serializer.endTag(null, "parkingDuration");
                serializer.startTag(null, "fatigueDrivingDuration");
                serializer.text(String.valueOf(restrictionMsg.getFatigueDrivingDuration()));
                serializer.endTag(null, "fatigueDrivingDuration");
                serializer.startTag(null, "decelerationX");
                serializer.text(String.valueOf(restrictionMsg.getDecelerationX()));
                serializer.endTag(null, "decelerationX");
                serializer.startTag(null, "accelerationX");
                serializer.text(String.valueOf(restrictionMsg.getAccelerationX()));
                serializer.endTag(null, "accelerationX");
                serializer.startTag(null, "decelerationY");
                serializer.text(String.valueOf(restrictionMsg.getDecelerationY()));
                serializer.endTag(null, "decelerationY");
                serializer.startTag(null, "accelerationY");
                serializer.text(String.valueOf(restrictionMsg.getAccelerationY()));
                serializer.endTag(null, "accelerationY");
                serializer.startTag(null, "decelerationZ");
                serializer.text(String.valueOf(restrictionMsg.getDecelerationZ()));
                serializer.endTag(null, "decelerationZ");
                serializer.startTag(null, "accelerationZ");
                serializer.text(String.valueOf(restrictionMsg.getAccelerationZ()));
                serializer.endTag(null, "accelerationZ");
                serializer.endTag(null, "restrictionMsg");

                serializer.endDocument();
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean configFileExists(Context context) {
        File file = new File(FILE_DIR, CONFIG_FILE);
        return file.exists();
    }

    public static boolean generateConfigFile(Context context) {
        ConfigBean configBean = createDefaultConfigBean();
        saveConfigBean(context, configBean);
        return true;
    }
}
