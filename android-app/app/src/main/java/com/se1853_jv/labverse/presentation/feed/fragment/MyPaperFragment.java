package com.se1853_jv.labverse.presentation.feed.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.reflect.TypeToken;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.paper.PaperApiHandler;
import com.se1853_jv.labverse.data.utils.Connectivity;
import com.se1853_jv.labverse.data.utils.ParseFileUtils;
import com.se1853_jv.labverse.data.utils.SessionManager;
import com.se1853_jv.labverse.domain.db.DatabaseClient;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;
import com.se1853_jv.labverse.domain.infrastructure.paper.repo.PaperRepository;
import com.se1853_jv.labverse.domain.infrastructure.workflow.model.ReadingWorkflow;
import com.se1853_jv.labverse.domain.infrastructure.workflow.repo.ReadingWorkflowRepository;
import com.se1853_jv.labverse.domain.enumerate.WorkflowStatus;
import com.se1853_jv.labverse.domain.infrastructure.user.model.Users;
import com.se1853_jv.labverse.domain.infrastructure.user.repo.UserRepository;
import com.se1853_jv.labverse.domain.infrastructure.collection.model.Collections;
import com.se1853_jv.labverse.domain.infrastructure.collection.repo.CollectionRepository;
import com.se1853_jv.labverse.domain.infrastructure.role.model.Roles;
import com.se1853_jv.labverse.domain.infrastructure.role.repo.RoleRepository;
import com.se1853_jv.labverse.presentation.feed.adapter.PersonalLibraryAdapter;
import com.se1853_jv.labverse.presentation.feed.entity.MyPaperItem;
import com.se1853_jv.labverse.presentation.feed.entity.Paper;
import com.se1853_jv.labverse.presentation.feed.entity.Summary;
import com.se1853_jv.labverse.presentation.paper.PdfReaderActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class MyPaperFragment extends Fragment {
    private static final String TAG = "MyPaperFragment";

    MyPaperItem item;
    Button recentlyAdded, recentlyRead, favorites;
    PaperApiHandler paperApiHandler;
    SessionManager sessionManager;
    PaperRepository paperRepository;
    ReadingWorkflowRepository workflowRepository;
    PersonalLibraryAdapter adapter;
    RecyclerView recyclerView;
    List<PaperResearch> allPapers = new ArrayList<>();
    List<String> favoritePaperIds = new ArrayList<>(); // Track favorite papers
    String currentFilter = "recently_added"; // "recently_added", "recently_read", "favorites"
    View rootView;
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_mypaper, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = view;

        // Initialize views
        recentlyAdded = view.findViewById(R.id.recently_added);
        favorites = view.findViewById(R.id.favorites);
        recentlyRead = view.findViewById(R.id.recently_read);
        recyclerView = view.findViewById(R.id.recycler_view_papers);

        // Initialize database and API
        var db = DatabaseClient.getInstance(requireContext()).getAppDatabase();
        paperRepository = db.paperRepository();
        workflowRepository = db.readingWorkflowRepository();
        paperApiHandler = new PaperApiHandler(requireContext());
        sessionManager = new SessionManager(requireContext());

        // Setup RecyclerView
        setupRecyclerView();

        // Load papers from local database first (fast load)
        loadPapersFromLocalDatabase();

        // Sync with backend
        syncWithBackend();

        // Setup tabs
        handleSwitchTabs(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload papers when fragment becomes visible
        Log.d(TAG, "onResume called - reloading papers");
        if (rootView != null) {
            loadPapersFromLocalDatabase();
            syncWithBackend();
        }
    }

    private void setupRecyclerView() {
        adapter = new PersonalLibraryAdapter();
        adapter.setOnPaperClickListener(paper -> {
            // Open PDF Reader
            openPDFReader(paper);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }


    /**
     * Load papers from local Room database for fast load times and offline access
     */
    private void loadPapersFromLocalDatabase() {
        executorService.execute(() -> {
            try {
                List<PaperResearch> papers = paperRepository.getAllPapers();

                // If no papers in database, create mock data
                if (papers == null || papers.isEmpty()) {
                    Log.d(TAG, "No papers found in database, creating mock data");
                    papers = createMockPapers();
                    if (papers != null && !papers.isEmpty()) {
                        paperRepository.insertOrUpdateAll(papers);
                        Log.d(TAG, "Created and saved " + papers.size() + " mock papers to database");
                    }
                }

                // Always ensure ReadingWorkflow and favorites are set up (even if papers already exist)
                if (papers != null && !papers.isEmpty()) {
                    // Create mock ReadingWorkflow data for recently_read tab (if not exists)
                    // This must be done on background thread before updating UI
                    ensureMockReadingWorkflows(papers);

                    // Set favorite papers (first 3 papers) if not already set
                    if (favoritePaperIds.isEmpty()) {
                        favoritePaperIds.clear();
                        for (int i = 0; i < Math.min(3, papers.size()); i++) {
                            favoritePaperIds.add(papers.get(i).getId());
                        }
                        Log.d(TAG, "Set favorite papers: " + favoritePaperIds.size() + " papers");
                    }
                }

                // Create final variable for use in lambda
                final List<PaperResearch> finalPapers = papers != null ? papers : new ArrayList<>();

                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    allPapers = finalPapers;
                    Log.d(TAG, "Loaded " + allPapers.size() + " papers from local database");
                    // Apply filter after ensuring workflows are created
                    applyFilter();
                    updateSummary();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading papers from local database", e);
            }
        });
    }

    /**
     * Create mock papers for testing/demo purposes
     */
    private List<PaperResearch> createMockPapers() {
        List<PaperResearch> mockPapers = new ArrayList<>();

        // Mock Paper 1
        PaperResearch paper1 = PaperResearch.builder()
                .id("mock-paper-1")
                .title("Deep Learning for Computer Vision: A Comprehensive Survey")
                .authors("John Smith, Jane Doe, Michael Johnson")
                .journal("IEEE Transactions on Pattern Analysis and Machine Intelligence")
                .publicationYear(2023)
                .doi("10.1109/TPAMI.2023.1234567")
                .description("This paper provides a comprehensive survey of deep learning techniques applied to computer vision tasks.")
                .dataUrl("https://example.com/pdfs/deep-learning-cv.pdf")
                .keyword(java.util.Arrays.asList("deep learning", "computer vision", "neural networks"))
                .build();
        mockPapers.add(paper1);

        // Mock Paper 2
        PaperResearch paper2 = PaperResearch.builder()
                .id("mock-paper-2")
                .title("Natural Language Processing with Transformers")
                .authors("Sarah Williams, David Brown")
                .journal("Journal of Artificial Intelligence Research")
                .publicationYear(2024)
                .doi("10.1613/jair.2024.12345")
                .description("An in-depth analysis of transformer architectures in modern NLP applications.")
                .dataUrl("https://example.com/pdfs/nlp-transformers.pdf")
                .keyword(java.util.Arrays.asList("NLP", "transformers", "BERT", "GPT"))
                .build();
        mockPapers.add(paper2);

        // Mock Paper 3
        PaperResearch paper3 = PaperResearch.builder()
                .id("mock-paper-3")
                .title("Reinforcement Learning in Autonomous Systems")
                .authors("Robert Chen, Emily Zhang, Alex Kumar")
                .journal("Nature Machine Intelligence")
                .publicationYear(2023)
                .doi("10.1038/s42256-023-00123-4")
                .description("Exploring reinforcement learning algorithms for autonomous vehicle navigation.")
                .dataUrl("https://example.com/pdfs/rl-autonomous.pdf")
                .keyword(java.util.Arrays.asList("reinforcement learning", "autonomous systems", "robotics"))
                .build();
        mockPapers.add(paper3);

        // Mock Paper 4
        PaperResearch paper4 = PaperResearch.builder()
                .id("mock-paper-4")
                .title("Quantum Computing: Current State and Future Prospects")
                .authors("Maria Garcia, James Wilson")
                .journal("Science")
                .publicationYear(2024)
                .doi("10.1126/science.2024.5678")
                .description("A review of quantum computing technologies and their potential applications.")
                .dataUrl("https://example.com/pdfs/quantum-computing.pdf")
                .keyword(java.util.Arrays.asList("quantum computing", "quantum algorithms", "quantum supremacy"))
                .build();
        mockPapers.add(paper4);

        // Mock Paper 5
        PaperResearch paper5 = PaperResearch.builder()
                .id("mock-paper-5")
                .title("Blockchain Technology in Supply Chain Management")
                .authors("Lisa Anderson, Tom Martinez")
                .journal("Harvard Business Review")
                .publicationYear(2023)
                .doi("10.1016/j.hbr.2023.98765")
                .description("Examining the impact of blockchain on modern supply chain operations.")
                .dataUrl("https://example.com/pdfs/blockchain-supply-chain.pdf")
                .keyword(java.util.Arrays.asList("blockchain", "supply chain", "distributed ledger"))
                .build();
        mockPapers.add(paper5);

        // Mock Paper 6
        PaperResearch paper6 = PaperResearch.builder()
                .id("mock-paper-6")
                .title("Machine Learning for Drug Discovery")
                .authors("Dr. Anna Lee, Dr. Peter Kim")
                .journal("Nature Biotechnology")
                .publicationYear(2024)
                .doi("10.1038/nbt.2024.11111")
                .description("Application of machine learning techniques in pharmaceutical research.")
                .dataUrl("https://example.com/pdfs/ml-drug-discovery.pdf")
                .keyword(java.util.Arrays.asList("machine learning", "drug discovery", "pharmaceuticals", "bioinformatics"))
                .build();
        mockPapers.add(paper6);

        // Mock Paper 7
        PaperResearch paper7 = PaperResearch.builder()
                .id("mock-paper-7")
                .title("Edge Computing and IoT: A Survey")
                .authors("Kevin Park, Rachel Green")
                .journal("ACM Computing Surveys")
                .publicationYear(2023)
                .doi("10.1145/1234567.1234567")
                .description("Comprehensive survey of edge computing architectures for IoT applications.")
                .dataUrl("https://example.com/pdfs/edge-computing-iot.pdf")
                .keyword(java.util.Arrays.asList("edge computing", "IoT", "distributed systems"))
                .build();
        mockPapers.add(paper7);

        // Mock Paper 8
        PaperResearch paper8 = PaperResearch.builder()
                .id("mock-paper-8")
                .title("Cybersecurity in the Age of AI")
                .authors("Daniel White, Olivia Black")
                .journal("IEEE Security & Privacy")
                .publicationYear(2024)
                .doi("10.1109/MSEC.2024.2222222")
                .description("Analyzing AI-powered security threats and defense mechanisms.")
                .dataUrl("https://example.com/pdfs/ai-cybersecurity.pdf")
                .keyword(java.util.Arrays.asList("cybersecurity", "artificial intelligence", "threat detection"))
                .build();
        mockPapers.add(paper8);

        return mockPapers;
    }

    /**
     * Ensure mock ReadingWorkflow data exists for recently_read tab
     * This method will create workflows if they don't exist, even if papers already exist in database
     */
    private void ensureMockReadingWorkflows(List<PaperResearch> papers) {
        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            Log.w(TAG, "User ID is null, cannot create ReadingWorkflow mock data");
            return;
        }

        String collectionId = "PERSONAL_LIBRARY";

        try {
            int createdCount = 0;
            int existingCount = 0;
            Log.d(TAG, "Ensuring ReadingWorkflow for " + Math.min(5, papers.size()) + " papers");

            // Create ReadingWorkflow for first 5 papers with different progress levels
            for (int i = 0; i < Math.min(5, papers.size()); i++) {
                PaperResearch paper = papers.get(i);
                String paperId = paper.getId();

                try {
                    // Ensure foreign key entities exist before creating workflow
                    if (!ensureForeignKeysExist(userId, paperId, collectionId)) {
                        Log.e(TAG, "Failed to ensure foreign keys exist for paper " + paperId + ", skipping workflow creation");
                        continue;
                    }

                    // Check if workflow already exists
                    ReadingWorkflow existing = workflowRepository.getByCompositeKey(userId, paperId, collectionId);
                    if (existing != null) {
                        existingCount++;
                        Log.d(TAG, "ReadingWorkflow already exists for paper " + paperId + " with progress " + existing.getProgress() + "%");
                        continue; // Skip if already exists
                    }

                    // Create workflow with different progress levels
                    int progress = 0;
                    WorkflowStatus status = WorkflowStatus.UNREAD;

                    switch (i) {
                        case 0: // Paper 1: 25% progress
                            progress = 25;
                            status = WorkflowStatus.READING;
                            break;
                        case 1: // Paper 2: 50% progress
                            progress = 50;
                            status = WorkflowStatus.READING;
                            break;
                        case 2: // Paper 3: 75% progress
                            progress = 75;
                            status = WorkflowStatus.READING;
                            break;
                        case 3: // Paper 4: 100% progress (finished)
                            progress = 100;
                            status = WorkflowStatus.FINISHED;
                            break;
                        case 4: // Paper 5: 10% progress
                            progress = 10;
                            status = WorkflowStatus.READING;
                            break;
                    }

                    ReadingWorkflow workflow = ReadingWorkflow.builder()
                            .userId(userId)
                            .paperId(paperId)
                            .collectionId(collectionId)
                            .status(status)
                            .progress(progress)
                            .lastPage(progress / 10) // Approximate page based on progress
                            .build();

                    workflowRepository.create(workflow);
                    createdCount++;
                    Log.d(TAG, "Created ReadingWorkflow for paper " + paperId + " with progress " + progress + "%");
                } catch (Exception e) {
                    Log.e(TAG, "Error creating/checking workflow for paper " + paperId, e);
                    e.printStackTrace();
                }
            }

            Log.d(TAG, "ReadingWorkflow summary - Created: " + createdCount + ", Already exists: " + existingCount);
        } catch (Exception e) {
            Log.e(TAG, "Error ensuring mock ReadingWorkflow data", e);
            e.printStackTrace();
        }
    }

    /**
     * Ensure all foreign key entities exist before creating ReadingWorkflow
     * Returns true if all entities exist or were created successfully
     */
    private boolean ensureForeignKeysExist(String userId, String paperId, String collectionId) {
        try {
            var db = DatabaseClient.getInstance(requireContext()).getAppDatabase();

            // Ensure User exists
            var userRepository = db.userRepository();
            Users user = userRepository.getById(userId);
            if (user == null) {
                Log.w(TAG, "User not found in database: " + userId + ". Creating minimal user record.");
                // Create minimal user record from SessionManager
                String email = sessionManager.getEmail();
                String username = sessionManager.getUsername();
                String fullName = sessionManager.getFullName();
                String role = sessionManager.getRole();

                // Use default roleId if role is not available
                String roleId = "role_researcher"; // Default role
                if (role != null) {
                    // Map role string to roleId
                    if (role.contains("PI") || role.contains("Principal")) {
                        roleId = "role_pi";
                    } else if (role.contains("Student") || role.contains("Intern")) {
                        roleId = "role_student";
                    }
                }

                // Ensure role exists in Roles table
                var roleRepository = db.roleRepository();
                Roles roleEntity = roleRepository.getById(roleId);
                if (roleEntity == null) {
                    // Create role if it doesn't exist
                    roleEntity = Roles.builder()
                            .id(roleId)
                            .role(com.se1853_jv.labverse.domain.enumerate.Role.RESEARCHER) // Default to RESEARCHER
                            .build();
                    try {
                        roleRepository.create(roleEntity);
                        Log.d(TAG, "Created role: " + roleId);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to create role: " + roleId, e);
                        // Continue anyway - might already exist
                    }
                }

                long currentTime = System.currentTimeMillis();
                user = Users.builder()
                        .id(userId)
                        .email(email != null ? email : userId + "@labverse.com")
                        .password("") // Empty password for minimal record
                        .name(fullName != null ? fullName : "User")
                        .username(username != null ? username : userId)
                        .createdDate(currentTime)
                        .updatedDate(currentTime)
                        .roleId(roleId)
                        .avatarUrl(sessionManager.getAvatarUrl())
                        .build();
                try {
                    userRepository.create(user);
                    Log.d(TAG, "Created minimal user record: " + userId);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to create user: " + userId, e);
                    return false;
                }
            }

            // Ensure PaperResearch exists (should already exist since we're creating workflows for existing papers)
            var paperRepository = db.paperRepository();
            PaperResearch paper = paperRepository.getById(paperId);
            if (paper == null) {
                Log.w(TAG, "Paper not found in database: " + paperId);
                return false; // Paper must exist
            }

            // Ensure Collection exists
            var collectionRepository = db.collectionRepository();
            Collections collection = collectionRepository.getById(collectionId);
            if (collection == null) {
                // Create collection if it doesn't exist
                if ("PERSONAL_LIBRARY".equals(collectionId)) {
                    collection = Collections.builder()
                            .id("PERSONAL_LIBRARY")
                            .name("Personal Library")
                            .build();
                    try {
                        collectionRepository.create(collection);
                        Log.d(TAG, "Created personal library collection");
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to create personal library collection", e);
                        return false;
                    }
                } else {
                    Log.w(TAG, "Collection not found: " + collectionId);
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error ensuring foreign keys exist", e);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sync with backend to fetch user's library metadata
     */
    private void syncWithBackend() {
        if (!Connectivity.isInternetAvailable(requireContext())) {
            Log.d(TAG, "No internet connection, using local data only");
            return;
        }

        // Get current user ID
        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "User ID is null or empty");
            return;
        }

        Log.d(TAG, "Syncing with backend for userId: " + userId);
        Log.d("MyPaperFragment", "Loading papers for userId: " + userId);

        // userId từ SessionManager đã được encode từ backend khi login
        // Không cần encode lại, sử dụng trực tiếp
        Log.d("MyPaperFragment", "Using userId directly (already encoded from backend): " + userId);

        // Load papers by user ID from API
        paperApiHandler.getPapersByUserId(userId, new ApiCallback<>() {
            @Override
            public void onSuccess(List<PaperResearch> paperResearchList) {
                if (getActivity() == null) return;

                Log.d(TAG, "Papers synced from backend: " + (paperResearchList != null ? paperResearchList.size() : 0) + " papers");

                // Save to local database
                executorService.execute(() -> {
                    try {
                        if (paperResearchList != null && !paperResearchList.isEmpty()) {
                            paperRepository.insertOrUpdateAll(paperResearchList);
                            Log.d(TAG, "Saved " + paperResearchList.size() + " papers to local database");
                        }

                        // Reload from local database
                        List<PaperResearch> papers = paperRepository.getAllPapers();

                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(() -> {
                            allPapers = papers;
                            applyFilter();
                            updateSummary();
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error saving papers to local database", e);
                    }
                });
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null) return;
                Log.e(TAG, "Error syncing papers from backend: " + error);
                // Continue using local data
            }
        });
    }

    /**
     * Apply filter based on current tab selection
     */
    private void applyFilter() {
        Log.d(TAG, "Applying filter: " + currentFilter + ", total papers: " + allPapers.size());

        String userId = sessionManager.getUserId();
        String collectionId = "PERSONAL_LIBRARY";
        boolean showProgress = "recently_read".equals(currentFilter);

        // For recently_read tab, load progress data on background thread
        if (showProgress && userId != null && !userId.isEmpty()) {
            Log.d(TAG, "Loading recently_read papers - userId: " + userId + ", collectionId: " + collectionId + ", total papers: " + allPapers.size());
            executorService.execute(() -> {
                try {
                    List<PaperResearch> filteredPapers = new ArrayList<>();
                    Map<String, Integer> progressMap = new HashMap<>();

                    Log.d(TAG, "Checking workflows for " + allPapers.size() + " papers");
                    for (PaperResearch paper : allPapers) {
                        try {
                            String paperId = paper.getId();
                            Log.d(TAG, "Checking workflow for paper: " + paperId);
                            ReadingWorkflow workflow = workflowRepository.getByCompositeKey(userId, paperId, collectionId);

                            if (workflow != null) {
                                Integer progress = workflow.getProgress();
                                Log.d(TAG, "Found workflow for paper " + paperId + " - progress: " + progress);
                                if (progress != null && progress > 0) {
                                    filteredPapers.add(paper);
                                    progressMap.put(paperId, progress);
                                    Log.d(TAG, "Added paper " + paperId + " with progress " + progress + "%");
                                } else {
                                    Log.d(TAG, "Paper " + paperId + " has workflow but progress is 0 or null");
                                }
                            } else {
                                Log.d(TAG, "No workflow found for paper: " + paperId);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error loading workflow for paper: " + paper.getId(), e);
                        }
                    }

                    Log.d(TAG, "Found " + filteredPapers.size() + " papers with progress > 0");

                    // Sort by progress descending (most read first)
                    filteredPapers.sort((a, b) -> {
                        Integer progressA = progressMap.get(a.getId());
                        Integer progressB = progressMap.get(b.getId());
                        if (progressA == null) progressA = 0;
                        if (progressB == null) progressB = 0;
                        return progressB.compareTo(progressA);
                    });

                    Log.d(TAG, "Recently read filtered papers count: " + filteredPapers.size() + ", progress map size: " + progressMap.size());

                    if (getActivity() == null) {
                        Log.w(TAG, "Activity is null, cannot update UI");
                        return;
                    }
                    getActivity().runOnUiThread(() -> {
                        adapter.setPapers(filteredPapers);
                        adapter.setShowProgressBar(true);
                        adapter.setPaperProgressData(progressMap);
                        Log.d(TAG, "Updated adapter with " + filteredPapers.size() + " papers for recently_read tab");
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error applying filter for recently_read", e);
                    e.printStackTrace();
                }
            });
            return;
        }

        // For other tabs, filter on main thread
        List<PaperResearch> filteredPapers = new ArrayList<>();

        switch (currentFilter) {
            case "recently_added":
                // Show all papers, sorted by ID descending (newest first)
                filteredPapers = new ArrayList<>(allPapers);
                filteredPapers.sort((a, b) -> b.getId().compareTo(a.getId()));
                break;
            case "favorites":
                // Show only favorite papers
                Log.d(TAG, "Filtering favorites - favoritePaperIds size: " + favoritePaperIds.size());
                for (PaperResearch paper : allPapers) {
                    if (favoritePaperIds.contains(paper.getId())) {
                        filteredPapers.add(paper);
                        Log.d(TAG, "Added favorite paper: " + paper.getId());
                    }
                }
                Log.d(TAG, "Favorites filtered papers count: " + filteredPapers.size());
                break;
            default:
                filteredPapers = new ArrayList<>(allPapers);
        }

        Log.d(TAG, "Filtered papers count: " + filteredPapers.size());
        adapter.setPapers(filteredPapers);
        adapter.setShowProgressBar(false);
        adapter.setPaperProgressData(new HashMap<>());

        // Show empty state if no papers
        if (filteredPapers.isEmpty()) {
            Log.d(TAG, "No papers to display, showing empty state");
            // TODO: Show empty state view
        }
    }

    /**
     * Update summary statistics
     */
    private void updateSummary() {
        Summary summary = createSummary(allPapers);
        item = new MyPaperItem(convertToPaperList(allPapers), null, summary);
        buildStatisticCards(getLayoutInflater(), rootView);
    }

    private Paper convertToPaper(PaperResearch paperResearch) {
        Paper paper = new Paper();
        paper.setId(paperResearch.getId());
        paper.setTitle(paperResearch.getTitle() != null ? paperResearch.getTitle() : "Untitled");
        paper.setAuthors(paperResearch.getAuthors() != null ? paperResearch.getAuthors() : "Unknown");
        paper.setJournal(paperResearch.getJournal() != null ? paperResearch.getJournal() : "Unknown");
        paper.setYear(paperResearch.getPublicationYear() != null ? paperResearch.getPublicationYear() : 0);
        paper.setStatus("in progress"); // Default status, can be updated later
        return paper;
    }

    private Summary createSummary(List<PaperResearch> papers) {
        Summary summary = new Summary();
        summary.setPapers(papers.size());
        summary.setCollections(0); // TODO: Track read papers
        summary.setTeamProjects(0); // TODO: Track favorite papers
        return summary;
    }

    private List<Paper> convertToPaperList(List<PaperResearch> paperResearchList) {
        if (paperResearchList == null || paperResearchList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Paper> result = new ArrayList<>();
        for (PaperResearch pr : paperResearchList) {
            Paper paper = new Paper();
            paper.setId(pr.getId());
            paper.setTitle(pr.getTitle() != null ? pr.getTitle() : "Untitled");
            paper.setAuthors(pr.getAuthors() != null ? pr.getAuthors() : "Unknown");
            paper.setJournal(pr.getJournal() != null ? pr.getJournal() : "Unknown");
            paper.setYear(pr.getPublicationYear() != null ? pr.getPublicationYear() : 0);
            paper.setStatus("in progress");
            result.add(paper);
        }
        return result;
    }

    private void openPDFReader(PaperResearch paper) {
        if (paper == null || paper.getId() == null) {
            Toast.makeText(requireContext(), "Paper information not available", Toast.LENGTH_SHORT).show();
            return;
        }

        String pdfUrl = paper.getDataUrl();
        if (pdfUrl == null || pdfUrl.isEmpty()) {
            Toast.makeText(requireContext(), "PDF URL not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(requireContext(), PdfReaderActivity.class);
        intent.putExtra("paperId", paper.getId());
        intent.putExtra("collectionId", "");
        intent.putExtra("pdfUrl", pdfUrl);
        startActivity(intent);
    }

    // <editor-fold> desc="Build statistic cards"
    private void buildStatisticCards(@NonNull LayoutInflater inflater, @NonNull View view) {
        LinearLayout container = view.findViewById(R.id.cards);
        container.removeAllViews();

        Summary summary = (item != null && item.getSummary() != null) ? item.getSummary() : createSummary(allPapers);
        var context = rootView.getContext();

        buildPapersCard(summary, container, inflater, context);
        buildCollectionsCard(summary, container, inflater, context);
        buildTeamProjectsCard(summary, container, inflater, context);
    }

    private void buildPapersCard(@NonNull Summary summary, LinearLayout container, @NonNull LayoutInflater inflater, @NonNull android.content.Context context) {
        View card = inflater.inflate(R.layout.layout_mypaper_stat_card, container, false);
        card.setBackgroundColor(ContextCompat.getColor(context, R.color.blue_50));

        TextView statValue = card.findViewById(R.id.tv_stat_value);
        statValue.setText(String.valueOf(summary.getPapers()));
        statValue.setTextColor(ContextCompat.getColor(context, R.color.blue_600));

        TextView statLabel = card.findViewById(R.id.tv_stat_label);
        statLabel.setText(R.string.papers);
        statLabel.setTextColor(ContextCompat.getColor(context, R.color.blue_400));
        container.addView(card);
    }

    private void buildCollectionsCard(@NonNull Summary summary, LinearLayout container, @NonNull LayoutInflater inflater, @NonNull android.content.Context context) {
        View card = inflater.inflate(R.layout.layout_mypaper_stat_card, container, false);
        card.setBackgroundColor(ContextCompat.getColor(context, R.color.first_green));

        TextView statValue = card.findViewById(R.id.tv_stat_value);
        statValue.setText(String.valueOf(summary.getCollections()));
        statValue.setTextColor(ContextCompat.getColor(context, R.color.seventh_green));

        TextView statLabel = card.findViewById(R.id.tv_stat_label);
        statLabel.setText(R.string.collections);
        statLabel.setTextColor(ContextCompat.getColor(context, R.color.third_green));
        container.addView(card);
    }

    private void buildTeamProjectsCard(@NonNull Summary summary, LinearLayout container, @NonNull LayoutInflater inflater, @NonNull android.content.Context context) {
        View card = inflater.inflate(R.layout.layout_mypaper_stat_card, container, false);
        card.setBackgroundColor(ContextCompat.getColor(context, R.color.skin));

        TextView statValue = card.findViewById(R.id.tv_stat_value);
        statValue.setText(String.valueOf(summary.getTeamProjects()));
        statValue.setTextColor(ContextCompat.getColor(context, R.color.yellow));

        TextView statLabel = card.findViewById(R.id.tv_stat_label);
        statLabel.setText(R.string.team_projects);
        statLabel.setTextColor(ContextCompat.getColor(context, R.color.brown));
        container.addView(card);
    }
    // </editor-fold>

    private void handleSwitchTabs(@NonNull View view) {
        switchToRecentlyAdded(view);
        switchToRecentlyRead(view);
        switchToFavorites(view);
    }

    private void switchToRecentlyAdded(View view) {
        recentlyAdded.setOnClickListener(v -> {
            currentFilter = "recently_added";
            recentlyAdded.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.blue));
            recentlyAdded.setTextColor(ContextCompat.getColor(view.getContext(), R.color.white));

            changeRecentlyReadTabColorIntoDefault(view);
            changeFavoritesTabColorIntoDefault(view);
            applyFilter();
        });
    }

    private void switchToRecentlyRead(View view) {
        recentlyRead.setOnClickListener(v -> {
            currentFilter = "recently_read";
            recentlyRead.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.blue));
            recentlyRead.setTextColor(ContextCompat.getColor(view.getContext(), R.color.white));

            changeRecentlyAddedTabColorIntoDefault(view);
            changeFavoritesTabColorIntoDefault(view);
            applyFilter();
        });
    }

    private void switchToFavorites(View view) {
        favorites.setOnClickListener(v -> {
            currentFilter = "favorites";
            favorites.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.blue));
            favorites.setTextColor(ContextCompat.getColor(view.getContext(), R.color.white));

            changeRecentlyReadTabColorIntoDefault(view);
            changeRecentlyAddedTabColorIntoDefault(view);
            applyFilter();
        });
    }

    private void changeRecentlyAddedTabColorIntoDefault(@NonNull View view) {
        recentlyAdded.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.dark_gray));
        recentlyAdded.setTextColor(ContextCompat.getColor(view.getContext(), R.color.black));
    }

    private void changeRecentlyReadTabColorIntoDefault(@NonNull View view) {
        recentlyRead.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.dark_gray));
        recentlyRead.setTextColor(ContextCompat.getColor(view.getContext(), R.color.black));
    }

    private void changeFavoritesTabColorIntoDefault(@NonNull View view) {
        favorites.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.dark_gray));
        favorites.setTextColor(ContextCompat.getColor(view.getContext(), R.color.black));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}