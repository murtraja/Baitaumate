package com.smb.murtraja.baitaumate;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ConfigureLightsActivity extends Activity implements OnInteractionListener{

    private static String TAG = "CLAct";

    private Activity mConfigureLightsActivity = null;

    private FragmentManager mFragmentManager;
    private Fragment mCurrentlyAttachedFragment;

    private JSONObject mMapping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_lights);
        mFragmentManager = getFragmentManager();
        mConfigureLightsActivity = this;
        
        mMapping = getMappingFromSharedPreferences();
        ArrayList<String> deviceList = getDeviceListFromMapping();
        
        ListDevicesFragment fragment = ListDevicesFragment.newInstance(deviceList, InteractionResultType.DEVICE_SELECTED_FOR_CONFIG);
        setFragment(fragment);
    }

    private ArrayList<String> getDeviceListFromMapping() {
        ArrayList<String> deviceList = new ArrayList<>();
        Iterator<String> iterator = mMapping.keys();
        while(iterator.hasNext()) {
            String deviceMAC = iterator.next();
            deviceList.add(deviceMAC);
        }
        return deviceList;
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        if(mCurrentlyAttachedFragment != null) {
            fragmentTransaction.remove(mCurrentlyAttachedFragment);
        }
        fragmentTransaction.add(R.id.ll_configure_lights, fragment);
        fragmentTransaction.commit();
        mCurrentlyAttachedFragment = fragment;
    }

    @Override
    public void onInteraction(InteractionResultType resultType, Object result) {

    }

    JSONObject getMappingFromSharedPreferences() {
        SharedPreferences preferences = this.getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        String mappingString = preferences.getString("mapping", "");
        if(mappingString == "") {
            Log.d(TAG, "No preferences with mapping keyword exists");
            return null;
        }
        JSONObject mappingJson = null;
        try {
            mappingJson = new JSONObject(mappingString);
            Log.d(TAG, "retrieved the following from mapping key: "+mappingString);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        // TODO: now check if there are any devices in this mapping
        return mappingJson;
    }
}
