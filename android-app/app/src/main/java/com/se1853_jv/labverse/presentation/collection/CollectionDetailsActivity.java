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
import com.se1853_jv.labverse.presentation.paper.PdfReaderActivity;

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
        // Status is now read-only (calculated automatically), so no status click listener
        // Only priority can be changed (by AUTHOR only)
        adapter.setOnPriorityClickListener(paper -> showPriorityBottomSheet(paper));
        adapter.setOnRemoveClickListener(paper -> showRemovePaperDialog(paper));
        adapter.setOnPaperClickListener(paper -> openPDFReader(paper));
        
        // Set current user's access level in adapter
        AccessLevel currentUserAccessLevel = collection.getCurrentUserAccessLevel();
        adapter.setCurrentUserAccessLevel(currentUserAccessLevel);
        
        recyclerPapers.setLayoutManager(new LinearLayoutManager(this));
        recyclerPapers.setAdapter(adapter);
    }

    /**
     * Show bottom sheet to change priority only (status is now read-only, calculated automatically)
     * Only AUTHOR can change priority
     */
    private void showPriorityBottomSheet(CollectionPaperDetailResponse paper) {
        AccessLevel currentUserAccessLevel = collection.getCurrentUserAccessLevel();
        boolean isAuthor = currentUserAccessLevel == AccessLevel.AUTHOR;
        
        if (!isAuthor) {
            Toast.makeText(this, "Only collection authors can change paper priority", Toast.LENGTH_SHORT).show();
            return;
        }
        
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_paper_status, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        Spinner spinnerStatus = bottomSheetView.findViewById(R.id.spinner_status);
        Spinner spinnerPriority = bottomSheetView.findViewById(R.id.spinner_priority);
        MaterialButton buttonSave = bottomSheetView.findViewById(R.id.button_save);

        // Hide status spinner (status is now read-only, calculated automatically)
        if (spinnerStatus != null) {
            spinnerStatus.setVisibility(View.GONE);
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

        buttonSave.setOnClickListener(v -> {
            String selectedPriority = (String) spinnerPriority.getSelectedItem();
            String newPriority = null;
            
            // Only send priority if it's different from current
            if (selectedPriority != null && !selectedPriority.equals(currentPriority)) {
                newPriority = selectedPriority;
            }
            
            // Status is not sent anymore (it's calculated automatically)
            updatePaperPriority(paper, newPriority);
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

    /**
     * Update paper priority only (status is now calculated automatically, read-only)
     */
    private void updatePaperPriority(CollectionPaperDetailResponse paper, String priority) {
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
        // Status is not sent - it's calculated automatically by backend
        // Only send priority if it's being changed
        request.setPriority(priority);

        apiHandler.updatePaperStatus(request, new ApiCallback<CollectionPaperResponse>() {
            @Override
            public void onSuccess(CollectionPaperResponse response) {
                runOnUiThread(() -> {
                    Toast.makeText(CollectionDetailsActivity.this,
                            "Paper priority updated successfully",
                            Toast.LENGTH_SHORT).show();
                    // Refresh papers list
                    loadPapers();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    android.util.Log.e(TAG, "Error updating paper priority: " + error);
                    String errorMessage = error;
                    // Check if it's an authorization error
                    if (error != null && (error.contains("Read-only") || error.contains("Only collection authors"))) {
                        errorMessage = "You don't have permission to perform this action. " + error;
                    }
                    Toast.makeText(CollectionDetailsActivity.this,
                            "Failed to update priority: " + errorMessage,
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
                        // Log status for debugging
                        for (CollectionPaperDetailResponse paper : response) {
                            android.util.Log.d(TAG, "Paper loaded - paperId: " + paper.getPaperId() + 
                                ", status: " + paper.getStatus() + ", title: " + paper.getTitle());
                        }
                    }
                    adapter.setPapers(papers);
                    updateEmptyState();
                    
                    // Load reading progress from database
                    loadReadingProgressForPapers();
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
    
    /**
     * Load reading progress from ReadingWorkflow database for all papers in collection
     */
    private void loadReadingProgressForPapers() {
        if (papers == null || papers.isEmpty()) {
            return;
        }
        
        // Get user ID
        SessionManager sessionManager = new SessionManager(this);
        String userId = sessionManager.getUserId();
        
        if (userId == null) {
            return;
        }
        
        // Get workflow repository
        var db = com.se1853_jv.labverse.domain.db.DatabaseClient.getInstance(this).getAppDatabase();
        var workflowRepository = db.readingWorkflowRepository();
        
        // Load reading progress for each paper on background thread
        new Thread(() -> {
            try {
                String collectionId = collection.getId();
                java.util.Map<String, Integer> progressMap = new java.util.HashMap<>();
                
                for (CollectionPaperDetailResponse paper : papers) {
                    try {
                        com.se1853_jv.labverse.domain.infrastructure.workflow.model.ReadingWorkflow workflow = 
                            workflowRepository.getByCompositeKey(userId, paper.getPaperId(), collectionId);
                        
                        if (workflow != null && workflow.getProgress() != null) {
                            progressMap.put(paper.getPaperId(), workflow.getProgress());
                        }
                    } catch (Exception e) {
                        android.util.Log.e(TAG, "Error loading progress for paper: " + paper.getPaperId(), e);
                    }
                }
                
                // Update UI on main thread
                runOnUiThread(() -> {
                    adapter.setPaperProgressData(progressMap);
                });
                
            } catch (Exception e) {
                android.util.Log.e(TAG, "Error loading reading progress", e);
            }
        }).start();
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
                    Intent intent = new Intent(CollectionDetailsActivity.this, PdfReaderActivity.class);
                    intent.putExtra("paperId", paperId);
                    String collectionIdValue = collection != null && collection.getId() != null ? collection.getId() : "";
                    intent.putExtra("collectionId", collectionIdValue);
                    intent.putExtra("pdfUrl", pdfUrl);
                    android.util.Log.d(TAG, "Opening PDF Reader with paperId=" + paperId + ", collectionId=" + collectionIdValue);
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
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload papers from API to update status and reading progress
        // Backend automatically recalculates status when getPapersInCollection is called
        // So we just need to reload the papers list
        if (collection != null && collection.getId() != null) {
            loadPapers();
        }
    }
}

