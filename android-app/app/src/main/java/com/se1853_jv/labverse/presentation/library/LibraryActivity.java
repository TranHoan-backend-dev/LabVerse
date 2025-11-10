package com.se1853_jv.labverse.presentation.library;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.presentation.collection.CollectionsActivity;
import com.se1853_jv.labverse.presentation.common.BaseActivity;
import com.se1853_jv.labverse.presentation.common.HeaderHelper;
import com.se1853_jv.labverse.presentation.library.adapter.LibraryPaperAdapter;
import com.se1853_jv.labverse.presentation.library.data.LibraryMockDataProvider;
import com.se1853_jv.labverse.presentation.library.model.LibraryPaper;
import com.se1853_jv.labverse.presentation.paper.ImportPaperManuallyActivity;

import java.util.List;

public class LibraryActivity extends BaseActivity {
    private static final String TAG = "LibraryActivity";
    
    private MaterialButton btnRecentlyAdded, btnRecentlyRead, btnFavorites;
    private MaterialButton btnUnread, btnReading;
    private RecyclerView recyclerView;
    private LibraryPaperAdapter adapter;
    private FloatingActionButton fabImportPaper;
    private MaterialButton btnFilter;
    
    private LibraryMockDataProvider dataProvider;
    private String currentTab = "recently_added";
    private String currentFilter = "unread";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.library_root), (v, insets) -> {
            var statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(statusBar.left, statusBar.top, statusBar.right, statusBar.bottom);
            return insets;
        });
        
        setupBottomNavbar(findViewById(R.id.library_root), R.id.bottomNav);
        setupHeader();
        
        // Initialize data provider
        dataProvider = LibraryMockDataProvider.getInstance();
        
        initViews();
        setupFilterButtons();
        setupSubTabs();
        setupRecyclerView();
        setupFAB();
        
        // Load initial data
        loadPapers(currentTab);
        
        // Setup notification button
        HeaderHelper.setupNotificationClickListener(this);
        HeaderHelper.loadNotificationBadge(this);
    }
    
    private void setupHeader() {
        // Header is already included in layout, no additional setup needed
        // The header will show avatar and notification icons automatically
    }
    
    private void initViews() {
        btnRecentlyAdded = findViewById(R.id.btn_recently_added);
        btnRecentlyRead = findViewById(R.id.btn_recently_read);
        btnFavorites = findViewById(R.id.btn_favorites);
        btnUnread = findViewById(R.id.btn_unread);
        btnReading = findViewById(R.id.btn_reading);
        recyclerView = findViewById(R.id.recycler_papers);
        fabImportPaper = findViewById(R.id.fab_import_paper);
        btnFilter = findViewById(R.id.btn_filter);
    }
    
    private void setupFilterButtons() {
        btnUnread.setOnClickListener(v -> {
            currentFilter = "unread";
            updateFilterUI("unread");
            loadPapers(currentTab);
        });
        
        btnReading.setOnClickListener(v -> {
            currentFilter = "reading";
            updateFilterUI("reading");
            loadPapers(currentTab);
        });
    }
    
    private void updateFilterUI(String filter) {
        if ("unread".equals(filter)) {
            // Unread active
            btnUnread.setBackgroundTintList(getColorStateList(R.color.blue_400));
            btnUnread.setTextColor(getColor(R.color.white));
            btnUnread.setStrokeWidth(0);
            
            // Reading inactive
            btnReading.setBackgroundTintList(getColorStateList(android.R.color.transparent));
            btnReading.setTextColor(getColor(R.color.gray_500));
            btnReading.setStrokeWidth(2);
            btnReading.setStrokeColor(getColorStateList(R.color.gray_200));
        } else {
            // Reading active
            btnReading.setBackgroundTintList(getColorStateList(R.color.blue_400));
            btnReading.setTextColor(getColor(R.color.white));
            btnReading.setStrokeWidth(0);
            
            // Unread inactive
            btnUnread.setBackgroundTintList(getColorStateList(android.R.color.transparent));
            btnUnread.setTextColor(getColor(R.color.gray_500));
            btnUnread.setStrokeWidth(2);
            btnUnread.setStrokeColor(getColorStateList(R.color.gray_200));
        }
    }
    
    private void setupSubTabs() {
        btnRecentlyAdded.setOnClickListener(v -> switchTab("recently_added"));
        btnRecentlyRead.setOnClickListener(v -> switchTab("recently_read"));
        btnFavorites.setOnClickListener(v -> switchTab("favorites"));
        
        // Set initial tab state
        updateTabUI("recently_added");
    }
    
    private void switchTab(String tab) {
        currentTab = tab;
        updateTabUI(tab);
        loadPapers(tab);
    }
    
    private void updateTabUI(String activeTab) {
        // Reset all tabs to transparent background
        btnRecentlyAdded.setBackgroundTintList(getColorStateList(android.R.color.transparent));
        btnRecentlyAdded.setTextColor(getColor(R.color.gray_500));
        btnRecentlyRead.setBackgroundTintList(getColorStateList(android.R.color.transparent));
        btnRecentlyRead.setTextColor(getColor(R.color.gray_500));
        btnFavorites.setBackgroundTintList(getColorStateList(android.R.color.transparent));
        btnFavorites.setTextColor(getColor(R.color.gray_500));
        
        // Set active tab with blue background
        switch (activeTab) {
            case "recently_added":
                btnRecentlyAdded.setBackgroundTintList(getColorStateList(R.color.blue_400));
                btnRecentlyAdded.setTextColor(getColor(R.color.white));
                break;
            case "recently_read":
                btnRecentlyRead.setBackgroundTintList(getColorStateList(R.color.blue_400));
                btnRecentlyRead.setTextColor(getColor(R.color.white));
                break;
            case "favorites":
                btnFavorites.setBackgroundTintList(getColorStateList(R.color.blue_400));
                btnFavorites.setTextColor(getColor(R.color.white));
                break;
        }
    }
    
    private void setupRecyclerView() {
        adapter = new LibraryPaperAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    
    private void setupFAB() {
        fabImportPaper.setOnClickListener(v -> {
            Intent intent = new Intent(this, ImportPaperManuallyActivity.class);
            startActivity(intent);
        });
        
        btnFilter.setOnClickListener(v -> {
            android.widget.Toast.makeText(this, "Filter options", android.widget.Toast.LENGTH_SHORT).show();
        });
    }
    
    private void loadPapers(String tab) {
        List<LibraryPaper> papers;
        
        // Load papers from data provider based on current tab and filter
        if (currentFilter != null && !currentFilter.isEmpty()) {
            papers = dataProvider.getPapersByTabAndStatus(tab, currentFilter);
        } else {
            papers = dataProvider.getPapersByTab(tab);
        }
        
        // Enrich papers with reading progress from database
        enrichPapersWithReadingProgress(papers);
        
        adapter.setPapers(papers);
    }
    
    /**
     * Load reading progress from ReadingWorkflow database and update LibraryPaper objects
     */
    private void enrichPapersWithReadingProgress(List<LibraryPaper> papers) {
        if (papers == null || papers.isEmpty()) {
            return;
        }
        
        // Get user ID
        com.se1853_jv.labverse.data.utils.SessionManager sessionManager = 
            new com.se1853_jv.labverse.data.utils.SessionManager(this);
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
                String collectionId = "PERSONAL_LIBRARY"; // Special collection ID for personal library
                
                for (LibraryPaper paper : papers) {
                    try {
                        com.se1853_jv.labverse.domain.infrastructure.workflow.model.ReadingWorkflow workflow = 
                            workflowRepository.getByCompositeKey(userId, paper.getId(), collectionId);
                        
                        if (workflow != null) {
                            // Update progress
                            paper.setProgress(workflow.getProgress() != null ? workflow.getProgress() : 0);
                            
                            // Update status based on WorkflowStatus enum
                            com.se1853_jv.labverse.domain.enumerate.WorkflowStatus status = workflow.getStatus();
                            if (status != null) {
                                switch (status) {
                                    case UNREAD:
                                        paper.setStatus("Unread");
                                        paper.setStatusColor("blue");
                                        break;
                                    case READING:
                                        paper.setStatus("Reading");
                                        paper.setStatusColor("yellow");
                                        break;
                                    case FINISHED:
                                        paper.setStatus("Finished");
                                        paper.setStatusColor("green");
                                        break;
                                }
                            }
                        } else {
                            // No workflow found, set default values
                            paper.setProgress(0);
                            paper.setStatus("Unread");
                            paper.setStatusColor("blue");
                        }
                    } catch (Exception e) {
                        android.util.Log.e(TAG, "Error loading progress for paper: " + paper.getId(), e);
                    }
                }
                
                // Update UI on main thread
                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                });
                
            } catch (Exception e) {
                android.util.Log.e(TAG, "Error enriching papers with reading progress", e);
            }
        }).start();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        HeaderHelper.loadNotificationBadge(this);
        // Reload papers to update reading progress
        loadPapers(currentTab);
    }
}
