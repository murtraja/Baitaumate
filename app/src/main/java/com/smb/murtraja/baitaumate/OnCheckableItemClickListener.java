package com.smb.murtraja.baitaumate;

import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;

import java.util.List;

/**
 * Created by mraja on 5/6/2017.
 */

public class OnCheckableItemClickListener implements AdapterView.OnItemClickListener {

    List<String> mAccessPointsSelected;

    public OnCheckableItemClickListener(List<String> accessPointsSelected) {
        mAccessPointsSelected = accessPointsSelected;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        /*
        was the check box checked?
            yes
                1. uncheck it
                2. remove this SSID from the list
            no
                1. check it
                2. add this SSID to the list
         */



        CheckBox accessPointCheckBox = (CheckBox) adapterView.findViewById(R.id.cb_access_point);

        String accessPoint = accessPointCheckBox.getText().toString();

        if(accessPointCheckBox.isChecked()) {
            accessPointCheckBox.setChecked(false);
            mAccessPointsSelected.add(accessPoint);
        } else {
            accessPointCheckBox.setChecked(true);
            mAccessPointsSelected.remove(accessPoint);
        }
    }
}
