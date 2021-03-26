package com.applin.twitly.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.applin.twitly.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private SwitchMaterial switchToggleTheme;

    //public static final String THEME_PREFS = "nightModePrefs";
    //public static final String IS_NIGHT_MODE = "isNightMode";
    //private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setToolbar();

        LinearLayout layoutBookmarks = findViewById(R.id.settings_bookmarksLayout);
        LinearLayout layoutLogout = findViewById(R.id.settings_logoutLayout);
        switchToggleTheme = findViewById(R.id.settings_themeSwitch);

        //sharedPreferences = getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE);

        layoutBookmarks.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, BookmarksActivity.class)));
        layoutLogout.setOnClickListener(v -> userLogout());
    }

    private void setToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.settingsToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void userLogout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(SettingsActivity.this, StartActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**private void toggleThemes() {
        switchToggleTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                saveNightModeState(true);
                recreate();
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                saveNightModeState(false);
                recreate();
            }
        });
    }*/

    /**private void saveNightModeState(boolean b) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_NIGHT_MODE, b);
        editor.apply();
    }

    private void checkNightModeActivated() {
        if (sharedPreferences.getBoolean(IS_NIGHT_MODE, false)) {
            switchToggleTheme.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            switchToggleTheme.setChecked(false);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }*/
}