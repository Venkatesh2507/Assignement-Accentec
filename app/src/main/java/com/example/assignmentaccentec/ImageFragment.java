package com.example.assignmentaccentec;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.assignmentaccentec.databinding.FragmentImageBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ImageFragment extends Fragment {

    private static final String TAG = "IMAGE_LIST_TAG";

    private Context mContext;
    public FragmentImageBinding binding;

    private Uri imageUri = null;

    private ArrayList<ModalImage> imageArrayList;
    private ImageAdapter adapter;

    private ProgressDialog progressDialog;

    public ImageFragment(){

   }

    @Override
    public void onAttach(@NonNull Context context) {
       mContext = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentImageBinding.inflate(inflater);
        View view = binding.getRoot();
        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.addImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 showImageInputDialog();
            }
        });
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        loadImages();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_images,menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId==R.id.imagesConvertPdf){
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Convert to PDF")
                    .setMessage("Convert All/Selected Image ")
                    .setPositiveButton("CONVERT ALL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                              convertImagesToPdf(true);
                        }
                    })
                    .setNeutralButton("CONVERT SELECTED", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                              convertImagesToPdf(false);
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();

        }
        else if (itemId==R.id.imageDelete){
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Delete Images")
                    .setMessage("Are you sure you want to delete All/Selected Images")
                    .setPositiveButton("Delete All", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteImages(true);
                        }
                    })
                    .setNeutralButton("Delete Selected", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                             deleteImages(false);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();

        }
        return super.onOptionsItemSelected(item);


    }

    private void deleteImages(boolean deleteAll) {
        ArrayList<ModalImage> imageToDeleteList = new ArrayList<>();
        if (deleteAll) {
            imageToDeleteList = imageArrayList;

        } else {
            for (int i = 0; i < imageArrayList.size(); i++) {
                if (imageArrayList.get(i).isChecked()) {
                    imageToDeleteList.add(imageArrayList.get(i));

                }
            }
        }
        for (int i=0;i<imageToDeleteList.size();i++){

            try {
                String pathOfImageToDelete = imageToDeleteList.get(i).getImageUri().getPath();
                File file = new File(pathOfImageToDelete);
                if (file.exists()){
                    boolean isDeleted = file.delete();
                    Log.d(TAG, "deleteImages: isDeleted "+isDeleted);
                }
            

            } catch (Exception e){
                Log.d(TAG, "deleteImages: "+e.getMessage());
            }

        }
        Toast.makeText(mContext, "Deleted", Toast.LENGTH_SHORT).show();
        loadImages();
    }

    private void convertImagesToPdf(boolean convertAll){
        progressDialog.setMessage("Converting to PDF....");
        progressDialog.show();

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(new Runnable() {
            @Override
            public void run() {

                Log.d(TAG, "run: BG Work start");

                ArrayList<ModalImage> imageToPdfList = new ArrayList<>();
                if (convertAll){
                    imageToPdfList = imageArrayList;
                }
                else {
                    for (int i = 0; i < imageArrayList.size(); i++) {
                        if (imageArrayList.get(i).isChecked()){
                            imageToPdfList.add(imageArrayList.get(i));

                        }
                    }
                }
                Log.d(TAG, "run: imageToPdfListSize: "+imageToPdfList.size());
                try {
                    File root = new File(mContext.getExternalFilesDir(null),Constants.PDF_FOLDER);
                    root.mkdirs();
                    long timestamp = System.currentTimeMillis();
                    String fileName = "PDF_"+timestamp+".pdf";
                    Log.d(TAG, "run: File name "+fileName);
                    File file = new File(root,fileName);

                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    PdfDocument pdf = new PdfDocument();

                    for (int i=0;i<imageToPdfList.size();i++){
                        Uri imageToAdInPdfUri = imageToPdfList.get(i).getImageUri();

                        try {
                            Bitmap bitmap;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(mContext.getContentResolver(),imageToAdInPdfUri));
                            }
                            else {
                                bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(),imageToAdInPdfUri);
                            }
                            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888,false);


                            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), i+1).create();
                            PdfDocument.Page page = pdf.startPage(pageInfo);

                            Paint paint = new Paint();
                            paint.setColor(Color.WHITE);

                            Canvas canvas = page.getCanvas();
                            canvas.drawPaint(paint);
                            canvas.drawBitmap(bitmap,0f,0f,null);

                            pdf.finishPage(page);
                            bitmap.recycle();



                        } catch (Exception e){
                            Log.d(TAG, "run: Exception  "+e);
                            e.printStackTrace();
                        }


                    }

                    pdf.writeTo(fileOutputStream);
                    if (pdf!=null){
                        pdf.close();
                    }
                }catch (Exception e){

                    progressDialog.dismiss();
                    Log.d(TAG, "run: "+e.getMessage());
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: Converted...");
                        progressDialog.dismiss();
                        Toast.makeText(mContext, "Converted", Toast.LENGTH_SHORT).show();

                    }
                });

            }
        });

    }


    private void loadImages() {
        imageArrayList = new ArrayList<>();
        adapter = new ImageAdapter(mContext,imageArrayList);

        binding.recyclerView.setAdapter(adapter);

        File folder =new File(mContext.getExternalFilesDir(null),Constants.IMAGES_FOLDER);
        if (folder.exists()){
            Log.d(TAG, "loadImages: Folder exists");
            File[] files = folder.listFiles();
            if (files!=null){
                Log.d(TAG, "loadImages: Folder exists nad have images");
                for (File file: files){
                    Log.d(TAG, "loadImages: file name= "+file.getName());
                    Uri imageUri = Uri.fromFile(file);
                    ModalImage image = new ModalImage(imageUri,false);
                    imageArrayList.add(image);
                    adapter.notifyItemInserted(imageArrayList.size());
                }
            }
            else {
                Log.d(TAG, "loadImages: Folder exists but empty");
            }
        }
        else {
            Log.d(TAG, "loadImages: Folder do not exists");
        }


    }

    private void showImageInputDialog(){
        Log.d(TAG, "showImageInputDialog: ");
        
        PopupMenu popupMenu = new PopupMenu(mContext,binding.addImageBtn);
        popupMenu.getMenu().add(Menu.NONE,1,1,"CAMERA");
        popupMenu.getMenu().add(Menu.NONE,2,2,"GALLERY");
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == 1) {
                    Log.d(TAG, "onMenuItemClick: Camera is clicked , check if camera permissions are granted or not");
                    if (checkCameraPermissions()){
                        pickImageCamera();
                    }
                    else {
                      requestCameraPemission.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
                    }

                }
                else if (itemId==2){
                    if (checkStoragePermissions()){
                       pickImageGallery();
                    }
                    else {
                        requestCameraPemission.launch(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
                    }
                }
                return true;
            }
        });
    }
    private void saveImageToAppLevelDirectory(Uri imageToBeSaved){
        Log.d(TAG, "saveImageToAppLevelDirectory: ");

        try {
            Bitmap bitmap;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(mContext.getContentResolver(),imageToBeSaved));
            }
            else {
                bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(),imageToBeSaved);
            }
            File directory = new File(mContext.getExternalFilesDir(null),Constants.IMAGES_FOLDER);
            directory.mkdirs();

            long timeStamps = System.currentTimeMillis();
            String fileName = timeStamps+".jpg";

            File file = new File(mContext.getExternalFilesDir(null),""+Constants.IMAGES_FOLDER+"/"+fileName);
            try {
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
                fos.flush();
                fos.close();
                Toast.makeText(mContext, "Image Saved", Toast.LENGTH_SHORT).show();
                loadImages();
            }catch (Exception e){
                Toast.makeText(mContext, "Failed to save images due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            Toast.makeText(mContext, "Failed to prepare image due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void pickImageGallery(){
        Log.d(TAG, "pickImageGallery: ");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryResultLauncher.launch(intent);
    }
    private ActivityResultLauncher<Intent> galleryResultLauncher = registerForActivityResult(new
            ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {

            if (result.getResultCode()== Activity.RESULT_OK){
                Intent data = result.getData();
                 imageUri = data.getData();
                Log.d(TAG, "onActivityResult: Picked Image Gallery: "+imageUri);
                 saveImageToAppLevelDirectory(imageUri);
                 ModalImage modalImage = new ModalImage(imageUri,false);
                 imageArrayList.add(modalImage);
                 adapter.notifyItemInserted(imageArrayList.size());

            }
            else {
                Toast.makeText(mContext, "Cancelled...", Toast.LENGTH_SHORT).show();
            }
        }
    });
    private void pickImageCamera(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"TEMP_IMAGE_TITLE");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"TEMP_IMAGE_DESCRIPTION");
        imageUri = mContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        cameraActivityResultLauncher.launch(intent);

    }
    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(new
                    ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode()==Activity.RESULT_OK){
                        Log.d(TAG, "onActivityResult: Picked Image camera: "+imageUri);
                        saveImageToAppLevelDirectory(imageUri);
                    }
                    else {
                        Toast.makeText(mContext, "Cancelled...", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private boolean checkStoragePermissions(){
        Log.d(TAG, "checkStoragePermissions: ");
       boolean result = ContextCompat.checkSelfPermission(mContext,Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED;
       return result;
    }

    private ActivityResultLauncher<String> requestStoragePermissions = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    Log.d(TAG, "onActivityResult: isGranted"+isGranted);
                    if (isGranted){
                        pickImageGallery();
                    }
                    else {
                        Toast.makeText(mContext, "Permission denied", Toast.LENGTH_SHORT).show();
                    }

                }
            }
    );
    private ActivityResultLauncher<String[]> requestCameraPemission = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                     boolean areallGranted = true;
                     for (Boolean isGranted: result.values()){
                         Log.d(TAG, "onActivityResult: isGranted"+isGranted);
                         areallGranted = areallGranted && isGranted;

                     }
                     if (areallGranted){
                         Log.d(TAG, "onActivityResult: All granted eg Camera & Storage ");
                         pickImageCamera();
                     }
                     else {
                         Log.d(TAG, "onActivityResult: Camera or storage both denied....");
                         Toast.makeText(mContext, "Camera or storage both deneid", Toast.LENGTH_SHORT).show();
                     }
                }
            }
    );


    private boolean checkCameraPermissions(){
        boolean cameraResult = ContextCompat.checkSelfPermission(mContext,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED;
        boolean storageResult = ContextCompat.checkSelfPermission(mContext,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED;
        return cameraResult&&storageResult;
    }



}