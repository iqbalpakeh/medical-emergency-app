package com.progremastudio.emergencymedicalteam;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class BaseActivity extends AppCompatActivity {

    private final static String TAG = "app-compat-activity";

    private ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setMessage(getString(R.string.str_Loading));
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public String getDisplayName() {
        /*
        There's a known bug of FIREBASE getDisplayName api that return NULL only after first Sign-Up.
        The workaround is to SharedPreference for saving current user properties.
         */
        return FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    }

    public String getUserEmail() {
        return FirebaseAuth.getInstance().getCurrentUser().getEmail();
    }

    public String currentTimestamp() {
        return String.valueOf(Calendar.getInstance().getTimeInMillis());
    }

}
