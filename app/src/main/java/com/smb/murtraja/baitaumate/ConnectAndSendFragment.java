package com.smb.murtraja.baitaumate;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ConnectAndSendFragment extends Fragment implements OnInteractionListener {

    /*
    This fragment connects to a device which is in Direct Mode
    and sends a command to it

    > INPUT:
        1. The Device AP to connect to
        2. The command to send

    > PROCESSING:
        1. with the help of nested fragments, need to register WifiConnectFragment inside this one
        2. proper error messages and call backs need to be implemented
        3. more result types need to be added

    > OUTPUT:
        1. whether the feat was successful or not
        2. if not, where is went wrong
        3. if it was, the reply string from the device

     */

    private static final String ARG_AP_NAME = "AP_NAME";
    private static final String ARG_COMMAND = "COMMAND";
    private static final String ARG_RESULT_TYPE = "RESULT_TYPE";

    private String mAPName;
    private String mCommand;

    private OnInteractionListener mListener;
    private InteractionResultType mResultType;

    private FragmentManager mFragmentManager;
    private Fragment mCurrentlyAttachedFragment;
    private TextView mConnectAndSendStatusTextView;

    public ConnectAndSendFragment() {
        // Required empty public constructor
    }

    public static ConnectAndSendFragment newInstance(String apName, String command, InteractionResultType resultType) {
        ConnectAndSendFragment fragment = new ConnectAndSendFragment();
        Bundle args = new Bundle();
        args.putString(ARG_AP_NAME, apName);
        args.putString(ARG_COMMAND, command);
        args.putSerializable(ARG_RESULT_TYPE, resultType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAPName = getArguments().getString(ARG_AP_NAME);
            mCommand = getArguments().getString(ARG_COMMAND);
            mResultType = (InteractionResultType)getArguments().getSerializable(ARG_RESULT_TYPE);
        }
        mFragmentManager = getChildFragmentManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_connect_and_send, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mConnectAndSendStatusTextView = (TextView) view.findViewById(R.id.tv_connect_and_send_status);
        WifiConnectFragment wifiConnectFragment = WifiConnectFragment.newInstance(mAPName, "", InteractionResultType.ACCESS_POINT_CONNECTED);
        setFragment(wifiConnectFragment);
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        if(mCurrentlyAttachedFragment != null) {
            fragmentTransaction.remove(mCurrentlyAttachedFragment);
        }
        fragmentTransaction.add(R.id.ll_connect_and_send, fragment);
        fragmentTransaction.commit();
        mCurrentlyAttachedFragment = fragment;
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
        Log.d(MainActivity.TAG, "inside on attach of fragment");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onInteraction(InteractionResultType resultType, Object result) {

        if(resultType == InteractionResultType.ACCESS_POINT_CONNECTED) {
            onAccessPointConnected((boolean) result);
        }

        else if(resultType == InteractionResultType.COMMAND_SENT) {
            onCommandSent((String) result);
        }
    }

    private void onAccessPointConnected(boolean successful) {
        if(successful) {
            // now send command to the device
            Log.d(MainActivity.TAG, "CSFrag: successfully connected to "+mAPName);
            String deviceIP = determineCurrentlyConnectedDeviceIP();
            CommandSender commandSender = new CommandSender(deviceIP, InteractionResultType.COMMAND_SENT, this);
            commandSender.send(mCommand);
            // now capture the send's callback

        } else {
            mConnectAndSendStatusTextView.setText("Unable to connect to AP");
        }
    }

    private void onCommandSent(final String reply) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mListener.onInteraction(mResultType, reply);
            }
        });
    }

    private String determineCurrentlyConnectedDeviceIP() {
        // TODO: use some DHCP service to figure out the gateway IP instead of hardcoding
        return "192.168.43.1";

    }
}
