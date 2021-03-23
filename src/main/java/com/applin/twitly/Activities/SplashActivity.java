package com.applin.twitly.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.applin.twitly.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, StartActivity.class));
            finish();
        }, SPLASH_TIME_OUT);
    }
}