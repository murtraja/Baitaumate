package com.smb.murtraja.baitaumate;

import android.util.Log;

import com.smb.murtraja.baitaumate.OnInteractionListener.InteractionResultType;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by murtraja on 21/5/17.
 */

public class CommandSenderThread extends Thread {

    /*
    INPUT
        > deviceIP
        > command

    PROCESS
        > connects with the device over mSocket and sends it the command
        > if the device replies, it returns the reply via the listener

    OUTPUT
        > the reply received as a string
    */
    private static final String TAG = "CSThread";


    String mDeviceIP, mCommand;
    OnInteractionListener mListener;
    InteractionResultType mResultType;

    int DEVICE_PORT = 1155;
    Socket mSocket;
    PrintWriter mPrintWriter;
    BufferedReader mReader;

    public CommandSenderThread(String deviceIP, String command, InteractionResultType resultType, OnInteractionListener listener) {
        mDeviceIP = deviceIP;
        mCommand = command;
        mListener = listener;
        mResultType = resultType;
    }

    private void init() {

        try {
            //TODO: the line below throws an exception that unable to connect, although wifi was connected, maybe sleep the thread and try again?
            mSocket = new Socket(mDeviceIP, DEVICE_PORT);
            mReader = new BufferedReader(new InputStreamReader(this.mSocket.getInputStream()));
            mPrintWriter = new PrintWriter(new DataOutputStream(this.mSocket.getOutputStream()));
            Log.d(TAG, "init successful");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        Log.d(TAG, "Now running the thread...");
        init();
        sendCommand();
        String reply = receiveReply();
        teardown();
        mListener.onInteraction(mResultType, reply);
    }

    private void sendCommand() {
        Log.d(TAG, "printWriter: "+mCommand);
        mPrintWriter.write(mCommand);
        mPrintWriter.flush();
    }

    private String receiveReply() {
        String reply = "";
        Log.d(TAG, "receiveReply: now starting loop");
        String nextReplyLine = "";
        try {
            while((nextReplyLine = mReader.readLine())!=null) {
                Log.d(TAG, "receiveReply: nextReplyLine: "+nextReplyLine);
                if("EOF".equals(nextReplyLine)) {
                    break;
                }
                reply += "\n" + nextReplyLine;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "received reply: "+reply);
        return reply;
    }

    private void teardown() {
        try {
            mReader.close();
            mPrintWriter.close();
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
