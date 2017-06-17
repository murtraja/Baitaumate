package com.smb.murtraja.baitaumate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    private Button configureLightsButton;
    private Button wifiModeConfigureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configureLightsButton = (Button) findViewById(R.id.btn_configure_lights);
        configureLightsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConfigureLights();
            }
        });

        wifiModeConfigureButton= (Button) findViewById(R.id.btn_wifi_mode_config);
        wifiModeConfigureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onWifiModeConfigure();
            }
        });



    }

    private void onWifiModeConfigure() {
        Intent intent = new Intent(this, WifiModeActivity.class);
        startActivity(intent);
    }

    private void onConfigureLights() {
        Intent intent = new Intent(this, ConfigureLightsActivity.class);
        startActivity(intent);
    }

}