package com.applin.twitly.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applin.twitly.Adapters.UserAdapter;
import com.applin.twitly.Entities.User;
import com.applin.twitly.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LikedByActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvTag;

    private UserAdapter userAdapter;
    private List<User> userList;
    private List<String> idList;

    private String postid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liked_by);

        setToolbar();

        recyclerView = findViewById(R.id.likedBy_recyclerView);
        tvTag = findViewById(R.id.likedBy_tvTag);

        userList = new ArrayList<>();
        idList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userList);

        Intent intent = getIntent();
        postid = intent.getStringExtra("POST_ID_LIKED_BY");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(userAdapter);

        getLikedBy();
    }

    private void getLikedBy() {
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference("Likes").child(postid);
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();

                if (snapshot.getChildrenCount() == 0) {
                    tvTag.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvTag.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        idList.add(snapshot1.getKey());
                    }
                    showUsers();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showUsers() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    User user = snapshot1.getValue(User.class);
                    for (String id : idList) {
                        assert user != null;
                        if (user.getId().equals(id)) {
                            userList.add(user);
                        }
                    }
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.likedByToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(v -> finish());
    }
}