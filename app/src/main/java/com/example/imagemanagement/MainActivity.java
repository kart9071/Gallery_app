package com.example.imagemanagement;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.os.Environment.MEDIA_MOUNTED;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import com.example.imagemanagement.R;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    static int PERMISSION_REQUEST_CODE = 100;
    RecyclerView recycler;
    ArrayList<String> images;
    GalleryAdapter adapter;
    GridLayoutManager manager;
    ImageButton b1;
    TextView totalimages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recycler = findViewById(R.id.gallery_recycler);
        images = new ArrayList<>();
        adapter = new GalleryAdapter(this, images);
        manager = new GridLayoutManager(this, 3);
        totalimages = findViewById(R.id.gallery_total_images);
        b1=findViewById(R.id.b1);

        recycler.setAdapter(adapter);
        recycler.setLayoutManager(manager);

        checkPermissions();
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,Gallery_editing.class);
                startActivity(intent);
            }
        });

    }

    private void checkPermissions() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            loadImages();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            boolean accepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (accepted) {
                loadImages();
            } else {
                Toast.makeText(this, "You have denied the permissions..", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadImages() {
        boolean SDCard = Environment.getExternalStorageState().equals(MEDIA_MOUNTED);
        if (SDCard) {
            final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
            final String order = MediaStore.Images.Media.DATE_TAKEN + " DESC";

            Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, order);
            int count = cursor.getCount();
            totalimages.setText("Total items: "+ count);

            for (int i=0; i<count; i++) {
                cursor.moveToPosition(i);
                int columnindex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                images.add(cursor.getString(columnindex));
            }
            recycler.getAdapter().notifyDataSetChanged();
            cursor.close();
        }
    }


}