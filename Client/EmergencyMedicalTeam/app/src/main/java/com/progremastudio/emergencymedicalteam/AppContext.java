package com.progremastudio.emergencymedicalteam;

import android.content.Context;
import android.content.SharedPreferences;

public class AppContext {

    private static final String APP_CONTEXT = "app-context";
    private static final String USER_EMAIL = "user-email";
    private static final String USER_PHONE_NUMBER = "user-phone-number";
    private static final String USER_DISPLAY_NAME = "user-display-name";
    private static final String LAST_LOCATION_LATITUDE = "last-location-latitude";
    private static final String LAST_LOCATION_LONGITUDE = "last-location-longitude";

    private static final String DEFAULT_LATITUDE = "3.58333";
    private static final String DEFAULT_LONGITUDE= "98.66667";

    public static void logOutCurrentUser(Context context) {
        storeCurrentUser(context, "", "", "");
    }

    public static void storeCurrentUser(Context context,
                                        String displayName,
                                        String email,
                                        String phoneNumber) {

        storeCurrentUserDisplayName(context, displayName);
        storeCurrentUserEmail(context, email);
        storeCurrentUserPhoneNumber(context, phoneNumber);
    }

    public static void storeCurrentUserDisplayName(Context context, String displayName) {
        SharedPreferences appContext = context.getSharedPreferences(APP_CONTEXT, 0);
        SharedPreferences.Editor editor = appContext.edit();
        editor.putString(USER_DISPLAY_NAME, displayName);
        editor.commit();
    }

    public static String fetchCurrentUserDisplayName(Context context) {
        SharedPreferences appContext = context.getSharedPreferences(APP_CONTEXT, 0);
        return appContext.getString(USER_DISPLAY_NAME, "");
    }

    public static void storeCurrentUserEmail(Context context, String email) {
        SharedPreferences appContext = context.getSharedPreferences(APP_CONTEXT, 0);
        SharedPreferences.Editor editor = appContext.edit();
        editor.putString(USER_EMAIL, email);
        editor.commit();
    }

    public static String fetchCurrentUserEmail(Context context) {
        SharedPreferences appContext = context.getSharedPreferences(APP_CONTEXT, 0);
        return appContext.getString(USER_EMAIL, "");
    }

    public static void storeCurrentUserPhoneNumber(Context context, String phoneNumber) {
        SharedPreferences appContext = context.getSharedPreferences(APP_CONTEXT, 0);
        SharedPreferences.Editor editor = appContext.edit();
        editor.putString(USER_PHONE_NUMBER, phoneNumber);
        editor.commit();
    }

    public static String fetchCurrentUserPhoneNumber(Context context) {
        SharedPreferences appContext = context.getSharedPreferences(APP_CONTEXT, 0);
        return appContext.getString(USER_PHONE_NUMBER, "");
    }

    public static void storeCurrentUserLastLatitudeLocation(Context context, String latitude) {
        SharedPreferences appContext = context.getSharedPreferences(APP_CONTEXT, 0);
        SharedPreferences.Editor editor = appContext.edit();
        editor.putString(LAST_LOCATION_LATITUDE, latitude);
        editor.commit();
    }

    public static String fetchCurrentUserLastLatitudeLocation(Context context) {
        SharedPreferences appContext = context.getSharedPreferences(APP_CONTEXT, 0);
        return appContext.getString(LAST_LOCATION_LATITUDE, DEFAULT_LATITUDE);
    }

    public static void storeCurrentUserLastLongitudeLocation(Context context, String longitude) {
        SharedPreferences appContext = context.getSharedPreferences(APP_CONTEXT, 0);
        SharedPreferences.Editor editor = appContext.edit();
        editor.putString(LAST_LOCATION_LONGITUDE, longitude);
        editor.commit();
    }

    public static String fetchCurrentUserLastLongitudeLocation(Context context) {
        SharedPreferences appContext = context.getSharedPreferences(APP_CONTEXT, 0);
        return appContext.getString(LAST_LOCATION_LONGITUDE, DEFAULT_LONGITUDE);
    }

}
