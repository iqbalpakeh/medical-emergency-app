/*
 * Copyright (c) 2017, Progrema Studio. All rights reserved.
 */

package com.progremastudio.emergencymedicalteam.authentication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.progremastudio.emergencymedicalteam.BaseActivity;
import com.progremastudio.emergencymedicalteam.core.MainActivity;
import com.progremastudio.emergencymedicalteam.R;

import java.util.Arrays;

public class SplashScreenActivity extends BaseActivity {

    private final String TAG = "splash-screen-activity";

    private final int SPLASH_TIME = 3000; // in ms

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        Initiate activity layout
         */
        setContentView(R.layout.activity_splash_screen);

        /*
        Initiate Firebase Authorization object
         */
        mAuth = FirebaseAuth.getInstance();

        /*
        Set application version
         */
        try {
            String packageName = getPackageName();
            String version = getPackageManager().getPackageInfo(packageName, 0).versionName;
            TextView applicationVersion = (TextView) findViewById(R.id.app_version);
            applicationVersion.setText(getString(R.string.str_version) + " " + version);
        } catch (PackageManager.NameNotFoundException error) {
            Log.e("ERROR", Arrays.toString(error.getStackTrace()));
        }

        /*
        Prepare waiting thread
         */
        final Thread thread = new Thread() {
            public void run() {
                /*
                Start waiting counter
                 */
                int wait = 0;
                try {
                    while (SPLASH_TIME > wait) {
                        sleep(100);
                        wait += 100;
                    }
                } catch (InterruptedException e) {
                    /*
                    Log error message
                     */
                    Log.e(TAG, e.getMessage());
                } finally {
                    /*
                    Go to Sign-In page
                     */
                    startActivity(new Intent(SplashScreenActivity.this, SignInActivity.class));
                    finish();
                }
            }
        };

        if (mAuth.getCurrentUser() != null) {
            /*
            If user already sign-in, go to main Page
             */
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            /*
            If not, execute waiting thread
             */
            thread.start();
        }
    }

}
