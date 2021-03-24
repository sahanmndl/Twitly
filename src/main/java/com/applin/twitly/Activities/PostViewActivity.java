package com.applin.twitly.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.applin.twitly.Adapters.CommentAdapter;
import com.applin.twitly.Entities.Comment;
import com.applin.twitly.Entities.Post;
import com.applin.twitly.Entities.User;
import com.applin.twitly.R;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostViewActivity extends AppCompatActivity {

    private TextView tvName, tvUsername, tvContent, tvTimestamp, tvLikesCounter, tvCommentsCounter;
    private CircleImageView ivProfilePic;
    private RoundedImageView ivPostImage;
    private ImageView btnPopUp;
    private RecyclerView recyclerView;
    private ProgressDialog progressDialog;

    private User user;
    private Post post;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    private String postId, userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_view);

        setToolbar();

        tvName = findViewById(R.id.postView_name);
        tvUsername = findViewById(R.id.postView_username);
        tvContent = findViewById(R.id.postView_content);
        tvTimestamp = findViewById(R.id.postView_timing);
        tvLikesCounter = findViewById(R.id.postView_likesCounter);
        tvCommentsCounter = findViewById(R.id.postView_commentsCounter);
        ivProfilePic = findViewById(R.id.postView_ivProfileImg);
        ivPostImage = findViewById(R.id.postView_image);
        btnPopUp = findViewById(R.id.postView_btnPopUp);
        recyclerView = findViewById(R.id.postView_recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList);

        Intent intent = getIntent();
        postId = intent.getStringExtra("POST_ID");
        userId = intent.getStringExtra("USER_ID");

        progressDialog = new ProgressDialog(this, R.style.DialogTheme);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        progressDialog.show();

        initializePostView();
    }

    private void initializePostView() {
        displayPublisherInfo();
        displayPost();
        displayLikesAndCommentsCount();
        displayComments();

        progressDialog.dismiss();
    }

    private void displayPublisherInfo() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        usersRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                assert user != null;
                tvUsername.setText("@" + user.getUsername());
                tvName.setText(user.getName());
                if (user.getImage().equals("default")) {
                    ivProfilePic.setImageResource(R.drawable.user);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImage()).into(ivProfilePic);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void displayPost() {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                post = snapshot.getValue(Post.class);
                assert post != null;
                tvContent.setText(post.getPostcontent());
                tvTimestamp.setText(post.getTimestamp());
                if (post.getPostimage() == null) {
                    ivPostImage.setVisibility(View.GONE);
                } else {
                    ivPostImage.setVisibility(View.VISIBLE);
                    if (getApplicationContext() == null) {
                        return;
                    }
                    Glide.with(getApplicationContext()).load(post.getPostimage()).into(ivPostImage);

                    ivPostImage.setOnClickListener(v -> {
                        Intent intent = new Intent(getApplicationContext(), ImageViewActivity.class);
                        intent.putExtra("EXTRA_IMAGE", post.getPostimage());
                        startActivity(intent);
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void displayLikesAndCommentsCount() {
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference("Likes").child(postId);
        likesRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvLikesCounter.setText(snapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("Comments").child(postId);
        commentsRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvCommentsCounter.setText(snapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void displayComments() {
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("Comments").child(postId);
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Comment comment = snapshot1.getValue(Comment.class);
                    commentList.add(comment);
                }
                commentAdapter.notifyDataSetChanged();
                recyclerView.setAdapter(commentAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.postViewToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(v -> finish());
    }
}