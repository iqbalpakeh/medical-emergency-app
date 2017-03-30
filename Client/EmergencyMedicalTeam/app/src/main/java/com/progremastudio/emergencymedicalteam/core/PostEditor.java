package com.progremastudio.emergencymedicalteam.core;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.progremastudio.emergencymedicalteam.AppSharedPreferences;
import com.progremastudio.emergencymedicalteam.BaseActivity;
import com.progremastudio.emergencymedicalteam.R;

public class PostEditor extends BaseActivity {

    private static final String TAG = "post-editor";

    private TextView mAddress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        Initiate post editor layout
         */
        setContentView(R.layout.activity_post_editor);

        /*
        Initiate widget
         */
        mAddress = (TextView) findViewById(R.id.address_text_view);
        mAddress.setText(AppSharedPreferences.fetchCurrentUserAddress(this));
    }

}
