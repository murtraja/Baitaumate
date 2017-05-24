package com.smb.murtraja.baitaumate;

/**
 * Created by murtraja on 7/5/17.
 */

public interface OnInteractionListener {
    public enum InteractionResultType {
        ROUTER_SELECTED,
        ROUTER_CONNECTED,
        ROUTER_PASSWORD_SET,
        MULTIPLE_ACCESS_POINT_SELECTED,
        ACCESS_POINT_CONNECTED,

        COMMAND_SENT,

        WIFI_SCAN_RESULTS_AVAILABLE,
        WIFI_STATE_CHANGED_ACTION,

        DEBUG,
        ERROR
    }
    void onInteraction(InteractionResultType resultType, Object result);
}
