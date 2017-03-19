package com.progremastudio.emergencymedicalteam;

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
import com.progremastudio.emergencymedicalteam.fragment.ChatFragment;
import com.progremastudio.emergencymedicalteam.fragment.DashboardFragment;
import com.progremastudio.emergencymedicalteam.fragment.PostFragment;

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
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        /*
        Create the adapter that will return a fragment for each section
         */
        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            private final Fragment[] mFragments = new Fragment[] {
                    new DashboardFragment(),
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
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        /*
        Add icon to dashboard tab
         */
        //mTabLayout.getTabAt(0).setCustomView(getLayoutInflater().inflate(R.layout.tab_dashboard, null));
        mTabLayout.getTabAt(0).setIcon(R.drawable.ic_directions_bus_white_48dp);

        /*
        Add icon to report tab
         */
        //mTabLayout.getTabAt(1).setCustomView(getLayoutInflater().inflate(R.layout.tab_report, null));
        mTabLayout.getTabAt(1).setIcon(R.drawable.ic_assignment_white_48dp);

        /*
        Add icon to chat tab
         */
        //mTabLayout.getTabAt(2).setCustomView(getLayoutInflater().inflate(R.layout.tab_chat, null));
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
