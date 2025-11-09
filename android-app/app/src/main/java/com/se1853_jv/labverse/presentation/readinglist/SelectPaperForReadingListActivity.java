package com.se1853_jv.labverse.presentation.readinglist;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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
import com.se1853_jv.labverse.data.api.paper.PaperApiHandler;
import com.se1853_jv.labverse.data.api.readinglist.ReadingListApiHandler;
import com.se1853_jv.labverse.data.dto.request.UpdateReadingListPapersRequest;
import com.se1853_jv.labverse.data.dto.response.ReadingListResponse;
import com.se1853_jv.labverse.data.utils.Connectivity;
import com.se1853_jv.labverse.data.utils.EncoderUtils;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;
import com.se1853_jv.labverse.presentation.collection.adapter.SelectPaperAdapter;

import java.util.ArrayList;
import java.util.List;

public class SelectPaperForReadingListActivity extends AppCompatActivity {
    private static final String TAG = "SelectPaperForRL";
    private static final long SEARCH_DEBOUNCE_DELAY = 500; // milliseconds
    
    private String readingListId;
    private PaperApiHandler paperApiHandler;
    private ReadingListApiHandler readingListApiHandler;
    
    private MaterialToolbar toolbar;
    private EditText editSearch;
    private RecyclerView recyclerPapers;
    private TextView textEmptyState;
    private ProgressBar progressBar;
    private SelectPaperAdapter adapter;
    
