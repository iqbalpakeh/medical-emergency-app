/*
 * Copyright (c) 2017, Progrema Studio. All rights reserved.
 */

package com.progremastudio.emergencymedicalteam;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class BaseActivity extends AppCompatActivity {

    private final static String TAG = "app-compat-activity";

    private ProgressDialog mProgressDialog;

    /**
     * Show progress dialog
     */
    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setMessage(getString(R.string.str_Loading));
        }
        mProgressDialog.show();
    }

    /**
     * Hide progress dialog
     */
    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    /**
     * Get Firebase user id
     *
     * @return user id
     */
    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    /**
     * Get current time stamp
     *
     * @return current timestamp in milisecond
     */
    public String currentTimestamp() {
        return String.valueOf(Calendar.getInstance().getTimeInMillis());
    }

}
