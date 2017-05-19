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
    private AskPasswordFragment mAskPasswordFragment;
    private WifiConnectFragment mWifiConnectFragment;
    private FragmentManager mFragmentManager;

    private List<String> mAccessPoints;
    private Fragment currentlyAttachedFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_mode);
        mFragmentManager = getFragmentManager();


        boolean scanDevices = false, checkable = false;
        WifiScanDisplayFragment wifiScanDisplayFragment = WifiScanDisplayFragment.newInstance(scanDevices, checkable, FragmentResultType.ROUTER_SELECTED);
        setFragment(wifiScanDisplayFragment);
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        if(currentlyAttachedFragment != null) {
            fragmentTransaction.remove(currentlyAttachedFragment);
        }
        fragmentTransaction.add(R.id.ll_wifi_mode, fragment);
        fragmentTransaction.commit();
        currentlyAttachedFragment = fragment;
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
        Log.d(MainActivity.TAG, "received "+wifiNetworkName);
        AskPasswordFragment askPasswordFragment = AskPasswordFragment.newInstance(FragmentResultType.ROUTER_PASSWORD_SET);
        setFragment(askPasswordFragment);

    }

    private void onRouterPasswordSet(String password) {
        Log.d(MainActivity.TAG, "received "+password);
        WifiScanDisplayFragment wifiScanDisplayFragment = WifiScanDisplayFragment.newInstance(true, true, FragmentResultType.MULTIPLE_ACCESS_POINT_SELECTED);
        setFragment(wifiScanDisplayFragment);
    }

    private void onMultipleAcessPointsSelected(List<String> accessPoints) {
        Log.d(MainActivity.TAG, "Got the following selected Access Points: " + accessPoints);
        mAccessPoints = accessPoints;
        connectToTheNextAccessPoint();



    }

    private void connectToTheNextAccessPoint() {
        if(mAccessPoints.size() == 0) {
            // now connect to the router and probe the network
        } else {
            String accessPoint = mAccessPoints.remove(0);
            WifiConnectFragment wifiConnectFragment = WifiConnectFragment.newInstance(accessPoint, "", FragmentResultType.ACCESS_POINT_CONNECTED);
            setFragment(wifiConnectFragment);
        }
    }
}