    private final List<PaperResearch> allPapers = new ArrayList<>();
    private final List<PaperResearch> filteredPapers = new ArrayList<>();
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    
    private int currentPage = 0;
    private static final int PAGE_SIZE = 50;
    private boolean isLoading = false;
    private boolean hasMorePages = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_paper);

        readingListId = getIntent().getStringExtra("readingListId");
        if (readingListId == null || readingListId.isEmpty()) {
            Toast.makeText(this, "Reading list ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        paperApiHandler = new PaperApiHandler(this);
        readingListApiHandler = new ReadingListApiHandler(this);
        
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupScrollListener();
        // Load tất cả papers (getAllPapers)
        loadAllPapers(0, PAGE_SIZE);
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        editSearch = findViewById(R.id.edit_search);
        recyclerPapers = findViewById(R.id.recycler_papers);
        textEmptyState = findViewById(R.id.text_empty_state);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Add Paper to Reading List");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new SelectPaperAdapter(new ArrayList<>(), this::addPaperToReadingList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerPapers.setLayoutManager(layoutManager);
        recyclerPapers.setAdapter(adapter);
    }
    
    private void setupScrollListener() {
        recyclerPapers.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                    
                    // Load more khi scroll gần đến cuối danh sách
                    if (!isLoading && hasMorePages && 
                        (firstVisibleItemPosition + visibleItemCount) >= totalItemCount - 5) {
                        loadMorePapers();
                    }
                }
            }
        });
    }

    private void setupSearch() {
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cancel previous search
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                
                // Schedule new search with debounce
                searchRunnable = () -> performSearch(s.toString());
                searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            // Show all papers if search is empty - reload from beginning
            currentPage = 0;
            hasMorePages = true;
            allPapers.clear();
            loadAllPapers(0, PAGE_SIZE);
        } else {
            // Reset pagination for new search
            currentPage = 0;
            hasMorePages = true;
            // Clear existing papers when starting new search
            allPapers.clear();
            // Search on server
            searchPapersOnServer(query, 0, PAGE_SIZE);
        }
    }
    
    private void searchPapersOnServer(String query, int page, int pageSize) {
        if (!Connectivity.isInternetAvailable(this)) {
            return;
        }

        if (isLoading) return;
        isLoading = true;
        showLoading(true);
        
        paperApiHandler.getAllPapers(query, page, pageSize, new ApiCallback<>() {
            @Override
            public void onSuccess(List<PaperResearch> response) {
                runOnUiThread(() -> {
                    isLoading = false;
                    showLoading(false);
                    if (response != null) {
                        if (page == 0) {
                            // First page of search - clear existing papers
                            allPapers.clear();
                        }
                        if (response.isEmpty()) {
                            hasMorePages = false;
                        } else {
                            // Add papers, avoiding duplicates
                            for (PaperResearch paper : response) {
                                if (!containsPaper(allPapers, paper.getId())) {
                                    allPapers.add(paper);
                                }
                            }
                            // Nếu số papers trả về ít hơn pageSize, không còn pages nào nữa
                            if (response.size() < pageSize) {
                                hasMorePages = false;
                            } else {
                                hasMorePages = true;
                            }
                        }
                    } else {
                        hasMorePages = false;
                    }
                    filterPapers(query);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    isLoading = false;
                    showLoading(false);
                    android.util.Log.e(TAG, "Error searching papers: " + error);
                    hasMorePages = false;
                    filterPapers(query); // Still show what we have
                });
            }
        });
    }

    private void filterPapers(String query) {
        if (query == null || query.trim().isEmpty()) {
            filteredPapers.clear();
            filteredPapers.addAll(allPapers);
        } else {
            String lowerQuery = query.toLowerCase();
            filteredPapers.clear();
            for (PaperResearch paper : allPapers) {
                if ((paper.getTitle() != null && paper.getTitle().toLowerCase().contains(lowerQuery)) ||
                    (paper.getAuthors() != null && paper.getAuthors().toLowerCase().contains(lowerQuery)) ||
                    (paper.getJournal() != null && paper.getJournal().toLowerCase().contains(lowerQuery))) {
                    filteredPapers.add(paper);
                }
            }
        }
        adapter.setPapers(filteredPapers);
        updateEmptyState();
    }


    private boolean containsPaper(List<PaperResearch> papers, String id) {
        if (id == null) return false;
        for (PaperResearch paper : papers) {
            if (id.equals(paper.getId())) {
                return true;
            }
        }
        return false;
    }

    private void loadAllPapers(int page, int pageSize) {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            updateEmptyState();
            return;
        }

        if (isLoading) return;
        isLoading = true;
        showLoading(true);

        // Gọi getAllPapers với searchQuery = null để lấy tất cả papers
        paperApiHandler.getAllPapers(null, page, pageSize, new ApiCallback<>() {
            @Override
            public void onSuccess(List<PaperResearch> response) {
                runOnUiThread(() -> {
                    isLoading = false;
                    showLoading(false);
                    if (response != null) {
                        if (page == 0) {
                            // First page - clear existing papers
                            allPapers.clear();
                        }
                        if (response.isEmpty()) {
                            hasMorePages = false;
                        } else {
                            // Add papers, avoiding duplicates
                            for (PaperResearch paper : response) {
                                if (!containsPaper(allPapers, paper.getId())) {
                                    allPapers.add(paper);
                                }
                            }
                            // Nếu số papers trả về ít hơn pageSize, không còn pages nào nữa
                            if (response.size() < pageSize) {
                                hasMorePages = false;
                            } else {
                                hasMorePages = true;
                            }
                        }
                    } else {
                        hasMorePages = false;
                    }
                    filterPapers(editSearch.getText() != null ? editSearch.getText().toString() : "");
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    isLoading = false;
                    showLoading(false);
                    android.util.Log.e(TAG, "Error loading papers: " + error);
                    Toast.makeText(SelectPaperForReadingListActivity.this,
                            "Failed to load papers: " + error,
                            Toast.LENGTH_SHORT).show();
                    hasMorePages = false;
                    updateEmptyState();
                });
            }
        });
    }
    
    private void loadMorePapers() {
        if (!Connectivity.isInternetAvailable(this) || isLoading || !hasMorePages) {
            return;
        }
        
        String searchQuery = editSearch.getText() != null ? editSearch.getText().toString().trim() : "";
        currentPage++;
        
        if (searchQuery.isEmpty()) {
            loadAllPapers(currentPage, PAGE_SIZE);
        } else {
            searchPapersOnServer(searchQuery, currentPage, PAGE_SIZE);
        }
    }

    private void addPaperToReadingList(PaperResearch paper) {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        // Encode paper ID before adding
        String encodedPaperId = EncoderUtils.encode(paper.getId());
        
        UpdateReadingListPapersRequest request = new UpdateReadingListPapersRequest();
        request.setAction("add");
        List<String> paperIds = new ArrayList<>();
        paperIds.add(encodedPaperId);
        request.setPaperIds(paperIds);

        showLoading(true);

        readingListApiHandler.updatePapers(readingListId, request, new ApiCallback<>() {
            @Override
            public void onSuccess(ReadingListResponse response) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(SelectPaperForReadingListActivity.this,
                            "Paper added to reading list successfully",
                            Toast.LENGTH_SHORT).show();
                    
                    // Remove paper from list to prevent duplicate selection
                    allPapers.removeIf(p -> p.getId().equals(paper.getId()));
                    filterPapers(editSearch.getText().toString());
                    
                    // Return result to parent activity
                    setResult(RESULT_OK);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    android.util.Log.e(TAG, "Error adding paper: " + error);
                    Toast.makeText(SelectPaperForReadingListActivity.this,
                            "Failed to add paper: " + error,
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateEmptyState() {
        if (filteredPapers.isEmpty()) {
            textEmptyState.setVisibility(View.VISIBLE);
            recyclerPapers.setVisibility(View.GONE);
            if (editSearch.getText().toString().trim().isEmpty()) {
                textEmptyState.setText("No papers found");
            } else {
                textEmptyState.setText("No papers match your search");
            }
        } else {
            textEmptyState.setVisibility(View.GONE);
            recyclerPapers.setVisibility(View.VISIBLE);
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
}

