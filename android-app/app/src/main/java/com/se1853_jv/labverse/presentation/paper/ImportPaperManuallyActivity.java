package com.se1853_jv.labverse.presentation.paper;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.paper.PaperApiHandler;
import com.se1853_jv.labverse.data.dto.request.UploadPdfRequest;
import com.se1853_jv.labverse.data.utils.CloudinaryStorageHelper;
import com.se1853_jv.labverse.data.utils.Connectivity;
import com.se1853_jv.labverse.data.utils.SessionManager;
import com.se1853_jv.labverse.data.utils.TestPdfGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImportPaperManuallyActivity extends AppCompatActivity {
    private static final String TAG = "ImportPaperActivity";
    
    private MaterialButton chooseFileBtn;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private LinearLayout recentUploadsContainer;
    private final List<RecentUploadItem> recentUploads = new ArrayList<>();
    
    private CloudinaryStorageHelper storageHelper;
    private PaperApiHandler paperApiHandler;
    private SessionManager sessionManager;
    private Uri selectedFileUri;
    private String selectedFileName;

    // Mock data for recent uploads
    static class RecentUploadItem {
        String fileName;
        String timeAgo;
        String fileSize;

        RecentUploadItem(String fileName, String timeAgo, String fileSize) {
            this.fileName = fileName;
            this.timeAgo = timeAgo;
            this.fileSize = fileSize;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_new_paper);

        // Mock recent uploads data
        recentUploads.add(new RecentUploadItem("Machine Learning in Healthcare.pdf", "2 hours ago", "2.4 MB"));
        recentUploads.add(new RecentUploadItem("Deep Learning Applications.pdf", "1 day ago", "1.8 MB"));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root), (v, insets) -> {
            var statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(statusBar.left, statusBar.top, statusBar.right, statusBar.bottom);
            return insets;
        });

        // Initialize helpers
        storageHelper = new CloudinaryStorageHelper();
        paperApiHandler = new PaperApiHandler(this);
        sessionManager = new SessionManager(this);
        // Cloudinary đã được khởi tạo trong LabVerseApplication
        
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            selectedFileUri = uri;
                            selectedFileName = getFileName(uri);
                            Toast.makeText(this, "Selected: " + selectedFileName, Toast.LENGTH_SHORT).show();
                            // Show dialog to enter paper metadata
                            showPaperMetadataDialog();
                        }
                    }
                }
        );

        bindViews();
        setupRecentUploads();
        handleEvents();
    }

    private void bindViews() {
        chooseFileBtn = findViewById(R.id.btn_choose_file);
        recentUploadsContainer = findViewById(R.id.recent_uploads_container);

        ImageButton backBtn = findViewById(R.id.ic_back);
        backBtn.setOnClickListener(v -> finish());

        ImageButton menuBtn = findViewById(R.id.ic_menu);
        menuBtn.setOnClickListener(v -> {
            // Handle menu click
            Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecentUploads() {
        recentUploadsContainer.removeAllViews();
        
        for (RecentUploadItem item : recentUploads) {
            MaterialCardView card = createRecentUploadCard(item);
            recentUploadsContainer.addView(card);
        }
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    private MaterialCardView createRecentUploadCard(@NonNull RecentUploadItem item) {
        MaterialCardView card = new MaterialCardView(this);
        card.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        card.setUseCompatPadding(true);
        card.setCardElevation(2);
        card.setRadius(12);
        card.setPadding(16, 16, 16, 16);

        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.HORIZONTAL);
        contentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // PDF icon
        ImageButton pdfIcon = new ImageButton(this);
        pdfIcon.setImageResource(R.drawable.ic_pdf_file);
        pdfIcon.setBackgroundResource(android.R.color.transparent);
        int iconSize = (int) (48 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(iconSize, iconSize);
        iconParams.setMargins(0, 0, 16, 0);
        pdfIcon.setLayoutParams(iconParams);
        pdfIcon.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);

        // Content text layout
        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        textLayout.setLayoutParams(textParams);

        TextView fileName = new TextView(this);
        fileName.setText(item.fileName);
        fileName.setTextSize(16);
        fileName.setTextColor(ContextCompat.getColor(this, R.color.light_black));
        fileName.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView metaInfo = new TextView(this);
        metaInfo.setText(item.timeAgo + " • " + item.fileSize);
        metaInfo.setTextSize(14);
        metaInfo.setTextColor(ContextCompat.getColor(this, R.color.gray_400));
        metaInfo.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        textLayout.addView(fileName);
        textLayout.addView(metaInfo);

        // Menu icon
        ImageButton menuIcon = new ImageButton(this);
        menuIcon.setImageResource(R.drawable.ic_more_vert_24);
        menuIcon.setBackgroundResource(android.R.color.transparent);
        int smallIconSize = (int) (24 * getResources().getDisplayMetrics().density);
        menuIcon.setLayoutParams(new LinearLayout.LayoutParams(smallIconSize, smallIconSize));
        menuIcon.setOnClickListener(v -> {
            Toast.makeText(this, "Options for " + item.fileName, Toast.LENGTH_SHORT).show();
        });

        contentLayout.addView(pdfIcon);
        contentLayout.addView(textLayout);
        contentLayout.addView(menuIcon);

        card.addView(contentLayout);
        return card;
    }

    private void handleEvents() {
        chooseFileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            filePickerLauncher.launch(Intent.createChooser(intent, "Choose PDF file"));
        });

        // Import from URL option
        MaterialCardView urlOption = findViewById(R.id.option_import_url);
        if (urlOption != null) {
            urlOption.setOnClickListener(v -> {
                Toast.makeText(this, "Import from URL", Toast.LENGTH_SHORT).show();
                // Navigate to URL import activity or show dialog
            });
        }

        // Import BibTeX option
        MaterialCardView bibtexOption = findViewById(R.id.option_import_bibtex);
        if (bibtexOption != null) {
            bibtexOption.setOnClickListener(v -> {
                Intent intent = new Intent(this, ImportPaperByBibtexActivity.class);
                startActivity(intent);
            });
        }
        
        // TEST: Long press on Choose File button để tạo test PDF
        chooseFileBtn.setOnLongClickListener(v -> {
            Uri testPdfUri = TestPdfGenerator.createTestPdf(this);
            if (testPdfUri != null) {
                selectedFileUri = testPdfUri;
                selectedFileName = "test_paper.pdf";
                Toast.makeText(this, "✅ Test PDF created! Tap 'Choose File' to use it.", Toast.LENGTH_LONG).show();
                // Tự động mở dialog để test upload
                showPaperMetadataDialog();
            } else {
                Toast.makeText(this, "❌ Failed to create test PDF", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    @NonNull
    @SuppressLint("Range")
    private String getFileName(@NonNull Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result != null ? result : "Unknown file";
    }
    
    private void showPaperMetadataDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_upload_paper_metadata, null);
        
        TextInputEditText etTitle = dialogView.findViewById(R.id.etTitle);
        TextInputEditText etAuthors = dialogView.findViewById(R.id.etAuthors);
        TextInputEditText etJournal = dialogView.findViewById(R.id.etJournal);
        TextInputEditText etYear = dialogView.findViewById(R.id.etYear);
        TextInputEditText etDoi = dialogView.findViewById(R.id.etDoi);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etDescription);
        TextInputEditText etKeywords = dialogView.findViewById(R.id.etKeywords);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        MaterialButton btnUpload = dialogView.findViewById(R.id.btnUpload);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Paper Information")
                .setView(dialogView)
                .setCancelable(false)
                .create();
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnUpload.setOnClickListener(v -> {
            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            String authors = etAuthors.getText() != null ? etAuthors.getText().toString().trim() : "";
            String journal = etJournal.getText() != null ? etJournal.getText().toString().trim() : "";
            String yearStr = etYear.getText() != null ? etYear.getText().toString().trim() : "";
            String doi = etDoi.getText() != null ? etDoi.getText().toString().trim() : "";
            String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
            String keywordsStr = etKeywords.getText() != null ? etKeywords.getText().toString().trim() : "";
            
            // Validation
            if (title.isEmpty()) {
                etTitle.setError("Title is required");
                etTitle.requestFocus();
                return;
            }
            
            if (authors.isEmpty()) {
                etAuthors.setError("Authors is required");
                etAuthors.requestFocus();
                return;
            }
            
            int year = 0;
            try {
                if (!yearStr.isEmpty()) {
                    year = Integer.parseInt(yearStr);
                }
            } catch (NumberFormatException e) {
                etYear.setError("Invalid year");
                etYear.requestFocus();
                return;
            }
            
            // Validate DOI format if provided (optional - will be auto-generated if empty)
            // DOI format: 10.xxxx/xxxxx (e.g., 10.1234/example.2024.123)
            if (!doi.isEmpty()) {
                // Pattern: (?:doi:\s*|https?://(?:dx\.)?doi\\.org/)?(10\.\d{4,9}/[-._;()/:A-Z0-9]+)
                // Simplified: starts with 10. followed by digits, then /, then alphanumeric and special chars
                String doiPattern = "^(?:doi:\\s*|https?://(?:dx\\.)?doi\\.org/)?(10\\.\\d{4,9}/[-._;()/:A-Z0-9]+)$";
                if (!doi.matches(doiPattern)) {
                    etDoi.setError("DOI format is invalid. Example: 10.1234/example.2024.123 (Leave empty to auto-generate)");
                    etDoi.requestFocus();
                    return;
                }
            }
            // If DOI is empty, backend will auto-generate a unique DOI
            
            // Parse keywords
            List<String> keywords = new ArrayList<>();
            if (!keywordsStr.isEmpty()) {
                keywords = Arrays.asList(keywordsStr.split(","));
            }
            
            dialog.dismiss();
            uploadPaper(title, authors, journal, year, doi, description, keywords, progressBar);
        });
        
        dialog.show();
    }
    
    private void uploadPaper(String title, String authors, String journal, int year, 
                            String doi, String description, List<String> keywords, ProgressBar progressBar) {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedFileUri == null) {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (progressBar != null) {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }
        
        // Step 1: Upload file to Cloudinary
        // Pass context để hỗ trợ content:// URI (Google Drive, etc.)
        storageHelper.uploadPdfFile(this, selectedFileUri, new CloudinaryStorageHelper.StorageUploadCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                Log.d(TAG, "✅ File uploaded to Cloudinary successfully!");
                Log.d(TAG, "📎 Download URL: " + downloadUrl);
                Log.d(TAG, "💡 Check Cloudinary Console → Media Library → papers/ to see the file");
                
                // Step 2: Create paper metadata via API
                UploadPdfRequest request = new UploadPdfRequest();
                request.setDataUrl(downloadUrl);
                request.setTitle(title);
                request.setAuthors(authors);
                request.setJournal(journal);
                request.setPublicationYear(year > 0 ? year : null);
                
                // DOI: If empty, backend will auto-generate a unique DOI
                // If provided, use the user's DOI (already validated above)
                if (doi.isEmpty()) {
                    request.setDoi(null); // Backend will auto-generate
                    Log.d(TAG, "DOI is empty, backend will auto-generate a unique DOI");
                } else {
                    request.setDoi(doi); // Use user-provided DOI
                }
                
                request.setDescription(description.isEmpty() ? null : description);
                request.setKeywords(keywords.isEmpty() ? null : keywords);
                request.setTags(null); // Can be added later
                
                // Get current user ID
                String userId = sessionManager.getUserId();
                Log.d(TAG, "Current userId: " + userId);
                if (userId == null || userId.isEmpty()) {
                    runOnUiThread(() -> {
                        if (progressBar != null) {
                            progressBar.setVisibility(ProgressBar.GONE);
                        }
                        Toast.makeText(ImportPaperManuallyActivity.this, 
                                "User not logged in. Please login first.", Toast.LENGTH_LONG).show();
                    });
                    return;
                }
                
                Log.d(TAG, "Uploading paper with userId: " + userId + ", DOI: " + request.getDoi());
                paperApiHandler.uploadPdf(request, userId, new ApiCallback<Object>() {
                    @Override
                    public void onSuccess(Object result) {
                        runOnUiThread(() -> {
                            if (progressBar != null) {
                                progressBar.setVisibility(ProgressBar.GONE);
                            }
                            Toast.makeText(ImportPaperManuallyActivity.this, 
                                    "Paper uploaded successfully!", Toast.LENGTH_SHORT).show();
                            // Set result to notify that paper was uploaded
                            setResult(RESULT_OK);
                            finish();
                        });
                    }
                    
                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            if (progressBar != null) {
                                progressBar.setVisibility(ProgressBar.GONE);
                            }
                            Log.e(TAG, "Error uploading paper: " + error);
                            Toast.makeText(ImportPaperManuallyActivity.this, 
                                    "Failed to upload paper: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(ProgressBar.GONE);
                    }
                    Log.e(TAG, "Error uploading file: " + error);
                    Toast.makeText(ImportPaperManuallyActivity.this, 
                            "Failed to upload file: " + error, Toast.LENGTH_LONG).show();
                });
            }
            
            @Override
            public void onProgress(int progress) {
                runOnUiThread(() -> {
                    if (progressBar != null) {
                        progressBar.setProgress(progress);
                    }
                });
            }
        });
    }
}

