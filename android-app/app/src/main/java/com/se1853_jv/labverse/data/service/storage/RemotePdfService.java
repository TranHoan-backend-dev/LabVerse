package com.se1853_jv.labverse.data.service.storage;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Service to download remote PDF files and cache them locally.
 * Replaces the previous FirebaseStorage-based implementation.
 */
public class RemotePdfService {

    private final OkHttpClient okHttpClient;

    public RemotePdfService() {
        this.okHttpClient = new OkHttpClient();
    }

    public void downloadPdfFromUrl(
            @NonNull String pdfUrl,
            @NonNull String paperId,
            @NonNull Context context,
            @NonNull DownloadCallback callback
    ) {
        Request request = new Request.Builder()
                .url(pdfUrl)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (!response.isSuccessful()) {
                    callback.onFailure(new IOException("Unexpected HTTP code: " + response.code()));
                    return;
                }

                ResponseBody body = response.body();
                if (body == null) {
                    callback.onFailure(new IOException("Empty response body while downloading PDF"));
                    return;
                }

                try {
                    File outputFile = writeResponseBodyToDisk(context, paperId, body);
                    callback.onSuccess(outputFile);
                } catch (IOException e) {
                    callback.onFailure(e);
                } finally {
                    body.close();
                }
            }
        });
    }

    private File writeResponseBodyToDisk(Context context, String paperId, ResponseBody body) throws IOException {
        File cacheDir = new File(context.getCacheDir(), "pdf_cache");
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            throw new IOException("Unable to create PDF cache directory");
        }

        File outputFile = new File(cacheDir, paperId + ".pdf");

        try (InputStream inputStream = body.byteStream();
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            return outputFile;
        }
    }

    public interface DownloadCallback {
        void onSuccess(File localFile);

        void onFailure(Exception e);
    }
}

