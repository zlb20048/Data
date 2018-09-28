package com.chleon.datacollection.bean;

import com.chleon.telematics.BaseParamMsg;

/**
 * Created by Ryan Fan on 2016/2/29.
 */
public class ConfigBean {
    private BaseParamMsg.ChannelIdMsg channelId;
    private BaseParamMsg.NetworkParamMsg networkParam;
    private BaseParamMsg.NetworkParamMsg backupNetworkParam;
    private BaseParamMsg.TimerMsg timer;
    private BaseParamMsg.ReportMsg accReport;
    private BaseParamMsg.ReportMsg statusReport;
    private BaseParamMsg.ReportMsg faultReport;
    private BaseParamMsg.ReportMsg alarmReport;
    private BaseParamMsg.RestrictionMsg restriction;

    public ConfigBean() {
    }

    public ConfigBean(BaseParamMsg.ChannelIdMsg channelId,
                      BaseParamMsg.NetworkParamMsg networkParam,
                      BaseParamMsg.TimerMsg timer,
                      BaseParamMsg.ReportMsg accReport,
                      BaseParamMsg.ReportMsg statusReport,
                      BaseParamMsg.ReportMsg faultReport,
                      BaseParamMsg.ReportMsg alarmReport,
                      BaseParamMsg.RestrictionMsg restriction) {
        this.channelId = channelId;
        this.networkParam = networkParam;
        this.timer = timer;
        this.accReport = accReport;
        this.statusReport = statusReport;
        this.faultReport = faultReport;
        this.alarmReport = alarmReport;
        this.restriction = restriction;
    }

    public BaseParamMsg.ChannelIdMsg getChannelId() {
        return channelId;
    }

    public void setChannelId(BaseParamMsg.ChannelIdMsg channelId) {
        this.channelId = channelId;
    }

    public BaseParamMsg.NetworkParamMsg getNetworkParam() {
        return networkParam;
    }

    public void setNetworkParam(BaseParamMsg.NetworkParamMsg networkParam) {
        this.networkParam = networkParam;
    }

    public BaseParamMsg.NetworkParamMsg getBackupNetworkParam() {
        return backupNetworkParam;
    }

    public void setBackupNetworkParam(BaseParamMsg.NetworkParamMsg backupNetworkParam) {
        this.backupNetworkParam = backupNetworkParam;
    }

    public BaseParamMsg.TimerMsg getTimer() {
        return timer;
    }

    public void setTimer(BaseParamMsg.TimerMsg timer) {
        this.timer = timer;
    }

    public BaseParamMsg.ReportMsg getAccReport() {
        return accReport;
    }

    public void setAccReport(BaseParamMsg.ReportMsg accReport) {
        this.accReport = accReport;
    }

    public BaseParamMsg.ReportMsg getStatusReport() {
        return statusReport;
    }

    public void setStatusReport(BaseParamMsg.ReportMsg statusReport) {
        this.statusReport = statusReport;
    }

    public BaseParamMsg.ReportMsg getFaultReport() {
        return faultReport;
    }

    public void setFaultReport(BaseParamMsg.ReportMsg faultReport) {
        this.faultReport = faultReport;
    }

    public BaseParamMsg.ReportMsg getAlarmReport() {
        return alarmReport;
    }

    public void setAlarmReport(BaseParamMsg.ReportMsg alarmReport) {
        this.alarmReport = alarmReport;
    }

    public BaseParamMsg.RestrictionMsg getRestriction() {
        return restriction;
    }

    public void setRestriction(BaseParamMsg.RestrictionMsg restriction) {
        this.restriction = restriction;
    }

    public void updateRestriction(BaseParamMsg.RestrictionMsg restriction) {
        if(restriction.isRestrictMaxSpeed()) {
            this.restriction.setMaxSpeed(restriction.getMaxSpeed());
        }
        if(restriction.isRestrictOverSpeedDuration()) {
            this.restriction.setOverspeedDuration(restriction.getOverspeedDuration());
        }
        if(restriction.isRestrictParkingDuration()) {
            this.restriction.setParkingDuration(restriction.getParkingDuration());
        }
        if(restriction.isRestrictFatigueDrivingDuration()) {
            this.restriction.setFatigueDrivingDuration(restriction.getFatigueDrivingDuration());
        }
        if(restriction.isRestrictDecelerationX()) {
            this.restriction.setDecelerationX(restriction.getDecelerationX());
        }
        if(restriction.isRestrictAccelerationX()) {
            this.restriction.setAccelerationX(restriction.getAccelerationX());
        }
        if(restriction.isRestrictDecelerationY()) {
            this.restriction.setDecelerationY(restriction.getDecelerationY());
        }
        if(restriction.isRestrictAccelerationY()) {
            this.restriction.setAccelerationY(restriction.getAccelerationY());
        }
        if(restriction.isRestrictDecelerationZ()) {
            this.restriction.setDecelerationZ(restriction.getDecelerationZ());
        }
        if(restriction.isRestrictAccelerationZ()) {
            this.restriction.setAccelerationZ(restriction.getAccelerationZ());
        }
    }
}
