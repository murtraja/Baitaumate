package com.smb.murtraja.baitaumate;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class WifiModeActivity extends Activity implements OnInteractionListener {

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
            b.
                i. Make a new fragment ConnectAndSendFragment ? to do the job

            i. The retrieved list must be stored in the activity as List or Queue call it DL
            ii. Whenever 5.i. returns a callback with a list of devices, start the WifiConnectFragment
            iii. remove an element from DL, connect to it
            iv. Then on its callback start the CommandSenderOld
            v. and on its callback go to iii if DL is not empty
        7.
            i. Use WifiConnectFragment
            HOW WILL IT KNOW NOT to follow 6 iv upon return?
            Use resultType here too perhaps? YES, for 6iv, it will return a result type of
        8.
            need to write a network prober that does this job

     */

    private static String TAG = "WMAct";

    private Activity mWifiModeActivity = null;

    private TextView mStatusTextView;

    private FragmentManager mFragmentManager;
    private Fragment mCurrentlyAttachedFragment;

    private String mWifiNetworkName, mPassword;
    private List<String> mAccessPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_mode);
        mFragmentManager = getFragmentManager();

        mStatusTextView = (TextView) findViewById(R.id.tv_wifi_mode_config_status);

        mWifiModeActivity = this;

        //*
        boolean scanDevices = false, checkable = false;
        WifiScanDisplayFragment wifiScanDisplayFragment = WifiScanDisplayFragment.newInstance(
                scanDevices, checkable, InteractionResultType.ROUTER_SELECTED);
        updateStatus("1. Please select your router SSID", true);
        setFragment(wifiScanDisplayFragment);
        //*/

        /*
        // for debugging purposes:
        String arrayOfDeviceMAC[] = {"5c:cf:7f:c3:71:0c", "5c:cf:7f:c4:43:e5", "5c:cf:7f:c3:cc:22", "5c:cf:7f:c3:74:a6" }; //, "18:26:66:6f:b8:6f" };
        ArrayList<String> hardwareAddressList = new ArrayList<>(Arrays.asList(arrayOfDeviceMAC));
        ProbeNetworkFragment fragment = ProbeNetworkFragment.newInstance(hardwareAddressList, InteractionResultType.PROBE_FINISHED);
        setFragment(fragment);
        //*/
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        if(mCurrentlyAttachedFragment != null) {
            fragmentTransaction.remove(mCurrentlyAttachedFragment);
        }
        fragmentTransaction.add(R.id.ll_wifi_mode, fragment);
        fragmentTransaction.commit();
        mCurrentlyAttachedFragment = fragment;
    }


    @Override
    public void onInteraction(InteractionResultType resultType, Object result) {
        if (resultType == InteractionResultType.ROUTER_SELECTED) {
            onRouterSelected(((List<String>) result).get(0));
        }

        else if (resultType == InteractionResultType.ROUTER_PASSWORD_SET) {
            onRouterPasswordSet((String) result);
        }

        else if (resultType == InteractionResultType.MULTIPLE_ACCESS_POINT_SELECTED) {
            onMultipleAccessPointsSelected((List<String>) result);
        }

        else if(resultType == InteractionResultType.COMMAND_SENT) {
            onCommandSent((String) result);
        }

        else if(resultType == InteractionResultType.ROUTER_CONNECTED) {
            onRouterConnected((boolean) result);
        }
        
        else if(resultType == InteractionResultType.PROBE_FINISHED) {
            onProbeFinished((HashMap<String,String>)result);
        }

        else if(resultType == InteractionResultType.DEBUG) {
            onDebugNetworkProber(result);
        }
    }

    private void onProbeFinished(HashMap<String, String> mapping) {
        Log.d(TAG, "network prober returned to wifi mode activity" + mapping);
        SharedPreferences preferences = this.getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        JSONObject jsonMapping = new JSONObject(mapping);
        String stringMapping = jsonMapping.toString();
        Log.d(TAG, "mapping: "+stringMapping);
        editor.putString("mapping", stringMapping);
        editor.commit();
        Log.d(TAG, String.format("Stored %s:%s in shared preferences", "mapping", stringMapping));
        finish();
    }

    private void onDebugNetworkProber(Object result) {
        HashMap<String, String> mapping = (HashMap<String, String>) result;
        Log.d(TAG, "DEBUG: network prober returned to wifi mode activity " + mapping);
    }

    private void onRouterConnected(boolean connected) {
        if(connected) {
            String arrayOfDeviceMAC[] = {"5c:cf:7f:c3:71:0c", "5c:cf:7f:c4:43:e5", "5c:cf:7f:c3:cc:22", "5c:cf:7f:c3:74:a6" };//, "18:26:66:6f:b8:6f" };
            ArrayList<String> hardwareAddressList = new ArrayList<>(Arrays.asList(arrayOfDeviceMAC));
            ProbeNetworkFragment fragment = ProbeNetworkFragment.newInstance(hardwareAddressList, InteractionResultType.PROBE_FINISHED);
            updateStatus("5. Finding devices on "+mWifiNetworkName, true);
            setFragment(fragment);

        }
    }

    private void onRouterSelected(String wifiNetworkName) {
        Log.d(TAG, "received "+wifiNetworkName);
        mWifiNetworkName = wifiNetworkName;
        AskPasswordFragment askPasswordFragment = AskPasswordFragment.newInstance(InteractionResultType.ROUTER_PASSWORD_SET);
        updateStatus("2. Enter the password for "+mWifiNetworkName+"\nLeave it blank if no password", true);
        setFragment(askPasswordFragment);

    }

    private void onRouterPasswordSet(String password) {
        Log.d(TAG, "received "+password);
        mPassword = password;
        WifiScanDisplayFragment wifiScanDisplayFragment =
                WifiScanDisplayFragment.newInstance(true, true, InteractionResultType.MULTIPLE_ACCESS_POINT_SELECTED);
        updateStatus("3. Which devices would you like to add to "+mWifiNetworkName, true);
        setFragment(wifiScanDisplayFragment);
    }

    private void onMultipleAccessPointsSelected(List<String> accessPoints) {
        Log.d(TAG, "Got the following selected Access Points: " + accessPoints);
        mAccessPoints = accessPoints;
        connectToTheNextAccessPoint();
    }

    private void onCommandSent(String reply) {
        Log.d(TAG, "Got this reply from device: "+reply);
        connectToTheNextAccessPoint();
    }
    private void onDebug(Object result) {
        Log.d(TAG, "WifiModeActivity: returned from CS frag with "+result);
    }

    private void connectToTheNextAccessPoint() {
        if(mAccessPoints.size() == 0) {
            // now connect to the router
            WifiConnectFragment connectFragment = WifiConnectFragment.newInstance(mWifiNetworkName, mPassword, InteractionResultType.ROUTER_CONNECTED);
            updateStatus("4. Now connecting to "+mWifiNetworkName, true);
            setFragment(connectFragment);
        } else {
            updateStatus((mAccessPoints.size())+" more to go!", true);
            String accessPoint = mAccessPoints.remove(0);
            String setRouterCommand = CommandGenerator.generateSetRouterCommand(mWifiNetworkName, mPassword);
            ConnectAndSendFragment csFragment = ConnectAndSendFragment.newInstance(accessPoint, setRouterCommand, InteractionResultType.COMMAND_SENT);
            setFragment(csFragment);
        }
    }
    void updateStatus(final String updateText, final boolean clearAll) {
        mWifiModeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(clearAll)    mStatusTextView.setText("");
                mStatusTextView.append("\n"+updateText);
            }
        });
    }
}

