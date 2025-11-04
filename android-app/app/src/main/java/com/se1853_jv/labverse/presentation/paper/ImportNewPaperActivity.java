package com.se1853_jv.labverse.presentation.paper;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.se1853_jv.labverse.R;

import java.util.ArrayList;
import java.util.List;

public class ImportNewPaperActivity extends AppCompatActivity {
    private MaterialButton chooseFileBtn;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private LinearLayout recentUploadsContainer;
    private List<RecentUploadItem> recentUploads = new ArrayList<>();

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

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            String fileName = getFileName(uri);
                            Toast.makeText(this, "Selected: " + fileName, Toast.LENGTH_SHORT).show();
                            // Handle file upload logic here
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

    @NonNull
    private MaterialCardView createRecentUploadCard(RecentUploadItem item) {
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
        fileName.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        fileName.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView metaInfo = new TextView(this);
        metaInfo.setText(item.timeAgo + " • " + item.fileSize);
        metaInfo.setTextSize(14);
        metaInfo.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
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
        urlOption.setOnClickListener(v -> {
            Toast.makeText(this, "Import from URL", Toast.LENGTH_SHORT).show();
            // Navigate to URL import activity or show dialog
        });

        // Import BibTeX option
        MaterialCardView bibtexOption = findViewById(R.id.option_import_bibtex);
        bibtexOption.setOnClickListener(v -> {
            Intent intent = new Intent(this, ImportPaperByBibtexActivity.class);
            startActivity(intent);
        });
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
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
}

