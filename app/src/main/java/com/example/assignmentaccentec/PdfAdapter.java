package com.example.assignmentaccentec;

import static android.provider.Telephony.Mms.Part.FILENAME;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.assignmentaccentec.databinding.PdfCardviewBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PdfAdapter extends RecyclerView.Adapter<PdfAdapter.ViewHolder> {

    private Context context;
    private ArrayList<ModalPdf> pdfArrayList;
    private PdfRenderer pdfRenderer;
    ViewPdf viewPdf;

    public PdfAdapter(Context context, ArrayList<ModalPdf> pdfArrayList,ViewPdf viewPdf) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.viewPdf = viewPdf;
    }

    private static final String TAG = "ADAPTER_PDF_TAG";
    @NonNull
    @Override
    public PdfAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pdf_cardview,parent,false);
        return new ViewHolder(view,viewPdf);
    }

    @Override
    public void onBindViewHolder(@NonNull PdfAdapter.ViewHolder holder, int position) {
       ModalPdf modalPdf = pdfArrayList.get(position);
       String name = modalPdf.getFile().getName();
       long timestamp = modalPdf.getFile().lastModified();
       String formattedDate = DateConverter.formatTimestamp(timestamp);
       holder.binding.pdfName.setText(name);
       holder.binding.dateTv.setText(formattedDate);
       loadPdfSize(modalPdf,holder);
       loadThumbnailFromPdf(modalPdf,holder);
     holder.itemView.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             viewPdf.onPdfClick(modalPdf,position);
         }
     });
     holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
         @Override
         public boolean onLongClick(View v) {
             viewPdf.onMorePdfClick(modalPdf,position,holder);
             return false;
         }
     });

    }

    private void loadThumbnailFromPdf(ModalPdf modalPdf, ViewHolder holder) {
        Log.d(TAG, "loadThumbnailFromPdf: ");
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Bitmap thumbNailBitmap = null;
                int pageCount = 0;
                try {
                    ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(modalPdf.getFile(),ParcelFileDescriptor.MODE_READ_ONLY);
                    if (parcelFileDescriptor != null) {
                       pdfRenderer = new PdfRenderer(parcelFileDescriptor);
                    }
                    pageCount = pdfRenderer.getPageCount();
                    if (pageCount<=0){
                        Log.d(TAG, "run: No pages");

                    }
                    else {
                        PdfRenderer.Page currentPage = pdfRenderer.openPage(0);
                        thumbNailBitmap = Bitmap.createBitmap(currentPage.getWidth(),currentPage.getHeight(),Bitmap.Config.ARGB_8888);
                        currentPage.render(thumbNailBitmap,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                    }
                }catch (Exception e){
                    Log.d(TAG, "run: ",e);
                }
                Bitmap finalThumbNailBitmap = thumbNailBitmap;
                int finalPageCount = pageCount;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(context).load(finalThumbNailBitmap).fitCenter().placeholder(R.drawable.baseline_picture_as_pdf_24)
                                .into(holder.binding.thumbnailIv);
                        holder.binding.pageTv.setText(""+ finalPageCount +" pages");

                    }
                });
            }
        });
    }

    private void loadPdfSize(ModalPdf modalPdf, ViewHolder holder) {
        double bytes = modalPdf.getFile().length();
        double kb = bytes/1024;
        double mb = kb/1024;
        String size = "";
        if (mb>1){
            size = String.format("%.2f",mb)+" MB";

        }
        else if (kb>=1){
            size = String.format("%.2f",kb)+" KB";

        }
        else {
            size = String.format("%.2f",bytes)+" bytes";
        }
        holder.binding.sizeTv.setText(size);
        Log.d(TAG, "loadPdfSize: size "+size);
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        PdfCardviewBinding binding;
        public ViewHolder(@NonNull View itemView,ViewPdf viewPdf) {
            super(itemView);
            binding = PdfCardviewBinding.bind(itemView);
        }
    }
interface ViewPdf{
void onPdfClick(ModalPdf modalPdf,int position);
void onMorePdfClick(ModalPdf modalPdf,int position,PdfAdapter.ViewHolder holder);
}
}
