package com.smb.murtraja.baitaumate;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import java.util.List;

public class DirectModeActivity extends Activity implements OnInteractionListener{

    /*
    This activity does the following
        1. Display a list of Devices which are in Direct mode
        2. User selects on device
        3. App then tries to connect to it
        4. Once connected, user is able to configure it
     */
    private static final String TAG = "DMAct";


    private FragmentManager mFragmentManager;
    private Fragment mCurrentlyAttachedFragment;

    private String mAccessPointName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direct_mode);
        mFragmentManager = getFragmentManager();

        boolean scanDevices = true, checkable = false;
        WifiScanDisplayFragment wifiScanDisplayFragment = WifiScanDisplayFragment.newInstance(
                scanDevices, checkable, InteractionResultType.ACCESS_POINT_CONNECTED);
        setFragment(wifiScanDisplayFragment);
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        if(mCurrentlyAttachedFragment != null) {
            fragmentTransaction.remove(mCurrentlyAttachedFragment);
        }
        fragmentTransaction.add(R.id.ll_direct_mode, fragment);
        fragmentTransaction.commit();
        mCurrentlyAttachedFragment = fragment;
    }

    @Override
    public void onInteraction(InteractionResultType resultType, Object result) {
        if(resultType == InteractionResultType.ACCESS_POINT_CONNECTED) {
            onAccessPointConnected(((List<String>) result).get(0));
        }

        else if(resultType == InteractionResultType.DEVICE_CONFIG_DONE) {
            onDeviceConfigDone(result);
        }
    }

    private void onDeviceConfigDone(Object result) {
        // set the previous fragment
        boolean scanDevices = true, checkable = false;
        WifiScanDisplayFragment wifiScanDisplayFragment = WifiScanDisplayFragment.newInstance(
                scanDevices, checkable, InteractionResultType.ACCESS_POINT_CONNECTED);
        setFragment(wifiScanDisplayFragment);
    }

    private void onAccessPointConnected(String apName) {
        mAccessPointName = apName;
        ConfigureLightFragment configFragment = ConfigureLightFragment.newInstance(null, InteractionResultType.DEVICE_CONFIG_DONE);
        setFragment(configFragment);
    }
}
