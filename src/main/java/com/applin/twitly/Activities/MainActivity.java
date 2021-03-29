package com.applin.twitly.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.applin.twitly.Entities.Post;
import com.applin.twitly.Fragments.ExploreFragment;
import com.applin.twitly.Fragments.HomeFragment;
import com.applin.twitly.Fragments.NotificationsFragment;
import com.applin.twitly.Fragments.ProfileFragment;
import com.applin.twitly.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton floatingActionButton;
    BottomNavigationView bottomNavigationView;
    Fragment selectedFragment = null;

    private Post post;

    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ComposeActivity.class);
            startActivity(intent);
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragmentContainer, new HomeFragment()).commit();
    }

    /**private void setFabVisibility() {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("Posts");
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    post = snapshot1.getValue(Post.class);
                    assert post != null;
                    if (post.getPublisher().equals(currentUser.getUid())) {
                        if (post.getPostdate().equals(getPresentDate())) {
                            Toast.makeText(MainActivity.this, getPresentDate(), Toast.LENGTH_SHORT).show();
                            floatingActionButton.setEnabled(false);
                        } else {
                            floatingActionButton.setEnabled(true);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }*/

    @SuppressLint("NonConstantResourceId")
    private final BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            item -> {

                switch (item.getItemId()) {
                    case R.id.nav_home:
                        selectedFragment = new HomeFragment();
                        floatingActionButton.show();
                        break;

                    case R.id.nav_search:
                        selectedFragment = new ExploreFragment();
                        floatingActionButton.show();
                        break;

                    case R.id.nav_notifications:
                        selectedFragment = new NotificationsFragment();
                        floatingActionButton.show();
                        break;

                    case R.id.nav_profile:
                        selectedFragment = new ProfileFragment();
                        floatingActionButton.hide();
                        break;
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_fragmentContainer, selectedFragment).commit();
                }

                return true;
            };
}