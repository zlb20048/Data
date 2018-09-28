package com.chleon.datacollection.autolink;

import android.os.RegistrantList;
import android.os.Registrant;
import android.os.Handler;
import android.os.AsyncResult;
import android.content.Context;

/**
 * Created by Ryan Fan on 2016/2/29.
 */
public class BaseCommands {

    protected Context mContext;

    protected boolean mServerAvailable = false;

    protected RegistrantList mSetParamRegistrants = new RegistrantList();

    protected RegistrantList mGetParamRegistrants = new RegistrantList();

    protected RegistrantList mAvailRegistrants = new RegistrantList();

    protected RegistrantList mNotAvailRegistrants = new RegistrantList();

    protected RegistrantList mNetworkParamChangeRegistrants = new RegistrantList();

    protected RegistrantList mCanMessageRegistrants = new RegistrantList();

    public BaseCommands(Context context) {
        mContext = context;  // May be null (if so we won't log statistics)
    }

    protected void setServerAvailable(boolean available) {
        if (mServerAvailable != available) {
            mServerAvailable = available;
            if (mServerAvailable) {
                mAvailRegistrants.notifyRegistrants();
            } else {
                mNotAvailRegistrants.notifyRegistrants();
            }
        }
    }

    public boolean isServerAvailable() {
        return mServerAvailable;
    }

    public void registerForSetParamMsg(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);

        mSetParamRegistrants.add(r);
    }

    public void unregisterForSetParamMsg(Handler h) {
        mSetParamRegistrants.remove(h);
    }

    public void registerForGetParamMsg(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);

        mGetParamRegistrants.add(r);
    }

    public void unregisterForGetParamMsg(Handler h) {
        mGetParamRegistrants.remove(h);
    }

    public void registerForAvailable(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);

        mAvailRegistrants.add(r);

        if (mServerAvailable) {
            r.notifyRegistrant(new AsyncResult(null, null, null));
        }
    }

    public void unregisterForAvailable(Handler h) {
        mAvailRegistrants.remove(h);
    }

    public void registerForNotAvailable(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);

        mNotAvailRegistrants.add(r);

        if (!mServerAvailable) {
            r.notifyRegistrant(new AsyncResult(null, null, null));
        }
    }

    public void unregisterForNotAvailable(Handler h) {
        mNotAvailRegistrants.remove(h);
    }

    public void registerForNetworkParamChange(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);

        mNetworkParamChangeRegistrants.add(r);
    }

    public void unregisterForNetworkParamChange(Handler h) {
        mNetworkParamChangeRegistrants.remove(h);
    }

    public void registerCanMessageChange(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);

        mCanMessageRegistrants.add(r);
    }

    public void unregisterCanMessageChange(Handler h) {
        mCanMessageRegistrants.remove(h);
    }
}
