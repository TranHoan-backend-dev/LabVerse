package com.se1853_jv.labverse.presentation.paper.fragments;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.paper.CrossRefApiHandler;
import com.se1853_jv.labverse.data.api.paper.PaperApiHandler;
import com.se1853_jv.labverse.data.dto.request.UploadPdfRequest;
import com.se1853_jv.labverse.data.service.cloudinary.CloudinaryService;
import com.se1853_jv.labverse.data.service.cloudinary.CloudinaryService.UploadCallback;
import com.se1853_jv.labverse.data.service.unpaywall.UnpaywallService;
import com.se1853_jv.labverse.data.utils.ParseFileUtils;
import com.se1853_jv.labverse.data.utils.SessionManager;
import com.se1853_jv.labverse.domain.infrastructure.BibEntry;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ImportBibtexFragment extends Fragment {
    MaterialButton chooseFileBtn, importBtn, cancelBtn;
    ActivityResultLauncher<Intent> filePickerLauncher;
    List<BibEntry> entries;
    final List<BibEntry> selectedEntries; // Entries được chọn để import
    final CloudinaryService cloudinaryService;
    final UnpaywallService unpaywallService;
    final String TAG_NAME = "ImportBibtexFragment";
    View view;
    PaperApiHandler paperApiHandler;
    SessionManager sessionManager;

    public ImportBibtexFragment() {
        this.entries = new ArrayList<>();
        this.selectedEntries = new ArrayList<>();
        this.cloudinaryService = new CloudinaryService();
        this.unpaywallService = new UnpaywallService();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_import_bibtex, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        var abstractFileLocationInDevice = result.getData().getData(); // dia chi truu tuong cua du lieu
                        var bibtexFileContent = readBibFile(abstractFileLocationInDevice);
                        entries = ParseFileUtils.parseBibEntries(bibtexFileContent);
                        requireActivity().runOnUiThread(() -> displayPreview(view));
                    }
                }
        );
        this.view = view;

        // Initialize API handlers
        paperApiHandler = new com.se1853_jv.labverse.data.api.paper.PaperApiHandler(requireContext());
        sessionManager = new com.se1853_jv.labverse.data.utils.SessionManager(requireContext());

        bindView();
        openFilesCategory();
        handleImportEvent();
        handleCancelEvent();
    }

    private void bindView() {
        chooseFileBtn = view.findViewById(R.id.btn_choose_file);
        importBtn = view.findViewById(R.id.btn_import);
        cancelBtn = view.findViewById(R.id.btn_cancel);
    }

    private void handleCancelEvent() {
        if (cancelBtn != null) {
            cancelBtn.setOnClickListener(v -> {
                requireActivity().finish();
            });
        }
    }

    private void openFilesCategory() {
        chooseFileBtn.setOnClickListener(v -> {
            var intent = new Intent(Intent.ACTION_GET_CONTENT);

            intent.setType("*/*"); // mo moi loai file
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            filePickerLauncher.launch(Intent.createChooser(intent, "Choose a file"));
        });
    }

    // <editor-fold> desc="read file module"
    @NonNull
    private String readBibFile(Uri uri) {
        var builder = new StringBuilder();

        // anh xa dang dia chi vat ly roi doc file do
        // content resolver xac dinh provider can thiet de xu ly (media, downloads,...)
        try (var inputStream = requireActivity().getContentResolver().openInputStream(uri);
             var reader = new java.io.BufferedReader(new java.io.InputStreamReader(Objects.requireNonNull(inputStream)))) {

            String line;
            var lineCount = 0;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');

                if (++lineCount > 50000) {
                    Log.w(TAG_NAME, "File quá lớn, dừng đọc sớm để tránh tràn bộ nhớ");
                    break;
                }
            }

        } catch (Exception e) {
            Log.e(TAG_NAME, "Error reading file: " + e.getMessage(), e);
            return "";
        }

        return builder.toString();
    }

    @SuppressLint("SetTextI18n")
    private void displayPreview(@NonNull View view) {
        LinearLayout previewContainer = view.findViewById(R.id.pdfSummaryWrapper);
        previewContainer.removeAllViews();

        selectedEntries.clear(); // Reset selected entries

        for (var e : entries) {
            var card = buildCardView(view);
            card.setTag(e); // Store BibEntry in card tag for later retrieval

            var body = buildParentForBibTexContent(view);
            body.addView(buildIcon(view));

            var mainContent = new LinearLayout(view.getContext());
            mainContent.setOrientation(LinearLayout.VERTICAL);
            mainContent.setPadding(16, 0, 0, 0);

            mainContent.addView(buildTitle(e, view));
            mainContent.addView(buildAuthorsAndYear(e, view));

            // Add journal/source if available
            if (e.getSource() != null && !e.getSource().isEmpty()) {
                mainContent.addView(buildJournal(e, view));
            }

            body.addView(mainContent);

            // Bọc nội dung, checkbox và nút xóa trong FrameLayout
            var wrapper = new FrameLayout(view.getContext());
            wrapper.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            ));
            wrapper.addView(body);
            wrapper.addView(buildCheckBox(e, view)); // Add checkbox
            wrapper.addView(buildDeleteBadge(card, view));

            card.addView(wrapper);
            previewContainer.addView(card);
        }

        importBtn.setClickable(true);
        importBtn.setTextColor(ContextCompat.getColor(view.getContext(), R.color.white));
        importBtn.setEnabled(!selectedEntries.isEmpty());
        importBtn.bringToFront();
        importBtn.setElevation(10);

        updateImportButtonText();
    }

    @NonNull
    private TextView buildJournal(@NonNull BibEntry e, @NonNull View view) {
        var journal = new TextView(view.getContext());
        journal.setText(e.getSource());
        journal.setTextSize(12);
        journal.setTextColor(android.graphics.Color.GRAY);
        journal.setPadding(0, 4, 0, 0);
        return journal;
    }

    @NonNull
    private android.widget.CheckBox buildCheckBox(@NonNull BibEntry entry, @NonNull View view) {
        var checkBox = new android.widget.CheckBox(view.getContext());
        checkBox.setChecked(true); // Default: all selected
        selectedEntries.add(entry); // Add to selected list

        var params = new FrameLayout.LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics())
        );
        params.gravity = Gravity.START | Gravity.TOP;
        params.setMargins(8, 8, 0, 0);
        checkBox.setLayoutParams(params);

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedEntries.contains(entry)) {
                    selectedEntries.add(entry);
                }
            } else {
                selectedEntries.remove(entry);
            }
            updateImportButtonText();
        });

        return checkBox;
    }

    @SuppressLint("SetTextI18n")
    private void updateImportButtonText() {
        if (importBtn != null) {
            int count = selectedEntries.size();
            if (count > 0) {
                importBtn.setEnabled(true);
                importBtn.setText(getString(R.string.import_selected) + " (" + count + ")");
                importBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#9333EA"))); // Purple
            } else {
                importBtn.setEnabled(false);
                importBtn.setText(getString(R.string.import_selected));
                importBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#D1D5DB"))); // Gray
            }
        }
    }

    @NonNull
    private TextView buildTitle(@NonNull BibEntry e, @NonNull View view) {
        var title = new TextView(view.getContext());
        title.setText(e.getTitle());
        title.setTextSize(16);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        return title;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    private TextView buildAuthorsAndYear(@NonNull BibEntry e, @NonNull View view) {
        var authorsAndYear = new TextView(view.getContext());
        authorsAndYear.setText(e.getAuthor() + " (" + e.getYear() + ")");
        authorsAndYear.setTextSize(14);
        return authorsAndYear;
    }

    @NonNull
    private ImageView buildIcon(@NonNull View view) {
        var icon = new ImageView(view.getContext());
        var width = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
        var height = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());
        icon.setLayoutParams(new LinearLayout.LayoutParams(width, height));
//        icon.setImageResource(R.drawable.ic_file);
        icon.setImageResource(R.drawable.ic_pdf_file);
        icon.setContentDescription(getString(R.string.pdf_file));
        return icon;
    }

    /**
     * Tạo layout bao ngoài cho các card view
     *
     * @return LinearLayout
     */
    @NonNull
    private LinearLayout buildParentForBibTexContent(@NonNull View view) {
        var body = new LinearLayout(view.getContext());
        body.setOrientation(LinearLayout.HORIZONTAL);
        body.setPadding(16, 16, 16, 16);
        body.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        return body;
    }

    /**
     * Tạo card view cho 1 citation
     *
     * @return MaterialCardView
     */
    @NonNull
    private MaterialCardView buildCardView(@NonNull View view) {
        var card = new MaterialCardView(view.getContext());
        card.setId(View.generateViewId());
        card.setContentPadding(16, 16, 16, 16);
        card.setUseCompatPadding(true);
        card.setLayoutParams(new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        ));
        return card;
    }

    /**
     * Tạo nút badge để xóa 1 card
     *
     * @param card thẻ card cha
     * @return 1 view là ImageButton
     */
    @NonNull
    private ImageButton buildDeleteBadge(@NonNull MaterialCardView card, @NonNull View view) {
        var btn = new ImageButton(view.getContext());
        btn.setImageResource(R.drawable.ic_close_24);
        btn.setBackgroundResource(R.color.white);

        var params = new FrameLayout.LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics())
        );
        params.gravity = Gravity.END | Gravity.TOP;
        params.setMargins(0, 8, 8, 0);
        btn.setLayoutParams(params);

        btn.setOnClickListener(v -> {
            ((ViewGroup) card.getParent()).removeView(card);
            Toast.makeText(view.getContext(), "Đã xoá thẻ", Toast.LENGTH_SHORT).show();
        });

        return btn;
    }
    // </editor-fold>

    // <editor-fold> desc="import file module"
    private void handleImportEvent() {
        importBtn.setOnClickListener(v -> {
            if (selectedEntries == null || selectedEntries.isEmpty()) {
                Toast.makeText(view.getContext(), "Vui lòng chọn ít nhất một paper để import", Toast.LENGTH_SHORT).show();
                return;
            }

            // Disable button during import
            importBtn.setEnabled(false);
            importBtn.setText("Đang import...");

            // Import papers in background
            Executors.newSingleThreadExecutor().execute(() -> {
                int successCount = 0;
                int failCount = 0;

                for (BibEntry entry : selectedEntries) {
                    try {
                        importBibEntry(entry);
                        successCount++;
                    } catch (Exception e) {
                        Log.e(TAG_NAME, "Error importing entry: " + entry.getTitle(), e);
                        failCount++;
                    }
                }

                final int finalSuccess = successCount;
                final int finalFail = failCount;

                requireActivity().runOnUiThread(() -> {
                    @SuppressLint("DefaultLocale") String message = String.format("Import hoàn tất: %d thành công, %d thất bại", finalSuccess, finalFail);
                    Toast.makeText(view.getContext(), message, Toast.LENGTH_LONG).show();

                    // Close activity if all successful
                    if (finalFail == 0) {
                        requireActivity().setResult(android.app.Activity.RESULT_OK);
                        requireActivity().finish();
                    } else {
                        importBtn.setEnabled(true);
                        importBtn.setText(getString(R.string.import_selected));
                    }
                });
            });
        });
    }

    private void importBibEntry(@NonNull BibEntry entry) {
        // Parse year
        Integer year = null;
        try {
            if (entry.getYear() != null && !entry.getYear().isEmpty()) {
                year = Integer.parseInt(entry.getYear());
            }
        } catch (NumberFormatException e) {
            Log.w(TAG_NAME, "Invalid year format: " + entry.getYear());
        }

        // Create UploadPdfRequest
        UploadPdfRequest request = new UploadPdfRequest();
        request.setTitle(entry.getTitle() != null ? entry.getTitle() : "Untitled");
        request.setAuthors(entry.getAuthor() != null ? entry.getAuthor() : "");
        request.setJournal(entry.getSource() != null ? entry.getSource() : "");
        request.setPublicationYear(year);
        request.setDoi(entry.getDoi()); // Can be null - backend will auto-generate
        request.setDataUrl(null); // No PDF URL for BibTeX import
        request.setDescription(null);
        request.setKeywords(null);
        request.setTags(null);

        // Call API synchronously (we're already in background thread)
        final boolean[] success = {false};
        final String[] errorMessage = {null};

        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);

