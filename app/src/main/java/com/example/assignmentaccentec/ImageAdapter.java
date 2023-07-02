package com.example.assignmentaccentec;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.assignmentaccentec.databinding.ImageCardviewBinding;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private Context context;
    private ArrayList<ModalImage> imageArrayList;

    public ImageAdapter(Context context, ArrayList<ModalImage> imageArrayList) {
        this.context = context;
        this.imageArrayList = imageArrayList;
    }

    @NonNull
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_cardview,parent,false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ViewHolder holder, int position) {
       ModalImage modalImage = imageArrayList.get(position);
        Uri imageUri = modalImage.getImageUri();
        Glide.with(context).load(imageUri)
                .placeholder(R.drawable.baseline_image_24)
                .into(holder.binding.image);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context.getApplicationContext(),ImageViewActivity.class);
                intent.putExtra("imageUri",imageUri);
                context.startActivity(intent);
            }
        });

        holder.binding.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                modalImage.setChecked(isChecked);

            }
        });

    }

    @Override
    public int getItemCount() {
        return imageArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageCardviewBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ImageCardviewBinding.bind(itemView);
        }
    }
}
