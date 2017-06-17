package com.smb.murtraja.baitaumate;

import com.smb.murtraja.baitaumate.OnInteractionListener.InteractionResultType;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class ListDevicesFragment extends Fragment {

    /*
    This Fragment is responsible for listing all the devices
    in the wifi mode as of now and when clicked on a device,
    it will expand to show its configuration, scrollable

    INPUT
        > apart from the usual result stuff, nothing
        > the devices info will be stored in SharedPreferences

    PROCESS
        > display a list of devices
        > on clicking it, expand it to show its configuration
        > handle events properly

    OUTPUT
        > nothing

     */


    private static final String TAG = "LisDevicesFrag";

    private static final String ARG_RESULT_TYPE = "RESULT_TYPE";

    private OnInteractionListener mListener;
    private InteractionResultType mResultType;

    public ListDevicesFragment() {
        // Required empty public constructor
    }

    public static ListDevicesFragment newInstance(InteractionResultType resultType) {
        ListDevicesFragment fragment = new ListDevicesFragment();
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
        return inflater.inflate(R.layout.fragment_list_devices, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

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

    public void sendResultToActivity() {
        Log.d(TAG, "sending result to activity "+"");
        mListener.onInteraction(mResultType, "");
    }
}
