package com.example.assignmentaccentec;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.assignmentaccentec.databinding.PdfViewCardviewBinding;

import java.util.ArrayList;

public class PdfViewerAdapter extends RecyclerView.Adapter<PdfViewerAdapter.ViewHolder> {

    private Context context;
    private ArrayList<ModelPdfView> pdfViewArrayList;

    public PdfViewerAdapter(Context context, ArrayList<ModelPdfView> pdfViewArrayList) {
        this.context = context;
        this.pdfViewArrayList = pdfViewArrayList;
    }

    @NonNull
    @Override
    public PdfViewerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pdf_view_cardview,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PdfViewerAdapter.ViewHolder holder, int position) {
         ModelPdfView modelPdfView = pdfViewArrayList.get(position);
         int pageNumber = position+1;
        Bitmap bitmap = modelPdfView.getBitmap();
        Glide.with(context).load(bitmap).placeholder(R.drawable.baseline_image_24).into(holder.binding.pdfIv);
        holder.binding.pageNumberTv.setText(""+pageNumber);
    }

    @Override
    public int getItemCount() {
        return pdfViewArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        PdfViewCardviewBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = PdfViewCardviewBinding.bind(itemView);

        }
    }
}
