package com.progremastudio.emergencymedicalteam;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class SplashScreenActivity extends BaseActivity {

    private final String TAG = "splash-screen-activity";

    private final int SPLASH_TIME = 4000;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        Set activity layout
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
            applicationVersion.setText(getString(R.string.str_Version) + version);
        } catch (PackageManager.NameNotFoundException error) {
            Log.e("ERROR", error.getStackTrace().toString());
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

        /*
        Execute waiting thread
         */
        thread.start();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            /*
            If user already sig-in, go to main Page
             */
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

}
