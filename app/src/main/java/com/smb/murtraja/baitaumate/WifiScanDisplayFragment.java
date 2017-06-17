package com.smb.murtraja.baitaumate;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.smb.murtraja.baitaumate.OnInteractionListener.InteractionResultType;

public class WifiScanDisplayFragment extends Fragment implements OnInteractionListener {

    /*
    As the name suggests, I want this fragment to not only scan the
    available WiFi networks, but also display them such that the results
    are clickable, and actions can be performed accordingly

    > INPUT
        1. scan and display devices or routers (mScanDevices = true or false)
        2. should the results have a checkbox besides them (mCheckable = true or false)

    > PROCESS
        1. mCheckable is true
            > show a checkbox besides every AP
            > also need to show two buttons - select all and select none
            > and a final button - configure (which sends mAccessPointName and password to all the selected APs)
            > when user clicks on configure, activity call back will be invoked with a list of ScanResults

        2. mCheckable is false
            > just a simple list view with simple_list_item will do here

        3. mScanDevices is true
            > show only APs which have device regex

        4. mScanDevices is false
            > show APs which are not blank and do not match the device regex

    > OUTPUT
        1. a call will be made to activity call back mListener with the list (l) of *required* ScanResults
        2. if mCheckable = false, then size(l) = 1

    TODO: Make the buttons visible or disabled in case when there are no results and mCheckable is true

    ***OLD COMMENTS BELOW, REMOVED CLICKABLE BEHAVIOUR, NOW HANDLED BY MAIN ACTIVITY***
    > Scan the available WiFi networks
    > Display them
        > if searching for devices, then those APs who follow a pattern should be displayed
            > in this case, the results can either be clickable or checkable
        > if searching for routers, then the others should be displayed
            > in this case the results can only be clickable
    > I want this to be reusable
        > essentially boils down to parameters that need to be set for different scenarios
            > boolean mCheckable        if true, then display a check box beside each entry (instead of radio?)
            > OnClickListener           what action to perform when clicked (one fragment should not directly communicate with the other)
            > boolean displayDevices    if true, then devices are displayed, else, routers
    > How will this fragment communicate the results back to main activity
     */

    private static final String TAG = "WSDFrag";

    private static final String ARG_SCAN_DEVICES = "SCAN_DEVICES";
    private static final String ARG_CHECKABLE = "CHECKABLE";
    private static final String ARG_RESULT_TYPE = "RESULT_TYPE";


    private boolean mScanDevices;
    private boolean mCheckable;

    private OnInteractionListener mListener;
    private InteractionResultType mResultType;

    private Button mScanAgainButton;
    private ListView mScanResultsListView;
    private TextView mStatusTextView;
    private Button mSelectAllButton;
    private Button mSelectNoneButton;
    private Button mDoneButton;

    private WifiManager wifiManager;
    private List<String> mAccessPointsSelected = new ArrayList<>(); //this holds the final list of APs which the user has selected

    public WifiScanDisplayFragment() {
        // Required empty public constructor
    }



