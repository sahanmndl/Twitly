package com.applin.twitly.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.applin.twitly.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etName, etUsername, etPassword;
    private MaterialButton btnRegister;

    private FirebaseAuth auth;
    private DatabaseReference reference;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.register_email);
        etName = findViewById(R.id.register_name);
        etUsername = findViewById(R.id.register_username);
        etPassword = findViewById(R.id.register_password);
        btnRegister = findViewById(R.id.register_btnRegister);
        TextView txtBackToLogin = findViewById(R.id.register_txtBackToLogin);

        auth = FirebaseAuth.getInstance();

        txtBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        btnRegister.setOnClickListener(v -> {
            progressDialog = new ProgressDialog(RegisterActivity.this);
            progressDialog.setTitle("REGISTERING");
            progressDialog.setMessage("Your new account is being created, please wait...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            btnRegister.setEnabled(false);
            String str_username = Objects.requireNonNull(etUsername.getText()).toString();
            String str_name = Objects.requireNonNull(etName.getText()).toString();
            String str_email = Objects.requireNonNull(etEmail.getText()).toString();
            String str_password = Objects.requireNonNull(etPassword.getText()).toString();

            Query checkUsername = FirebaseDatabase.getInstance().getReference("Users")
                    .orderByChild("username").equalTo(str_username);
            checkUsername.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        progressDialog.dismiss();
                        btnRegister.setEnabled(true);
                        etUsername.setError("Username not available");
                        etUsername.requestFocus();
                    } else {
                        if (TextUtils.isEmpty(str_email)) {
                            progressDialog.dismiss();
                            btnRegister.setEnabled(true);
                            etEmail.setError("Empty");
                            etEmail.requestFocus();
                        } else if (TextUtils.isEmpty(str_name)) {
                            progressDialog.dismiss();
                            btnRegister.setEnabled(true);
                            etName.setError("Empty");
                            etName.requestFocus();
                        } else if (str_name.contains("@") || str_name.contains("$") || str_name.contains("#")
                                || str_name.contains("*") || str_name.contains("&")) {
                            progressDialog.dismiss();
                            btnRegister.setEnabled(true);
                            etName.setError("Invalid Characters present");
                            etName.requestFocus();
                        } else if (TextUtils.isEmpty(str_username)) {
                            progressDialog.dismiss();
                            btnRegister.setEnabled(true);
                            etUsername.setError("Empty");
                            etUsername.requestFocus();
                        } else if (str_username.contains("@") || str_username.contains("$") || str_username.contains("#")
                                || str_username.contains("*") || str_username.contains("&") || str_username.contains(" ")) {
                            progressDialog.dismiss();
                            btnRegister.setEnabled(true);
                            etUsername.setError("Invalid Characters present");
                            etUsername.requestFocus();
                        } else if (TextUtils.isEmpty(str_password)) {
                            progressDialog.dismiss();
                            btnRegister.setEnabled(true);
                            etPassword.setError("Empty");
                            etPassword.requestFocus();
                        } else if (str_password.contains(" ")) {
                            progressDialog.dismiss();
                            btnRegister.setEnabled(true);
                            etPassword.setError("Password cannot contain space");
                            etPassword.requestFocus();
                        } else if (str_password.length() < 8) {
                            progressDialog.dismiss();
                            btnRegister.setEnabled(true);
                            etPassword.setError("Minimum 8 characters required");
                            etPassword.requestFocus();
                        } else if (!isConnected()) {
                            progressDialog.dismiss();
                            btnRegister.setEnabled(true);
                            Toast.makeText(RegisterActivity.this, "No Internet Access!", Toast.LENGTH_SHORT).show();
                        } else {
                            btnRegister.setEnabled(true);
                            userRegistration(str_email, str_name, str_username, str_password);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressDialog.dismiss();
                    btnRegister.setEnabled(true);
                }
            });
        });
    }

    private void userRegistration(String email, String name, String username, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Objects.requireNonNull(auth.getCurrentUser()).sendEmailVerification().addOnCompleteListener(task12 -> {
                            if (task12.isSuccessful()) {
                                FirebaseUser firebaseUser = auth.getCurrentUser();
                                assert firebaseUser != null;
                                String userID = firebaseUser.getUid();

                                reference = FirebaseDatabase.getInstance().getReference("Users").child(userID);

                                HashMap<String, String> hashMap = new HashMap<>();
                                hashMap.put("id", userID);
                                hashMap.put("name", name);
                                hashMap.put("username", username.toLowerCase());
                                hashMap.put("bio", "");
                                hashMap.put("image", "default");
                                hashMap.put("search", name.toLowerCase());

                                reference.setValue(hashMap).addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        progressDialog.dismiss();
                                        Toast.makeText(RegisterActivity.this, "Verification link has been sent to your email!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        progressDialog.dismiss();
                                        FirebaseAuthException e = (FirebaseAuthException) task12.getException();
                                        assert e != null;
                                        Toast.makeText(RegisterActivity.this, "REGISTRATION FAILED: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                progressDialog.dismiss();
                                FirebaseAuthException f = (FirebaseAuthException) task12.getException();
                                assert f != null;
                                Toast.makeText(RegisterActivity.this, "REGISTRATION FAILED: "+f.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        progressDialog.dismiss();
                        FirebaseAuthException g = (FirebaseAuthException)task.getException();
                        assert g != null;
                        Toast.makeText(RegisterActivity.this, "REGISTRATION FAILED: "+g.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}