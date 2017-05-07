package com.smb.murtraja.baitaumate;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

public class WifiModeActivity extends Activity implements WifiScanDisplayFragment.OnFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_mode);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        boolean scanDevices = true, checkable = true;
        Fragment wifiScanDisplayFragment = WifiScanDisplayFragment.newInstance(scanDevices, checkable);
        fragmentTransaction.add(R.id.ll_wifi_mode, wifiScanDisplayFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onFragmentInteraction(List<String> accessPointsSelected) {
        Log.d(MainActivity.TAG, String.format("Got %s selectedAceessPoints", accessPointsSelected));
    }
}
