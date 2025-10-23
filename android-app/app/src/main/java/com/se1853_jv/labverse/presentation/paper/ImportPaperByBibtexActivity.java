package com.se1853_jv.labverse.presentation.paper;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.se1853_jv.labverse.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;
import java.util.concurrent.Executors;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ImportPaperByBibtexActivity extends AppCompatActivity {
    @NonFinal
    MaterialButton button;
    @NonFinal
    ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_bibtex);
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        var uri = result.getData().getData();
                        savePdfFile(uri);
                        copyPdfInBackground(uri);
                    }
                }
        );
        bindView();
        handleImportFileEvent();
    }

    private void bindView() {
        button = findViewById(R.id.btn_choose_file);
    }

    private void handleImportFileEvent() {
        button.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf"); // mo moi loai file
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            filePickerLauncher.launch(Intent.createChooser(intent, "Choose a file"));
        });
    }

    private void displaySummaryInformationOfPdfFile() {
        MaterialCardView layout = findViewById(R.id.pdfSummaryCard);
        MaterialButton importBtn = findViewById(R.id.btn_import);
        layout.setVisibility(View.VISIBLE);
        importBtn.setClickable(true);
    }

    @Nullable
    @SuppressLint("Range")
    private File savePdfFile(Uri uri) {
        File outFile = null;
        try {
            var pdfDir = new File(getFilesDir(), "papers");
            if (!pdfDir.exists()) {
                boolean created = pdfDir.mkdirs();
                if (!created) {
                    Log.e("Save pdf file", "Không thể tạo thư mục lưu file PDF");
                    return null;
                }
            }

            Log.d("Location of pdf file", pdfDir.getAbsolutePath());

            var fileName = "document.pdf";
            var cursor = getContentResolver().query(uri, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                cursor.close();
            }

            var input = getContentResolver().openInputStream(uri);
            @SuppressLint("UnsanitizedFilenameFromContentProvider")
            var uniqueName = System.currentTimeMillis() + "_" + fileName;
            outFile = new File(pdfDir, uniqueName);
            var output = new FileOutputStream(outFile);

            var buffer = new byte[1024];
            int length;
            while ((length = Objects.requireNonNull(input).read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            input.close();
            output.close();

        } catch (Exception e) {
            Log.e("Save pdf file", Objects.requireNonNull(e.getMessage()));
        }
        return outFile;
    }

    private void copyPdfInBackground(Uri uri) {
        Executors.newSingleThreadExecutor().execute(() -> {
            var imported = savePdfFile(uri);
            runOnUiThread(() -> {
//                if (imported != null) viewPdfFileAfterImporting(imported);
                displaySummaryInformationOfPdfFile();
            });
        });
    }

    // only use for debugging
//    private void viewPdfFileAfterImporting(File importedFile) {
//        try {
//            var uri = FileProvider.getUriForFile(
//                    this,
//                    getPackageName() + ".provider",
//                    importedFile
//            );
//
//            var intent = new Intent(Intent.ACTION_VIEW);
//            intent.setDataAndType(uri, "application/pdf");
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//
//            startActivity(intent);
//        } catch (Exception e) {
//            Log.e("View pdf file", Objects.requireNonNull(e.getMessage()));
//        }
//    }
}
