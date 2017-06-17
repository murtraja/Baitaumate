package com.smb.murtraja.baitaumate;

/**
 * Created by murtraja on 7/5/17.
 */

public interface OnInteractionListener {
    public enum InteractionResultType {

        /* WifiModeConfig related enums*/

        ROUTER_SELECTED,
        ROUTER_CONNECTED,
        ROUTER_PASSWORD_SET,
        MULTIPLE_ACCESS_POINT_SELECTED,
        ACCESS_POINT_CONNECTED,

        COMMAND_SENT,

        WIFI_SCAN_RESULTS_AVAILABLE,
        WIFI_STATE_CHANGED_ACTION,

        HOST_PROBED,
        SUBNET_PROBED,
        PROBE_FINISHED,

        DEVICE_CONFIG_DONE,

        /* ConfigureLights related enums */

        DEVICE_SELECTED_FOR_CONFIG,

        DEBUG,
        ERROR
    }
    void onInteraction(InteractionResultType resultType, Object result);
}
