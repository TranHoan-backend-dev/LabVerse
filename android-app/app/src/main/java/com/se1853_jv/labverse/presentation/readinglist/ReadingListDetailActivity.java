package com.se1853_jv.labverse.presentation.readinglist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.paper.PaperApiHandler;
import com.se1853_jv.labverse.data.api.readinglist.ReadingListApiHandler;
import com.se1853_jv.labverse.data.dto.request.UpdateReadingListPapersRequest;
import com.se1853_jv.labverse.data.dto.response.ReadingListResponse;
import com.se1853_jv.labverse.data.utils.Connectivity;
import com.se1853_jv.labverse.data.utils.EncoderUtils;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;
import com.se1853_jv.labverse.presentation.common.HeaderHelper;
import com.se1853_jv.labverse.presentation.paper.PaperDetailsActivity;
import com.se1853_jv.labverse.presentation.readinglist.adapter.ReadingListPaperAdapter;

import java.util.ArrayList;
import java.util.List;

public class ReadingListDetailActivity extends AppCompatActivity {
    private static final String TAG = "ReadingListDetail";
    private static final int REQUEST_CODE_SELECT_PAPER = 100;

    private TextView tvListName;
    private TextView tvPaperCount;
    private TextView tvMemberCount;
    private MaterialButton btnAddPaper;
    private RecyclerView recyclerViewPapers;
    private LinearLayout emptyState;

