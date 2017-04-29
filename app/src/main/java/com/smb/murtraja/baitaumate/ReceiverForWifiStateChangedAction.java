package com.smb.murtraja.baitaumate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created by murtraja on 29/4/17.
 */

public class ReceiverForWifiStateChangedAction extends BroadcastReceiver {

    String ssid;
    WifiManager wifiManager;
    IWifiStateChangedActionHandler handler;
    public ReceiverForWifiStateChangedAction(String ssid, WifiManager wifiManager, IWifiStateChangedActionHandler handler) {
        this.ssid = ssid;
        this.handler = handler;
        this.wifiManager = wifiManager;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        SupplicantState supplicantState = logAndGetCurrentState(intent);
        boolean isConnected = checkIfConnected(supplicantState);
        if(isConnected) {
            context.unregisterReceiver(this);
            this.handler.handleWifiStateChangedAction();
        }

    }
    public boolean checkIfConnected(SupplicantState supplicantState) {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//        if(wifiInfo == null)
//            return false;
        String currentSSID = wifiInfo.getSSID();
        if(currentSSID.equals(this.ssid) && supplicantState == SupplicantState.COMPLETED) {
            Log.d(MainActivity.TAG, String.format("Connected to wifi network %s", currentSSID));
            return true;
        }
        return false;
    }
    public SupplicantState logAndGetCurrentState(Intent intent) {
        //http://stackoverflow.com/questions/13318646/android-network-connectivity-states-missing
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
        return supplicantState;
    }
}
