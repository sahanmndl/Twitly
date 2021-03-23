package com.applin.twitly.Fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applin.twitly.Activities.EditProfileActivity;
import com.applin.twitly.Activities.SettingsActivity;
import com.applin.twitly.Adapters.PostAdapter;
import com.applin.twitly.Entities.Post;
import com.applin.twitly.Entities.User;
import com.applin.twitly.R;
import com.bumptech.glide.Glide;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private CircleImageView ivProfile;
    private TextView tvPosts, tvFollowers, tvFollowing, tvUsername, tvName, tvBio;

    private FirebaseUser currentUser;

    private List<Post> userPostsList;
    private PostAdapter postAdapter;

    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        MaterialButton btnEditProfile = view.findViewById(R.id.profile_btnEditProfile);
        btnEditProfile.setOnClickListener(v -> startActivity(new Intent(getContext(), EditProfileActivity.class)));

        ivProfile = view.findViewById(R.id.profile_ivProfileImage);
        tvPosts = view.findViewById(R.id.profile_tvPosts);
        tvFollowers = view.findViewById(R.id.profile_tvFollowers);
        tvFollowing = view.findViewById(R.id.profile_tvFollowings);
        tvUsername = view.findViewById(R.id.profile_tvUsername);
        tvName = view.findViewById(R.id.profile_tvName);
        tvBio = view.findViewById(R.id.profile_tvBio);
        ImageView btnMenu = view.findViewById(R.id.profile_btnMenu);
        RecyclerView recyclerView = view.findViewById(R.id.profile_recyclerView);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        userPostsList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), userPostsList);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(postAdapter);

        /**tvFollowers.setOnClickListener(v -> {
            if (getActivity() == null) {
                return;
            }
            Intent intent = new Intent(getActivity(), FollowersListActivity.class);
            startActivity(intent);
        });*/

        btnMenu.setOnClickListener(v -> startActivity(new Intent(getActivity(), SettingsActivity.class)));

        progressDialog = new ProgressDialog(getContext(), R.style.DialogTheme);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        progressDialog.show();

        showUserInfo();
        showUserPosts();
        showFollowersAndFollowingCount();
        showPostsCount();

        return view;
    }

    private void showUserInfo() {
        assert currentUser != null;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"UseRequireInsteadOfGet", "SetTextI18n"})
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (getContext() == null) {
                    return;
                }

                User user = dataSnapshot.getValue(User.class);

                assert user != null;
                tvName.setText(user.getName());
                tvUsername.setText(user.getUsername());
                tvBio.setText(user.getBio());

                if (user.getImage().equals("default")) {
                    ivProfile.setImageResource(R.drawable.user);
                } else {
                    Glide.with(getContext()).load(user.getImage()).into(ivProfile);
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showUserPosts() {
        assert currentUser != null;
        DatabaseReference postsDatabase = FirebaseDatabase.getInstance().getReference("Posts");
        postsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userPostsList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Post post = snapshot1.getValue(Post.class);
                    assert post != null;
                    if (post.getPublisher().equals(currentUser.getUid())) {
                        userPostsList.add(post);
                    }
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showPostsCount() {
        assert currentUser != null;
        DatabaseReference postsDatabase = FirebaseDatabase.getInstance().getReference("Posts");
        postsDatabase.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Post post = snapshot1.getValue(Post.class);
                    assert post != null;
                    if (post.getPublisher().equals(currentUser.getUid())) {
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

    private void showFollowersAndFollowingCount() {
        assert currentUser != null;
        DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference("Follows")
                .child(currentUser.getUid()).child("followers");
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
                .child(currentUser.getUid()).child("following");
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