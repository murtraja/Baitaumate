package com.smb.murtraja.baitaumate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity implements IScanResultsAvailableActionHandler, IWifiStateChangedActionListener {

    static String TAG = "MMR";
    private WifiManager wifiManager;
    private TextView textView;
    private Button scanAgain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context context = getApplicationContext();
        wifiManager = (WifiManager)context.getSystemService(WIFI_SERVICE);
        textView = (TextView)findViewById(R.id.tv_status);
        scanAgain = (Button) findViewById(R.id.btn_scanAgain);
        scanAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWifiScan();
            }
        });


        startWifiScan();

    }

    private void startWifiScan() {
        scanAgain.setEnabled(false);
        final IntentFilter scanResultsIntent = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        ReceiverForScanResultsAvailableAction receiverForScanResultsAvailableAction =
                new ReceiverForScanResultsAvailableAction(wifiManager, this);

        this.registerReceiver(receiverForScanResultsAvailableAction, scanResultsIntent);
        wifiManager.startScan();
        textView.setText("Searching for Wifi networks...");
    }
    private void connectToAccessPoint(String ssid) {
        ssid = String.format("\"%s\"", ssid);
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = ssid;
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        int netId = wifiManager.addNetwork(wifiConfiguration);
        wifiManager.enableNetwork(netId, true);
        textView.setText(String.format("Now connecting to %s", ssid));

        IntentFilter stateChangedIntent = new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        //stateChangedIntent.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        ReceiverForWifiStateChangedAction receiverForWifiStateChangedAction =
                new ReceiverForWifiStateChangedAction(ssid, wifiManager, this);
        Context context = getApplicationContext();
        context.registerReceiver(receiverForWifiStateChangedAction, stateChangedIntent);


    }

    @Override
    protected void onStop() {
        super.onStop();
        return;
//        for(WifiConfiguration configuration : mWifiManager.getConfiguredNetworks()) {
//            //Log.d(MainActivity.TAG, configuration.SSID);
//            if(configuration.SSID.equals("\"Celerio\"")) {
//                mWifiManager.enableNetwork(configuration.networkId, true);
//            }
//        }

    }

    @Override
    public void handleScanResultsAvailableAction(List<ScanResult> results) {
        TextView textView = (TextView)findViewById(R.id.tv_status);
        ListView listView = (ListView)findViewById(R.id.lv_scanResults);
        WifiResultsProcessor wifiResultsProcessor = new WifiResultsProcessor(results);
        List<String> accessPoints = wifiResultsProcessor.computeUniqueAPsFromScanResults(true);
        textView.setText(String.format("Found %d Wifi networks", accessPoints.size()));
        ListAdapter listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, accessPoints);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String ssid = ((TextView)view).getText().toString();
//                Toast.makeText(MainActivity.this, ""+mAccessPointName, Toast.LENGTH_SHORT).show();
                connectToAccessPoint(ssid);
            }
        });
        scanAgain.setEnabled(true);
    }

    @Override
    public void handleWifiStateChangedAction(String accessPointName, boolean successful) {
        if(successful) {
            Toast.makeText(this, "Successfully connected", Toast.LENGTH_SHORT).show();
            //now make a new activity here!
            Intent intent = new Intent(this, ConfigureLightActivity.class);
            this.startActivity(intent);
        }
    }
}

/*





 */