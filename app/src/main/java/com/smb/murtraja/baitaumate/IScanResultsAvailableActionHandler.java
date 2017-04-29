package com.smb.murtraja.baitaumate;

import android.net.wifi.ScanResult;

import java.util.List;

/**
 * Created by murtraja on 28/4/17.
 */

public interface IScanResultsAvailableActionHandler {
    public void handleScanResultsAvailableAction(List<ScanResult> results);
}
