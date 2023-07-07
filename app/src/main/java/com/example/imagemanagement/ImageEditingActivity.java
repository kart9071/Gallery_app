package com.example.imagemanagement;

import androidx.appcompat.app.AppCompatActivity;

import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;

import android.widget.Button;
import android.widget.Toast;

public class ImageEditingActivity extends AppCompatActivity {

    private ImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_editing);
        imageView = findViewById(R.id.image_editing_view);


        // Retrieve the image path from the intent
        String imagePath = getIntent().getStringExtra("imagePath");
        // Load the image into the ImageView using Glide or any other image loading library
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            Glide.with(this).load(imageFile).into(imageView);
        }
        Button setWallpaperButton = findViewById(R.id.button_set_wallpaper);
        setWallpaperButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAsWallpaper();
            }
        });


    }
    private void setAsWallpaper() {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        Drawable wallpaperDrawable = imageView.getDrawable();

        if (wallpaperDrawable != null) {
            Bitmap bitmap = ((BitmapDrawable) wallpaperDrawable).getBitmap();
            try {
                wallpaperManager.setBitmap(bitmap);
                Toast.makeText(this, "Wallpaper is set", Toast.LENGTH_SHORT).show();
                // Show a success message or perform any other action if needed
            } catch (IOException e) {
                e.printStackTrace();
                // Show an error message or perform any other action if setting wallpaper fails
            }
        }
    }


}
