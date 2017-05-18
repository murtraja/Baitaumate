package com.smb.murtraja.baitaumate;

import android.graphics.Color;
import android.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by murtraja on 2/5/17.
 */

public class CommandSender implements Runnable {

    /*
    INPUT
        > DEVICE_IP
        > DEVICE_PORT
        > command
        > listener

    PROCESS
        > connects with the device over socket and sends it the command
        > if the device replies, it returns the reply via the listener

    OUTPUT
        > the reply received as a string

    TODO: CommandSender should automatically spawn a new thread and the calling thread must be notified via callback
    The usage should be as simple as CommandSender.send(command, device)
    and the callback onCommandSent(String reply, device)
     */

    IOnCommandSentListener onCommandSentListener;
    String DEVICE_IP = "192.168.4.1";
    int DEVICE_PORT = 1155;
    Socket socket;
    PrintWriter printWriter;
    Scanner scanner;
    int mColourToSet;
    ConfigureLightActivity mConfigureLightActivity;
    public CommandSender(int colourToSet, IOnCommandSentListener onCommandSentListener) {
        this.onCommandSentListener = onCommandSentListener;
        this.mColourToSet = colourToSet;
        this.mConfigureLightActivity = (ConfigureLightActivity) onCommandSentListener;
    }
    private void init() {

        try {
            this.socket = new Socket(DEVICE_IP, DEVICE_PORT);
            Log.d(MainActivity.TAG, "changeColour: "+String.format("Connected to %s:%d", DEVICE_IP, DEVICE_PORT));
            this.scanner = new Scanner(new InputStreamReader(this.socket.getInputStream()));
            this.printWriter = new PrintWriter(this.socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private String getChangeColourCommand(int r, int g, int b) {
        StringBuilder sb = new StringBuilder();
        sb.append("#%");
        sb.append(String.format("%03d,%03d,%03d", g,r,b));
        sb.append("");
        return sb.toString();
    }

    public String changeColour(int r, int g, int b) {
        try {

            String changeColourCommand = getChangeColourCommand(r,g,b);
            Log.d(MainActivity.TAG, "changeColour: "+"$ "+changeColourCommand);
            printWriter.write(changeColourCommand);
            printWriter.flush();
            String receivedFromDevice = new String();
            int i =0;
            while(i++<3 && scanner.hasNext()) {
                receivedFromDevice += scanner.next() + "\n";
            }
            Log.d(MainActivity.TAG, "changeColour: "+receivedFromDevice);
            return receivedFromDevice;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    @Override
    public void run() {
        this.init();
        int r = Color.red(this.mColourToSet);
        int g = Color.green(this.mColourToSet);
        int b = Color.blue(this.mColourToSet);
        final String reply = this.changeColour(r,g,b);
        this.teardown();

        mConfigureLightActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConfigureLightActivity.onCommandSentHandler(reply);
            }
        });
    }
    private void teardown() {
        try {
            scanner.close();
            printWriter.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
