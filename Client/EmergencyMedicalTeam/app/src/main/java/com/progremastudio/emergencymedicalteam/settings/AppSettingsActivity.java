package com.progremastudio.emergencymedicalteam.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.progremastudio.emergencymedicalteam.BaseActivity;

public class AppSettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new AppSettingsFragment())
                .commit();
    }

}
