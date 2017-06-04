package com.smb.murtraja.baitaumate;

import com.smb.murtraja.baitaumate.OnInteractionListener.InteractionResultType;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
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
    Error handling functionality needs to be added here
     */


    String mAccessPointName;
    WifiManager mWifiManager;
    NetworkInfo mNetworkInfo;

    InteractionResultType mResultType;
    OnInteractionListener mListener;

    public WifiStateChangedActionReceiver(String accessPointName, WifiManager wifiManager,
                                          NetworkInfo networkInfo, InteractionResultType resultType, OnInteractionListener listener) {
        mAccessPointName = accessPointName;
        mWifiManager = wifiManager;
        mNetworkInfo = networkInfo;

        mResultType = resultType;
        mListener = listener;
    }
    boolean networkInfoIsConnected(Intent intent) {
        String action = intent.getAction();
        String methodName = "networkInfoIsConnected";
        if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(info.isConnected()) {
                String extra = info.getExtraInfo();
                Log.d(TAG(methodName), "extra is "+extra);
                if(extra.equals(mAccessPointName)) return true;
            }
        }
        return false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        boolean isConnected = networkInfoIsConnected(intent);
        if(isConnected) {
            sendResultBack(context, true);
            return;
        }
//        if(supplicantState == null) {
//            sendResultBack(context, false);
//            return;
//        }
    }
    public void sendResultBack(Context context, boolean successful) {
        context.unregisterReceiver(this);
        mListener.onInteraction(mResultType, successful);
    }
    private NetworkInfo getNetworkInfo() {
        // TODO: WARNING: i am assuming that mListener is a fragment
        ConnectivityManager connectivityManager = (ConnectivityManager)
                ((Fragment)mListener).getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return networkInfo;
    }
    private boolean isWifiNetworkConnected() {
        return getNetworkInfo().isConnected();
    }
    private boolean isSSID(String ssid) {
        return false;
    }
    public boolean checkIfConnected(SupplicantState supplicantState, Intent intent) {

        mNetworkInfo = getNetworkInfo();
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
//        if(wifiInfo == null)
//            return false;
        String currentSSID = wifiInfo.getSSID();

        NetworkInfo.DetailedState networkInfoState = WifiInfo.getDetailedStateOf(supplicantState);
        String methodName = "checkIfConnected";
        Log.e(TAG(methodName, "SSID"), currentSSID);
        Log.e(TAG(methodName, "Network Info Connection"),""+mNetworkInfo);
        Log.e(TAG(methodName, "Detailed State"), networkInfoState+"");
        if(currentSSID.equals(this.mAccessPointName)
                && supplicantState == SupplicantState.COMPLETED
                && networkInfoState == NetworkInfo.DetailedState.OBTAINING_IPADDR
                //&& isWifiNetworkConnected()
                )
                 {
            //Log.e(TAG(methodName, "Network Info Connection"),""+networkInfo.isConnected());
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
        if(supplicantState == null)
            return null;
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
            supplicantState = null; //necessary, to detect this error
        }
        return supplicantState;
    }
    String TAG(String ... secondaryTags ) {
        String tag = "WSCAR";
        for (String secondaryTag : secondaryTags) {
            tag += "/" + secondaryTag;
        }
        return tag;
    }
}
