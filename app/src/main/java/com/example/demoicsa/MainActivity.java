package com.example.demoicsa;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class MainActivity extends AppCompatActivity {
    /*
     * Number of pages (fragments) to show
     */
    private static final int numPages = 3;

    /*
     * Pager widget
     */
    private ViewPager mPager;

    /*
     * Pager adapter provides the pages to the ViewPager
     */
    private PagerAdapter pagerAdapter;

    private BottomNavigationViewEx.OnNavigationItemSelectedListener navListener =
            new BottomNavigationViewEx.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment sfragment = null;

                    switch (item.getItemId()){
                        case R.id.nav_home:
                            sfragment = new HomeFragment();
                            break;
                        case R.id.nav_cloud:
                            sfragment = new NavigationFragment();
                            break;
                        case R.id.nav_menu:
                            sfragment = new MenuFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            sfragment).commit();

                    return true;
                }

            };

    private static final String TAG = "MainActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: super & setContentView");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: start of code");
        FrameLayout frameLayout = findViewById(R.id.fragment_container);
        Log.d(TAG, "onCreate: FrameLayout fragment_container found");
        DisplayMetrics displayMetrics = new DisplayMetrics();
        int height, bottomNavHeight;
        BottomNavigationViewEx bottomNav = (BottomNavigationViewEx) findViewById(R.id.bottom_nav);
        Log.d(TAG, "onCreate: BottomNavigationEx bottom_nav found");
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        Log.d(TAG, "onCreate: BottomNavigationEx listener set");
        bottomNavHeight = bottomNav.getHeight();
        height = displayMetrics.heightPixels - bottomNavHeight;
        Log.d(TAG, "onCreate: Calculated height variable");
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        Log.d(TAG, "onCreate: HomeFragment selected");
//        frameLayout.setLayoutParams(new ConstraintLayout.LayoutParams(
//                ConstraintLayout.LayoutParams.MATCH_PARENT,
//                ConstraintLayout.LayoutParams.MATCH_PARENT
//                ));
        Log.d(TAG, "onCreate: FrameLayout params set");
        Log.d(TAG, "onCreate: end");
    }

    private void setupBottomNavigation(){
        Log.d(TAG, "setupBottomNavigation: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottom_nav);
        bottomNavigationViewEx.enableAnimation(false)
                .setTextVisibility(true);
    }
}
