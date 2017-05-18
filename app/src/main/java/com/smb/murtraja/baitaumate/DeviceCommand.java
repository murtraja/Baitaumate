package com.smb.murtraja.baitaumate;

import android.graphics.Color;

/**
 * Created by murtraja on 7/5/17.
 */

public class DeviceCommand {

    /*
    this won't scale very well if there are multiple types of devices having multiple commands
    also, set colour for LED may differ that from other light type
    TODO: rewire the class, think of a better approach, change the name to CommandGenerator
     */

    private static String SET_COLOUR_PREFIX = "#%";
    private static String SET_COLOUR_DELIMITER = ",";

    private static String SET_ROUTER_PREFIX = "$";
    private static String SET_ROUTER_DELIMITER = ":";


    public static String generateSetColourCommand(int colour) {
        int r = Color.red(colour);
        int g = Color.green(colour);
        int b = Color.blue(colour);
        String command = String.format("%s%03d%s%03d%s%03d", SET_COLOUR_PREFIX, g, SET_COLOUR_DELIMITER, r, SET_COLOUR_DELIMITER, b);
        return command;
    }

    public static String generateSetRouterCommand(String accessPointName, String accessPointPassword) {
        String command = String.format("%s%s%s%s", SET_ROUTER_PREFIX, accessPointName, SET_ROUTER_DELIMITER, accessPointPassword);
        return command;
    }
}
