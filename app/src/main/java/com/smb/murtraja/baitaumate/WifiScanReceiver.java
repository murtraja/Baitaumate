package com.smb.murtraja.baitaumate;

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

    WifiManager mWifiManager;
    IWifiScanDisplayHandler mHandler;

    public WifiScanReceiver(WifiManager wifiManager, IWifiScanDisplayHandler handler) {
        mWifiManager = wifiManager;
        mHandler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        List<ScanResult> results = mWifiManager.getScanResults();
        int resultsSize = results.size();
        Log.d(MainActivity.TAG, "inside on receive, total results: "+resultsSize);
        for (ScanResult result : results) {
            Log.d("MMR", result.SSID);
        }
        context.unregisterReceiver(this);
        mHandler.onWifiScanResultsAvailable(results);
    }
}