/*
06-11 12:48:50.041 29714-29714/? I/SELinux: Function: selinux_android_load_priority [0], There is no sepolicy file.

06-11 12:48:50.041 29714-29714/? I/SELinux: Function: selinux_android_load_priority [1], There is no sepolicy version file.

06-11 12:48:50.041 29714-29714/? I/SELinux: Function: selinux_android_load_priority , priority version is VE=SEPF_SM-G7102_4.4.2_0033


06-11 12:48:50.041 29714-29714/? I/SELinux: selinux_android_seapp_context_reload: seapp_contexts file is loaded from /seapp_contexts
06-11 12:48:50.041 29714-29714/? E/SELinux: [DEBUG] seapp_context_lookup: seinfoCategory = default
06-11 12:48:50.041 29714-29714/? E/dalvikvm: >>>>> Normal User
06-11 12:48:50.041 29714-29714/? E/dalvikvm: >>>>> com.smb.murtraja.baitaumate [ userId:0 | appId:10262 ]
06-11 12:48:50.041 29714-29714/? E/SELinux: [DEBUG] seapp_context_lookup: seinfoCategory = default
06-11 12:48:50.041 29714-29714/? D/dalvikvm: Late-enabling CheckJNI
06-11 12:48:50.451 29714-29714/com.smb.murtraja.baitaumate I/dalvikvm: Could not find method android.app.Fragment.onAttach, referenced from method com.smb.murtraja.baitaumate.WifiScanDisplayFragment.onAttach
06-11 12:48:50.451 29714-29714/com.smb.murtraja.baitaumate W/dalvikvm: VFY: unable to resolve virtual method 120: Landroid/app/Fragment;.onAttach (Landroid/content/Context;)V
06-11 12:48:50.451 29714-29714/com.smb.murtraja.baitaumate D/dalvikvm: VFY: replacing opcode 0x6f at 0x0000
06-11 12:48:50.471 29714-29714/com.smb.murtraja.baitaumate D/MMR: inside on attach of fragment
06-11 12:48:50.581 29714-29714/com.smb.murtraja.baitaumate D/AbsListView: Get MotionRecognitionManager
06-11 12:48:50.691 29714-29714/com.smb.murtraja.baitaumate I/Adreno-EGL: <qeglDrvAPI_eglInitialize:410>: EGL 1.4 QUALCOMM build:  ()
                                                                         OpenGL ES Shader Compiler Version: E031.24.00.08
                                                                         Build Date: 07/15/14 Tue
                                                                         Local Branch: AU200-20140715-all-patches-au200-839308
                                                                         Remote Branch:
                                                                         Local Patches:
                                                                         Reconstruct Branch:
06-11 12:48:50.751 29714-29714/com.smb.murtraja.baitaumate D/OpenGLRenderer: Enabling debug mode 0
06-11 12:48:51.731 29714-29714/com.smb.murtraja.baitaumate D/MMR: inside on receive, total results: 6
06-11 12:48:51.731 29714-29714/com.smb.murtraja.baitaumate D/MMR: 5C:CF:7F:C3:74:A6
06-11 12:48:51.731 29714-29714/com.smb.murtraja.baitaumate D/MMR: Celerio
06-11 12:48:51.731 29714-29714/com.smb.murtraja.baitaumate D/MMR: 5C:CF:7F:C3:74:A7
06-11 12:48:51.731 29714-29714/com.smb.murtraja.baitaumate D/MMR: 5C:CF:7F:C3:CC:22
06-11 12:48:51.731 29714-29714/com.smb.murtraja.baitaumate D/MMR: 5C:CF:7F:C3:71:0C
06-11 12:48:51.731 29714-29714/com.smb.murtraja.baitaumate D/MMR: 5C:CF:7F:C4:43:E5
06-11 12:48:51.731 29714-29714/com.smb.murtraja.baitaumate D/MMR: Converted 6 results to 6 unique results
06-11 12:49:19.991 29714-29714/com.smb.murtraja.baitaumate D/MMR: received Celerio
06-11 12:49:19.991 29714-29714/com.smb.murtraja.baitaumate I/dalvikvm: Could not find method android.app.Fragment.onAttach, referenced from method com.smb.murtraja.baitaumate.AskPasswordFragment.onAttach
06-11 12:49:19.991 29714-29714/com.smb.murtraja.baitaumate W/dalvikvm: VFY: unable to resolve virtual method 120: Landroid/app/Fragment;.onAttach (Landroid/content/Context;)V
06-11 12:49:19.991 29714-29714/com.smb.murtraja.baitaumate D/dalvikvm: VFY: replacing opcode 0x6f at 0x0000
06-11 12:49:20.031 29714-29714/com.smb.murtraja.baitaumate D/AbsListView: onDetachedFromWindow
06-11 12:49:20.031 29714-29714/com.smb.murtraja.baitaumate D/MMR: inside on attach of fragment
06-11 12:49:20.641 29714-29714/com.smb.murtraja.baitaumate D/MMR: received hadtochange
06-11 12:49:20.661 29714-29714/com.smb.murtraja.baitaumate D/MMR: inside on attach of fragment
06-11 12:49:20.681 29714-29714/com.smb.murtraja.baitaumate D/AbsListView: Get MotionRecognitionManager
06-11 12:49:21.771 29714-29714/com.smb.murtraja.baitaumate D/MMR: inside on receive, total results: 6
06-11 12:49:21.771 29714-29714/com.smb.murtraja.baitaumate D/MMR: 5C:CF:7F:C3:74:A6
06-11 12:49:21.771 29714-29714/com.smb.murtraja.baitaumate D/MMR: Celerio
06-11 12:49:21.771 29714-29714/com.smb.murtraja.baitaumate D/MMR: 5C:CF:7F:C3:74:A7
06-11 12:49:21.771 29714-29714/com.smb.murtraja.baitaumate D/MMR: 5C:CF:7F:C3:CC:22
06-11 12:49:21.771 29714-29714/com.smb.murtraja.baitaumate D/MMR: 5C:CF:7F:C3:71:0C
06-11 12:49:21.771 29714-29714/com.smb.murtraja.baitaumate D/MMR: 5C:CF:7F:C4:43:E5
06-11 12:49:21.771 29714-29714/com.smb.murtraja.baitaumate D/MMR: Converted 6 results to 5 unique results
06-11 12:49:23.481 29714-29714/com.smb.murtraja.baitaumate D/MMR: Got the following selected Access Points: [5C:CF:7F:C3:74:A6, 5C:CF:7F:C3:74:A7, 5C:CF:7F:C3:CC:22, 5C:CF:7F:C3:71:0C, 5C:CF:7F:C4:43:E5]
06-11 12:49:23.481 29714-29714/com.smb.murtraja.baitaumate I/dalvikvm: Could not find method android.app.Fragment.onAttach, referenced from method com.smb.murtraja.baitaumate.ConnectAndSendFragment.onAttach
06-11 12:49:23.481 29714-29714/com.smb.murtraja.baitaumate W/dalvikvm: VFY: unable to resolve virtual method 120: Landroid/app/Fragment;.onAttach (Landroid/content/Context;)V
06-11 12:49:23.481 29714-29714/com.smb.murtraja.baitaumate D/dalvikvm: VFY: replacing opcode 0x6f at 0x0000
06-11 12:49:23.491 29714-29714/com.smb.murtraja.baitaumate D/AbsListView: onDetachedFromWindow
06-11 12:49:23.491 29714-29714/com.smb.murtraja.baitaumate D/MMR: inside on attach of fragment
06-11 12:49:23.501 29714-29714/com.smb.murtraja.baitaumate I/dalvikvm: Could not find method android.app.Fragment.onAttach, referenced from method com.smb.murtraja.baitaumate.WifiConnectFragment.onAttach
06-11 12:49:23.501 29714-29714/com.smb.murtraja.baitaumate W/dalvikvm: VFY: unable to resolve virtual method 120: Landroid/app/Fragment;.onAttach (Landroid/content/Context;)V
06-11 12:49:23.501 29714-29714/com.smb.murtraja.baitaumate D/dalvikvm: VFY: replacing opcode 0x6f at 0x0000
06-11 12:49:23.561 29714-29714/com.smb.murtraja.baitaumate D/MMR: Now enabling network "5C:CF:7F:C3:74:A6"
06-11 12:49:23.591 29714-29714/com.smb.murtraja.baitaumate D/WifiStateChange/isWifiConnected: extra is "5C:CF:7F:C3:74:A6"
06-11 12:49:23.591 29714-29714/com.smb.murtraja.baitaumate D/MMR: WCFrag: sending result to activity true
06-11 12:49:23.591 29714-29714/com.smb.murtraja.baitaumate D/MMR: CSFrag: successfully connected to 5C:CF:7F:C3:74:A6
06-11 12:49:23.591 29714-29714/com.smb.murtraja.baitaumate D/DHCP: device address: 192.168.4.1
06-11 12:49:23.591 29714-29714/com.smb.murtraja.baitaumate D/MMR: CommandSender: now sending >> $Celerio:hadtochange
06-11 12:49:23.591 29714-31118/com.smb.murtraja.baitaumate D/MMR: Now running the thread...
06-11 12:49:23.611 29714-31118/com.smb.murtraja.baitaumate D/MMR: init successful
06-11 12:49:23.611 29714-31118/com.smb.murtraja.baitaumate D/MMR: printWriter: $Celerio:hadtochange
06-11 12:49:23.611 29714-31118/com.smb.murtraja.baitaumate D/MMR: receiveReply: now starting loop
06-11 12:49:23.751 29714-31118/com.smb.murtraja.baitaumate D/MMR: received reply:
06-11 12:49:23.751 29714-29714/com.smb.murtraja.baitaumate D/MMR: Got this reply from device:
06-11 12:49:23.771 29714-29714/com.smb.murtraja.baitaumate D/MMR: inside on attach of fragment
06-11 12:49:23.841 29714-29714/com.smb.murtraja.baitaumate D/MMR: Now enabling network "5C:CF:7F:C3:74:A7"
06-11 12:49:23.891 29714-29714/com.smb.murtraja.baitaumate D/WifiStateChange/isWifiConnected: extra is "5C:CF:7F:C3:74:A6"
06-11 12:49:29.191 29714-29714/com.smb.murtraja.baitaumate D/WifiStateChange/isWifiConnected: extra is "5C:CF:7F:C3:74:A7"
06-11 12:49:29.191 29714-29714/com.smb.murtraja.baitaumate D/MMR: WCFrag: sending result to activity true
06-11 12:49:29.191 29714-29714/com.smb.murtraja.baitaumate D/MMR: CSFrag: successfully connected to 5C:CF:7F:C3:74:A7
06-11 12:49:29.201 29714-29714/com.smb.murtraja.baitaumate D/DHCP: device address: 192.168.43.1
06-11 12:49:29.201 29714-29714/com.smb.murtraja.baitaumate D/MMR: CommandSender: now sending >> $Celerio:hadtochange
06-11 12:49:29.201 29714-31483/com.smb.murtraja.baitaumate D/MMR: Now running the thread...
06-11 12:49:29.241 29714-31483/com.smb.murtraja.baitaumate D/MMR: init successful
06-11 12:49:29.241 29714-31483/com.smb.murtraja.baitaumate D/MMR: printWriter: $Celerio:hadtochange
06-11 12:49:29.241 29714-31483/com.smb.murtraja.baitaumate D/MMR: receiveReply: now starting loop
06-11 12:49:29.281 29714-31483/com.smb.murtraja.baitaumate D/MMR: receiveReply: nextReplyLine: Received WFN configuration parameters
06-11 12:49:29.281 29714-31483/com.smb.murtraja.baitaumate D/MMR: receiveReply: nextReplyLine: EOF
06-11 12:49:29.281 29714-31483/com.smb.murtraja.baitaumate D/MMR: received reply: Received WFN configuration parameters
06-11 12:49:29.281 29714-29714/com.smb.murtraja.baitaumate D/MMR: Got this reply from device: Received WFN configuration parameters
06-11 12:49:29.291 29714-29714/com.smb.murtraja.baitaumate D/MMR: inside on attach of fragment
06-11 12:49:29.351 29714-29714/com.smb.murtraja.baitaumate D/MMR: Now enabling network "5C:CF:7F:C3:CC:22"
06-11 12:49:29.391 29714-29714/com.smb.murtraja.baitaumate D/WifiStateChange/isWifiConnected: extra is "5C:CF:7F:C3:74:A7"
06-11 12:49:31.961 29714-29714/com.smb.murtraja.baitaumate D/WifiStateChange/isWifiConnected: extra is "5C:CF:7F:C3:CC:22"
06-11 12:49:31.971 29714-29714/com.smb.murtraja.baitaumate D/MMR: WCFrag: sending result to activity true
06-11 12:49:31.971 29714-29714/com.smb.murtraja.baitaumate D/MMR: CSFrag: successfully connected to 5C:CF:7F:C3:CC:22
06-11 12:49:31.971 29714-29714/com.smb.murtraja.baitaumate D/DHCP: device address: 192.168.4.1
06-11 12:49:31.971 29714-29714/com.smb.murtraja.baitaumate D/MMR: CommandSender: now sending >> $Celerio:hadtochange
06-11 12:49:31.971 29714-31691/com.smb.murtraja.baitaumate D/MMR: Now running the thread...
06-11 12:49:31.981 29714-31691/com.smb.murtraja.baitaumate D/MMR: init successful
06-11 12:49:31.981 29714-31691/com.smb.murtraja.baitaumate D/MMR: printWriter: $Celerio:hadtochange
06-11 12:49:31.981 29714-31691/com.smb.murtraja.baitaumate D/MMR: receiveReply: now starting loop
06-11 12:49:32.141 29714-31691/com.smb.murtraja.baitaumate D/MMR: receiveReply: nextReplyLine: Celerio
06-11 12:49:32.141 29714-31691/com.smb.murtraja.baitaumate D/MMR: receiveReply: nextReplyLine: hadtochange
06-11 12:49:32.141 29714-31691/com.smb.murtraja.baitaumate D/MMR: receiveReply: nextReplyLine:
06-11 12:49:32.151 29714-31691/com.smb.murtraja.baitaumate D/MMR: receiveReply: nextReplyLine: EOF
06-11 12:49:32.151 29714-31691/com.smb.murtraja.baitaumate D/MMR: received reply: Celeriohadtochange
06-11 12:49:32.151 29714-29714/com.smb.murtraja.baitaumate D/MMR: Got this reply from device: Celeriohadtochange
06-11 12:49:32.171 29714-29714/com.smb.murtraja.baitaumate D/MMR: inside on attach of fragment
06-11 12:49:32.261 29714-29714/com.smb.murtraja.baitaumate D/MMR: Now enabling network "5C:CF:7F:C3:71:0C"
06-11 12:49:32.281 29714-29714/com.smb.murtraja.baitaumate D/WifiStateChange/isWifiConnected: extra is "5C:CF:7F:C3:CC:22"
06-11 12:49:38.861 29714-29714/com.smb.murtraja.baitaumate D/WifiStateChange/isWifiConnected: extra is "5C:CF:7F:C3:71:0C"
06-11 12:49:38.861 29714-29714/com.smb.murtraja.baitaumate D/MMR: WCFrag: sending result to activity true
06-11 12:49:38.861 29714-29714/com.smb.murtraja.baitaumate D/MMR: CSFrag: successfully connected to 5C:CF:7F:C3:71:0C

                                                                  [ 06-11 12:49:38.861   831:  948 D/         ]
                                                                  TIMA: QCOM_send_command result--0


                                                                  [ 06-11 12:49:38.861   831:  948 D/         ]
                                                                  TIMA_PKM_measure_kernel returns with resp..ret=512!


                                                                  [ 06-11 12:49:38.861   831:  948 D/         ]
                                                                  TAL: TIMA_PKM_measure_kernel--int8_t TIMA_PKM_measure_kernel(TIMA_handle_t, void*, uint32_t, void*, uint32_t, uint32_t)


                                                                  [ 06-11 12:49:38.861   831:  948 D/         ]
                                                                  TIMA: QCOM_send_command--int8_t QCOM_send_command(TIMA_handle_t, void*, uint32_t, void*, uint32_t, uint32_t)


                                                                  [ 06-11 12:49:38.861   831:  948 D/         ]
                                                                  rsp_len = 1008 rcvd_data_len = 1040, self_calculate_len=1040
06-11 12:49:38.861 29714-29714/com.smb.murtraja.baitaumate D/DHCP: device address: 192.168.4.1
06-11 12:49:38.861 29714-29714/com.smb.murtraja.baitaumate D/MMR: CommandSender: now sending >> $Celerio:hadtochange
06-11 12:49:38.871 29714-32202/com.smb.murtraja.baitaumate D/MMR: Now running the thread...

                                                                  [ 06-11 12:49:38.871   831:  948 D/         ]
                                                                  TIMA: QCOM_send_command result--0


                                                                  [ 06-11 12:49:38.871   831:  948 D/         ]
                                                                  TIMA_PKM_measure_kernel returns with resp..ret=512!


                                                                  [ 06-11 12:49:38.871   831:  948 D/         ]
                                                                  TAL: TIMA_PKM_measure_kernel--int8_t TIMA_PKM_measure_kernel(TIMA_handle_t, void*, uint32_t, void*, uint32_t, uint32_t)


                                                                  [ 06-11 12:49:38.871   831:  948 D/         ]
                                                                  TIMA: QCOM_send_command--int8_t QCOM_send_command(TIMA_handle_t, void*, uint32_t, void*, uint32_t, uint32_t)


                                                                  [ 06-11 12:49:38.871   831:  948 D/         ]
                                                                  rsp_len = 1008 rcvd_data_len = 1040, self_calculate_len=1040
06-11 12:49:38.881 29714-32202/com.smb.murtraja.baitaumate D/MMR: init successful
06-11 12:49:38.881 29714-32202/com.smb.murtraja.baitaumate D/MMR: printWriter: $Celerio:hadtochange
06-11 12:49:38.881 29714-32202/com.smb.murtraja.baitaumate D/MMR: receiveReply: now starting loop

                                                                  [ 06-11 12:49:38.881   831:  948 D/         ]
                                                                  TIMA: QCOM_send_command result--0


                                                                  [ 06-11 12:49:38.881   831:  948 D/         ]
                                                                  TIMA_PKM_measure_kernel returns with resp..ret=512!


                                                                  [ 06-11 12:49:38.881   831:  948 D/         ]
                                                                  TAL: TIMA_PKM_measure_kernel--int8_t TIMA_PKM_measure_kernel(TIMA_handle_t, void*, uint32_t, void*, uint32_t, uint32_t)


                                                                  [ 06-11 12:49:38.881   831:  948 D/         ]
                                                                  TIMA: QCOM_send_command--int8_t QCOM_send_command(TIMA_handle_t, void*, uint32_t, void*, uint32_t, uint32_t)


                                                                  [ 06-11 12:49:38.881   831:  948 D/         ]
                                                                  rsp_len = 1008 rcvd_data_len = 1040, self_calculate_len=1040
06-11 12:49:39.011 29714-32202/com.smb.murtraja.baitaumate D/MMR: receiveReply: nextReplyLine: Celerio
06-11 12:49:39.011 29714-32202/com.smb.murtraja.baitaumate D/MMR: receiveReply: nextReplyLine: hadtochange
06-11 12:49:39.011 29714-32202/com.smb.murtraja.baitaumate D/MMR: receiveReply: nextReplyLine:

                                                                  [ 06-11 12:49:39.011   831:  948 D/         ]
                                                                  TIMA: QCOM_send_command result--0


                                                                  [ 06-11 12:49:39.011   831:  948 D/         ]
                                                                  TIMA_PKM_measure_kernel returns with resp..ret=512!


                                                                  [ 06-11 12:49:39.011   831:  948 D/         ]
                                                                  TAL: TIMA_PKM_measure_kernel--int8_t TIMA_PKM_measure_kernel(TIMA_handle_t, void*, uint32_t, void*, uint32_t, uint32_t)


                                                                  [ 06-11 12:49:39.011   831:  948 D/         ]
                                                                  TIMA: QCOM_send_command--int8_t QCOM_send_command(TIMA_handle_t, void*, uint32_t, void*, uint32_t, uint32_t)


                                                                  [ 06-11 12:49:39.011   831:  948 D/         ]
                                                                  rsp_len = 1008 rcvd_data_len = 1040, self_calculate_len=1040
06-11 12:49:39.011 29714-32202/com.smb.murtraja.baitaumate D/MMR: receiveReply: nextReplyLine: EOF
06-11 12:49:39.011 29714-32202/com.smb.murtraja.baitaumate D/MMR: received reply: Celeriohadtochange
06-11 12:49:39.011 29714-29714/com.smb.murtraja.baitaumate D/MMR: Got this reply from device: Celeriohadtochange
06-11 12:49:39.051 29714-29714/com.smb.murtraja.baitaumate D/MMR: inside on attach of fragment

                                                                  [ 06-11 12:49:39.061   831:  948 D/         ]
                                                                  TIMA: QCOM_send_command result--0


                                                                  [ 06-11 12:49:39.061   831:  948 D/         ]
                                                                  TIMA_PKM_measure_kernel returns with resp..ret=512!


                                                                  [ 06-11 12:49:39.061   831:  948 D/         ]
                                                                  TAL: TIMA_PKM_measure_kernel--int8_t TIMA_PKM_measure_kernel(TIMA_handle_t, void*, uint32_t, void*, uint32_t, uint32_t)


                                                                  [ 06-11 12:49:39.061   831:  948 D/         ]
                                                                  TIMA: QCOM_send_command--int8_t QCOM_send_command(TIMA_handle_t, void*, uint32_t, void*, uint32_t, uint32_t)


                                                                  [ 06-11 12:49:39.061   831:  948 D/         ]
                                                                  rsp_len = 1008 rcvd_data_len = 1040, self_calculate_len=1040
06-11 12:49:39.211 29714-29714/com.smb.murtraja.baitaumate D/MMR: Now enabling network "5C:CF:7F:C4:43:E5"
06-11 12:49:39.261 29714-29714/com.smb.murtraja.baitaumate D/WifiStateChange/isWifiConnected: extra is "5C:CF:7F:C3:71:0C"
06-11 12:49:46.141 29714-29714/com.smb.murtraja.baitaumate D/WifiStateChange/isWifiConnected: extra is "5C:CF:7F:C4:43:E5"
06-11 12:49:46.141 29714-29714/com.smb.murtraja.baitaumate D/MMR: WCFrag: sending result to activity true
06-11 12:49:46.141 29714-29714/com.smb.murtraja.baitaumate D/MMR: CSFrag: successfully connected to 5C:CF:7F:C4:43:E5
06-11 12:49:46.141 29714-29714/com.smb.murtraja.baitaumate D/DHCP: device address: 192.168.4.1
06-11 12:49:46.141 29714-29714/com.smb.murtraja.baitaumate D/MMR: CommandSender: now sending >> $Celerio:hadtochange
06-11 12:49:46.141 29714-308/com.smb.murtraja.baitaumate D/MMR: Now running the thread...
06-11 12:49:46.161 29714-308/com.smb.murtraja.baitaumate D/MMR: init successful
06-11 12:49:46.161 29714-308/com.smb.murtraja.baitaumate D/MMR: printWriter: $Celerio:hadtochange
06-11 12:49:46.161 29714-308/com.smb.murtraja.baitaumate D/MMR: receiveReply: now starting loop
06-11 12:49:46.161 29714-308/com.smb.murtraja.baitaumate D/MMR: receiveReply: nextReplyLine: Celerio
06-11 12:49:46.171 29714-308/com.smb.murtraja.baitaumate D/MMR: receiveReply: nextReplyLine: hadtochange
06-11 12:49:46.171 29714-308/com.smb.murtraja.baitaumate D/MMR: receiveReply: nextReplyLine:
06-11 12:49:46.171 29714-308/com.smb.murtraja.baitaumate D/MMR: receiveReply: nextReplyLine: EOF
06-11 12:49:46.171 29714-308/com.smb.murtraja.baitaumate D/MMR: received reply: Celeriohadtochange
06-11 12:49:46.171 29714-29714/com.smb.murtraja.baitaumate D/MMR: Got this reply from device: Celeriohadtochange
06-11 12:49:46.391 29714-29714/com.smb.murtraja.baitaumate D/MMR: Now enabling network "Celerio"
06-11 12:49:46.421 29714-29714/com.smb.murtraja.baitaumate D/WifiStateChange/isWifiConnected: extra is "5C:CF:7F:C4:43:E5"
06-11 12:49:48.951 29714-29714/com.smb.murtraja.baitaumate D/WifiStateChange/isWifiConnected: extra is "Celerio"
06-11 12:49:48.961 29714-29714/com.smb.murtraja.baitaumate D/MMR: WCFrag: sending result to activity true
06-11 12:49:48.961 29714-29714/com.smb.murtraja.baitaumate I/dalvikvm: Could not find method android.app.Fragment.onAttach, referenced from method com.smb.murtraja.baitaumate.ProbeNetworkFragment.onAttach
06-11 12:49:48.961 29714-29714/com.smb.murtraja.baitaumate W/dalvikvm: VFY: unable to resolve virtual method 120: Landroid/app/Fragment;.onAttach (Landroid/content/Context;)V
06-11 12:49:48.961 29714-29714/com.smb.murtraja.baitaumate D/dalvikvm: VFY: replacing opcode 0x6f at 0x0000
06-11 12:49:49.001 29714-29714/com.smb.murtraja.baitaumate D/ProbeNetworkFragment: current subnet: 192.168.0
06-11 12:49:49.021 29714-657/com.smb.murtraja.baitaumate I/System.out: 192.168.0.1 is reachable (14ms)
06-11 12:49:49.021 29714-657/com.smb.murtraja.baitaumate D/ProbeSubnetThread: discarding 192.168.0.1 -> e8:94:f6:6d:33:98
06-11 12:49:49.231 29714-798/com.smb.murtraja.baitaumate I/System.out: 192.168.0.101 is reachable (19ms)
06-11 12:49:49.231 29714-798/com.smb.murtraja.baitaumate D/ProbeHostThread: Couldn't find MAC for 192.168.0.101
06-11 12:49:50.391 29714-655/com.smb.murtraja.baitaumate D/ProbeNetworkFragment: ProbeSubnetThread returned: Probing finished

 */