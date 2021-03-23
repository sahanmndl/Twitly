package com.applin.twitly.Activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applin.twitly.Adapters.PostAdapter;
import com.applin.twitly.Entities.Post;
import com.applin.twitly.Entities.User;
import com.applin.twitly.R;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class OthersProfileActivity extends AppCompatActivity {

    private CircleImageView ivProfile;
    private TextView tvPosts, tvFollowers, tvFollowing, tvUsername, tvName, tvBio;
    private MaterialButton btnFollow;
    private ProgressDialog progressDialog;

    private List<Post> publisherPostsList;
    private PostAdapter postAdapter;
    private User user;

    private FirebaseUser currentUser;

    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_others_profile);

        MaterialToolbar toolbar = findViewById(R.id.othersProfileToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_cross);
        toolbar.setNavigationOnClickListener(v -> finish());

        ivProfile = findViewById(R.id.othersProfile_ivProfileImage);
        tvPosts = findViewById(R.id.othersProfile_tvPosts);
        tvFollowers = findViewById(R.id.othersProfile_tvFollowers);
        tvFollowing = findViewById(R.id.othersProfile_tvFollowings);
        tvUsername = findViewById(R.id.othersProfile_tvUsername);
        tvName = findViewById(R.id.othersProfile_tvName);
        tvBio = findViewById(R.id.othersProfile_tvBio);
        btnFollow = findViewById(R.id.othersProfile_btnFollow);
        RecyclerView recyclerView = findViewById(R.id.othersProfile_recyclerView);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        publisherPostsList = new ArrayList<>();
        postAdapter = new PostAdapter(this, publisherPostsList);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(postAdapter);

        Intent intent = getIntent();
        id = intent.getStringExtra("USER_ID");

        progressDialog = new ProgressDialog(this, R.style.DialogTheme);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        progressDialog.show();

        displayPublisherInfo();
        displayPublisherPosts();
        followStatus();
        setBtnFollow();
        displayPostsCount();
        displayFollowersAndFollowingCount();
    }

    private void displayPublisherInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(id);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);

                assert user != null;
                tvUsername.setText(user.getUsername());
                tvName.setText(user.getName());
                tvBio.setText(user.getBio());
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

    private void displayPublisherPosts() {
        DatabaseReference postsDatabase = FirebaseDatabase.getInstance().getReference("Posts");
        postsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                publisherPostsList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Post post = snapshot1.getValue(Post.class);
                    assert post != null;
                    if (post.getPublisher().equals(id)) {
                        publisherPostsList.add(post);
                    }
                }
                postAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void followStatus() {
        DatabaseReference followDatabase = FirebaseDatabase.getInstance().getReference("Follows")
                .child(currentUser.getUid()).child("following");
        followDatabase.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(id).exists()) {
                    btnFollow.setText("Following");
                    btnFollow.setBackgroundColor(getResources().getColor(R.color.colorDefaultBackground3));
                    btnFollow.setTextColor(getResources().getColor(R.color.accent_blue));
                    btnFollow.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#0095F6")));
                    btnFollow.setStrokeWidth(1);
                } else {
                    btnFollow.setText("Follow");
                    btnFollow.setBackgroundColor(getResources().getColor(R.color.accent_blue));
                    btnFollow.setTextColor(getResources().getColor(R.color.white));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setBtnFollow() {
        btnFollow.setOnClickListener(v -> {
            if (btnFollow.getText().toString().equals("Follow")) {
                FirebaseDatabase.getInstance().getReference("Follows")
                        .child(currentUser.getUid()).child("following").child(id).setValue(true);
                FirebaseDatabase.getInstance().getReference("Follows")
                        .child(id).child("followers").child(currentUser.getUid()).setValue(true);
            } else {
                FirebaseDatabase.getInstance().getReference("Follows")
                        .child(currentUser.getUid()).child("following").child(id).removeValue();
                FirebaseDatabase.getInstance().getReference("Follows")
                        .child(id).child("followers").child(currentUser.getUid()).removeValue();
            }
        });
    }

    private void displayPostsCount() {
        DatabaseReference postsDatabase = FirebaseDatabase.getInstance().getReference("Posts");
        postsDatabase.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Post post = snapshot1.getValue(Post.class);
                    assert post != null;
                    if (post.getPublisher().equals(id)) {
                        i++;
                    }
                }
                tvPosts.setText("" + i);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void displayFollowersAndFollowingCount() {
        DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference("Follows")
                .child(id).child("followers");
        followersRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvFollowers.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference("Follows")
                .child(id).child("following");
        followingRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvFollowing.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}