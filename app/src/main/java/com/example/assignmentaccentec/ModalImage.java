package com.example.assignmentaccentec;

import android.net.Uri;

public class ModalImage {
    Uri imageUri;

    boolean checked;


    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public ModalImage(Uri imageUri,boolean checked) {
        this.imageUri = imageUri;
        this.checked = checked;

    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }
}
