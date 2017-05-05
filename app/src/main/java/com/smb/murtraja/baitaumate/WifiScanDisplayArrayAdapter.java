package com.smb.murtraja.baitaumate;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by murtraja on 4/5/17.
 */

public class WifiScanDisplayArrayAdapter extends ArrayAdapter<String> {

    List<String> mAccessPoints;
    Context mContext;
    LayoutInflater mInflater;

    public WifiScanDisplayArrayAdapter(Context context, int resource, List<String> accessPoints) {
        super(context, resource, accessPoints);
        mAccessPoints = accessPoints;
        mContext = context;
        mInflater = (LayoutInflater)mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        String accessPoint = getItem(position);
        convertView = mInflater.inflate(R.layout.checkbox_list_item, parent, false);
        CheckBox accessPointCheckBox = (CheckBox) convertView.findViewById(R.id.cb_access_point);
        accessPointCheckBox.setText(accessPoint);
        return convertView;
    }
}
