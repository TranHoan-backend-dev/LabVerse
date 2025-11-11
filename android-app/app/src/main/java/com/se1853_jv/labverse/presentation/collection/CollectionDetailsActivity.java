package com.se1853_jv.labverse.presentation.collection;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.collection.CollectionApiHandler;
import com.se1853_jv.labverse.data.api.paper.PaperApiHandler;
import com.se1853_jv.labverse.data.dto.request.CollectionPaperRequest;
import com.se1853_jv.labverse.data.dto.response.CollectionPaperDetailResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionPaperResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionResponse;
import com.se1853_jv.labverse.data.utils.Connectivity;
import com.se1853_jv.labverse.data.utils.SessionManager;
import com.se1853_jv.labverse.domain.enumerate.AccessLevel;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;
import com.se1853_jv.labverse.presentation.collection.adapter.CollectionPaperAdapter;
import com.se1853_jv.labverse.presentation.paper.PDFReaderActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CollectionDetailsActivity extends AppCompatActivity {
    private static final String TAG = "CollectionDetailsActivity";

    private CollectionApiHandler apiHandler;
    private RecyclerView recyclerPapers;
    private TextView textCollectionName;
    private TextView textEmptyState;
    private MaterialButton buttonAddPaper;
    private CollectionPaperAdapter adapter;

    private CollectionResponse collection;
    private final List<CollectionPaperDetailResponse> papers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_details);

        collection = (CollectionResponse) getIntent().getSerializableExtra("collection");
        if (collection == null) {
            Toast.makeText(this, "Collection not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiHandler = new CollectionApiHandler();
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        loadPapers();
    }

    private void initializeViews() {
        recyclerPapers = findViewById(R.id.recycler_papers);
        textCollectionName = findViewById(R.id.text_collection_name);
        textEmptyState = findViewById(R.id.text_empty_state);
        buttonAddPaper = findViewById(R.id.button_add_paper);

        textCollectionName.setText(collection.getName());

        // Check access level to enable/disable add paper button
        AccessLevel currentUserAccessLevel = collection.getCurrentUserAccessLevel();
        boolean canAddPaper = currentUserAccessLevel == AccessLevel.AUTHOR || 
                              currentUserAccessLevel == AccessLevel.CONTRIBUTOR;
        
        if (!canAddPaper) {
            // READ_ONLY users cannot add papers
            buttonAddPaper.setEnabled(false);
            buttonAddPaper.setAlpha(0.5f);
        } else {
            buttonAddPaper.setOnClickListener(v -> {
                Intent intent = new Intent(CollectionDetailsActivity.this, SelectPaperActivity.class);
                intent.putExtra("collection", collection);
                startActivityForResult(intent, 100);
            });
        }
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new CollectionPaperAdapter();
        adapter.setOnStatusClickListener(paper -> showStatusPriorityBottomSheet(paper));
        adapter.setOnRemoveClickListener(paper -> showRemovePaperDialog(paper));
        adapter.setOnPaperClickListener(paper -> openPDFReader(paper));
        
        // Set current user's access level in adapter
        AccessLevel currentUserAccessLevel = collection.getCurrentUserAccessLevel();
        adapter.setCurrentUserAccessLevel(currentUserAccessLevel);
        
        recyclerPapers.setLayoutManager(new LinearLayoutManager(this));
        recyclerPapers.setAdapter(adapter);
    }

    private void showStatusPriorityBottomSheet(CollectionPaperDetailResponse paper) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_paper_status, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        Spinner spinnerStatus = bottomSheetView.findViewById(R.id.spinner_status);
        Spinner spinnerPriority = bottomSheetView.findViewById(R.id.spinner_priority);
        MaterialButton buttonSave = bottomSheetView.findViewById(R.id.button_save);

        // Setup Status spinner
        String[] statuses = {"ToRead", "Reading", "Finished"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);
        
        // Set current status
        String currentStatus = paper.getStatus() != null ? paper.getStatus() : "ToRead";
        int statusPosition = Arrays.asList(statuses).indexOf(currentStatus);
        if (statusPosition >= 0) {
            spinnerStatus.setSelection(statusPosition);
        }

        // Setup Priority spinner
        String[] priorities = {"HIGH", "MEDIUM", "LOW"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priorities);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);
        
        // Set current priority
        String currentPriority = paper.getPriority() != null ? paper.getPriority() : "MEDIUM";
        int priorityPosition = Arrays.asList(priorities).indexOf(currentPriority);
        if (priorityPosition >= 0) {
            spinnerPriority.setSelection(priorityPosition);
        }

        // Disable priority spinner for non-AUTHOR users (only AUTHOR can set priority)
        AccessLevel currentUserAccessLevel = collection.getCurrentUserAccessLevel();
        boolean isAuthor = currentUserAccessLevel == AccessLevel.AUTHOR;
        
        if (!isAuthor) {
            spinnerPriority.setEnabled(false);
            spinnerPriority.setAlpha(0.5f); // Visual indication of disabled state
            TextView priorityLabel = bottomSheetView.findViewById(R.id.text_priority_label);
            if (priorityLabel != null) {
                priorityLabel.setAlpha(0.5f);
            }
        }

        buttonSave.setOnClickListener(v -> {
            String newStatus = (String) spinnerStatus.getSelectedItem();
            String newPriority = null;
            
            // Only send priority if user is AUTHOR and priority is being changed
            if (isAuthor && spinnerPriority.isEnabled()) {
                String selectedPriority = (String) spinnerPriority.getSelectedItem();
                // Only send priority if it's different from current
                if (selectedPriority != null && !selectedPriority.equals(currentPriority)) {
                    newPriority = selectedPriority;
                } else {
                    // Don't send priority if it hasn't changed (backend will keep current)
                    newPriority = null;
                }
            } else {
                // For non-AUTHOR users, don't send priority (backend will keep current)
                newPriority = null;
            }
            
            updatePaperStatus(paper, newStatus, newPriority);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void showRemovePaperDialog(CollectionPaperDetailResponse paper) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Remove Paper")
                .setMessage("Are you sure you want to remove \"" + paper.getTitle() + "\" from this collection?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    removePaperFromCollection(paper);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removePaperFromCollection(CollectionPaperDetailResponse paper) {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        SessionManager sessionManager = new SessionManager(this);
        String userId = sessionManager.getUserId();

        apiHandler.removePaperFromCollection(collection.getId(), paper.getPaperId(), userId,
                new ApiCallback<Object>() {
                    @Override
                    public void onSuccess(Object response) {
                        runOnUiThread(() -> {
                            Toast.makeText(CollectionDetailsActivity.this,
                                    "Paper removed from collection successfully",
                                    Toast.LENGTH_SHORT).show();
                            loadPapers(); // Refresh papers list
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            android.util.Log.e(TAG, "Error removing paper: " + error);
                            Toast.makeText(CollectionDetailsActivity.this,
                                    "Failed to remove paper: " + error,
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    private void updatePaperStatus(CollectionPaperDetailResponse paper, String status, String priority) {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get userId from session for authorization
        SessionManager sessionManager = new SessionManager(this);
        String userId = sessionManager.getUserId();

        CollectionPaperRequest request = new CollectionPaperRequest();
        request.setCollectionId(collection.getId());
        request.setPaperId(paper.getPaperId());
        request.setUserId(userId); // Add userId for authorization check
        request.setStatus(status);
        // Only send priority if it's being changed (for AUTHOR) or keep current priority
        // Backend will handle: if priority is null or same as current, it won't update priority
        request.setPriority(priority);

        apiHandler.updatePaperStatus(request, new ApiCallback<CollectionPaperResponse>() {
            @Override
            public void onSuccess(CollectionPaperResponse response) {
                runOnUiThread(() -> {
                    Toast.makeText(CollectionDetailsActivity.this,
                            "Paper status updated successfully",
                            Toast.LENGTH_SHORT).show();
                    // Refresh papers list
                    loadPapers();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    android.util.Log.e(TAG, "Error updating paper status: " + error);
                    String errorMessage = error;
                    // Check if it's an authorization error
                    if (error != null && (error.contains("Read-only") || error.contains("Only collection authors"))) {
                        errorMessage = "You don't have permission to perform this action. " + error;
                    }
                    Toast.makeText(CollectionDetailsActivity.this,
                            "Failed to update status: " + errorMessage,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void loadPapers() {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        apiHandler.getPapersInCollection(collection.getId(), new ApiCallback<List<CollectionPaperDetailResponse>>() {
            @Override
            public void onSuccess(List<CollectionPaperDetailResponse> response) {
                runOnUiThread(() -> {
                    papers.clear();
                    if (response != null) {
                        papers.addAll(response);
                    }
                    adapter.setPapers(papers);
                    updateEmptyState();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    android.util.Log.e(TAG, "Error loading papers: " + error);
                    Toast.makeText(CollectionDetailsActivity.this,
                            "Failed to load papers: " + error,
                            Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });
            }
        });
    }

    private void updateEmptyState() {
        if (papers.isEmpty()) {
            textEmptyState.setVisibility(View.VISIBLE);
            recyclerPapers.setVisibility(View.GONE);
        } else {
            textEmptyState.setVisibility(View.GONE);
            recyclerPapers.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Open PDF Reader Activity when user clicks on a paper
     */
    private void openPDFReader(CollectionPaperDetailResponse paper) {
        String paperId = paper.getPaperId();
        if (paperId == null || paperId.isEmpty()) {
            Toast.makeText(this, "Paper ID not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading indicator
        Toast.makeText(this, "Loading paper...", Toast.LENGTH_SHORT).show();

        // Fetch paper details to get PDF URL (dataUrl)
        PaperApiHandler paperApiHandler = new PaperApiHandler(this);
        paperApiHandler.getPaperDetails(paperId, new ApiCallback<PaperResearch>() {
            @Override
            public void onSuccess(PaperResearch paperResearch) {
                runOnUiThread(() -> {
                    String pdfUrl = paperResearch.getDataUrl();
                    if (pdfUrl == null || pdfUrl.isEmpty()) {
                        Toast.makeText(CollectionDetailsActivity.this,
                                "PDF URL not available for this paper",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Open PDF Reader Activity
                    Intent intent = new Intent(CollectionDetailsActivity.this, PDFReaderActivity.class);
                    intent.putExtra("paperId", paperId);
                    intent.putExtra("collectionId", collection.getId());
                    intent.putExtra("pdfUrl", pdfUrl);
                    startActivity(intent);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    android.util.Log.e(TAG, "Error loading paper details: " + error);
                    Toast.makeText(CollectionDetailsActivity.this,
                            "Failed to load paper: " + error,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Refresh papers list after adding paper
            loadPapers();
        }
    }
}

