package com.smb.murtraja.baitaumate;

import com.smb.murtraja.baitaumate.OnInteractionListener.InteractionResultType;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class ListDevicesFragment extends ListFragment implements AdapterView.OnItemClickListener{

    /*
    currently this fragment just displays a list of Mac, and returns the clicked one

    This Fragment is responsible for listing all the devices
    in the wifi mode as of now and when clicked on a device,
    it will expand to show its configuration, scrollable

    INPUT
        > apart from the usual result stuff, nothing
        > the devices info will be stored in SharedPreferences

    PROCESS
        > display a list of devices
        > (TODO: future scope, on clicking it, expand it to show its configuration)
        > handle events properly

    OUTPUT
        > returns the Mac of the device clicked

     */


    private static final String TAG = "ListDevicesFrag";

    private static final String ARG_RESULT_TYPE = "RESULT_TYPE";
    private static final String ARG_DEVICE_LIST = "DEVICE_LIST";

    private OnInteractionListener mListener;
    private InteractionResultType mResultType;

    private ArrayList<String> mDeviceList;
    private String mClickedMac = null;

    public ListDevicesFragment() {
        // Required empty public constructor
    }

    public static ListDevicesFragment newInstance(ArrayList<String> deviceList, InteractionResultType resultType) {
        ListDevicesFragment fragment = new ListDevicesFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_RESULT_TYPE, resultType);
        args.putSerializable(ARG_DEVICE_LIST, deviceList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mResultType = (InteractionResultType) getArguments().getSerializable(ARG_RESULT_TYPE);
            mDeviceList = (ArrayList<String>) getArguments().getSerializable(ARG_DEVICE_LIST);
        }
    }

    /*
    using the default ListView layout for ListFragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_devices, container, false);
    }
    */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ArrayAdapter adapter = new ArrayAdapter((Context)mListener, android.R.layout.simple_list_item_1, mDeviceList);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);

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
        Log.d(TAG, "sending result to activity "+mClickedMac);
        mListener.onInteraction(mResultType, mClickedMac);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mClickedMac = (String) ((TextView)view).getText();
        Log.d(TAG, "clicked on "+mClickedMac);
        sendResultToActivity();
    }
}
