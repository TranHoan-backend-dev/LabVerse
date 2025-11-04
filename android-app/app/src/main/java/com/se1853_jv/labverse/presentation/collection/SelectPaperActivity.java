package com.se1853_jv.labverse.presentation.collection;

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
    
    private CollectionResponse collection;
    private PaperApiHandler paperApiHandler;
    private CollectionApiHandler collectionApiHandler;
    
    private MaterialToolbar toolbar;
    private EditText editSearch;
    private RecyclerView recyclerPapers;
    private TextView textEmptyState;
    private ProgressBar progressBar;
    private SelectPaperAdapter adapter;
    
    private List<PaperResearch> allPapers = new ArrayList<>();
    private List<PaperResearch> filteredPapers = new ArrayList<>();
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

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
        loadPapers();
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
            getSupportActionBar().setTitle("Add Paper to Collection");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new SelectPaperAdapter(new ArrayList<>(), paper -> {
            addPaperToCollection(paper);
        });
        recyclerPapers.setLayoutManager(new LinearLayoutManager(this));
        recyclerPapers.setAdapter(adapter);
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
            // Show all papers if search is empty
            filterPapers("");
        } else {
            // Filter locally first
            filterPapers(query);
            
            // Then search on server
            searchPapersOnServer(query);
        }
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

    private void searchPapersOnServer(String query) {
        if (!Connectivity.isInternetAvailable(this)) {
            return;
        }

        showLoading(true);
        
        paperApiHandler.getAllPapers(query, new ApiCallback<List<PaperResearch>>() {
            @Override
            public void onSuccess(List<PaperResearch> response) {
                runOnUiThread(() -> {
                    showLoading(false);
                    if (response != null && !response.isEmpty()) {
                        // Merge new results with existing ones
                        for (PaperResearch paper : response) {
                            if (!containsPaper(allPapers, paper.getId())) {
                                allPapers.add(paper);
                            }
                        }
                        filterPapers(query);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    android.util.Log.e(TAG, "Error searching papers: " + error);
                });
            }
        });
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

    private void loadPapers() {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            updateEmptyState();
            return;
        }

        showLoading(true);

        paperApiHandler.getAllPapers(null, new ApiCallback<List<PaperResearch>>() {
            @Override
            public void onSuccess(List<PaperResearch> response) {
                runOnUiThread(() -> {
                    showLoading(false);
                    allPapers.clear();
                    if (response != null) {
                        allPapers.addAll(response);
                    }
                    filterPapers(editSearch.getText().toString());
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    android.util.Log.e(TAG, "Error loading papers: " + error);
                    Toast.makeText(SelectPaperActivity.this,
                            "Failed to load papers: " + error,
                            Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });
            }
        });
    }

    private void addPaperToCollection(PaperResearch paper) {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionPaperRequest request = new CollectionPaperRequest();
        request.setCollectionId(collection.getId());
        request.setPaperId(paper.getId());
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
                    Toast.makeText(SelectPaperActivity.this,
                            "Failed to add paper: " + error,
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

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


