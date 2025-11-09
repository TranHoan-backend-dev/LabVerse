package com.se1853_jv.labverse.data.service.firebase;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FirebaseService {
    private static final String TAG = "FirebaseService";
    
    /**
     * Upload PDF to Firebase Storage
     */
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

    /**
     * Download PDF from Firebase Storage URL and cache locally
     * @param pdfUrl Firebase Storage download URL (from dataUrl in PaperResearch)
     * @param paperId Paper ID to use as cache filename
     * @param context Context for file operations
     * @param callback Callback with local file path
     */
    public void downloadPdfFromFirebase(
            @NonNull String pdfUrl,
            @NonNull String paperId,
            @NonNull Context context,
            @NonNull DownloadCallback callback
    ) {
        // Check if PDF is already cached locally
        File cachedFile = getCachedPdfFile(context, paperId);
        if (cachedFile.exists() && cachedFile.length() > 0) {
            Log.d(TAG, "PDF already cached: " + cachedFile.getAbsolutePath());
            callback.onSuccess(cachedFile);
            return;
        }

        // Download from Firebase Storage URL
        Log.d(TAG, "Downloading PDF from: " + pdfUrl);
        
        // Check if URL is a Firebase Storage URL
        if (pdfUrl.contains("firebasestorage.googleapis.com") || pdfUrl.contains("firebase")) {
            // Use Firebase Storage SDK
            downloadFromFirebaseStorage(pdfUrl, paperId, context, callback);
        } else {
            // Use HTTP download for direct URLs
            downloadViaHttp(pdfUrl, paperId, context, callback);
        }
    }

    /**
     * Download PDF using Firebase Storage SDK
     */
    private void downloadFromFirebaseStorage(
            String pdfUrl,
            String paperId,
            Context context,
            DownloadCallback callback
    ) {
        try {
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
            File localFile = getCachedPdfFile(context, paperId);
            
            storageRef.getFile(localFile)
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d(TAG, "PDF downloaded successfully: " + localFile.getAbsolutePath());
                        callback.onSuccess(localFile);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error downloading PDF from Firebase Storage", e);
                        callback.onFailure(e);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error parsing Firebase Storage URL", e);
            // Fallback to HTTP download
            downloadViaHttp(pdfUrl, paperId, context, callback);
        }
    }

    /**
     * Download PDF via HTTP (for direct URLs)
     */
    private void downloadViaHttp(
            String pdfUrl,
            String paperId,
            Context context,
            DownloadCallback callback
    ) {
        new Thread(() -> {
            try {
                URL url = new URL(pdfUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    callback.onFailure(new Exception("Server returned HTTP " + connection.getResponseCode()));
                    return;
                }

                File localFile = getCachedPdfFile(context, paperId);
                FileOutputStream outputStream = new FileOutputStream(localFile);
                InputStream inputStream = connection.getInputStream();

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                inputStream.close();
                connection.disconnect();

                Log.d(TAG, "PDF downloaded via HTTP: " + localFile.getAbsolutePath());
                callback.onSuccess(localFile);

            } catch (Exception e) {
                Log.e(TAG, "Error downloading PDF via HTTP", e);
                callback.onFailure(e);
            }
        }).start();
    }

    /**
     * Get cached PDF file path for a paper
     */
    private File getCachedPdfFile(Context context, String paperId) {
        File cacheDir = new File(context.getCacheDir(), "pdfs");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return new File(cacheDir, paperId + ".pdf");
    }

    /**
     * Clear cached PDF for a paper
     */
    public void clearCachedPdf(Context context, String paperId) {
        File cachedFile = getCachedPdfFile(context, paperId);
        if (cachedFile.exists()) {
            cachedFile.delete();
            Log.d(TAG, "Cleared cached PDF: " + paperId);
        }
    }

    /**
     * Callback for PDF download
     */
    public interface DownloadCallback {
        void onSuccess(File localFile);
        void onFailure(Exception e);
    }

}
