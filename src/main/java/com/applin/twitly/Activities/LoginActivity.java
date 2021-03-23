package com.applin.twitly.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.applin.twitly.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText email, password;
    MaterialButton btnLogin;
    TextView txtBackToRegister, txtForgotPassword;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);
        btnLogin = findViewById(R.id.login_btnLogin);
        txtBackToRegister = findViewById(R.id.login_txtBackToRegister);
        txtForgotPassword = findViewById(R.id.login_txtForgotPassword);

        auth = FirebaseAuth.getInstance();

        txtBackToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });

        txtForgotPassword.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class)));

        btnLogin.setOnClickListener(v -> {
            String str_email = Objects.requireNonNull(email.getText()).toString();
            String str_password = Objects.requireNonNull(password.getText()).toString();

            if (TextUtils.isEmpty(str_email)) {
                email.setError("Empty");
                email.requestFocus();
            } else if (TextUtils.isEmpty(str_password)) {
                password.setError("Empty");
                password.requestFocus();
            } else if (str_password.contains(" ")) {
                password.setError("Password cannot contain space");
                password.requestFocus();
            } else if (str_password.length() < 8) {
                password.setError("Minimum 8 characters required");
                password.requestFocus();
            } else if (!isConnected()) {
                Toast.makeText(this, "NO INTERNET ACCESS!", Toast.LENGTH_SHORT).show();
            } else {
                ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setTitle("LOGGING IN");
                progressDialog.setMessage("Just a second...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                auth.signInWithEmailAndPassword(str_email, str_password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (Objects.requireNonNull(auth.getCurrentUser()).isEmailVerified()) {
                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users")
                                            .child(Objects.requireNonNull(auth.getCurrentUser()).getUid());

                                    reference.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            progressDialog.dismiss();
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            progressDialog.dismiss();
                                        }
                                    });
                                } else {
                                    progressDialog.dismiss();
                                    auth.signOut();
                                    Toast.makeText(this, "Verify your email first!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                progressDialog.dismiss();
                                FirebaseAuthException e = (FirebaseAuthException)task.getException();
                                assert e != null;
                                Toast.makeText(LoginActivity.this, "AUTHENTICATION FAILED: "+e.getMessage(), Toast.LENGTH_SHORT).show();
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