package com.example.imagemanagement;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> imagesList;

    public GalleryAdapter(Context context, ArrayList<String> imagesList) {
        this.context = context;
        this.imagesList = imagesList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.gallery_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File imageFile = new File(imagesList.get(position));

        if (imageFile.exists()) {
            Glide.with(context).load(imageFile).into(holder.image);
        }
    }

    @Override
    public int getItemCount() {
        return imagesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.gallery_item);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                String imagePath = imagesList.get(position);
                openImage(imagePath);
            }
        }
    }

    private void openImage(String imagePath) {
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            // Pass the image file path to the ImageEditingActivity
            Intent intent = new Intent(context, ImageEditingActivity.class);
            intent.putExtra("imagePath", imagePath);
            context.startActivity(intent);
        }
    }


}
