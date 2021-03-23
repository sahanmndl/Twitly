package com.applin.twitly.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.applin.twitly.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {

    MaterialButton goToRegister;
    TextView goToLogin;

    FirebaseUser firebaseUser;
    FirebaseAuth auth;

    @Override
    protected void onStart() {
        super.onStart();

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        if (firebaseUser != null) {
            if (firebaseUser.isEmailVerified()) {
                startActivity(new Intent(StartActivity.this, MainActivity.class));
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        goToLogin = findViewById(R.id.start_btnLogin);
        goToRegister = findViewById(R.id.start_btnRegister);

        goToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, LoginActivity.class);
            startActivity(intent);
        });
        goToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}