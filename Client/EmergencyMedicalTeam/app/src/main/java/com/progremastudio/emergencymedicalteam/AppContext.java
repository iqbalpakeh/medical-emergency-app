package com.progremastudio.emergencymedicalteam;

import android.content.Context;
import android.content.SharedPreferences;

public class AppContext {

    private static final String CONTEXT_APP = "CONTEXT_APP";
    private static final String CONTEXT_USER_EMAIL = "CONTEXT_USER_EMAIL";
    private static final String CONTEXT_USER_PHONE_NUMBER = "CONTEXT_USER_PHONE_NUMBER";
    private static final String CONTEXT_USER_DISPLAY_NAME = "CONTEXT_USER_DISPLAY_NAME";

    public static void storeCurrentUser(Context context,
                                        String displayName,
                                        String email,
                                        String phoneNumber) {

        storeCurrentUserDisplayName(context, displayName);
        storeCurrentUserEmail(context, email);
        storeCurrentUserPhoneNumber(context, phoneNumber);
    }

    public static void storeCurrentUserDisplayName(Context context, String displayName) {
        SharedPreferences appContext = context.getSharedPreferences(CONTEXT_APP, 0);
        SharedPreferences.Editor editor = appContext.edit();
        editor.putString(CONTEXT_USER_DISPLAY_NAME, displayName);
        editor.commit();
    }

    public static String fetchCurrentUserDisplayName(Context context) {
        SharedPreferences appContext = context.getSharedPreferences(CONTEXT_APP, 0);
        return appContext.getString(CONTEXT_USER_DISPLAY_NAME, "");
    }

    public static void storeCurrentUserEmail(Context context, String email) {
        SharedPreferences appContext = context.getSharedPreferences(CONTEXT_APP, 0);
        SharedPreferences.Editor editor = appContext.edit();
        editor.putString(CONTEXT_USER_EMAIL, email);
        editor.commit();
    }

    public static String fetchCurrentUserEmail(Context context) {
        SharedPreferences appContext = context.getSharedPreferences(CONTEXT_APP, 0);
        return appContext.getString(CONTEXT_USER_EMAIL, "");
    }

    public static void storeCurrentUserPhoneNumber(Context context, String phoneNumber) {
        SharedPreferences appContext = context.getSharedPreferences(CONTEXT_APP, 0);
        SharedPreferences.Editor editor = appContext.edit();
        editor.putString(CONTEXT_USER_PHONE_NUMBER, phoneNumber);
        editor.commit();
    }

    public static String fetchCurrentUserPhoneNumber(Context context) {
        SharedPreferences appContext = context.getSharedPreferences(CONTEXT_APP, 0);
        return appContext.getString(CONTEXT_USER_PHONE_NUMBER, "");
    }



}
