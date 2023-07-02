package com.example.assignmentaccentec;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.assignmentaccentec.databinding.DialogRenameBinding;
import com.example.assignmentaccentec.databinding.FragmentPdfBinding;

import java.io.File;
import java.util.ArrayList;

public class PdfFragment extends Fragment implements PdfAdapter.ViewPdf{

    private Context mContext;
    private ArrayList<ModalPdf> pdfArrayList;
    private PdfAdapter adapter;
    private FragmentPdfBinding binding;
    private static final String TAG="PDF_LIST_TAG";

    public PdfFragment(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPdfBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        mContext = context;
        super.onAttach(context);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadPdfDocuments();
    }

    private void loadPdfDocuments() {
        pdfArrayList = new ArrayList<>();
        adapter = new PdfAdapter(mContext, pdfArrayList,this);

        binding.recyclerView.setAdapter(adapter);
        File folder = new File(mContext.getExternalFilesDir(null),Constants.PDF_FOLDER);
        if (folder.exists()){
            File[] files = folder.listFiles();
            Log.d(TAG, "loadPdfDocuments: ");
            for (File fileEntry: files){
                Log.d(TAG, "loadPdfDocuments: "+fileEntry.getName());
                Uri uri = Uri.fromFile(fileEntry);
                ModalPdf modalPdf = new ModalPdf(fileEntry,uri);
                pdfArrayList.add(modalPdf);
                adapter.notifyItemInserted(pdfArrayList.size());
            }
        }
    }

    @Override
    public void onPdfClick(ModalPdf modalPdf, int position) {
        Intent intent = new Intent(mContext, PdfViewActivity.class);
        Log.d(TAG, "onPdfClick: "+modalPdf.getUri());
        intent.putExtra("PDF_URI",""+modalPdf.getUri());
        startActivity(intent);
    }

    @Override
    public void onMorePdfClick(ModalPdf modalPdf, int position,PdfAdapter.ViewHolder holder) {
        PopupMenu popupMenu = new PopupMenu(mContext,holder.binding.thumbnailLayout);

        popupMenu.getMenu().add(Menu.NONE,0,0,"Rename");
        popupMenu.getMenu().add(Menu.NONE,1,1,"Delete");
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId==0){
                    pdfRename(modalPdf);
                }
                if (itemId==1) {
                    pdfDelete(modalPdf);
                }
                return true;
            }
        });

    }

    private void pdfDelete(ModalPdf modalPdf) {
        Log.d(TAG, "pdfDelete: ");
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Delete File")
                .setMessage("Are you sure you want to delete this file")
                .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            modalPdf.getFile().delete();
                            Toast.makeText(mContext, "File deleted successfully", Toast.LENGTH_SHORT).show();
                            loadPdfDocuments();

                        }catch (Exception e){
                            Log.d(TAG, " pdfDelete onClick ");
                            Toast.makeText(mContext, "Failed to delete due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void pdfRename(ModalPdf modalPdf) {
        DialogRenameBinding renameBinding = DialogRenameBinding.inflate(getLayoutInflater());
        renameBinding.getRoot();

        String previousName = ""+modalPdf.getFile().getName();

        Log.d(TAG, "pdfRename: "+previousName);
        renameBinding.renmaeEt.setText(previousName);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(renameBinding.getRoot());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        renameBinding.renameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = renameBinding.renmaeEt.getText().toString();
                Log.d(TAG, "onClick: newName "+newName);
                if (newName.isEmpty()){
                    Toast.makeText(mContext, "Enter a new name", Toast.LENGTH_SHORT).show();

                }
                else {
                    try {

                        File newFile = new File(mContext.getExternalFilesDir(null),Constants.PDF_FOLDER+"/"+newName+".pdf");

                        modalPdf.getFile().renameTo(newFile);

                        Toast.makeText(mContext, "Renamed Successfully", Toast.LENGTH_SHORT).show();

                        loadPdfDocuments();
                    } catch (Exception e){
                        Log.d(TAG, "onClick: rename onClick: ",e);
                        Toast.makeText(mContext, "Failed rename due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    alertDialog.dismiss();
                }
            }
        });
    }
}