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
     */


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private Button mScanAgainButton;
    private ListView mScanResultsListView;
    private TextView mStatusTextView;

    private WifiManager wifiManager;

    public WifiScanDisplayFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WifiScanDisplayFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WifiScanDisplayFragment newInstance(String param1, String param2) {
        WifiScanDisplayFragment fragment = new WifiScanDisplayFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
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
