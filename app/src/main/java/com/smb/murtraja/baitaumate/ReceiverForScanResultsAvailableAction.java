package com.smb.murtraja.baitaumate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

/**
 * Created by murtraja on 28/4/17.
 */

public class ReceiverForScanResultsAvailableAction extends BroadcastReceiver {
    WifiManager wifiManager;
    IScanResultsAvailableActionHandler handler;
    public ReceiverForScanResultsAvailableAction(WifiManager wifiManager, IScanResultsAvailableActionHandler handler) {
        this.wifiManager = wifiManager;
        this.handler = handler;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        List<ScanResult> results = wifiManager.getScanResults();
        int resultsSize = results.size();
        Log.d(MainActivity.TAG, "inside on receive, total results: "+resultsSize);
        for (ScanResult result : results) {
            Log.d("MMR", result.SSID);
        }
        context.unregisterReceiver(this);
        this.handler.handleScanResultsAvailableAction(results);
    }
}
