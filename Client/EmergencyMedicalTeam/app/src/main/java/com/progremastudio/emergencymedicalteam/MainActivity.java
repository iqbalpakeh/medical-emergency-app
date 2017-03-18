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

        // Set up home toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Create the adapter that will return a fragment for each section
        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            private final Fragment[] mFragments = new Fragment[] {
                    new DashboardFragment(),
                    new PostFragment(),
                    new ChatFragment(),
            };
            private final String[] mFragmentNames = new String[] {
                    getString(R.string.heading_dashboard),
                    getString(R.string.heading_post),
                    getString(R.string.heading_chat)
            };
            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }
            @Override
            public int getCount() {
                return mFragments.length;
            }
            @Override
            public CharSequence getPageTitle(int position) {
                return mFragmentNames[position];
            }
        };

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_logout) {

            AppContext.logOutCurrentUser(this);

            FirebaseAuth.getInstance().signOut();

            startActivity(new Intent(this, SignInActivity.class));
            finish();

            return true;

        } else if (i == R.id.action_about){

            startActivity(new Intent(this, AboutActivity.class));
            finish();

            return true;

        } else {
            return super.onOptionsItemSelected(item);
        }
    }

}
