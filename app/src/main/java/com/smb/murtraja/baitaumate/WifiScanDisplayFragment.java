package com.smb.murtraja.baitaumate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WifiScanDisplayFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WifiScanDisplayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WifiScanDisplayFragment extends Fragment implements IWifiScanDisplayHandler {

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
            > and a final button - configure (which sends ssid and password to all the selected APs)
            > when user clicks on configure, activity call back will be invoked with a list of ScanResults

        2. mCheckable is false
            > just a simple list view with simple_list_item will do here

        3. mScanDevices is true
            > show only APs which have device regex

        4. mScanDevices is false
            > show APs which are not blank and do not match the device regex

    > OUTPUT
        1. a call will be made to activity call back handler with the list (l) of *required* ScanResults
        2. if mCheckable = false, then size(l) = 1


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


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SCAN_DEVICES = "SCAN_DEVICES";
    private static final String ARG_CHECKABLE = "CHECKABLE";

    // TODO: Rename and change types of parameters
    private boolean mScanDevices;
    private boolean mCheckable;

    private OnFragmentInteractionListener mListener;

    private Button mScanAgainButton;
    private ListView mScanResultsListView;
    private TextView mStatusTextView;

    private WifiManager wifiManager;

    public WifiScanDisplayFragment() {
        // Required empty public constructor
    }



    public static WifiScanDisplayFragment newInstance(boolean scanDevices, boolean checkable) {
        WifiScanDisplayFragment fragment = new WifiScanDisplayFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_SCAN_DEVICES, scanDevices);
        args.putBoolean(ARG_CHECKABLE, checkable);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mScanDevices = getArguments().getBoolean(ARG_SCAN_DEVICES);
            mCheckable = getArguments().getBoolean(ARG_CHECKABLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wifi_scan_display, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mScanAgainButton = (Button) view.findViewById(R.id.btn_scanAgain);
        mScanAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWifiScan();
            }
        });
        mScanResultsListView = (ListView) view.findViewById(R.id.lv_scanResults);
        mStatusTextView = (TextView) view.findViewById(R.id.tv_wifiScanDisplayStatus);

        this.wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    private void startWifiScan() {
        this.mScanAgainButton.setEnabled(false);
        BroadcastReceiver wifiScanReceiver = new WifiScanReceiver(wifiManager, this);
        IntentFilter intentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getActivity().registerReceiver(wifiScanReceiver, intentFilter);
        wifiManager.startScan();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        startWifiScan();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onWifiScanResultsAvailable(List<ScanResult> results) {
        List<String> accessPoints = WifiResultsProcessor.getUniqueAPsFromScanResults(results, false);
        mStatusTextView.setText(String.format("Found %d Wifi networks", accessPoints.size()));

        ArrayAdapter<String> arrayAdapter = new WifiScanDisplayArrayAdapter(getActivity(), R.layout.checkbox_list_item, accessPoints);
        mScanResultsListView.setAdapter(arrayAdapter);
//        mScanResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//                String ssid = ((TextView)view).getText().toString();
//                Toast.makeText(getActivity(), ""+ssid, Toast.LENGTH_SHORT).show();
//            }
//        });

        mScanAgainButton.setEnabled(true);
    }

    @Override
    public void onWifiConnected() {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
