package com.smb.murtraja.baitaumate;

import com.smb.murtraja.baitaumate.OnInteractionListener.InteractionResultType;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

/**
 * Created by murtraja on 4/5/17.
 */

public class WifiScanReceiver extends BroadcastReceiver {


    private static final String TAG = "WifiScanReceiver";
    WifiManager mWifiManager;
    InteractionResultType mResultType;
    OnInteractionListener mListener;

    public WifiScanReceiver(WifiManager wifiManager, InteractionResultType resultType, OnInteractionListener listener) {
        mWifiManager = wifiManager;
        mResultType = resultType;
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        List<ScanResult> results = mWifiManager.getScanResults();
        int resultsSize = results.size();
        Log.d(TAG, "inside on receive, total results: "+resultsSize);
        for (ScanResult result : results) {
            Log.d("MMR", result.SSID);
        }
        context.unregisterReceiver(this);
        mListener.onInteraction(mResultType, results);
    }
}
