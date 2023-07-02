package com.example.assignmentaccentec;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.assignmentaccentec.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.bottomNavigationView.setSelectedItemId(R.id.bottom_menu_image);
        binding.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId==R.id.bottom_menu_image){
                    loadImagesFragment();
                }
                else if (itemId==R.id.bottom_menu_pdf){
                    loadPdfFragment();
                }
                return true;
            }
        });

    }

    private void loadImagesFragment() {
        setTitle("Images");
        ImageFragment imageFragment = new ImageFragment();
       FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
       fragmentTransaction.replace(binding.frameLayout.getId(),imageFragment,"ImageFragment");
       fragmentTransaction.commit();

    }
    private void loadPdfFragment() {
        setTitle("Pdf");
        PdfFragment pdfFragment = new PdfFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.frameLayout.getId(),pdfFragment,"PdfFragment");
        fragmentTransaction.commit();
    }
}