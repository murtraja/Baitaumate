package com.smb.murtraja.baitaumate;

import com.github.danielnilsson9.colorpickerview.view.ColorPickerView;
import com.smb.murtraja.baitaumate.OnInteractionListener.InteractionResultType;


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


public class ConfigureLightFragment extends Fragment implements OnInteractionListener{

    /*
    This Fragment should actually be called setLightColourFragment
    it was created to just make direct mode work with the fragment approach

    > INPUT:

    > PROCESSING:
        1. show a colour picker
        2. send colour change command to the device
        3. display the received output
        4. also show a "done" button, to go back

    > OUTPUT:
        1. if it was succcessfully configured or not
        2. errors if any

     */
    private static final String TAG = "ConfLightFrag";

    private static final String ARG_DEVICE_IP_ADDRESS = "DEVICE_IP_ADDRESS";
    private static final String ARG_RESULT_TYPE = "RESULT_TYPE";

    private String mDeviceIpAddress = null;

    private InteractionResultType mResultType;
    private OnInteractionListener mListener;

    Button mSetColourButton;
    Button mConfigDoneButton;
    ColorPickerView mColorPicker;
    TextView mCommandStatusTextView;
    private Activity mParentActivity;

    public ConfigureLightFragment() {
        // Required empty public constructor
    }


    public static ConfigureLightFragment newInstance(String deviceIpAddress, InteractionResultType resultType) {
        ConfigureLightFragment fragment = new ConfigureLightFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_RESULT_TYPE, resultType);
        args.putString(ARG_DEVICE_IP_ADDRESS, deviceIpAddress);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mResultType = (InteractionResultType) getArguments().getSerializable(ARG_RESULT_TYPE);
            mDeviceIpAddress = getArguments().getString(ARG_DEVICE_IP_ADDRESS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_configure_light, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mColorPicker = (ColorPickerView) view.findViewById(R.id.cpv_pickColour);
        mCommandStatusTextView = (TextView) view.findViewById(R.id.tv_commandStatus);

        this.mSetColourButton = (Button) view.findViewById(R.id.btn_setColour);
        this.mSetColourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSetColourButton();
            }
        });

        mConfigDoneButton = (Button) view.findViewById(R.id.btn_configDone);
        mConfigDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickConfigDoneButton();
            }
        });

        if(mDeviceIpAddress == null)    mDeviceIpAddress = determineCurrentlyConnectedDeviceIP();
    }

    private void onClickConfigDoneButton() {
        sendResultBackToActivity();
    }

    private void onClickSetColourButton() {
        mCommandStatusTextView.setText("Now changing colour...");
        int color = mColorPicker.getColor();
        CommandSender commandSender = new CommandSender(mDeviceIpAddress, InteractionResultType.COMMAND_SENT, this);
        String command = CommandGenerator.generateSetColourCommand(color);
        commandSender.send(command);
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
    public void onInteraction(InteractionResultType resultType, Object result) {
        if(resultType == InteractionResultType.COMMAND_SENT) {
            onCommandSent((String) result);
        }

    }

    private void onCommandSent(final String reply) {
        mParentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCommandStatusTextView.setText(reply);
            }
        });

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    private void sendResultBackToActivity() {
        mListener.onInteraction(mResultType, null);
    }

    //TODO: all these network related functions need to be in a separate static class

    private String determineCurrentlyConnectedDeviceIP() {
        WifiManager wifiManager = (WifiManager) mParentActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo info = wifiManager.getDhcpInfo();
        String gateway = intToIp(info.gateway);
        return gateway;
    }

    public String intToIp(int address) {
        //https://stackoverflow.com/questions/5387036/programmatically-getting-the-gateway-and-subnet-mask-details
        return  ((address & 0xFF) + "." +
                ((address >>>= 8) & 0xFF) + "." +
                ((address >>>= 8) & 0xFF) + "." +
                ((address >>>= 8) & 0xFF));
    }
}
