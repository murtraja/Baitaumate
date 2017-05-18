package com.smb.murtraja.baitaumate;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

public class WifiModeActivity extends Activity implements OnFragmentInteractionListener {

    /*
    This Wifi Mode is responsible for the following
        1. It displays a list of routers which are currently available
        2. The user selects one from it (OR says use currently connected access point?) say WFN
        3. It then asks for the password for the WFN
        4. Now it shows the list of all the devices which are in Direct Mode
        5. User selects those devices which he wishes to connect to the router say D1, D2, ..., DN
        6. Now for i <- 1 to N, the following happens
            a. The app connects to Di via direct mode
            b. it sends the $<AP>:<password> command to Di
        7. The app then connects to the WFN
        8. It finds the IP:MAC mapping for D1, D2, ..., DN
        9. Displays all devices currently on WFN

    Breaking up the steps
        1.
        2.
            i. Use WifiScanDisplayFragment to get the WFN name scanDevices=false and checkable = false
        3.
            i. Make a new fragment just to take the password
        4.
        5.
            i. Use WifiScanDisplayFragment with scanDevices and checkable equal to true
        6.
            a.
                i. Use WifiConnectFragment to connect to Di
            b.
                i. Modify the CommandSender to do the job

            i. The retrieved list must be stored in the activity as List or Queue call it DL
            ii. Whenever 5.i. returns a callback with a list of devices, start the WifiConnectFragment
            iii. remove an element from DL, connect to it
            iv. Then on its callback start the CommandSender
            v. and on its callback go to iii if DL is not empty
        7.
            i. Use WifiConnectFragment
            HOW WILL IT KNOW NOT to follow 6 iv upon return?
            Use resultType here too perhaps? YES, for 6iv, it will return a result type of
        8.
            need to write a network prober that does this job

     */

    private WifiScanDisplayFragment mWifiScanDisplayFragment;
    private WifiConnectFragment mWifiConnectFragment;
    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_mode);
        mFragmentManager = getFragmentManager();
        boolean scanDevices = true, checkable = true;
        mWifiScanDisplayFragment = WifiScanDisplayFragment.newInstance(scanDevices, checkable, FragmentResultType.MULTIPLE_ACCESS_POINT_SELECTED);
//        mWifiConnectFragment = WifiConnectFragment.newInstance("Celerio", "hadtochange", FragmentResultType.ACCESS_POINT_CONNECTED);
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.ll_wifi_mode, mWifiScanDisplayFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onFragmentInteraction(FragmentResultType resultType, Object result) {
        if (resultType == FragmentResultType.ROUTER_SELECTED) {
            onRouterSelected(((List<String>) result).get(0));
        }

        else if (resultType == FragmentResultType.ROUTER_PASSWORD_SET) {
            onRouterPasswordSet((String) result);
        }

        else if (resultType == FragmentResultType.MULTIPLE_ACCESS_POINT_SELECTED) {
            onMultipleAcessPointsSelected((List<String>) result);
        }
    }

    private void onRouterSelected(String wifiNetworkName) {

    }

    private void onRouterPasswordSet(String password) {

    }

    private void onMultipleAcessPointsSelected(List<String> accessPoints) {
        Log.d(MainActivity.TAG, "Got the following selected Access Points: " + accessPoints);
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.ll_wifi_mode, mWifiScanDisplayFragment);
        fragmentTransaction.commit();


    }
}
