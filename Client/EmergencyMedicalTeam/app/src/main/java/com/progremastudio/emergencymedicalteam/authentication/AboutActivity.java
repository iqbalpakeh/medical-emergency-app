package com.progremastudio.emergencymedicalteam.authentication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import com.progremastudio.emergencymedicalteam.BaseActivity;
import com.progremastudio.emergencymedicalteam.MainActivity;
import com.progremastudio.emergencymedicalteam.R;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        Set activity layout
         */
        setContentView(R.layout.activity_splash_screen);

        /*
        Set application version
         */
        try {
            String packageName = getPackageName();
            String version = getPackageManager().getPackageInfo(packageName, 0).versionName;
            TextView applicationVersion = (TextView) findViewById(R.id.app_version);
            applicationVersion.setText(getString(R.string.str_Version) + " " + version);
        } catch (PackageManager.NameNotFoundException error) {
            Log.e("ERROR", error.getStackTrace().toString());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
