package com.smb.murtraja.baitaumate;

import android.net.wifi.ScanResult;

import java.util.List;

/**
 * Created by murtraja on 4/5/17.
 */

public interface IWifiScanDisplayHandler {
    public void onWifiScanResultsAvailable(List<ScanResult> results);
}
