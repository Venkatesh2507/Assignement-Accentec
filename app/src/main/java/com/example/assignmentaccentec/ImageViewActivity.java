package com.example.assignmentaccentec;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.assignmentaccentec.databinding.ActivityImageViewBinding;

public class ImageViewActivity extends AppCompatActivity {
    ActivityImageViewBinding binding;
    private String image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImageViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("ImageView");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        image = getIntent().getStringExtra("imageUri");



        Glide.with(this).load(image).placeholder(R.drawable.baseline_image_24).into(binding.imageView);



    }

    @Override
    public boolean onSupportNavigateUp() {
        return super.onSupportNavigateUp();

    }
}