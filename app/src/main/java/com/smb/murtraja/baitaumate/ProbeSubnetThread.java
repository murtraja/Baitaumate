package com.smb.murtraja.baitaumate;

import android.util.Log;

import com.smb.murtraja.baitaumate.OnInteractionListener;
import com.smb.murtraja.baitaumate.ProbeHostThread;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by murtraja on 10/6/17.
 */

public class ProbeSubnetThread extends Thread implements OnInteractionListener{

    /*
    this thread is the master thread responsible for handling
    the worker threads
     */

    private static String TAG = "ProbeSubnetThread";
    String mSubnet;
    ArrayList<Thread> mProbeHostThread = new ArrayList<>();
    ArrayList<String> mDeviceHardwareAddressList;

    OnInteractionListener mListener;

    public ProbeSubnetThread(String subnet, ArrayList<String> deviceHardwareAddressList, OnInteractionListener listener) {
        mSubnet = subnet;
        mDeviceHardwareAddressList = deviceHardwareAddressList;
        mListener = listener;
    }

    @Override
    public void run() {
        spawnHostThreads();
        waitForAllThreadsToJoin();
        sendResultBack();
    }

    private void sendResultBack() {
        //just notify the listener that the probing is finished
        mListener.onInteraction(InteractionResultType.SUBNET_PROBED, "Probing finished");
    }

    public void spawnHostThreads() {
        for (int i=1;i<255;i++) {
            Thread thread = new ProbeHostThread(mSubnet + "." + i, InteractionResultType.HOST_PROBED, this);
            thread.start();
            mProbeHostThread.add(thread);
        }
    }

    @Override
    public void onInteraction(InteractionResultType resultType, Object result) {
        if(resultType == InteractionResultType.HOST_PROBED) {
            onHostProbed((String[])result);
        }
    }

    public void waitForAllThreadsToJoin() {
        for (Thread t : mProbeHostThread) {
            try {
                t.join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private boolean addressBelongsToDevice(String address) {
        return mDeviceHardwareAddressList.contains(address);
    }

    private void onHostProbed(String[] addressPair) {
        // now check if addressPair[1] belongs to any device
        if(addressBelongsToDevice(addressPair[1])) {
            // notify the listener regarding this
            sendProbedResultBack(addressPair);
        } else {
            Log.d(TAG, "discarding "+addressPair[0]+" -> "+addressPair[1]);
        }
    }

    private void sendProbedResultBack(String[] addressPair) {
        mListener.onInteraction(InteractionResultType.HOST_PROBED, addressPair);
    }

    private void sendDeviceMappingBack(String[] addressPair) {
        mListener.onInteraction(InteractionResultType.HOST_PROBED, addressPair);
    }
}
