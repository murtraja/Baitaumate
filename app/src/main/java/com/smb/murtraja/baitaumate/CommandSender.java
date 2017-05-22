package com.smb.murtraja.baitaumate;

import android.util.Log;

/**
 * Created by murtraja on 21/5/17.
 */

public class CommandSender implements OnInteractionListener {

    /*
    This class just starts the thread, and when the thread sends the command
    and gets the reply, this class just forwards that reply to the parent (listener)
     */

    String mDeviceIP;
    InteractionResultType mResultType;
    OnInteractionListener mListener;

    CommandSenderThread senderThread;

    public CommandSender(String deviceIP, InteractionResultType resultType, OnInteractionListener listener) {
        mDeviceIP = deviceIP;
        mListener = listener;
        mResultType = resultType;
    }

    public void send(String command) {
        Log.d(MainActivity.TAG, "CommandSender: now sending >> "+command);
        senderThread = new CommandSenderThread(mDeviceIP, command, InteractionResultType.COMMAND_SENT, this);
        senderThread.start();
    }

    @Override
    public void onInteraction(InteractionResultType resultType, Object result) {
        if(resultType == InteractionResultType.COMMAND_SENT) {
            onCommandSent((String) result);
        }
    }

    private void onCommandSent(String reply) {
        mListener.onInteraction(mResultType, reply);
    }
}
