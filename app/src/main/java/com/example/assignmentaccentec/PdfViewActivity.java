package com.example.assignmentaccentec;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.example.assignmentaccentec.databinding.ActivityPdfViewBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PdfViewActivity extends AppCompatActivity {
    String PdfUri;
    ActivityPdfViewBinding binding;
    private final String TAG = "PDF_VIEW_TAG";

    private PdfViewerAdapter adapter;
    private ArrayList<ModelPdfView> pdfViewArrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        PdfUri = getIntent().getStringExtra("PDF_URI");
        Log.d(TAG, "onCreate: PdfUri" + PdfUri);
        getSupportActionBar().setTitle("Pdf Viewer");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        pdfViewArrayList = new ArrayList<>();
        adapter = new PdfViewerAdapter(this, pdfViewArrayList);
        binding.pdfView.setAdapter(adapter);
        binding.pdfView.setLayoutManager(new LinearLayoutManager(this));
        loadPdfPages();

    }

    private PdfRenderer.Page page = null;

    private void loadPdfPages() {
        Log.d(TAG, "loadPdfPages: ");

        File file = new File(Uri.parse(PdfUri).getPath());
        try {
            getSupportActionBar().setSubtitle(file.getName());

        } catch (Exception e) {
            Log.d(TAG, "loadPdfPages: ", e);
        }
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);

                    PdfRenderer pdfRenderer = new PdfRenderer(parcelFileDescriptor);
                    int pageCount = pdfRenderer.getPageCount();

                    if (pageCount <= 0) {
                        Log.d(TAG, "run: No pages in pdf file");
                    } else {
                        Log.d(TAG, "run: Have pages in pdf file");
                        for (int i = 0; i < pageCount; i++) {
                            if (page != null) {
                                page.close();
                            }
                            page = pdfRenderer.openPage(i);
                            Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                            pdfViewArrayList.add(new ModelPdfView(Uri.parse(PdfUri), (i + 1), pageCount, bitmap));

                        }
                    }
                } catch (Exception e) {

                    Log.d(TAG, "run: 01", e);
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: UI thread");
                        adapter.notifyDataSetChanged();

                    }
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}