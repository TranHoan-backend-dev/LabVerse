package com.se1853_jv.labverse.presentation.collection;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.collection.CollectionApiHandler;
import com.se1853_jv.labverse.data.api.paper.PaperApiHandler;
import com.se1853_jv.labverse.data.dto.request.CollectionPaperRequest;
import com.se1853_jv.labverse.data.dto.response.CollectionPaperResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionResponse;
import com.se1853_jv.labverse.data.utils.Connectivity;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;
import com.se1853_jv.labverse.presentation.collection.adapter.SelectPaperAdapter;

import java.util.ArrayList;
import java.util.List;

public class SelectPaperActivity extends AppCompatActivity {
    private static final String TAG = "SelectPaperActivity";
    private static final long SEARCH_DEBOUNCE_DELAY = 500; // milliseconds
    private static final int PAGE_SIZE = 10;

    private CollectionResponse collection;
    private PaperApiHandler paperApiHandler;
    private CollectionApiHandler collectionApiHandler;

    private MaterialToolbar toolbar;
    private EditText editSearch;
    private RecyclerView recyclerPapers;
    private TextView textEmptyState;
    private ProgressBar progressBar;
    private View paginationView;
    private ImageButton btnPreviousPage;
    private ImageButton btnNextPage;
    private TextView tvPageInfo;
    private SelectPaperAdapter adapter;

