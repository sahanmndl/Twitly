package com.applin.twitly.Activities;

import android.content.Intent;
import android.os.Bundle;
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

public class FollowsListActivity extends AppCompatActivity {

    private TextView tvTitle;

    private UserAdapter userAdapter;
    private List<User> userList;
    private List<String> idList;

    private String userid, title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follows_list);

        RecyclerView recyclerView = findViewById(R.id.followsList_recyclerView);
        tvTitle = findViewById(R.id.followsList_tvTitle);

        userList = new ArrayList<>();
        idList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userList);

        Intent intent = getIntent();
        userid = intent.getStringExtra("USER_ID_FOLLOWS");
        title = intent.getStringExtra("TITLE");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(userAdapter);

        switch (title) {
            case "Followers":
                getFollowers();
                break;

            case "Following":
                getFollowing();
                break;
        }
    }

    private void getFollowers() {
        setToolbar();
        tvTitle.setText(title);
        DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference("Follows")
                .child(userid).child("followers");
        followersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    idList.add(snapshot1.getKey());
                }
                showUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollowing() {
        setToolbar();
        tvTitle.setText(title);
        DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference("Follows")
                .child(userid).child("following");
        followingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    idList.add(snapshot1.getKey());
                }
                showUsers();
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
        MaterialToolbar toolbar = findViewById(R.id.followsListToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(v -> finish());
    }
}