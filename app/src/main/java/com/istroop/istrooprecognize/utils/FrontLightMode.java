package com.istroop.istrooprecognize.utils;

import android.content.SharedPreferences;

/**
 * Created by joshua-zr on 8/26/15.
 */
public enum FrontLightMode {
    /**
     * Always on.
     */
    ON,
    /**
     * On only when ambient light is low.
     */
    AUTO,
    /**
     * Always off.
     */
    OFF;

    private static FrontLightMode parse( String modeString ) {
        return modeString == null ? OFF : valueOf( modeString );
    }

    public static FrontLightMode readPref( SharedPreferences sharedPrefs ) {
        return parse( sharedPrefs.getString( PreferencesActivity.KEY_FRONT_LIGHT_MODE, OFF.toString() ) );
    }
}
