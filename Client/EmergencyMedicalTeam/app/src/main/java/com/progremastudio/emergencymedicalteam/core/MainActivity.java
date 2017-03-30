package com.progremastudio.emergencymedicalteam.core;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.progremastudio.emergencymedicalteam.AppSharedPreferences;
import com.progremastudio.emergencymedicalteam.BaseActivity;
import com.progremastudio.emergencymedicalteam.R;
import com.progremastudio.emergencymedicalteam.authentication.AboutActivity;
import com.progremastudio.emergencymedicalteam.authentication.SignInActivity;

public class MainActivity extends BaseActivity {

    private static final String TAG = "main-activity";

    private final int PERMISSION_CALL_PHONE = 0;

    private Toolbar mToolbar;

    private TabLayout mTabLayout;

    private FragmentPagerAdapter mPagerAdapter;

    private ViewPager mViewPager;

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
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getString(R.string.heading_location));
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        /*
        Create the adapter that will return a fragment for each section
         */
        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            private final Fragment[] mFragments = new Fragment[] {
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
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    getSupportActionBar().setTitle(getString(R.string.heading_location));
                } else if(position == 1) {
                    getSupportActionBar().setTitle(getString(R.string.heading_post));
                } else if (position == 2) {
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
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.getTabAt(0).setIcon(R.drawable.ic_map_white_48dp);
        mTabLayout.getTabAt(1).setIcon(R.drawable.ic_assignment_late_white_48dp);
        mTabLayout.getTabAt(2).setIcon(R.drawable.ic_forum_white_24dp);

        /*
        Initiate floating action button
         */
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
        else if (i == R.id.action_about){

            /*
            Go to AboutActivity
             */
            startActivity(new Intent(this, AboutActivity.class));
            finish();
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
        /*
        Check Call permission access
         */
        if(!checkCallAccess()) {
            Log.d(TAG, "No access to phone call");
            return;
        }

        /*
        Make a phone call to predefined ambulance if permission is granted
         */
        makePhoneCall();
    }

    /**
     * Make a phone call by using ACTION_CALL intent
     */
    private void makePhoneCall() {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + getString(R.string.Ambulance_Phone_Number)));
        startActivity(intent);
    }

    /**
     * Check access for ACTION_CALL intent
     *
     * @return permission status. TRUE if granted. Otherwise, return FALSE
     */
    private boolean checkCallAccess() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CALL_PHONE);
            }
            return false;
        } else  {
            return true;
        }
    }

    /**
     * Open post editor activity to create post. Post contain current location,
     * picture and short description of the event
     */
    private void openPostEditor() {
        Intent intent = new Intent(this, PostEditor.class);
        intent.putExtra(PostEditor.EXTRA, PostEditor.EXTRA_DELETE_PICTURE);
        startActivity(intent);
    }

}