    private ReadingListApiHandler readingListApiHandler;
    private PaperApiHandler paperApiHandler;
    private ReadingListPaperAdapter adapter;
    private ReadingListResponse currentList;
    private List<PaperResearch> papers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_list_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scrollContent), (v, insets) -> {
            var statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(statusBar.left, statusBar.top, statusBar.right, statusBar.bottom);
            return insets;
        });

        // Get reading list from intent
        currentList = (ReadingListResponse) getIntent().getSerializableExtra("readingList");
        if (currentList == null) {
            Toast.makeText(this, "Reading list not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize
        readingListApiHandler = new ReadingListApiHandler(this);
        paperApiHandler = new PaperApiHandler(this);

        bindViews();
        setupRecyclerView();
        handleEvents();

        // Setup header
        HeaderHelper.setupProfileClickListeners(this);
        HeaderHelper.setupListsNavigationClickListener(this);

        // Load data
        displayListInfo();
        loadPapers();
    }

    private void bindViews() {
        tvListName = findViewById(R.id.tvListName);
        tvPaperCount = findViewById(R.id.tvPaperCount);
        tvMemberCount = findViewById(R.id.tvMemberCount);
        btnAddPaper = findViewById(R.id.btnAddPaper);
        recyclerViewPapers = findViewById(R.id.recyclerViewPapers);
        emptyState = findViewById(R.id.emptyState);
    }

    private void setupRecyclerView() {
        adapter = new ReadingListPaperAdapter();
        adapter.setOnPaperActionListener(new ReadingListPaperAdapter.OnPaperActionListener() {
            @Override
            public void onPaperClick(PaperResearch paper) {
                // Navigate to paper details
                Intent intent = new Intent(ReadingListDetailActivity.this, PaperDetailsActivity.class);
                intent.putExtra("paperId", paper.getId());
                startActivity(intent);
            }

            @Override
            public void onRemovePaper(PaperResearch paper) {
                showRemovePaperConfirmation(paper);
            }
        });

        recyclerViewPapers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPapers.setAdapter(adapter);
    }

    private void handleEvents() {
        if (btnAddPaper != null) {
            btnAddPaper.setOnClickListener(v -> {
                // Navigate to select paper activity
                Intent intent = new Intent(ReadingListDetailActivity.this, SelectPaperForReadingListActivity.class);
                intent.putExtra("readingListId", currentList.getId());
                startActivityForResult(intent, REQUEST_CODE_SELECT_PAPER);
            });
        }
    }

    private void displayListInfo() {
        if (currentList == null) return;

        // Set list name
        if (tvListName != null) {
            tvListName.setText(currentList.getName() != null ? currentList.getName() : "Untitled List");
        }

        // Update counts (will be updated when papers are loaded)
        updateCounts();
    }

    private void updateCounts() {
        if (tvPaperCount != null) {
            tvPaperCount.setText(String.valueOf(papers.size()));
        }

        if (tvMemberCount != null) {
            int memberCount = currentList.getUserIdsList() != null ? currentList.getUserIdsList().size() : 0;
            tvMemberCount.setText(String.valueOf(memberCount));
        }
    }

    private void loadPapers() {
        if (currentList == null || currentList.getPaperIdsList() == null || currentList.getPaperIdsList().isEmpty()) {
            papers.clear();
            adapter.setPapers(papers);
            updateEmptyState();
            updateCounts();
            return;
        }

        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        // Load papers by IDs
        papers.clear();
        loadPapersRecursive(0);
    }

    private void loadPapersRecursive(int index) {
        if (index >= currentList.getPaperIdsList().size()) {
            // All papers loaded
            adapter.setPapers(papers);
            updateEmptyState();
            updateCounts();
            return;
        }

        String paperId = currentList.getPaperIdsList().get(index);
        // Paper IDs from API response are already encoded, but PaperApi expects decoded IDs
        // So we need to decode it
        String decodedPaperId = EncoderUtils.decode(paperId);

        paperApiHandler.getPaperDetails(decodedPaperId, new ApiCallback<PaperResearch>() {
            @Override
            public void onSuccess(PaperResearch paper) {
                runOnUiThread(() -> {
                    papers.add(paper);
                    // Load next paper
                    loadPapersRecursive(index + 1);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error loading paper " + paperId + ": " + error);
                    // Continue loading next paper even if this one fails
                    loadPapersRecursive(index + 1);
                });
            }
        });
    }

    private void showRemovePaperConfirmation(PaperResearch paper) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Remove Paper")
                .setMessage("Are you sure you want to remove \"" + 
                           (paper.getTitle() != null ? paper.getTitle() : "this paper") + 
                           "\" from this reading list?")
                .setPositiveButton("Remove", (dialog, which) -> removePaper(paper))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removePaper(PaperResearch paper) {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        UpdateReadingListPapersRequest request = new UpdateReadingListPapersRequest();
        request.setAction("remove");
        List<String> paperIds = new ArrayList<>();
        // Encode paper ID before removing
        String encodedPaperId = EncoderUtils.encode(paper.getId());
        paperIds.add(encodedPaperId);
        request.setPaperIds(paperIds);

        readingListApiHandler.updatePapers(currentList.getId(), request, new ApiCallback<ReadingListResponse>() {
            @Override
            public void onSuccess(ReadingListResponse updatedList) {
                runOnUiThread(() -> {
                    currentList = updatedList;
                    papers.remove(paper);
                    adapter.removePaper(paper);
                    updateEmptyState();
                    updateCounts();
                    Toast.makeText(ReadingListDetailActivity.this,
                            "Paper removed successfully", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error removing paper: " + error);
                    Toast.makeText(ReadingListDetailActivity.this,
                            "Failed to remove paper: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void updateEmptyState() {
        if (emptyState != null && recyclerViewPapers != null) {
            if (adapter.getItemCount() == 0) {
                emptyState.setVisibility(View.VISIBLE);
                recyclerViewPapers.setVisibility(View.GONE);
            } else {
                emptyState.setVisibility(View.GONE);
                recyclerViewPapers.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_PAPER && resultCode == RESULT_OK) {
            // Reload reading list to get updated paper IDs
            if (currentList != null && currentList.getId() != null) {
                reloadReadingList();
            }
        }
    }

    private void reloadReadingList() {
        // Reload reading list from API to get updated paper IDs
        if (currentList == null || currentList.getId() == null) {
            return;
        }

        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get user ID from session
        com.se1853_jv.labverse.data.utils.SessionManager sessionManager =
                new com.se1853_jv.labverse.data.utils.SessionManager(this);
        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            loadPapers(); // Fallback to just reload papers
            return;
        }

        // Get updated reading list
        readingListApiHandler.getReadingListsByUser(userId, new ApiCallback<List<ReadingListResponse>>() {
            @Override
            public void onSuccess(List<ReadingListResponse> lists) {
                runOnUiThread(() -> {
                    // Find the current list in the updated list
                    if (lists != null) {
                        for (ReadingListResponse list : lists) {
                            if (list.getId().equals(currentList.getId())) {
                                currentList = list;
                                break;
                            }
                        }
                    }
                    // Reload papers with updated list
                    loadPapers();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error reloading reading list: " + error);
                    // Fallback to just reload papers
                    loadPapers();
                });
            }
        });
    }
}