//        paperApiHandler.uploadPdf(request, new ApiCallback<Object>() {
//            @Override
//            public void onSuccess(Object data) {
//                success[0] = true;
//                latch.countDown();
//            }
//
//            @Override
//            public void onError(String error) {
//                success[0] = false;
//                errorMessage[0] = error;
//                latch.countDown();
//            }
//        });

        try {
            latch.await(); // Wait for API call to complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Import interrupted", e);
        }

        if (!success[0]) {
            throw new RuntimeException("Failed to import: " + errorMessage[0]);
        }

        Log.d(TAG_NAME, "Successfully imported: " + entry.getTitle());
    }

    private String getPdfLink(String doi, View view) {
        AtomicReference<String> uri = new AtomicReference<>();
        unpaywallService.getPdfUrl(doi, new ApiCallback<>() {
            @Override
            public void onSuccess(String data) {
//                Log.d(TAG_NAME, "pdf link: " + data);
                uri.set(data);
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> Toast.makeText(view.getContext(), "DOI không hợp lệ", Toast.LENGTH_SHORT).show());
            }
        });
        return uri.get();
    }

    private void uploadPdfToCloudinary(String doi) {
        var uri = getPdfLink("10.34190/iccws.20.1.3366", view);
        Log.d(TAG_NAME, "pdf link: " + uri);
        if (uri != null) {
            cloudinaryService.uploadPdfToCloudinary(requireContext(), Uri.parse(uri), new UploadCallback() {

                @Override
                public void onSuccess(String downloadUrl) {
                    Log.d(TAG_NAME, "download url successfully: " + downloadUrl);
                }

                @Override
                public void onFailure(Exception e) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(view.getContext(), "Upload không thành công", Toast.LENGTH_SHORT).show());
                }
            });
        }
    }
    // </editor-fold>
}
