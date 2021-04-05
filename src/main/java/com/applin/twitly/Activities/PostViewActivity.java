package com.applin.twitly.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.applin.twitly.Adapters.CommentAdapter;
import com.applin.twitly.Entities.Comment;
import com.applin.twitly.Entities.Post;
import com.applin.twitly.Entities.User;
import com.applin.twitly.R;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
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

    private FirebaseUser currentUser;

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
        LinearLayout linearLayout = findViewById(R.id.ll1);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList, postId, userId);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent = getIntent();
        postId = intent.getStringExtra("POST_ID");
        userId = intent.getStringExtra("USER_ID");
        
        linearLayout.setOnClickListener(v -> {
            Intent intent1 = new Intent(PostViewActivity.this, LikedByActivity.class);
            intent1.putExtra("POST_ID_LIKED_BY", postId);
            startActivity(intent1);
        });

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
        setUpPopUpMenu();

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

                    ivProfilePic.setOnClickListener(v -> {
                        if (!post.getPublisher().equals(currentUser.getUid())) {
                            Intent intent = new Intent(getApplicationContext(), OthersProfileActivity.class);
                            intent.putExtra("USER_ID", post.getPublisher());
                            startActivity(intent);
                        }
                    });
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
                if (post == null) {
                    return;
                }
                if (post.getPostcontent().equals("")) {
                    tvContent.setVisibility(View.GONE);
                } else {
                    tvContent.setVisibility(View.VISIBLE);
                    tvContent.setText(post.getPostcontent());
                }

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

    private void setUpPopUpMenu() {
        if (userId.equals(currentUser.getUid())) {
            btnPopUp.setVisibility(View.VISIBLE);
            btnPopUp.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(this, v);
                popupMenu.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.popup_btnDelete:
                            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                            alertDialog.setTitle("Do you want to delete this post?");

                            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                                    (dialog, which) -> dialog.dismiss());

                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                                    (dialog, which) -> {
                                        FirebaseDatabase.getInstance().getReference("Posts")
                                                .child(post.getPostid()).removeValue();
                                        FirebaseDatabase.getInstance().getReference("Comments")
                                                .child(post.getPostid()).removeValue();
                                        FirebaseDatabase.getInstance().getReference("Likes")
                                                .child(post.getPostid()).removeValue();
                                        FirebaseDatabase.getInstance().getReference("Bookmarks")
                                                .child(post.getPostid()).removeValue();

                                        clearNotificationsOnPostDeletion();

                                        String url = post.getPostimage();
                                        if (url != null) {
                                            StorageReference imagesRef = FirebaseStorage.getInstance().getReferenceFromUrl(url);
                                            imagesRef.delete().addOnSuccessListener(aVoid -> Toast.makeText(PostViewActivity.this, "Deleted!", Toast.LENGTH_SHORT).show())
                                                    .addOnFailureListener(e -> Toast.makeText(PostViewActivity.this, "ERROR: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                        } else {
                                            Toast.makeText(PostViewActivity.this, "Deleted!", Toast.LENGTH_SHORT).show();
                                        }

                                        dialog.dismiss();
                                        finish();
                                    });

                            alertDialog.show();

                            return true;

                        default:
                            return false;
                    }
                });
                popupMenu.inflate(R.menu.itemview_popup_menu);
                popupMenu.show();
            });
        } else {
            btnPopUp.setVisibility(View.GONE);
        }
    }

    private void clearNotificationsOnPostDeletion() {
        DatabaseReference notifyRef = FirebaseDatabase.getInstance().getReference("Notifications").child(currentUser.getUid());
        notifyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if (Objects.requireNonNull(snapshot1.child("postid").getValue()).equals(postId)) {
                        snapshot1.getRef().removeValue()
                                .addOnCompleteListener(task -> {
                                    if (!task.isSuccessful()) {
                                        FirebaseAuthException exception = (FirebaseAuthException) task.getException();
                                        assert exception != null;
                                        Toast.makeText(PostViewActivity.this, "ERROR: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
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