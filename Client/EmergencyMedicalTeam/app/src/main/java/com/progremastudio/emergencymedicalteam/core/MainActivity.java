package com.progremastudio.emergencymedicalteam.core;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.progremastudio.emergencymedicalteam.AppContext;
import com.progremastudio.emergencymedicalteam.BaseActivity;
import com.progremastudio.emergencymedicalteam.R;
import com.progremastudio.emergencymedicalteam.authentication.AboutActivity;
import com.progremastudio.emergencymedicalteam.authentication.SignInActivity;

public class MainActivity extends BaseActivity {

    private static final String TAG = "main-activity";

    private Toolbar mToolbar;

    private TabLayout mTabLayout;

    private FragmentPagerAdapter mPagerAdapter;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        mTabLayout.getTabAt(0).setIcon(R.drawable.ic_directions_bus_white_48dp);
        mTabLayout.getTabAt(1).setIcon(R.drawable.ic_assignment_white_48dp);
        mTabLayout.getTabAt(2).setIcon(R.drawable.ic_tag_faces_white_48dp);

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
            AppContext.logOutCurrentUser(this);

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

}
