package com.smb.murtraja.baitaumate;

import android.net.wifi.ScanResult;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by murtraja on 28/4/17.
 */

public class WifiResultsProcessor {
    static String octet = "([A-Fa-f0-9]{2})";
    static List<String> getUniqueAPsFromScanResults(List<ScanResult> results, boolean isDirectMode) {
        List<String> uniqueAPs = new ArrayList<>();
        HashMap<String, ScanResult> signalStrength = new HashMap<>();
        for (int i = 0; i < results.size(); i++) {
            ScanResult result = results.get(i);
            String ssid = result.SSID;
            if("".equals(ssid) || (!ssid.matches(String.format("^%s(:%s){5}$", octet, octet)) && isDirectMode))
                continue;
            if(signalStrength.containsKey(ssid)) {
                //now check which one has greater signal strength
                ScanResult existingResult = signalStrength.get(ssid);
                if(existingResult.level < result.level) {
                    //the new result has better level
                    Log.d(MainActivity.TAG, String.format("for SSID %s, replacing %d with %d", ssid, existingResult.level, result.level));
                    signalStrength.put(ssid, result);
                    //uniqueAPs.set(uniqueAPs.indexOf(ssid))
                }
            } else {
                signalStrength.put(ssid, result);
                uniqueAPs.add(ssid);
            }
        }
        Log.d(MainActivity.TAG, String.format("Converted %d results to %d unique results", results.size(), uniqueAPs.size()));
        return uniqueAPs;
    }
}
