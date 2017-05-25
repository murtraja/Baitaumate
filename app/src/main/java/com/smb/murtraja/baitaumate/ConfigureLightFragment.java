package com.smb.murtraja.baitaumate;

import com.github.danielnilsson9.colorpickerview.view.ColorPickerView;
import com.smb.murtraja.baitaumate.OnInteractionListener.InteractionResultType;


import android.app.Activity;
import android.content.Context;
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

    private static final String ARG_RESULT_TYPE = "RESULT_TYPE";

    private InteractionResultType mResultType;
    private OnInteractionListener mListener;

    Button mSetColourButton;
    Button mConfigDoneButton;
    ColorPickerView mColorPicker;
    TextView mCommandStatusTextView;

    public ConfigureLightFragment() {
        // Required empty public constructor
    }


    public static ConfigureLightFragment newInstance(InteractionResultType resultType) {
        ConfigureLightFragment fragment = new ConfigureLightFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_RESULT_TYPE, resultType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mResultType = (InteractionResultType) getArguments().getSerializable(ARG_RESULT_TYPE);
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
    }

    private void onClickConfigDoneButton() {
        sendResultBackToActivity();
    }

    private void onClickSetColourButton() {
        mCommandStatusTextView.setText("Now sending command...");
        int color = mColorPicker.getColor();
        String deviceIP = determineCurrentlyConnectedDeviceIP();
        CommandSender commandSender = new CommandSender(deviceIP, InteractionResultType.COMMAND_SENT, this);
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
            mListener = (OnInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnInteractionListener");
        }
        Log.d(MainActivity.TAG, "inside on attach of fragment");
    }

    @Override
    public void onInteraction(InteractionResultType resultType, Object result) {
        if(resultType == InteractionResultType.COMMAND_SENT) {
            onCommandSent((String) result);
        }

    }

    private void onCommandSent(String reply) {
        mCommandStatusTextView.setText(reply);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    private void sendResultBackToActivity() {
        mListener.onInteraction(mResultType, null);
    }

    private String determineCurrentlyConnectedDeviceIP() {
        // TODO: use some DHCP service to figure out the gateway IP instead of hardcoding
        return "192.168.43.1";

    }
}
