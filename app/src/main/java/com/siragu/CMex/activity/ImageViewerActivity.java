package com.siragu.CMex.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;

import com.github.chrisbanes.photoview.PhotoView;
import com.bumptech.glide.Glide;
import com.siragu.CMex.R;

public class ImageViewerActivity extends AppCompatActivity {
    private static final String IMAGE_URL = "image_url";

    public static Intent newInstance(Context context, String imageUrl) {
        Intent intent = new Intent(context, ImageViewerActivity.class);
        intent.putExtra(IMAGE_URL, imageUrl);
        return intent;
    }

    PhotoView photoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        photoView = findViewById(R.id.photo_view);

        String imageUrl = getIntent().getStringExtra(IMAGE_URL);
        if (!TextUtils.isEmpty(imageUrl)) Glide.with(this).load(imageUrl).into(photoView);
    }
}
