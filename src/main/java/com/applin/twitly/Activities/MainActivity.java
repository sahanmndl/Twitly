package com.applin.twitly.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.applin.twitly.Fragments.ExploreFragment;
import com.applin.twitly.Fragments.HomeFragment;
import com.applin.twitly.Fragments.NotificationsFragment;
import com.applin.twitly.Fragments.ProfileFragment;
import com.applin.twitly.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton floatingActionButton;
    BottomNavigationView bottomNavigationView;
    Fragment selectedFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ComposeActivity.class);
            startActivity(intent);
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragmentContainer, new HomeFragment()).commit();
    }

    @SuppressLint("NonConstantResourceId")
    private final BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            item -> {

                switch (item.getItemId()) {
                    case R.id.nav_home:
                        selectedFragment = new HomeFragment();
                        floatingActionButton.show();
                        break;

                    case R.id.nav_search:
                        selectedFragment = new ExploreFragment();
                        floatingActionButton.show();
                        break;

                    case R.id.nav_notifications:
                        selectedFragment = new NotificationsFragment();
                        floatingActionButton.show();
                        break;

                    case R.id.nav_profile:
                        selectedFragment = new ProfileFragment();
                        floatingActionButton.hide();
                        break;
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_fragmentContainer, selectedFragment).commit();
                }

                return true;
            };
}