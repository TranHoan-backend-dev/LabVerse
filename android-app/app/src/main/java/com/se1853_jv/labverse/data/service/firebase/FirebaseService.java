package com.se1853_jv.labverse.data.service.firebase;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.firebase.storage.FirebaseStorage;

public class FirebaseService {
    public void uploadPdfToFirebase(Uri pdfUri, @NonNull UploadCallback callback) {
        var storageReference = FirebaseStorage.getInstance().getReference();
        var fileName = "pdfs/" + System.currentTimeMillis() + ".pdf";
        var pdfReference = storageReference.child(fileName);

        pdfReference.putFile(pdfUri)
                .addOnSuccessListener(taskSnapshot ->
                        pdfReference
                                .getDownloadUrl()
                                .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                                .addOnFailureListener(callback::onFailure)
                )
                .addOnFailureListener(callback::onFailure);
    }

    public interface UploadCallback {
        void onSuccess(String downloadUrl);

        void onFailure(Exception e);
    }
}
