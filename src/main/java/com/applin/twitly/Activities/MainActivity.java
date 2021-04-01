package com.applin.twitly.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.applin.twitly.Fragments.ExploreFragment;
import com.applin.twitly.Fragments.HomeFragment;
import com.applin.twitly.Fragments.NotificationsFragment;
import com.applin.twitly.Fragments.ProfileFragment;
import com.applin.twitly.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton floatingActionButton;
    BottomNavigationView bottomNavigationView;
    Fragment selectedFragment = null;
    private LinearLayout linearLayout;

    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        floatingActionButton = findViewById(R.id.fab);
        linearLayout = findViewById(R.id.layout_bottom);
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragmentContainer, new HomeFragment()).commit();

        KeyboardVisibilityEvent.setEventListener(this, isOpen -> {
            if (isOpen) {
                linearLayout.setVisibility(View.GONE);
            } else {
                linearLayout.setVisibility(View.VISIBLE);
            }
        });

        setBtnFab();
    }

    private String getPresentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void setBtnFab() {
        String postquery = currentUser.getUid() + "_" + getPresentDate();
        Query query = FirebaseDatabase.getInstance().getReference("Posts")
                .orderByChild("postquery").equalTo(postquery);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() >= 5) {
                    floatingActionButton.setOnClickListener(v -> {
                        Snackbar snackbar = Snackbar.make(v,
                                "You have already posted 5 times today. Delete one if you want to post another.", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    });
                } else {
                    floatingActionButton.setOnClickListener(v ->
                            startActivity(new Intent(MainActivity.this, ComposeActivity.class)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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