package com.chleon.datacollection.autolink;

import android.os.Handler;
import android.os.Message;
import com.chleon.telematics.*;

public interface CommandsInterface {

    void login(ConnectionMsg.LoginReqMsg loginReqMsg, Message result);

    void heartbeat(Message result);

    void logout(Message result);

    void ackSetParam(SetParamRspMsg setParamRspMsg, Message result);

    void ackGetParam(GetParamRspMsg getParamRspMsg, Message result);

    void singleReport(SingleReportMsg singleReportMsg, Message result);

    void batchReport(BatchReportMsg batchReportMsg, Message result);

    void canReport(CanReqMsg canReportMsg, Message result);

    void resetSocket(BaseParamMsg.NetworkParamMsg networkParamMsg);

    void reset();

    /**
     * Sets the handler for Server notifications.
     * Unlike the register* methods, there's only one notification handler
     *
     * @param h    Handler for notification message.
     * @param what User-defined message code.
     * @param obj  User object.
     */

    void registerForSetParamMsg(Handler h, int what, Object obj);

    void unregisterForSetParamMsg(Handler h);

    /**
     * Sets the handler for Server notifications.
     * Unlike the register* methods, there's only one notification handler
     *
     * @param h    Handler for notification message.
     * @param what User-defined message code.
     * @param obj  User object.
     */

    void registerForGetParamMsg(Handler h, int what, Object obj);

    void unregisterForGetParamMsg(Handler h);

    /**
     * Fires on any transition out of RadioState.isAvailable()
     * Fires immediately if currently in that state
     * In general, actions should be idempotent. State may change
     * before event is received.
     */
    void registerForAvailable(Handler h, int what, Object obj);

    void unregisterForAvailable(Handler h);

    /**
     * Fires on any transition into !RadioState.isAvailable()
     * Fires immediately if currently in that state
     * In general, actions should be idempotent. State may change
     * before event is received.
     */
    void registerForNotAvailable(Handler h, int what, Object obj);

    void unregisterForNotAvailable(Handler h);

    boolean isServerAvailable();

    void registerForNetworkParamChange(Handler h, int what, Object obj);

    void unregisterForNetworkParamChange(Handler h);

    void registerCanMessageChange(Handler h, int what, Object obj);

    void unregisterCanMessageChange(Handler h);
}
