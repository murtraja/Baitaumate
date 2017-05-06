package com.smb.murtraja.baitaumate;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Toast;

import java.util.List;

/**
 * Created by mraja on 5/6/2017.
 */

public class OnCheckableItemClickListener implements AdapterView.OnItemClickListener {

    List<String> mAccessPointsSelected;
    Context mContext;

    public OnCheckableItemClickListener(List<String> accessPointsSelected, Context context) {
        mAccessPointsSelected = accessPointsSelected;
        mAccessPointsSelected.clear();
        mContext = context;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

        /*
        was the check box checked?
            yes
                1. uncheck it
                2. remove this SSID from the list
            no
                1. check it
                2. add this SSID to the list
         */

        CheckBox accessPointCheckBox = (CheckBox) view.findViewById(R.id.cb_access_point);

        String accessPoint = accessPointCheckBox.getText().toString();

        if(accessPointCheckBox.isChecked()) {
            accessPointCheckBox.setChecked(false);
            mAccessPointsSelected.remove(accessPoint);
        } else {
            accessPointCheckBox.setChecked(true);
            mAccessPointsSelected.add(accessPoint);
        }
        Toast.makeText(mContext, "You clicked on "+accessPoint+mAccessPointsSelected.size(), Toast.LENGTH_SHORT).show();
    }
}
