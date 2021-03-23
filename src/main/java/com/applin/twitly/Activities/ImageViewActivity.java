package com.applin.twitly.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.applin.twitly.R;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Objects;
import java.util.Random;

public class ImageViewActivity extends AppCompatActivity {

    RelativeLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        MaterialToolbar toolbar = findViewById(R.id.imageViewToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_white);
        toolbar.setNavigationOnClickListener(v -> finish());

        ImageView ivPicture = findViewById(R.id.imageView_ivImage);
        layout = findViewById(R.id.imageView_background);

        Intent intent = getIntent();
        String img = intent.getStringExtra("EXTRA_IMAGE");
        Glide.with(getApplicationContext()).load(img).into(ivPicture);

        setRandomBackgroundColor();
    }

    private void setRandomBackgroundColor() {
        String[] mColors = {"403A29", "232934", "401814", "403D3D", "262F40", "333140"};
        int i = new Random().nextInt(5);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setColor(Color.parseColor("#" + mColors[new Random().nextInt(5)]));
        layout.setBackground(gradientDrawable);
    }
}