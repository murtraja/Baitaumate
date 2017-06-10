package com.smb.murtraja.baitaumate;

import com.smb.murtraja.baitaumate.OnInteractionListener.InteractionResultType;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created by murtraja on 29/4/17.
 */

public class WifiStateChangedActionReceiver extends BroadcastReceiver {

    /*
    This class has gone into a lot of modification recently
    it used to receive supplicant state change events,
    now it only receives network state change events.
    and that too it uses a deprecated function call
    so i need
        TODO: Not connected to WFN identification, and handling it accordingly
        TODO: remove deprecated function calls

     */


    String mAccessPointName; // with double quotes
    WifiManager mWifiManager;

    InteractionResultType mResultType;
    OnInteractionListener mListener;

    public WifiStateChangedActionReceiver(String accessPointName, WifiManager wifiManager, InteractionResultType resultType, OnInteractionListener listener) {
        mAccessPointName = accessPointName;
        mWifiManager = wifiManager;

        mResultType = resultType;
        mListener = listener;
    }
    boolean isWifiConnected(Intent intent) {
        String action = intent.getAction();
        String methodName = "isWifiConnected";
        if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(info.isConnected()) {
                String accessPointName = info.getExtraInfo();
                Log.d(TAG(methodName), "extra is "+accessPointName);
                if(accessPointName.equals(mAccessPointName)) return true;
            }
        }
        return false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        boolean isConnected = isWifiConnected(intent);
        if(isConnected) {
            sendResultBack(context, true);
            return;
        }
    }

    public void sendResultBack(Context context, boolean successful) {
        context.unregisterReceiver(this);
        mListener.onInteraction(mResultType, successful);
    }

    String TAG(String ... secondaryTags ) {
        String tag = "WifiStateChange";
        for (String secondaryTag : secondaryTags) {
            tag += "/" + secondaryTag;
        }
        return tag;
    }
}
