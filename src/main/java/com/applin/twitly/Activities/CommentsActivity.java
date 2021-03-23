package com.applin.twitly.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applin.twitly.Adapters.CommentAdapter;
import com.applin.twitly.Entities.Comment;
import com.applin.twitly.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wafflecopter.charcounttextview.CharCountTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CommentsActivity extends AppCompatActivity {

    private TextInputEditText etComment;
    private TextView btnShare;
    private CharCountTextView tvCounter;
    private RecyclerView recyclerView;

    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    private String postid;

    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        setToolbar();

        etComment = findViewById(R.id.comments_etComment);
        btnShare = findViewById(R.id.comments_tvShare);
        tvCounter = findViewById(R.id.comments_tvCharCounter);
        recyclerView = findViewById(R.id.comments_recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList);

        Intent intent = getIntent();
        postid = intent.getStringExtra("POST_ID");

        btnShare.setOnClickListener(v -> shareComment());

        displayComments();
        charCounter();
    }

    private void shareComment() {
        if (Objects.requireNonNull(etComment.getText()).toString().equals("")) {
            Toast.makeText(this, "You can't share an empty comment", Toast.LENGTH_SHORT).show();
        } else {
            DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("Comments").child(postid);

            String strCommentId = commentsRef.push().getKey();
            String strComment = etComment.getText().toString();
            String strPublisher = currentUser.getUid();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("commentid", strCommentId);
            hashMap.put("comment", strComment);
            hashMap.put("publisher", strPublisher);
            hashMap.put("timestamp", getTimeStamp());

            assert strCommentId != null;
            commentsRef.child(strCommentId).setValue(hashMap)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            etComment.setText("");
                        } else {
                            FirebaseAuthException exception = (FirebaseAuthException) task.getException();
                            assert exception != null;
                            Toast.makeText(CommentsActivity.this, "ERROR: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void displayComments() {
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("Comments").child(postid);
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

    private String getTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a, dd.MM.yy", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void charCounter() {
        tvCounter.setEditText(etComment);
        tvCounter.setMaxCharacters(150);
        tvCounter.setCharCountChangedListener((i, b) -> btnShare.setEnabled(!b));
    }

    private void setToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.comments_toolBar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(v -> finish());
    }
}