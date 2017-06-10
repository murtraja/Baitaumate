package com.smb.murtraja.baitaumate;
import android.util.Log;

import com.smb.murtraja.baitaumate.OnInteractionListener;
import com.smb.murtraja.baitaumate.OnInteractionListener.InteractionResultType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by murtraja on 10/6/17.
 */

public class ProbeHostThread extends Thread {

    static String TAG = "ProbeHostThread";
    private final static String MAC_RE = "^%s\\s+0x1\\s+0x2\\s+([:0-9a-fA-F]+)\\s+\\*\\s+\\w+$";
    private final static int BUF = 8 * 1024;
    int TIMEOUT = 2000;

    String mHostAddress;
    String mHardwareAddress;

    OnInteractionListener mListener;
    InteractionResultType mResultType;

    public ProbeHostThread(String hostAddress, InteractionResultType resultType, OnInteractionListener listener) {
        mHostAddress = hostAddress;
        mListener = listener;
        mResultType = resultType;
    }

    public void run() {
        long t1 = System.currentTimeMillis() % 1000;
        try {
            if (InetAddress.getByName(mHostAddress).isReachable(TIMEOUT)){
                String arp = getHardwareAddress(mHostAddress);
                if( arp == null || "".equals(arp))      mHardwareAddress = null;
                else                                    mHardwareAddress = arp;

                long t2 = System.currentTimeMillis() % 1000;
                System.out.println(mHostAddress + " is reachable ("+(t2-t1)+"ms)");
                sendResultBack();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String getHardwareAddress(String ip) {
        //https://github.com/rorist/android-network-discovery/blob/master/src/info/lamatricexiste/network/Network/HardwareAddress.java
        String hw = "";
        BufferedReader bufferedReader = null;
        try {
            if (ip != null) {
                String ptrn = String.format(MAC_RE, ip.replace(".", "\\."));
                Pattern pattern = Pattern.compile(ptrn);
                bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"), BUF);
                String line;
                Matcher matcher;
                while ((line = bufferedReader.readLine()) != null) {
                    matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                        hw = matcher.group(1);
                        break;
                    }
                }
            } else {
                Log.e(TAG, "ip is null");
            }
        } catch (IOException e) {
            Log.e(TAG, "Can't open/read file ARP: " + e.getMessage());
            return hw;
        } finally {
            try {
                if(bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return hw;
    }

    public void sendResultBack() {

        /*
        so i was wondering what if mHardwareAddress is null?
        why should i send a null value back?
        i should just skip sending it right?
         */
        if( mHardwareAddress != null) {
            mListener.onInteraction(mResultType, new String[] {mHostAddress,mHardwareAddress});
        } else {
            Log.d(TAG, "Couldn't find MAC for "+mHostAddress);
        }
    }

}
