package com.smb.murtraja.baitaumate;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smb.murtraja.baitaumate.OnInteractionListener.InteractionResultType;

public class WifiConnectFragment extends Fragment implements IWifiStateChangedActionListener {

    /*
    this fragment simply connects to an Access Point
    and displays the status accordingly in its view

    INPUT
        > the access point name to connect to
        > the password
        > listener (which is implicit)
        > result type

    PROCESS
        > register a receiver and listen for state changes.
        > error handling
        > make a mTimer, once the enableNetwork  is called, if connection succeeds, the mTimer should be turned off.
            if mTimer expires then, the connection was unsuccessful

    OUTPUT
        > whether connected or not
        > the ssid to which the connection request was made
     */

    private static final String ARG_ACCESS_POINT = "ACCESS_POINT";
    private static final String ARG_PASSWORD = "PASSWORD";
    private static final String ARG_RESULT_TYPE = "RESULT_TYPE";

    private String mAccessPointName;
    private String mPassword;

    private InteractionResultType mResultType;
    private OnInteractionListener mListener;

    private TextView mStatusTextView;

    private boolean mConnected = false;
    private WifiManager mWifiManager;
    private CountDownTimer mTimer = new CountDownTimer(1000, 500) {
        @Override
        public void onTick(long millisUntilFinished) {
            mStatusTextView.setText(mStatusTextView.getText()+".");
        }

        @Override
        public void onFinish() {
            Log.d(MainActivity.TAG, "timer timed out!");
            unregisterReceiver();
            sendResultToActivity();
        }
    };
    private ReceiverForWifiStateChangedAction mReceiverForWifiStateChangedAction;

    private void unregisterReceiver() {

        /*
        this function is useful when timer times out
         */

        Context context = getActivity().getApplicationContext();
        context.unregisterReceiver(mReceiverForWifiStateChangedAction);
    }


    public WifiConnectFragment() {
        // Required empty public constructor
    }

    public static WifiConnectFragment newInstance(String accessPoint, String password, InteractionResultType resultType) {
        WifiConnectFragment fragment = new WifiConnectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ACCESS_POINT, accessPoint);
        args.putString(ARG_PASSWORD, password);
        args.putSerializable(ARG_RESULT_TYPE, resultType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAccessPointName = getArguments().getString(ARG_ACCESS_POINT);
            mPassword = getArguments().getString(ARG_PASSWORD);
            mResultType = (InteractionResultType) getArguments().getSerializable(ARG_RESULT_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wifi_connect, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        initAttach(context);
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        initAttach(activity);
    }

    private void initAttach(Context context) {
        if (context instanceof OnInteractionListener) {
            mListener = (OnInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mStatusTextView = (TextView) view.findViewById(R.id.tv_wifiConnectStatus);
        mStatusTextView.setText("Now connecting to "+ mAccessPointName);

        mWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mTimer.start();
        connectToAccessPoint(mAccessPointName, mPassword);
    }

    private void connectToAccessPoint(String ssid, String password) {
        ssid = String.format("\"%s\"", ssid);
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = ssid;
        if(password == null || "".equals(password)) {
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else {
            wifiConfiguration.preSharedKey = String.format("\"%s\"", password);
        }

        int netId = mWifiManager.addNetwork(wifiConfiguration);
        boolean enableSuccess = mWifiManager.enableNetwork(netId, true);
        if(enableSuccess) {
            Log.d(MainActivity.TAG, "Now enabling network "+ssid);
        } else {
            throw new RuntimeException("Could not connect to "+ssid);
        }

        IntentFilter stateChangedIntent = new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        //stateChangedIntent.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mReceiverForWifiStateChangedAction = new ReceiverForWifiStateChangedAction(ssid, mWifiManager, this);
        Context context = getActivity().getApplicationContext();
        context.registerReceiver(mReceiverForWifiStateChangedAction, stateChangedIntent);
    }

    public void sendResultToActivity() {
        mListener.onInteraction(mResultType, mConnected);
    }

    @Override
    public void handleWifiStateChangedAction(String accessPointName, boolean successful) {
        mTimer.cancel();
        mConnected = successful;
        if(mConnected) {
            mStatusTextView.setText("Successfully connected to "+accessPointName);
        } else {
            mStatusTextView.setText("Could not connect to "+accessPointName);
        }
        sendResultToActivity();
    }

}
