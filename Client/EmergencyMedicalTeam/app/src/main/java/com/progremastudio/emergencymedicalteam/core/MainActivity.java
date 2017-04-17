/*
 * Copyright (c) 2017, Progrema Studio. All rights reserved.
 */

package com.progremastudio.emergencymedicalteam.core;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.progremastudio.emergencymedicalteam.AppSharedPreferences;
import com.progremastudio.emergencymedicalteam.BaseActivity;
import com.progremastudio.emergencymedicalteam.R;
import com.progremastudio.emergencymedicalteam.authentication.AboutActivity;
import com.progremastudio.emergencymedicalteam.authentication.SignInActivity;
import com.progremastudio.emergencymedicalteam.settings.AppSettingsActivity;
import com.progremastudio.emergencymedicalteam.settings.AppSettingsFragment;

public class MainActivity extends BaseActivity {

    private static final String TAG = "main-activity";

    private static final int PERMISSION_TO_CALL_PHONE = 0;

    public static final String EXTRA_OPEN_PAGE = "com.progremastudio.emergencymedicalteam.core.PAGE";

    public static final int PAGE_LOCATION = 0;

    public static final int PAGE_POST = 1;

    public static final int PAGE_CHAT = 2;

    private ViewPager mViewPager;

    private FloatingActionsMenu mFabMenu;

    private int mPageOpen;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        Initiate activity layout
         */
        setContentView(R.layout.activity_main);

        /*
        Set up home toolbar
         */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.heading_location));
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        /*
        Create the adapter that will return a fragment for each section
         */
        FragmentPagerAdapter pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            private final Fragment[] mFragments = new Fragment[]{
                    new LocationFragment(),
                    new PostFragment(),
                    new ChatFragment(),
            };

            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }

            @Override
            public int getCount() {
                return mFragments.length;
            }
        };

        /*
        Set up the ViewPager with the sections adapter.
         */
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    mFabMenu.setVisibility(View.VISIBLE);
                    getSupportActionBar().setTitle(getString(R.string.heading_location));
                } else if (position == 1) {
                    mFabMenu.setVisibility(View.VISIBLE);
                    getSupportActionBar().setTitle(getString(R.string.heading_post));
                } else if (position == 2) {
                    mFabMenu.setVisibility(View.GONE);
                    getSupportActionBar().setTitle(getString(R.string.heading_chat));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        /*
        Initiate tab layout
         */
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_ambulance_light_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_electrocardiogram_report_24dp);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_doctor_white_24dp);

        /*
        Initiate floating action button
         */
        mFabMenu = (FloatingActionsMenu) findViewById(R.id.fab_menu);
        FloatingActionButton emergencyCallButton = (FloatingActionButton) findViewById(R.id.fab_call);
        emergencyCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callAmbulance();
            }
        });
        FloatingActionButton accidentPostButton = (FloatingActionButton) findViewById(R.id.fab_report);
        accidentPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPostEditor();
            }
        });

        /*
        Define open page
         */
        mPageOpen = getIntent().getIntExtra(EXTRA_OPEN_PAGE, PAGE_LOCATION);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mViewPager.setCurrentItem(mPageOpen);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
        Get selected menu
         */
        int i = item.getItemId();

        /*
        Log-out menu
         */
        if (i == R.id.action_logout) {

            /*
            Clear user data on shared-preference
             */
            AppSharedPreferences.logOutCurrentUser(this);

            /*
            Sign-out from Firebase authentication
             */
            FirebaseAuth.getInstance().signOut();

            /*
            Go back to sign-in activity
             */
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return true;
        }

        /*
        About menu
        */
        else if (i == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }

        /*
        Settings menu
         */
        else if (i == R.id.action_settings) {
            startActivity(new Intent(this, AppSettingsActivity.class));
            return true;
        }

        /*
        Profile update
         */
        else if (i == R.id.action_profile) {
            startActivity(new Intent(this, ProfileEditor.class));
            return true;
        }

        /*
        Other
         */
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Call Ambulance after User click Call Ambulance button
     */
    private void callAmbulance() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {
                    Manifest.permission.CALL_PHONE
            };
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_TO_CALL_PHONE);
            return;
        }
        makePhoneCall();
    }

    /**
     * Make a phone call by using ACTION_CALL intent
     */
    private void makePhoneCall() {
        /*
        Get phone number from user settings
         */
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String phoneNumber = sharedPref.getString(AppSettingsFragment.KEY_TBM_EMERGENCY_CONTACT, "");

        /*
        Call the ambulance
         */
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_TO_CALL_PHONE) {
            makePhoneCall();
        }
    }

    /**
     * Open post editor activity to create post. Post contain current location,
     * picture and short description of the event
     */
    private void openPostEditor() {
        Intent intent = new Intent(this, PostEditor.class);
        startActivity(intent);
    }

}
