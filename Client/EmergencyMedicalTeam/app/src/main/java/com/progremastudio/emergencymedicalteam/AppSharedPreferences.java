package com.progremastudio.emergencymedicalteam;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSharedPreferences {

    /*
    Shared preference names
     */
    private static final String APP_CONTEXT = "app-context";
    private static final String USER_ID = "user_id";
    private static final String USER_EMAIL = "user-email";
    private static final String USER_PHONE_NUMBER = "user-phone-number";
    private static final String USER_DISPLAY_NAME = "user-display-name";
    private static final String USER_PICURE_URL = "user-picture-url";
    private static final String LAST_LOCATION_LATITUDE = "last-location-latitude";
    private static final String LAST_LOCATION_LONGITUDE = "last-location-longitude";
    private static final String LAST_LOCATION_ADDRESS = "last-location-address";

    /*
    Default coordinate is Medan
     */
    public static final String DEFAULT_LATITUDE = "3.58333";
    public static final String DEFAULT_LONGITUDE = "98.66667";
    public static final String DEFAULT_ADDRESS = "Kota Medan";

    /*
    Public constant
     */
    public static final String NO_URL = "no-url";

    public static void logOutCurrentUser(Context context) {
        storeCurrentUser(context, "", "", "", "", "");
    }

    /**
     * Store current user's display name, email and phone number
     *
     * @param context application context
     * @param uid current user's id
     * @param displayName current user's display name
     * @param email current user's email
     * @param phoneNumber current user's phone number
     * @param pictureUrl current user's picture url
     */
    public static void storeCurrentUser(Context context,
                                        String uid,
                                        String displayName,
                                        String email,
                                        String phoneNumber,
                                        String pictureUrl) {

        storeCurrentUserId(context, uid);
        storeCurrentUserDisplayName(context, displayName);
        storeCurrentUserEmail(context, email);
        storeCurrentUserPhoneNumber(context, phoneNumber);
        storeCurrentUserPictureUrl(context, pictureUrl);
    }

    /**
     * Store current user's uid to shared-preference
     *
     * @param context application context
     * @param uid current user's uid
     */
    public static void storeCurrentUserId(Context context, String uid) {
        SharedPreferences appContext = context.getSharedPreferences(APP_CONTEXT, 0);
        SharedPreferences.Editor editor = appContext.edit();
        editor.putString(USER_ID, uid);
        editor.commit();
    }

    /**
     * Fetch current user's uid from shared-preference
     *
     * @param context application context
     * @return current user's uid
     */
    public static String getCurrentUserId(Context context) {
        SharedPreferences appContext = context.getSharedPreferences(APP_CONTEXT, 0);
        return appContext.getString(USER_ID, "");
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
     * Get current user's display name from shared-preference
     *
     * @param context application context
     * @return current user's display name
     */
    public static String getCurrentUserDisplayName(Context context) {
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
     * Get current user's email from shared-preference
     *
     * @param context application context
     * @return current user's email
     */
    public static String getCurrentUserEmail(Context context) {
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
     * Get current user's phone number from shared-preference
     *
     * @param context application context
     * @return current user's phone number
     */
    public static String getCurrentUserPhoneNumber(Context context) {
        SharedPreferences appContext = context.getSharedPreferences(APP_CONTEXT, 0);
        return appContext.getString(USER_PHONE_NUMBER, "");
    }

    /**
     * Store current user's picture url to shared-preference
     *
     * @param context application context
     * @param pictureUrl current user's phone number
     */
    public static void storeCurrentUserPictureUrl(Context context, String pictureUrl) {
        SharedPreferences appContext = context.getSharedPreferences(APP_CONTEXT, 0);
        SharedPreferences.Editor editor = appContext.edit();
        editor.putString(USER_PICURE_URL, pictureUrl);
        editor.commit();
    }

    /**
     * Get current user's picture url from shared-preference
     *
     * @param context application context
     * @return current user's picture url
     */
    public static String getCurrentUserPictureUrl(Context context) {
        SharedPreferences appContext = context.getSharedPreferences(APP_CONTEXT, 0);
        return appContext.getString(USER_PICURE_URL, "");
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
     * Get current user's last latitude location from shared-preference
     *
     * @param context application context
     * @return current user's last latitude location
     */
    public static String getCurrentUserLastLatitudeLocation(Context context) {
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
     * Get current user's last longitude location from shared-preference
     *
     * @param context application context
     * @return current user's last longitude location
     */
    public static String getCurrentUserLastLongitudeLocation(Context context) {
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
     * Get current user's last address from shared-preference
     *
     * @param context application context
     * @return current user's last address
     */
    public static String getCurrentUserAddress(Context context) {
        SharedPreferences appContext = context.getSharedPreferences(APP_CONTEXT, 0);
        return appContext.getString(LAST_LOCATION_ADDRESS, DEFAULT_ADDRESS);
    }

}
