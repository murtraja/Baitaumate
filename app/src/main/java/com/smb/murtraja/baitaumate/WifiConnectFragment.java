package com.smb.murtraja.baitaumate;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smb.murtraja.baitaumate.OnFragmentInteractionListener.FragmentResultType;

public class WifiConnectFragment extends Fragment implements IWifiStateChangedActionHandler{

    /*
    this fragment simply connect to an Access Point
    and displays the status accordingly in its view

    INPUT
        > the access point name to connect to
        > the password
        > listener (which is implicit)
        > result type

    PROCESS
        > register a receiver and listen for state changes.
        > error handling

    OUTPUT
        > whether connected or not
     */

    private static final String ARG_ACCESS_POINT = "ACCESS_POINT";
    private static final String ARG_PASSWORD = "PASSWORD";
    private static final String ARG_RESULT_TYPE = "RESULT_TYPE";

    private String mAccessPoint;
    private String mPassword;

    private FragmentResultType mResultType;
    private OnFragmentInteractionListener mListener;

    private TextView mStatusTextView;

    private boolean mConnected = false;
    private WifiManager mWifiManager;

    public WifiConnectFragment() {
        // Required empty public constructor
    }

    public static WifiConnectFragment newInstance(String accessPoint, String password, FragmentResultType resultType) {
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
            mAccessPoint = getArguments().getString(ARG_ACCESS_POINT);
            mPassword = getArguments().getString(ARG_PASSWORD);
            mResultType = (FragmentResultType) getArguments().getSerializable(ARG_RESULT_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wifi_connect, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mStatusTextView = (TextView) view.findViewById(R.id.tv_wifiScanDisplayStatus);
        mStatusTextView.setText("Now connecting to "+mAccessPoint);

        mWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        

    }

    private void connectToAccessPoint(String ssid) {
        ssid = String.format("\"%s\"", ssid);
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = ssid;
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        int netId = mWifiManager.addNetwork(wifiConfiguration);
        boolean enableSuccess = mWifiManager.enableNetwork(netId, true);
        if(enableSuccess) {
            Log.d(MainActivity.TAG, "Now enabling network "+ssid);
        } else {
            throw new RuntimeException("Could not connect to "+ssid);
        }

        IntentFilter stateChangedIntent = new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        //stateChangedIntent.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        ReceiverForWifiStateChangedAction receiverForWifiStateChangedAction =
                new ReceiverForWifiStateChangedAction(ssid, mWifiManager, this);
        Context context = getActivity().getApplicationContext();
        context.registerReceiver(receiverForWifiStateChangedAction, stateChangedIntent);


    }

    public void sendResultToActivity() {
        mListener.onFragmentInteraction(mResultType, mConnected);
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
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void handleWifiStateChangedAction() {

    }
}
