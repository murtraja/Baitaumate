package com.smb.murtraja.baitaumate;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

public class WifiModeActivity extends Activity implements OnFragmentInteractionListener{

    private WifiScanDisplayFragment mWifiScanDisplayFragment;
    private WifiConnectFragment mWifiConnectFragment;
    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_mode);
        mFragmentManager = getFragmentManager();
        boolean scanDevices = true, checkable = true;
//        mWifiScanDisplayFragment = WifiScanDisplayFragment.newInstance(scanDevices, checkable, FragmentResultType.MULTIPLE_ACCESS_POINT_SELECTED);
        mWifiConnectFragment = WifiConnectFragment.newInstance("Celerio", "hadtochhange", FragmentResultType.ACCESS_POINT_CONNECTED);
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.ll_wifi_mode, mWifiConnectFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onFragmentInteraction(FragmentResultType resultType, Object result) {
        if (resultType == FragmentResultType.MULTIPLE_ACCESS_POINT_SELECTED) {
            onMultipleAcessPointsSelected((List<String>) result);
        }
    }

    private void onMultipleAcessPointsSelected(List<String> accessPoints) {
        Log.d(MainActivity.TAG, "Got the following selected Access Points: "+accessPoints);
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.ll_wifi_mode, mWifiScanDisplayFragment);
        fragmentTransaction.commit();


    }
}
