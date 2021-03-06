package com.smb.murtraja.baitaumate;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class WifiConnectFragment extends Fragment implements OnInteractionListener {

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
    private static final String TAG = "WCFrag";


    private static final String ARG_ACCESS_POINT = "ACCESS_POINT";
    private static final String ARG_PASSWORD = "PASSWORD";
    private static final String ARG_RESULT_TYPE = "RESULT_TYPE";

    private String mAccessPointName;
    private String mPassword;

    private InteractionResultType mResultType;
    private OnInteractionListener mListener;

    private TextView mStatusTextView;
    private Button mConnectButton;
    private Button mSkipButton;

    private boolean mConnected = false;
    private WifiManager mWifiManager;

    private CountDownTimer mTimer = new CountDownTimer(10000, 500) {
        @Override
        public void onTick(long millisUntilFinished) {
            updateStatusTextView();
        }

        @Override
        public void onFinish() {
            Log.d(TAG, "timer timed out!");
            unregisterReceiver();
            mConnected = false;
            connectionAttemptFinish();
        }
    };
    private WifiStateChangedActionReceiver mWifiStateChangedActionReceiver;

    private void unregisterReceiver() {

        /*
        this function is useful when timer times out
         */
        Context context = getActivity().getApplicationContext();
        context.unregisterReceiver(mWifiStateChangedActionReceiver);
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
            mAccessPointName = String.format("\"%s\"", mAccessPointName);
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
            Fragment parentFragment = getParentFragment();
            if(parentFragment == null)
                mListener = (OnInteractionListener) context;
            else
                mListener = (OnInteractionListener) parentFragment;
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
        mStatusTextView = (TextView) view.findViewById(R.id.tv_wifi_connect_status);

        mConnectButton = (Button) view.findViewById(R.id.btn_connect);
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToAccessPoint();
            }
        });
        mSkipButton = (Button) view.findViewById(R.id.btn_skip_connect);
        mSkipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResultToActivity();
            }
        });

        mWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connectToAccessPoint();
    }

    private void connectToAccessPoint() {
        updateButton("Connecting...", false);
        mStatusTextView.setText("Now connecting to "+ mAccessPointName);
        mTimer.start();

        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = mAccessPointName;
        if(mPassword == null || "".equals(mPassword)) {
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else {
            wifiConfiguration.preSharedKey = String.format("\"%s\"", mPassword);
        }

        int netId = mWifiManager.addNetwork(wifiConfiguration);
        boolean enableSuccess = mWifiManager.enableNetwork(netId, true);
        if(enableSuccess) {
            Log.d(TAG, "Now enabling network "+mAccessPointName);
        } else {
            throw new RuntimeException("Could not connect to "+mAccessPointName);
        }

        IntentFilter stateChangedIntent = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mWifiStateChangedActionReceiver = new WifiStateChangedActionReceiver(
                mAccessPointName, mWifiManager, InteractionResultType.WIFI_STATE_CHANGED_ACTION, this);
        Context context = getActivity().getApplicationContext();
        context.registerReceiver(mWifiStateChangedActionReceiver, stateChangedIntent);
    }

    private void updateButton(String text, boolean enabled) {
        mConnectButton.setText(text);
        mConnectButton.setEnabled(enabled);
    }

    void connectionAttemptFinish() {
        if(mConnected) {
            mStatusTextView.setText("Successfully connected to "+mAccessPointName);
            sendResultToActivity();
        } else {
            mStatusTextView.setText("Could not connect to "+mAccessPointName);
            updateButton("Try again", true);
        }
    }
    public void sendResultToActivity() {
        Log.d(TAG, "sending result to activity "+mConnected);
        mListener.onInteraction(mResultType, mConnected);
    }

    @Override
    public void onInteraction(InteractionResultType resultType, Object result) {
        if(resultType == InteractionResultType.WIFI_STATE_CHANGED_ACTION) {
            onWifiStateChangedAction((boolean) result);
        }
    }

    public void onWifiStateChangedAction(boolean successful) {
        mTimer.cancel();
        mConnected = successful;
        connectionAttemptFinish();
    }
    void updateStatusTextView() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatusTextView.setText(mStatusTextView.getText()+".");
            }
        });
    }
}
