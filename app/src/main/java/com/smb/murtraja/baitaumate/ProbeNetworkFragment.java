package com.smb.murtraja.baitaumate;

import android.app.Activity;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;


public class ProbeNetworkFragment extends Fragment implements OnInteractionListener{

    /*

    This fragment probes the network and finds the connected devices
    by comparing their MAC addresses

    INPUT
        > The list of MAC addresses of the devices whose IP needs to be found

    PROCESS
        > it determines the subnet
        > since it is a network operation, it spawns thread to do the task
        > as soon as any device is found UI is updated without waiting for the whole ping to finish
        > since i will spawn a lot of threads, need a master thread to coordinate the slaves (join operation takes time, performed on master)

    OUTPUT
        > IP:MAC mapping
        > updating of UI accordingly

     */

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TAG = "ProbeNetworkFragment";
    private static final String ARG_HARDWARE_ADDRESS_LIST = "HARDWARE_ADDRESS_LIST";
    private static final String ARG_RESULT_TYPE = "RESULT_TYPE";

    private ArrayList<String> mHardwareAddressList;
    private HashMap<String, String> mMapping = new HashMap<>();
    private ProbeSubnetThread mProbeSubnetThread;

    private OnInteractionListener mListener;
    private InteractionResultType mResultType;

    private TextView mStatusTextView;
    private Button mProbeNetworkButton;
    private Button mProbeNetworkDoneButton;

    private Activity mParentActivity;

    public ProbeNetworkFragment() {
        // Required empty public constructor
    }

    public static ProbeNetworkFragment newInstance(ArrayList<String> hardwareAddressList, InteractionResultType resultType) {
        ProbeNetworkFragment fragment = new ProbeNetworkFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_HARDWARE_ADDRESS_LIST, hardwareAddressList);
        args.putSerializable(ARG_RESULT_TYPE, resultType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mHardwareAddressList = (ArrayList<String>) getArguments().getSerializable(ARG_HARDWARE_ADDRESS_LIST);
            mResultType = (InteractionResultType) getArguments().getSerializable(ARG_RESULT_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_probe_network, container, false);
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
            if(parentFragment == null) {
                mListener = (OnInteractionListener) context;
                mParentActivity = getActivity();
            }
            else {
                mListener = (OnInteractionListener) parentFragment;
                mParentActivity = parentFragment.getActivity();
            }
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
        mStatusTextView = (TextView) view.findViewById(R.id.tv_probe_status);
        mProbeNetworkButton = (Button) view.findViewById(R.id.btn_probe_network);
        mProbeNetworkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProbeSubnetThread();
            }
        });
        mProbeNetworkDoneButton = (Button) view.findViewById(R.id.btn_probe_network_done);
        mProbeNetworkDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResultToActivity();
            }
        });
        startProbeSubnetThread();
    }

    void updateButton(final String updateText, final boolean enabled) {
        mParentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProbeNetworkButton.setEnabled(enabled);
                mProbeNetworkButton.setText(updateText);
            }
        });
    }

    private void startProbeSubnetThread() {
        updateButton("Probing...", false);
        updateStatus("Probing started", true);
        String subnet = determineCurrentSubnet();
        Log.d(TAG, "current subnet: "+subnet);
        mProbeSubnetThread = new ProbeSubnetThread(subnet, mHardwareAddressList, this);
        mProbeSubnetThread.start();
    }

    private String determineCurrentSubnet() {
        WifiManager wifiManager = (WifiManager) mParentActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo info = wifiManager.getDhcpInfo();
        String gateway = intToIp(info.gateway);
        return gateway.substring(0, gateway.lastIndexOf('.'));
    }

    public String intToIp(int address) {
        //https://stackoverflow.com/questions/5387036/programmatically-getting-the-gateway-and-subnet-mask-details
        return  ((address & 0xFF) + "." +
                ((address >>>= 8) & 0xFF) + "." +
                ((address >>>= 8) & 0xFF) + "." +
                ((address >>>= 8) & 0xFF));
    }

    void updateStatus(final String updateText, final boolean clearAll) {
        mParentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(clearAll)    mStatusTextView.setText("");
                mStatusTextView.append("\n"+updateText);
            }
        });
    }

    public void sendResultToActivity() {
        mParentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mListener.onInteraction(mResultType, mMapping);
            }
        });
    }

    @Override
    public void onInteraction(InteractionResultType resultType, Object result) {
        if(resultType == InteractionResultType.HOST_PROBED) {
            onHostProbed((String[])result);
        }

        else if(resultType == InteractionResultType.SUBNET_PROBED) {
            onSubnetProbed((String)result);
        }
    }

    private void onSubnetProbed(String result) {
        // time to notify the main activity
        Log.d(TAG, "ProbeSubnetThread returned: "+result);
        updateStatus("Finished probing the network", false);
        updateButton("Probe again", true);
    }

    private void onHostProbed(String[] addressPair) {
        updateStatus("Found "+addressPair[0]+":"+addressPair[1], false);
        mMapping.put(addressPair[1], addressPair[0]);
    }
}
