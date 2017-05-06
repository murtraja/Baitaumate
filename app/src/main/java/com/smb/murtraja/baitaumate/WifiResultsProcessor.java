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

    private String octetRegex = "([A-Fa-f0-9]{2})";
    private String deviceRegex = String.format("^%s(:%s){5}$", octetRegex, octetRegex);
    private HashMap<String, ScanResult>  mAccessPointMap = new HashMap<>();
    private List<ScanResult> mResults;
    private List<String> mUniqueAPs = new ArrayList<>();

    public WifiResultsProcessor(List<ScanResult> results) {
        mResults = results;
    }

    public List<String> computeUniqueAPsFromScanResults(boolean matchDevices) {
        //http://stackoverflow.com/a/27046433/4014182
        init();
        for (int i = 0; i < mResults.size(); i++) {
            ScanResult currentResult = mResults.get(i);
            String ssid = currentResult.SSID;
            if(ssidMatchesCriteria(ssid, matchDevices)) {

                if (mAccessPointMap.containsKey(ssid)) {

                    //now check which one has greater signal strength and overwrite accordingly
                    updateMapWithHigherSignalStrength(currentResult);

                } else {

                    //this is a new AP, which matches our criteria and has not been added before
                    mAccessPointMap.put(ssid, currentResult);
                    mUniqueAPs.add(ssid);
                }
            }
        }
        Log.d(MainActivity.TAG, String.format("Converted %d results to %d unique results", mResults.size(), mUniqueAPs.size()));
        return mUniqueAPs;
    }

    private void init() {
        mUniqueAPs.clear();
        mAccessPointMap.clear();
    }

    private void updateMapWithHigherSignalStrength(ScanResult currentResult) {
        String ssid = currentResult.SSID;
        ScanResult existingResult = mAccessPointMap.get(ssid);
        if(existingResult.level < currentResult.level) {
            //the new result has better level
            Log.d(MainActivity.TAG, String.format("for SSID %s, replacing %d with %d", ssid, existingResult.level, currentResult.level));
            mAccessPointMap.put(ssid, currentResult);
        }
    }

    private boolean ssidMatchesCriteria(String ssid, boolean matchDevices) {
        boolean isDevice = ssid.matches(deviceRegex);

        // a small question here, if isDevice is True and matchDevices is False, what should be returned here?
        if(matchDevices)
            return isDevice;

        //time to match (only?) routers
        return "".equals(ssid) ? false : true;
    }
}