    private final List<PaperResearch> currentPapers = new ArrayList<>();
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private int currentPage = 0;
    private int totalPages = 1;
    private int totalResults = 0;
    private boolean isLoading = false;
    private String currentQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_paper);

        collection = (CollectionResponse) getIntent().getSerializableExtra("collection");
        if (collection == null) {
            Toast.makeText(this, "Collection not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        paperApiHandler = new PaperApiHandler();
        collectionApiHandler = new CollectionApiHandler();

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        resetPagination();
        fetchPapers();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        editSearch = findViewById(R.id.edit_search);
        recyclerPapers = findViewById(R.id.recycler_papers);
        textEmptyState = findViewById(R.id.text_empty_state);
        progressBar = findViewById(R.id.progress_bar);
        paginationView = findViewById(R.id.pagination_view);
        btnPreviousPage = findViewById(R.id.btn_prev);
        btnNextPage = findViewById(R.id.btn_next);
        tvPageInfo = findViewById(R.id.tv_page_info);

        if (btnPreviousPage != null) {
            btnPreviousPage.setOnClickListener(v -> goToPreviousPage());
        }
        if (btnNextPage != null) {
            btnNextPage.setOnClickListener(v -> goToNextPage());
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Add Paper to Collection");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new SelectPaperAdapter(new ArrayList<>(), this::addPaperToCollection);
        recyclerPapers.setLayoutManager(new LinearLayoutManager(this));
        recyclerPapers.setAdapter(adapter);
    }

    private void setupSearch() {
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s != null ? s.toString().trim() : "";
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                searchRunnable = () -> {
                    currentQuery = query;
                    resetPagination();
                    fetchPapers();
                };
                searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchPapers() {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            showLoading(false);
            updateEmptyState();
            updatePaginationControls();
            return;
        }

        isLoading = true;
        showLoading(true);
        updatePaginationControls();

        String searchQuery = currentQuery == null || currentQuery.isEmpty() ? null : currentQuery;

        paperApiHandler.getAllPapers(searchQuery, currentPage, PAGE_SIZE,
                null, null, null, null, new ApiCallback<List<PaperResearch>>() {
            @Override
            public void onSuccess(List<PaperResearch> response) {
                runOnUiThread(() -> {
                    showLoading(false);
                    isLoading = false;

                    if (response == null || response.isEmpty()) {
                        if (currentPage > 0) {
                            currentPage--;
                            totalPages = Math.max(1, currentPage + 1);
                            updatePaginationControls();
                            Toast.makeText(SelectPaperActivity.this,
                                    "No more papers", Toast.LENGTH_SHORT).show();
                            fetchPapers();
                        } else {
                            currentPapers.clear();
                            adapter.setPapers(new ArrayList<>(currentPapers));
                            totalResults = 0;
                            totalPages = 1;
                            updateEmptyState();
                            updatePaginationControls();
                        }
                        return;
                    }

                    currentPapers.clear();
                    currentPapers.addAll(response);
                    adapter.setPapers(new ArrayList<>(currentPapers));
                    updateEmptyState();

                    if (response.size() < PAGE_SIZE) {
                        totalResults = (currentPage * PAGE_SIZE) + response.size();
                        totalPages = currentPage + 1;
                    } else if (currentPage >= totalPages - 1) {
                        totalPages = currentPage + 2;
                    }

                    updatePaginationControls();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    isLoading = false;
                    Log.e(TAG, "Error loading papers: " + error);
                    Toast.makeText(SelectPaperActivity.this,
                            "Failed to load papers: " + error,
                            Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                    updatePaginationControls();
                });
            }
        });
    }

    private void addPaperToCollection(PaperResearch paper) {
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
        request.setPaperId(paper.getId());
        request.setUserId(userId);
        request.setPriority("MEDIUM");
        request.setStatus("ToRead");

        showLoading(true);

        collectionApiHandler.addPaperToCollection(request, new ApiCallback<CollectionPaperResponse>() {
            @Override
            public void onSuccess(CollectionPaperResponse response) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(SelectPaperActivity.this,
                            "Paper added to collection successfully",
                            Toast.LENGTH_SHORT).show();

                    currentPapers.removeIf(p -> p.getId().equals(paper.getId()));
                    adapter.setPapers(new ArrayList<>(currentPapers));
                    updateEmptyState();

                    // Refresh current page to keep page size consistent
                    fetchPapers();

                    // Return result to parent activity
                    setResult(RESULT_OK);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    android.util.Log.e(TAG, "Error adding paper: " + error);
                    Toast.makeText(SelectPaperActivity.this,
                            "Failed to add paper: " + error,
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateEmptyState() {
        boolean hasData = !currentPapers.isEmpty();
        if (!hasData) {
            textEmptyState.setVisibility(View.VISIBLE);
            recyclerPapers.setVisibility(View.GONE);
            if (paginationView != null) {
                paginationView.setVisibility(View.GONE);
            }
            if (editSearch.getText().toString().trim().isEmpty()) {
                textEmptyState.setText("No papers found");
            } else {
                textEmptyState.setText("No papers match your search");
            }
        } else {
            textEmptyState.setVisibility(View.GONE);
            recyclerPapers.setVisibility(View.VISIBLE);
            if (paginationView != null) {
                paginationView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
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
    protected void onDestroy() {
        super.onDestroy();
        // Clean up handler
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }

    private void resetPagination() {
        currentPage = 0;
        totalPages = 1;
        totalResults = 0;
    }

    private void goToPreviousPage() {
        if (currentPage > 0 && !isLoading) {
            currentPage--;
            fetchPapers();
        }
    }

    private void goToNextPage() {
        if (isLoading) {
            return;
        }

        boolean hasNext = adapter != null && (adapter.getItemCount() == PAGE_SIZE || currentPage < totalPages - 1);
        if (hasNext) {
            currentPage++;
            fetchPapers();
        } else {
            Toast.makeText(this, "Already on the last page", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePaginationControls() {
        if (paginationView == null || tvPageInfo == null || btnPreviousPage == null || btnNextPage == null) {
            return;
        }

        if (currentPapers.isEmpty() && !isLoading) {
            paginationView.setVisibility(View.GONE);
            return;
        }

        paginationView.setVisibility(View.VISIBLE);

        if (totalResults > 0) {
            tvPageInfo.setText(getString(R.string.page_info, currentPage + 1, totalPages));
        } else if (totalPages > 1) {
            tvPageInfo.setText("Page " + (currentPage + 1) + " of " + totalPages + "+");
        } else {
            tvPageInfo.setText("Page " + (currentPage + 1));
        }

        boolean canGoPrevious = currentPage > 0 && !isLoading;
        btnPreviousPage.setEnabled(canGoPrevious);
        btnPreviousPage.setAlpha(canGoPrevious ? 1.0f : 0.4f);

        int currentCount = adapter != null ? adapter.getItemCount() : 0;
        boolean hasNext = (currentCount == PAGE_SIZE) || (currentPage < totalPages - 1);
        boolean canGoNext = hasNext && !isLoading;
        btnNextPage.setEnabled(canGoNext);
        btnNextPage.setAlpha(canGoNext ? 1.0f : 0.4f);
    }
}








