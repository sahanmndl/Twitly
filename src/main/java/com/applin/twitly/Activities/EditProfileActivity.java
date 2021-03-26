package com.applin.twitly.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.applin.twitly.Entities.User;
import com.applin.twitly.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etName, etUsername, etBio;
    private CircleImageView ivProfileImg;
    private ImageView btnSave;
    private TextView tvRemovePic;

    private FirebaseUser currentUser;
    private DatabaseReference reference;
    private StorageReference profileImgReference;

    private Uri imageUri;
    private String downloadUrl = "";
    private String str_iniUsername;
    private String str_editName, str_editUsername, str_editBio;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        setUpToolbar();

        etName = findViewById(R.id.editProfile_name);
        etUsername = findViewById(R.id.editProfile_username);
        etBio = findViewById(R.id.editProfile_bio);
        btnSave = findViewById(R.id.editProfile_btnSave);
        ivProfileImg = findViewById(R.id.editProfile_profilePic);
        TextView tvChangePic = findViewById(R.id.editProfile_changeProfilePic);
        tvRemovePic = findViewById(R.id.editProfile_removeProfilePic);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        profileImgReference = FirebaseStorage.getInstance().getReference("ProfilePics");

        displayUserInfo();

        tvChangePic.setOnClickListener(v -> CropImage.activity()
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(EditProfileActivity.this));

        btnSave.setOnClickListener((View v) -> {
            if (!isConnected()) {
                Snackbar snackbar = Snackbar.make(v, "No internet access!", Snackbar.LENGTH_LONG);
                snackbar.show();
            } else {
                updateUserInfo();
            }
        });

    }

    private void displayUserInfo() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                assert user != null;
                etName.setText(user.getName());
                etUsername.setText(user.getUsername());
                etBio.setText(user.getBio());

                str_iniUsername = user.getUsername();

                if (user.getImage().equals("default")) {
                    ivProfileImg.setImageResource(R.drawable.user);
                    //tvRemovePic.setVisibility(View.GONE);
                } else {
                    if (getApplicationContext() == null) {
                        return;
                    }
                    Glide.with(getApplicationContext()).load(user.getImage()).into(ivProfileImg);

                    //tvRemovePic.setVisibility(View.VISIBLE);
                    //tvRemovePic.setOnClickListener(v -> removeProfilePic());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateUserInfo() {
        progressDialog = new ProgressDialog(EditProfileActivity.this);
        progressDialog.setTitle("UPDATING");
        progressDialog.setMessage("Please wait while we save your changes...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        btnSave.setEnabled(false);
        str_editName = Objects.requireNonNull(etName.getText()).toString().trim();
        str_editUsername = Objects.requireNonNull(etUsername.getText()).toString().trim();
        str_editBio = Objects.requireNonNull(etBio.getText()).toString().trim();

        Query checkUsername = FirebaseDatabase.getInstance().getReference("Users")
                .orderByChild("username").equalTo(str_editUsername);
        checkUsername.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && !str_editUsername.equals(str_iniUsername)) {
                    progressDialog.dismiss();
                    btnSave.setEnabled(true);
                    etUsername.setError("Username not available");
                    etUsername.requestFocus();
                } else {
                    checkDetails();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkDetails() {
        if (TextUtils.isEmpty(str_editName)) {
            progressDialog.dismiss();
            btnSave.setEnabled(true);
            etName.setError("Empty");
            etName.requestFocus();
        } else if (TextUtils.isEmpty(str_editUsername)) {
            progressDialog.dismiss();
            btnSave.setEnabled(true);
            etUsername.setError("Empty");
            etUsername.requestFocus();
        } else if (str_editName.contains("@") || str_editName.contains("$") || str_editName.contains("#")
                || str_editName.contains("*") || str_editName.contains("&")) {
            progressDialog.dismiss();
            btnSave.setEnabled(true);
            etName.setError("Invalid Characters present");
            etName.requestFocus();
        } else if (str_editUsername.contains("@") || str_editUsername.contains("$") || str_editUsername.contains("#")
                || str_editUsername.contains("*") || str_editUsername.contains("&") || str_editUsername.contains(" ")) {
            progressDialog.dismiss();
            btnSave.setEnabled(true);
            etUsername.setError("Invalid Characters present");
            etUsername.requestFocus();
        } else if (imageUri != null) {
            File imageSize = new File(imageUri.getPath());

            try {
                Bitmap compressedImage = new Compressor(this)
                        .setMaxWidth(300)
                        .setMaxHeight(300)
                        .setQuality(75)
                        .compressToBitmap(imageSize);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                compressedImage.compress(Bitmap.CompressFormat.JPEG, 75, baos);
                byte[] finalImage = baos.toByteArray();

                StorageReference fileReference = profileImgReference.child(currentUser.getUid() + ".jpg");
                StorageTask uploadTask = fileReference.putBytes(finalImage);
                uploadTask.continueWithTask((Continuation) task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return fileReference.getDownloadUrl();
                }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                    if (task.isSuccessful()) {
                        Uri resultUri = task.getResult();
                        assert resultUri != null;
                        downloadUrl = resultUri.toString();

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("name", str_editName);
                        hashMap.put("username", str_editUsername);
                        hashMap.put("bio", str_editBio);
                        hashMap.put("image", downloadUrl);

                        reference.updateChildren(hashMap).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                progressDialog.dismiss();
                                Toast.makeText(this, "Your profile has been updated!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                progressDialog.dismiss();
                                FirebaseAuthException exception = (FirebaseAuthException) task1.getException();
                                assert exception != null;
                                Toast.makeText(EditProfileActivity.this, "ERROR: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        progressDialog.dismiss();
                        FirebaseAuthException firebaseAuthException = (FirebaseAuthException) task.getException();
                        assert firebaseAuthException != null;
                        Toast.makeText(EditProfileActivity.this, "ERROR: " + firebaseAuthException.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("name", str_editName);
            hashMap.put("username", str_editUsername);
            hashMap.put("bio", str_editBio);

            reference.updateChildren(hashMap).addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    progressDialog.dismiss();
                    finish();
                } else {
                    progressDialog.dismiss();
                    FirebaseAuthException exception = (FirebaseAuthException) task1.getException();
                    assert exception != null;
                    Toast.makeText(EditProfileActivity.this, "ERROR: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            assert result != null;
            imageUri = result.getUri();
            ivProfileImg.setImageURI(imageUri);

        } else {
            finish();
        }
    }

    /**private void removeProfilePic() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;
                if (!user.getImage().equals("default")) {
                    String url = user.getImage();
                    if (url != null) {
                        StorageReference profilePicsRef = FirebaseStorage.getInstance().getReferenceFromUrl(url);
                        profilePicsRef.delete()
                                .addOnSuccessListener(aVoid -> Toast.makeText(EditProfileActivity.this, "Post deleted!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "ERROR: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("image", "default");
                    reference.updateChildren(hashMap)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(EditProfileActivity.this, "Profile Image Removed!", Toast.LENGTH_SHORT).show();
                                    tvRemovePic.setVisibility(View.GONE);
                                } else {
                                    FirebaseAuthException exception = (FirebaseAuthException) task.getException();
                                    assert exception != null;
                                    Toast.makeText(EditProfileActivity.this, "ERROR: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }*/

    private void setUpToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.editProfile_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}