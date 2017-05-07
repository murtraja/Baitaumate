package com.smb.murtraja.baitaumate;

import android.os.CountDownTimer;

/**
 * Created by murtraja on 29/4/17.
 */

public class WifiConnectedReporter extends CountDownTimer {
    IWifiStateChangedActionListener wifiStateChangedActionHandler;
    public WifiConnectedReporter(long millisInFuture, long countDownInterval,
                                 IWifiStateChangedActionListener wifiStateChangedActionHandler) {
        super(millisInFuture, countDownInterval);
        this.wifiStateChangedActionHandler = wifiStateChangedActionHandler;
    }

    @Override
    public void onTick(long millisUntilFinished) {

    }

    @Override
    public void onFinish() {

    }
}
