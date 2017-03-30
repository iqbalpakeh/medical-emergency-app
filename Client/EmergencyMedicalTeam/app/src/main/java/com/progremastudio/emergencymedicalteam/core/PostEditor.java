package com.progremastudio.emergencymedicalteam.core;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.progremastudio.emergencymedicalteam.BaseActivity;
import com.progremastudio.emergencymedicalteam.R;

public class PostEditor extends BaseActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        Initiate post editor layout
         */
        setContentView(R.layout.activity_post_editor);
    }

}
