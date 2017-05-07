package com.smb.murtraja.baitaumate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created by murtraja on 29/4/17.
 */

public class ReceiverForWifiStateChangedAction extends BroadcastReceiver {

    /*
    Error handling functionality needs to be added here
     */

    String mAccessPointName;
    WifiManager mWifiManager;
    IWifiStateChangedActionListener mListener;

    public ReceiverForWifiStateChangedAction(String accessPointName, WifiManager wifiManager, IWifiStateChangedActionListener listener) {
        mAccessPointName = accessPointName;
        mListener = listener;
        mWifiManager = wifiManager;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        SupplicantState supplicantState = logAndGetCurrentState(intent);
        boolean isConnected = checkIfConnected(supplicantState);
        if(isConnected) {
            sendResultBack(context, true);
            return;
        }
        if(supplicantState == null) {
            sendResultBack(context, false);
            return;
        }
    }
    public void sendResultBack(Context context, boolean successful) {
        context.unregisterReceiver(this);
        mListener.handleWifiStateChangedAction(mAccessPointName, successful);
    }
    public boolean checkIfConnected(SupplicantState supplicantState) {
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
//        if(wifiInfo == null)
//            return false;
        String currentSSID = wifiInfo.getSSID();
        if(currentSSID.equals(this.mAccessPointName) && supplicantState == SupplicantState.COMPLETED) {
            Log.d(MainActivity.TAG, String.format("Connected to wifi network %s", currentSSID));
            return true;
        }
        return false;
    }
    public SupplicantState logAndGetCurrentState(Intent intent) {
        //http://stackoverflow.com/questions/13318646/android-network-connectivity-states-missing
        // returns null if there is an error.
        Log.d("WifiReceiver", ">>>>SUPPLICANT_STATE_CHANGED_ACTION<<<<<<");
        SupplicantState supplicantState = ((SupplicantState) intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE));
        switch (supplicantState) {
            case ASSOCIATED:
                Log.i("SupplicantState", "ASSOCIATED");
                break;
            case ASSOCIATING:
                Log.i("SupplicantState", "ASSOCIATING");
                break;
            case AUTHENTICATING:
                Log.i("SupplicantState", "Authenticating...");
                break;
            case COMPLETED:
                Log.i("SupplicantState", "Connected");
                break;
            case DISCONNECTED:
                Log.i("SupplicantState", "Disconnected");
                break;
            case DORMANT:
                Log.i("SupplicantState", "DORMANT");
                break;
            case FOUR_WAY_HANDSHAKE:
                Log.i("SupplicantState", "FOUR_WAY_HANDSHAKE");
                break;
            case GROUP_HANDSHAKE:
                Log.i("SupplicantState", "GROUP_HANDSHAKE");
                break;
            case INACTIVE:
                Log.i("SupplicantState", "INACTIVE");
                break;
            case INTERFACE_DISABLED:
                Log.i("SupplicantState", "INTERFACE_DISABLED");
                break;
            case INVALID:
                Log.i("SupplicantState", "INVALID");
                break;
            case SCANNING:
                Log.i("SupplicantState", "SCANNING");
                break;
            case UNINITIALIZED:
                Log.i("SupplicantState", "UNINITIALIZED");
                break;
            default:
                Log.i("SupplicantState", "Unknown");
                break;

        }

        int supl_error=intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
        if(supl_error==WifiManager.ERROR_AUTHENTICATING){
            Log.i("ERROR_AUTHENTICATING", "ERROR_AUTHENTICATING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            supplicantState = null; //necessary to detect this error
        }
        return supplicantState;
    }
}
