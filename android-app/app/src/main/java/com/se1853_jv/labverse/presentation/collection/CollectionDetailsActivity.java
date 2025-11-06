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
import com.se1853_jv.labverse.data.dto.request.CollectionPaperRequest;
import com.se1853_jv.labverse.data.dto.response.CollectionPaperDetailResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionPaperResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionResponse;
import com.se1853_jv.labverse.data.utils.Connectivity;
import com.se1853_jv.labverse.presentation.collection.adapter.CollectionPaperAdapter;

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

        // Only show Add Paper button if user is the creator
        Boolean isCreator = collection.getIsCreator();
        if (isCreator != null && isCreator) {
            buttonAddPaper.setVisibility(View.VISIBLE);
            buttonAddPaper.setOnClickListener(v -> {
                Intent intent = new Intent(CollectionDetailsActivity.this, SelectPaperActivity.class);
                intent.putExtra("collection", collection);
                startActivityForResult(intent, 100);
            });
        } else {
            buttonAddPaper.setVisibility(View.GONE);
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
        recyclerPapers.setLayoutManager(new LinearLayoutManager(this));
        recyclerPapers.setAdapter(adapter);
    }

    private void showStatusPriorityBottomSheet(CollectionPaperDetailResponse paper) {
        // Check if user is creator - if not, don't allow status update
        Boolean isCreator = collection.getIsCreator();
        if (isCreator == null || !isCreator) {
            Toast.makeText(this, "Only the collection creator can update paper status", Toast.LENGTH_SHORT).show();
            return;
        }

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

        buttonSave.setOnClickListener(v -> {
            String newStatus = (String) spinnerStatus.getSelectedItem();
            String newPriority = (String) spinnerPriority.getSelectedItem();
            
            updatePaperStatus(paper, newStatus, newPriority);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void updatePaperStatus(CollectionPaperDetailResponse paper, String status, String priority) {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get userId from session
        com.se1853_jv.labverse.data.utils.SessionManager sessionManager =
                new com.se1853_jv.labverse.data.utils.SessionManager(this);
        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionPaperRequest request = new CollectionPaperRequest();
        request.setCollectionId(collection.getId());
        request.setPaperId(paper.getPaperId());
        request.setUserId(userId);
        request.setStatus(status);
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
                    Toast.makeText(CollectionDetailsActivity.this,
                            "Failed to update status: " + error,
                            Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Refresh papers list after adding paper
            loadPapers();
        }
    }
}

