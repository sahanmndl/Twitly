package com.applin.twitly.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.anstrontechnologies.corehelper.AnstronCoreHelper;
import com.applin.twitly.Entities.User;
import com.applin.twitly.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.makeramen.roundedimageview.RoundedImageView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.wafflecopter.charcounttextview.CharCountTextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ComposeActivity extends AppCompatActivity {

    private MaterialButton btnPost;
    private CircleImageView ivProfile;
    private RoundedImageView ivImageAdded;
    private TextInputEditText etContent;
    private CharCountTextView tvCounter;
    private TextView tvRemove;

    private Uri imageUri;
    private String downloadUrl = "";
    private StorageReference storageReference;
    private DatabaseReference usersDatabase;
    private AnstronCoreHelper coreHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        MaterialToolbar toolbar = findViewById(R.id.compose_toolBar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_cross);
        toolbar.setNavigationOnClickListener(v -> finish());

        btnPost = findViewById(R.id.compose_btnPost);
        ivProfile = findViewById(R.id.compose_ivProfile);
        ivImageAdded = findViewById(R.id.compose_imageAdded);
        ImageView ivGallery = findViewById(R.id.compose_ivGallery);
        etContent = findViewById(R.id.compose_etContent);
        tvCounter = findViewById(R.id.compose_tvCounter);
        tvRemove = findViewById(R.id.compose_tvRemove);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
        usersDatabase = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        storageReference = FirebaseStorage.getInstance().getReference("Images");
        coreHelper = new AnstronCoreHelper(this);

        btnPost.setOnClickListener(v -> post());
        ivGallery.setOnClickListener(v -> CropImage.activity()
                .start(ComposeActivity.this));
        ivImageAdded.setOnClickListener(v -> {
            if (imageUri != null) {
                Intent intent = new Intent(getApplicationContext(), ImageViewActivity.class);
                intent.putExtra("EXTRA_IMAGE", imageUri.toString());
                startActivity(intent);
            }
        });

        charCounter();
        displayProfilePhoto();
    }

    private void post() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        if (imageUri == null && Objects.requireNonNull(etContent.getText()).toString().trim().equals("")) {
            Toast.makeText(this, "Empty!", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();

        } else if (imageUri != null) {
            File imageSize = new File(imageUri.getPath());

            try {
                Bitmap compressedImage = new Compressor(this)
                        .setMaxWidth(480)
                        .setMaxHeight(480)
                        .setQuality(75)
                        .compressToBitmap(imageSize);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                compressedImage.compress(Bitmap.CompressFormat.JPEG, 75, baos);
                byte[] finalImage = baos.toByteArray();

                StorageReference fileReference = storageReference.child(coreHelper.getFileNameFromUri(imageUri));
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

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");

                        String postid = databaseReference.push().getKey();
                        String postcontent = Objects.requireNonNull(etContent.getText()).toString().trim();
                        String publisher = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("postid", postid);
                        hashMap.put("postcontent", postcontent);
                        hashMap.put("postimage", downloadUrl);
                        hashMap.put("publisher", publisher);
                        hashMap.put("timestamp", getTimeStamp());

                        assert postid != null;
                        databaseReference.child(postid).setValue(hashMap).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                progressDialog.dismiss();
                                finish();
                            } else {
                                progressDialog.dismiss();
                                FirebaseAuthException exception = (FirebaseAuthException) task1.getException();
                                assert exception != null;
                                Toast.makeText(ComposeActivity.this, "ERROR: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        progressDialog.dismiss();
                        FirebaseAuthException firebaseAuthException = (FirebaseAuthException) task.getException();
                        assert firebaseAuthException != null;
                        Toast.makeText(ComposeActivity.this, "ERROR: " + firebaseAuthException.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

            String postid = reference.push().getKey();
            String postcontent = Objects.requireNonNull(etContent.getText()).toString().trim();
            String publisher = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("postid", postid);
            hashMap.put("postcontent", postcontent);
            hashMap.put("publisher", publisher);
            hashMap.put("timestamp", getTimeStamp());

            assert postid != null;
            reference.child(postid).setValue(hashMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    startActivity(new Intent(ComposeActivity.this, MainActivity.class));
                    finish();
                } else {
                    progressDialog.dismiss();
                    FirebaseAuthException exception = (FirebaseAuthException) task.getException();
                    assert exception != null;
                    Toast.makeText(ComposeActivity.this, "ERROR: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String getTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a, dd.MM.yy", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void charCounter() {
        tvCounter.setEditText(etContent);
        tvCounter.setMaxCharacters(300);
        tvCounter.setCharCountChangedListener((i, b) -> btnPost.setEnabled(!b));
    }

    private void displayProfilePhoto() {
        usersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                assert user != null;
                if (user.getImage().equals("default")) {
                    ivProfile.setImageResource(R.drawable.user);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImage()).into(ivProfile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            assert result != null;
            imageUri = result.getUri();
            ivImageAdded.setVisibility(View.VISIBLE);
            tvRemove.setVisibility(View.VISIBLE);
            ivImageAdded.setImageURI(imageUri);

            tvRemove.setOnClickListener(v -> {
                imageUri = null;
                ivImageAdded.setVisibility(View.GONE);
                tvRemove.setVisibility(View.GONE);
            });

        } else {
            ivImageAdded.setVisibility(View.GONE);
            tvRemove.setVisibility(View.GONE);
        }
    }
}