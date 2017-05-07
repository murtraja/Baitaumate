package com.smb.murtraja.baitaumate;

/**
 * Created by murtraja on 7/5/17.
 */

public interface OnFragmentInteractionListener {
    public enum FragmentResultType {
        ROUTER_SELECTED,
        ROUTER_CONNECTED,
        MULTIPLE_ACCESS_POINT_SELECTED,
        ACCESS_POINT_CONNECTED,
        ERROR
    }
    void onFragmentInteraction(FragmentResultType resultType, Object result);
}
