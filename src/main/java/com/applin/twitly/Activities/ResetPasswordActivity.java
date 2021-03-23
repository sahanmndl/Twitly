package com.applin.twitly.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.widget.Toast;

import com.applin.twitly.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ResetPasswordActivity extends AppCompatActivity {

    TextInputEditText resetEmail;
    MaterialButton btnResetPassword;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        resetEmail = findViewById(R.id.resetP_email);
        btnResetPassword = findViewById(R.id.resetP_btnReset);

        firebaseAuth = FirebaseAuth.getInstance();

        btnResetPassword.setOnClickListener(v -> {
            String str_resetEmail = Objects.requireNonNull(resetEmail.getText()).toString();

            if (str_resetEmail.equals("")) {
                resetEmail.setError("Empty");
                resetEmail.requestFocus();
            } else if (!isConnected()) {
                Toast.makeText(this, "NO INTERNET ACCESS!", Toast.LENGTH_SHORT).show();
            } else {
                firebaseAuth.sendPasswordResetEmail(str_resetEmail).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ResetPasswordActivity.this, "Please check your email", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                    } else {
                        String e = Objects.requireNonNull(task.getException()).getMessage();
                        Toast.makeText(ResetPasswordActivity.this, "ERROR: " + e, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}