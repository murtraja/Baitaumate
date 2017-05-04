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

    /*
    the basic idea behind this class is that the results which are available
    through Wifi scan, need to be filtered out.

    For direct mode, i care about the SSIDs of Devices only (matchDevices = true)

    But for Wifi mode, i just need to search for routers, so, i need a list of APs
    which are not devices
     */

    static String octetRegex = "([A-Fa-f0-9]{2})";
    static String deviceRegex = String.format("^%s(:%s){5}$", octetRegex, octetRegex);
    static List<String> getUniqueAPsFromScanResults(List<ScanResult> results, boolean matchDevices) {
        //http://stackoverflow.com/a/27046433/4014182
        List<String> uniqueAPs = new ArrayList<>();
        HashMap<String, ScanResult> signalStrength = new HashMap<>();
        for (int i = 0; i < results.size(); i++) {
            ScanResult currentResult = results.get(i);
            String ssid = currentResult.SSID;
            if(!ssidMatchesCriteria(ssid, matchDevices))
                continue;
            if(signalStrength.containsKey(ssid)) {
                //now check which one has greater signal strength
                ScanResult existingResult = signalStrength.get(ssid);
                if(existingResult.level < currentResult.level) {
                    //the new result has better level
                    Log.d(MainActivity.TAG, String.format("for SSID %s, replacing %d with %d", ssid, existingResult.level, currentResult.level));
                    signalStrength.put(ssid, currentResult);
                    //uniqueAPs.set(uniqueAPs.indexOf(ssid))
                }
            } else {
                signalStrength.put(ssid, currentResult);
                uniqueAPs.add(ssid);
            }
        }
        Log.d(MainActivity.TAG, String.format("Converted %d results to %d unique results", results.size(), uniqueAPs.size()));
        return uniqueAPs;
    }
    static boolean ssidMatchesCriteria(String ssid, boolean matchDevices) {
        boolean isDevice = ssid.matches(deviceRegex);
        if(matchDevices)
            return isDevice;

        //time to match routers
        return "".equals(ssid) ? false : true;
    }
}
