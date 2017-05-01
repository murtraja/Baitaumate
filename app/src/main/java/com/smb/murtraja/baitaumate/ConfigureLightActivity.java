package com.smb.murtraja.baitaumate;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.danielnilsson9.colorpickerview.view.ColorPickerView;

public class ConfigureLightActivity extends Activity implements IOnCommandSentListener{

    Button mSetColourButton;
    ColorPickerView mColorPicker;
    TextView mCommandStatusTextView;
    ConfigureLightActivity mConfigureLightActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_light);

        mColorPicker = (ColorPickerView) findViewById(R.id.cpv_pickColour);
        mCommandStatusTextView = (TextView) findViewById(R.id.tv_commandStatus);
        mConfigureLightActivity = this;


        this.mSetColourButton = (Button) findViewById(R.id.btn_setColour);
        this.mSetColourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCommandStatusTextView.setText("Now sending command...");
                int color = mColorPicker.getColor();
                CommandSender commandSender = new CommandSender(color, mConfigureLightActivity);
                Thread thread = new Thread(commandSender);
                thread.start();
            }
        });
    }

    @Override
    public void onCommandSentHandler(String reply) {
        this.mCommandStatusTextView.setText("Successful!\n"+reply);
        this.mSetColourButton.setEnabled(true);
    }
}
