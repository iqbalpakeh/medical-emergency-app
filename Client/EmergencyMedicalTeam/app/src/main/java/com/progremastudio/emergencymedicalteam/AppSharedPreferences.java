package com.progremastudio.emergencymedicalteam;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSharedPreferences {

    /*
    Shared preference names
     */
    private static final String APP_CONTEXT = "app-context";
    private static final String USER_EMAIL = "user-email";
    private static final String USER_PHONE_NUMBER = "user-phone-number";
    private static final String USER_DISPLAY_NAME = "user-display-name";
    private static final String LAST_LOCATION_LATITUDE = "last-location-latitude";
    private static final String LAST_LOCATION_LONGITUDE = "last-location-longitude";
    private static final String LAST_LOCATION_ADDRESS = "last-location-address";

    /*
    Default coordinate is Medan
     */
    private static final String DEFAULT_LATITUDE = "3.58333";
    private static final String DEFAULT_LONGITUDE = "98.66667";

    public static void logOutCurrentUser(Context context) {
        storeCurrentUser(context, "", "", "");
    }

    /**
     * Store current user's display name, email and phone number
     *
     * @param context application context
     * @param displayName current user's display name
     * @param email current user's email
     * @param phoneNumber current user's phone number
     */
    public static void storeCurrentUser(Context context,
                                        String displayName,
                                        String email,
                                        String phoneNumber) {

        storeCurrentUserDisplayName(context, displayName);
        storeCurrentUserEmail(context, email);
        storeCurrentUserPhoneNumber(context, phoneNumber);
    }

    /**
     * Store current user's display name to shared-preference
     *
     * @param context application context
     * @param displayName current user's display name
     */
    public static void storeCurrentUserDisplayName(Context context, String displayName) {
        SharedPreferences appContext = context.getSharedPreferences(APP_CONTEXT, 0);
        SharedPreferences.Editor editor = appContext.edit();
        editor.putString(USER_DISPLAY_NAME, displayName);
        editor.commit();
    }

    /**
     * Fetch current user's display name from shared-preference
     *
     * @param context application context
     * @return current user's display name
     */
    public static String fetchCurrentUserDisplayName(Context context) {
        SharedPreferences appContext = context.getSharedPreferences(APP_CONTEXT, 0);
        return appContext.getString(USER_DISPLAY_NAME, "");
    }

    /**
     * Store current user's email to shared-preference
     *
     * @param context application context
     * @param email current user's email
     */
    public static void storeCurrentUserEmail(Context context, String email) {
        SharedPreferences appContext = context.getSharedPreferences(APP_CONTEXT, 0);
        SharedPreferences.Editor editor = appContext.edit();
        editor.putString(USER_EMAIL, email);
        editor.commit();
    }

    /**
     * Fetch current user's email from shared-preference
     *
     * @param context application context
     * @return current user's email
     */
    public static String fetchCurrentUserEmail(Context context) {
        SharedPreferences appContext = context.getSharedPreferences(APP_CONTEXT, 0);
        return appContext.getString(USER_EMAIL, "");
    }

    /**
     * Store current user's phone number to shared-preference
     *
     * @param context application context
     * @param phoneNumber current user's phone number
     */
    public static void storeCurrentUserPhoneNumber(Context context, String phoneNumber) {
        SharedPreferences appContext = context.getSharedPreferences(APP_CONTEXT, 0);
        SharedPreferences.Editor editor = appContext.edit();
        editor.putString(USER_PHONE_NUMBER, phoneNumber);
        editor.commit();
    }

    /**
     * Fetch current user's phone number from shared-preference
     *
     * @param context application context
     * @return current user's phone number
     */
    public static String fetchCurrentUserPhoneNumber(Context context) {
        SharedPreferences appContext = context.getSharedPreferences(APP_CONTEXT, 0);
        return appContext.getString(USER_PHONE_NUMBER, "");
    }

    /**
     * Store current user's last latitude location to shared-preference
     *
     * @param context application context
     * @param latitude current's user last latitude location
     */
    public static void storeCurrentUserLastLatitudeLocation(Context context, String latitude) {
        SharedPreferences appContext = context.getSharedPreferences(APP_CONTEXT, 0);
        SharedPreferences.Editor editor = appContext.edit();
        editor.putString(LAST_LOCATION_LATITUDE, latitude);
        editor.commit();
    }

    /**
     * Fetch current user's last latitude location from shared-preference
     *
     * @param context application context
     * @return current user's last latitude location
     */
    public static String fetchCurrentUserLastLatitudeLocation(Context context) {
        SharedPreferences appContext = context.getSharedPreferences(APP_CONTEXT, 0);
        return appContext.getString(LAST_LOCATION_LATITUDE, DEFAULT_LATITUDE);
    }

    /**
     * Store current user's last longitude location to shared-preference
     *
     * @param context application context
     * @param longitude current user's last longitude location
     */
    public static void storeCurrentUserLastLongitudeLocation(Context context, String longitude) {
        SharedPreferences appContext = context.getSharedPreferences(APP_CONTEXT, 0);
        SharedPreferences.Editor editor = appContext.edit();
        editor.putString(LAST_LOCATION_LONGITUDE, longitude);
        editor.commit();
    }

    /**
     * Fetch current user's last longitude location from shared-preference
     *
     * @param context application context
     * @return current user's last longitude location
     */
    public static String fetchCurrentUserLastLongitudeLocation(Context context) {
        SharedPreferences appContext = context.getSharedPreferences(APP_CONTEXT, 0);
        return appContext.getString(LAST_LOCATION_LONGITUDE, DEFAULT_LONGITUDE);
    }

    /**
     * Store current user's last address to shared-preference
     *
     * @param context application context
     * @param address current user's last address
     */
    public static void storeCurrentUserLastAddress(Context context, String address) {
        SharedPreferences appContext = context.getSharedPreferences(APP_CONTEXT, 0);
        SharedPreferences.Editor editor = appContext.edit();
        editor.putString(LAST_LOCATION_ADDRESS, address);
        editor.commit();
    }

    /**
     * Fetch current user's last address from shared-preference
     *
     * @param context application context
     * @return current user's last address
     */
    public static String fetchCurrentUserAddress(Context context) {
        SharedPreferences appContext = context.getSharedPreferences(APP_CONTEXT, 0);
        return appContext.getString(LAST_LOCATION_ADDRESS, DEFAULT_LONGITUDE);
    }

}
