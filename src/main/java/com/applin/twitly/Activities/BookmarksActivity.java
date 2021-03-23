package com.applin.twitly.Activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applin.twitly.Adapters.PostAdapter;
import com.applin.twitly.Entities.Post;
import com.applin.twitly.R;
import com.google.android.material.appbar.MaterialToolbar;
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

public class BookmarksActivity extends AppCompatActivity {

    private PostAdapter postAdapter;
    private List<Post> postList;
    private List<String> bookmarksList;

    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);

        setToolbar();

        RecyclerView recyclerView = findViewById(R.id.bookmarks_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        postList = new ArrayList<>();
        bookmarksList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setAdapter(postAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        displayBookmarks();
    }

    private void setToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.bookmarksToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void displayBookmarks() {
        DatabaseReference bookmarksRef = FirebaseDatabase.getInstance().getReference("Bookmarks").child(currentUser.getUid());
        bookmarksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    bookmarksList.add(snapshot1.getKey());
                }
                getBookmarks();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getBookmarks() {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("Posts");
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Post post = snapshot1.getValue(Post.class);

                    for (String id : bookmarksList) {
                        assert post != null;
                        if (post.getPostid().equals(id)) {
                            postList.add(post);
                        }
                    }
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}