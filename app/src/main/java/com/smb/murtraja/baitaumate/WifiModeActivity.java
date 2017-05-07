package com.smb.murtraja.baitaumate;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

public class WifiModeActivity extends Activity implements OnFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_mode);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        boolean scanDevices = true, checkable = true;
        Fragment wifiScanDisplayFragment = WifiScanDisplayFragment.newInstance(scanDevices, checkable, FragmentResultType.MULTIPLE_ACCESS_POINT_SELECTED);
        fragmentTransaction.add(R.id.ll_wifi_mode, wifiScanDisplayFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onFragmentInteraction(FragmentResultType resultType, Object result) {
        if (resultType == FragmentResultType.MULTIPLE_ACCESS_POINT_SELECTED) {
            onMultipleAcessPointsSelected((List<String>) result);
        }
    }

    private void onMultipleAcessPointsSelected(List<String> accessPoints) {
        Log.d(MainActivity.TAG, "Got the following selected Acess Points: "+accessPoints);
    }
}
