package com.smb.murtraja.baitaumate;

/**
 * Created by murtraja on 29/4/17.
 */

public interface IWifiStateChangedActionListener {
    public void handleWifiStateChangedAction(String accessPointName, boolean successful);
}
