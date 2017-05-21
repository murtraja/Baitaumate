package com.smb.murtraja.baitaumate;

import com.smb.murtraja.baitaumate.OnInteractionListener.InteractionResultType;

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
    String mDeviceIP, mCommand;
    OnInteractionListener mListener;
    InteractionResultType mResultType;

    int DEVICE_PORT = 1155;
    Socket mSocket;
    PrintWriter mPrintWriter;
    Scanner mScanner;

    public CommandSenderThread(String deviceIP, String command, InteractionResultType resultType, OnInteractionListener listener) {
        mDeviceIP = deviceIP;
        mCommand = command;
        mListener = listener;
        mResultType = resultType;
    }

    private void init() {

        try {
            mSocket = new Socket(mDeviceIP, DEVICE_PORT);
            mScanner = new Scanner(new InputStreamReader(this.mSocket.getInputStream()));
            mPrintWriter = new PrintWriter(this.mSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        init();
        sendCommand();
        String reply = receiveReply();
        teardown();
        mListener.onInteraction(mResultType, reply);
    }

    private void sendCommand() {
        mPrintWriter.write(mCommand);
        mPrintWriter.flush();
    }

    private String receiveReply() {
        String reply = "";
        while(mScanner.hasNext()) {
            String nextReplyLine = mScanner.next();
            if("EOF".equals(reply)) {
                break;
            }
            reply += nextReplyLine;
        }
        return reply;
    }

    private void teardown() {
        try {
            mScanner.close();
            mPrintWriter.close();
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
