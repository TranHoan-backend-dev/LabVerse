package com.se1853_jv.labverse.presentation.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.paper.PaperApiHandler;
import com.se1853_jv.labverse.data.utils.SessionManager;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;
import com.se1853_jv.labverse.presentation.common.BaseActivity;
import com.se1853_jv.labverse.presentation.common.HeaderHelper;
import com.se1853_jv.labverse.presentation.search.adapter.SearchResultAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends BaseActivity implements FilterDialogFragment.FilterDialogListener {
    private static final String TAG = "SearchActivity";
    
    private EditText searchInput;
    private ImageButton filterButton;
    private RecyclerView recyclerView;
    private SearchResultAdapter adapter;
    private ProgressBar progressBar;
    private View emptyState;
    private View paginationControls;
    private ImageButton btnPreviousPage;
    private ImageButton btnNextPage;
    private TextView textPageInfo;
    
    private PaperApiHandler paperApiHandler;
    private String currentQuery = "";
    private FilterDialogFragment.FilterData currentFilterData = null;
    
    // Pagination
    private int currentPage = 0;
    private static final int PAGE_SIZE = 4;
    private boolean isLoading = false;
    private int totalPages = 1; // Will be updated based on total results
    private int totalResults = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        paperApiHandler = new PaperApiHandler();
        
        setupBottomNavbar(findViewById(R.id.ui_search), R.id.bottomNav);
        HeaderHelper.setupAvatarClickListener(this);
        initViews();
        setupSearchBar();
        setupRecyclerView();
        
        // Load initial papers
        resetPagination();
        updatePaginationControls(); // Initialize controls
        performSearch();
    }

    private void initViews() {
        View searchBar = findViewById(R.id.search_bar);
        searchInput = searchBar.findViewById(R.id.input_search);
        filterButton = searchBar.findViewById(R.id.btn_filter);
        
        recyclerView = findViewById(R.id.recycler_search_results);
        progressBar = findViewById(R.id.progress_bar);
        emptyState = findViewById(R.id.empty_state);
        paginationControls = findViewById(R.id.pagination_controls);
        btnPreviousPage = findViewById(R.id.btn_previous_page);
        btnNextPage = findViewById(R.id.btn_next_page);
        textPageInfo = findViewById(R.id.text_page_info);
        
        // Setup pagination buttons
        btnPreviousPage.setOnClickListener(v -> goToPreviousPage());
        btnNextPage.setOnClickListener(v -> goToNextPage());
    }

    private void setupSearchBar() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newQuery = s.toString().trim();
                // Only reset pagination if query actually changed
                if (!newQuery.equals(currentQuery)) {
                    currentQuery = newQuery;
                    resetPagination();
                    // Debounce search - perform search after user stops typing
                    recyclerView.removeCallbacks(searchRunnable);
                    recyclerView.postDelayed(searchRunnable, 500); // 500ms delay
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        filterButton.setOnClickListener(v -> showFilterDialog());
    }

    private final Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            performSearch();
        }
    };

    private void setupRecyclerView() {
        adapter = new SearchResultAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    
    private void goToPreviousPage() {
        if (currentPage > 0 && !isLoading) {
            currentPage--;
            performSearch();
        }
    }
    
    private void goToNextPage() {
        if (currentPage < totalPages - 1 && !isLoading) {
            currentPage++;
            performSearch();
        }
    }
    
    private void updatePaginationControls() {
        // Update page info
        if (totalResults > 0) {
            // We know the total, show "Page X of Y"
            textPageInfo.setText(getString(R.string.page_info, currentPage + 1, totalPages));
        } else if (totalPages > 1) {
            // We've seen multiple pages but don't know total yet
            textPageInfo.setText("Page " + (currentPage + 1) + " of " + totalPages + "+");
        } else {
            // First page, don't know total yet
            textPageInfo.setText("Page " + (currentPage + 1));
        }
        
        // Update buttons
        boolean canGoPrevious = currentPage > 0 && !isLoading;
        btnPreviousPage.setEnabled(canGoPrevious);
        btnPreviousPage.setAlpha(canGoPrevious ? 1.0f : 0.4f);
        
        // Enable Next if we got a full page (might have more) or if we know there are more pages
        int currentCount = adapter.getItemCount();
        boolean hasNext = (currentCount == PAGE_SIZE) || (currentPage < totalPages - 1);
        boolean canGoNext = hasNext && !isLoading;
        btnNextPage.setEnabled(canGoNext);
        btnNextPage.setAlpha(canGoNext ? 1.0f : 0.4f);
    }

    private void showFilterDialog() {
        FilterDialogFragment filterDialog = FilterDialogFragment.newInstance();
        filterDialog.setListener(this);
        filterDialog.show(getSupportFragmentManager(), "FilterDialog");
    }

    @Override
    public void onFiltersApplied(FilterDialogFragment.FilterData filterData) {
        // If filterData is null, it means filters were reset
        currentFilterData = filterData;
        resetPagination();
        performSearch();
    }

    private void resetPagination() {
        currentPage = 0;
        totalPages = 1;
        totalResults = 0;
        isLoading = false;
    }

    private void performSearch() {
        if (isLoading) {
            return; // Prevent multiple simultaneous requests
        }
        
        isLoading = true;
        showLoading(true);
        hideEmptyState();
        updatePaginationControls(); // Disable buttons while loading

        // Build search query (combine query and keywords)
        String searchQuery = buildSearchQuery();
        String author = currentFilterData != null && currentFilterData.author != null && !currentFilterData.author.isEmpty() 
                ? currentFilterData.author : null;
        String journal = currentFilterData != null && currentFilterData.journal != null && !currentFilterData.journal.isEmpty() 
                ? currentFilterData.journal : null;
        Integer yearFrom = currentFilterData != null ? currentFilterData.yearFrom : null;
        Integer yearTo = currentFilterData != null ? currentFilterData.yearTo : null;
        
        paperApiHandler.getAllPapers(searchQuery, currentPage, PAGE_SIZE, author, journal, yearFrom, yearTo, 
                new ApiCallback<List<PaperResearch>>() {
            @Override
            public void onSuccess(List<PaperResearch> papers) {
                runOnUiThread(() -> {
                    showLoading(false);
                    isLoading = false;
                    
                    if (papers == null || papers.isEmpty()) {
                        // If we're not on page 0 and got empty results, we've reached the end
                        if (currentPage > 0) {
                            // User clicked Next but there are no more results
                            // Rollback to previous page
                            currentPage--;
                            totalPages = currentPage + 1;
                            // Reload the previous page data
                            performSearch();
                            Toast.makeText(SearchActivity.this, "No more papers", Toast.LENGTH_SHORT).show();
                            return; // Exit early, performSearch will handle the rest
                        } else {
                            // Page 0 is empty, show empty state
                            showEmptyState();
                            adapter.clear();
                            totalResults = 0;
                            totalPages = 1;
                            updatePaginationControls();
                        }
                    } else {
                        hideEmptyState();
                        adapter.setPapers(papers);
                        
                        // Load reading progress for papers
                        loadReadingProgress(papers);
                        
                        // Calculate total pages based on results
                        // Note: Backend should return total count, but for now we estimate
                        if (papers.size() < PAGE_SIZE) {
                            // This is the last page
                            totalResults = (currentPage * PAGE_SIZE) + papers.size();
                            totalPages = currentPage + 1;
                        } else {
                            // Got full page, there might be more
                            // We don't know the total, so we'll enable Next button
                            // and calculate pages as we go
                            if (currentPage >= totalPages - 1) {
                                // We're on a new page we haven't seen before
                                totalPages = currentPage + 2; // At least one more page
                            }
                            // totalResults remains unknown until we hit the last page
                        }
                        
                        updatePaginationControls();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Log.e(TAG, "Search error: " + error);
                    
                    // Show user-friendly error message
                    String errorMessage = "Cannot connect to server";
                    if (error != null) {
                        if (error.contains("failed to connect") || error.contains("Unable to resolve host")) {
                            errorMessage = "Server is not running. Please start backend services.";
                        } else if (error.contains("timeout")) {
                            errorMessage = "Connection timeout. Please check your network.";
                        } else {
                            errorMessage = "Error: " + error;
                        }
                    }
                    
                    Toast.makeText(SearchActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    showEmptyState();
                    adapter.clear();
                    totalResults = 0;
                    totalPages = 1;
                    isLoading = false;
                    updatePaginationControls();
                });
            }
        });
    }

    private String buildSearchQuery() {
        StringBuilder queryBuilder = new StringBuilder();
        
        // Add main search query
        if (currentQuery != null && !currentQuery.trim().isEmpty()) {
            queryBuilder.append(currentQuery.trim());
        }
        
        // Add keywords to search query (backend searches in keywords field)
        if (currentFilterData != null && currentFilterData.keywords != null && !currentFilterData.keywords.isEmpty()) {
            if (queryBuilder.length() > 0) {
                queryBuilder.append(" ");
            }
            queryBuilder.append(String.join(" ", currentFilterData.keywords));
        }
        
        String result = queryBuilder.toString().trim();
        return result.isEmpty() ? null : result;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        paginationControls.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        emptyState.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        paginationControls.setVisibility(View.VISIBLE);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload reading progress for currently displayed papers
        // This updates progress when user returns from reading a paper
        if (adapter != null && adapter.getItemCount() > 0) {
            List<PaperResearch> currentPapers = adapter.getPapers();
            if (currentPapers != null && !currentPapers.isEmpty()) {
                loadReadingProgress(currentPapers);
            }
        }
    }
    
    /**
     * Load reading progress for papers from ReadingWorkflow
     */
    private void loadReadingProgress(List<PaperResearch> papers) {
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
                // Use PERSONAL_LIBRARY as collectionId for search papers (not in a collection)
                String collectionId = "PERSONAL_LIBRARY";
                Map<String, Integer> progressMap = new HashMap<>();
                
                for (PaperResearch paper : papers) {
                    try {
                        com.se1853_jv.labverse.domain.infrastructure.workflow.model.ReadingWorkflow workflow = 
                            workflowRepository.getByCompositeKey(userId, paper.getId(), collectionId);
                        
                        if (workflow != null && workflow.getProgress() != null) {
                            progressMap.put(paper.getId(), workflow.getProgress());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error loading progress for paper: " + paper.getId(), e);
                    }
                }
                
                // Update UI on main thread
                runOnUiThread(() -> {
                    adapter.setPaperProgressData(progressMap);
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error loading reading progress", e);
            }
        }).start();
    }
}