    public static WifiScanDisplayFragment newInstance(boolean scanDevices, boolean checkable, InteractionResultType resultType) {
        WifiScanDisplayFragment fragment = new WifiScanDisplayFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_SCAN_DEVICES, scanDevices);
        args.putBoolean(ARG_CHECKABLE, checkable);
        args.putSerializable(ARG_RESULT_TYPE, resultType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mScanDevices = getArguments().getBoolean(ARG_SCAN_DEVICES);
            mCheckable = getArguments().getBoolean(ARG_CHECKABLE);
            mResultType = (InteractionResultType)getArguments().getSerializable(ARG_RESULT_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(mCheckable) {
            return inflater.inflate(R.layout.fragment_wifi_scan_display_checkable, container, false);
        }
        return inflater.inflate(R.layout.fragment_wifi_scan_display, container, false);

    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mScanAgainButton = (Button) view.findViewById(R.id.btn_scan_again);
        mScanAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWifiScan();
            }
        });
        mScanResultsListView = (ListView) view.findViewById(R.id.lv_scan_results);
        mStatusTextView = (TextView) view.findViewById(R.id.tv_wifi_scan_display_status);

        this.wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if(mCheckable) {
            mSelectAllButton = (Button) view.findViewById(R.id.btn_select_all);
            mSelectAllButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickSelectAllButton();
                }
            });

            mSelectNoneButton = (Button) view.findViewById(R.id.btn_select_none);
            mSelectNoneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickSelectNoneButton();
                }
            });

            mDoneButton = (Button) view.findViewById(R.id.btn_done);
            mDoneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickDoneButton();
                }
            });

        }

        startWifiScan();
    }

    @Override
    public void onAttach(Context context) {
        //http://stackoverflow.com/questions/32604552/onattach-not-called-in-fragment
        super.onAttach(context);
        onAttachInit(context);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        onAttachInit(activity);
    }

    private void onAttachInit(Context context) {
        if (context instanceof OnInteractionListener) {
            mListener = (OnInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnInteractionListener");
        }
        Log.d(TAG, "inside on attach of fragment");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void startWifiScan() {
        mScanAgainButton.setEnabled(false);
        BroadcastReceiver wifiScanReceiver = new WifiScanReceiver(wifiManager, InteractionResultType.WIFI_SCAN_RESULTS_AVAILABLE, this);
        IntentFilter intentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getActivity().registerReceiver(wifiScanReceiver, intentFilter);
        wifiManager.startScan();

    }

    @Override
    public void onInteraction(InteractionResultType resultType, Object result) {
        if(resultType == InteractionResultType.WIFI_SCAN_RESULTS_AVAILABLE) {
            onWifiScanResultsAvailable((List<ScanResult>)result);
        }
    }

    public void onWifiScanResultsAvailable(List<ScanResult> results) {
        /*
        Now we have got the ScanResults, need to display them.
        if mCheckable = true
            need an Adapter which correctly maps the results to the view
            need an OnItemClickListener which checks the boxes, and keeps track of what items are checked, etc.
            need an OnClickListener for done button, so that when user clicks on it, the items gathered in the previous step is returned to the activity
        if mCheckable = false
            need an Adapter, a simple one, just maps text to string
            need an OnItemClickListener, whenever clicked, that value is sent to the parent activity
         */
        WifiResultsProcessor wifiResultsProcessor = new WifiResultsProcessor(results);
        List<String> accessPoints = wifiResultsProcessor.computeUniqueAPsFromScanResults(mScanDevices);
        String statusText = "Found "+accessPoints.size()+" ";
        if(mScanDevices)    statusText += "devices";
        else                statusText += "Wifi networks";
        mStatusTextView.setText(statusText);

        if(mCheckable) {
            updateUIWithCheckableAccessPoints(accessPoints);
        } else {
            updateUIWithAccessPoints(accessPoints);
        }
        mScanAgainButton.setEnabled(true);
    }

    private void updateUIWithAccessPoints(List<String> accessPoints) {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, accessPoints);
        mScanResultsListView.setAdapter(arrayAdapter);
        mScanResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String ssid = ((TextView)view).getText().toString();
                mAccessPointsSelected.clear();
                mAccessPointsSelected.add(ssid);
                sendResultToActivity();

            }
        });
    }

    private void updateUIWithCheckableAccessPoints(List<String> accessPoints) {

        // TODO : check for null condition

        ArrayAdapter<String> arrayAdapter = new WifiScanDisplayCheckableArrayAdapter(getActivity(), R.layout.checkbox_list_item, R.id.cb_access_point, accessPoints);
        mScanResultsListView.setAdapter(arrayAdapter);
        mScanResultsListView.setOnItemClickListener(new OnCheckableItemClickListener(mAccessPointsSelected, getActivity()));

        // TODO: also set the buttons
    }

    private void onClickSelectAllButton() {
        setAllCheckBoxesToCheckedValue(true);
        mAccessPointsSelected.clear();
        mAccessPointsSelected.addAll(((WifiScanDisplayCheckableArrayAdapter)mScanResultsListView.getAdapter()).getAccessPoints());
    }

    private void onClickSelectNoneButton() {
        setAllCheckBoxesToCheckedValue(false);
        mAccessPointsSelected.clear();
    }

    private void onClickDoneButton() {
        sendResultToActivity();
    }

    private void setAllCheckBoxesToCheckedValue(boolean value) {
        for(int i=0; i<mScanResultsListView.getAdapter().getCount(); i++) {
            LinearLayout ll = (LinearLayout) mScanResultsListView.getChildAt(i);
            CheckBox cb = (CheckBox) ll.getChildAt(0);
            cb.setChecked(value);
        }
    }

    private void sendResultToActivity() {
        mListener.onInteraction(mResultType, mAccessPointsSelected);
    }
}
