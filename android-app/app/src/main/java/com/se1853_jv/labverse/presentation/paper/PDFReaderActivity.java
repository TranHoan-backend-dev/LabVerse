package com.se1853_jv.labverse.presentation.paper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnTapListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.annotation.AnnotationApiHandler;
import com.se1853_jv.labverse.data.service.storage.RemotePdfService;
import com.se1853_jv.labverse.data.sync.OfflineSyncHelper;
import com.se1853_jv.labverse.domain.infrastructure.annotation.model.Highlight;
import com.se1853_jv.labverse.domain.infrastructure.annotation.model.Note;
import com.se1853_jv.labverse.domain.infrastructure.workflow.model.ReadingWorkflow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * PDF Reader Activity - Phần 5: Integrated PDF Reader
 * 
 * Features:
 * - Render PDF với AndroidPdfViewer
 * - User tap để add note
 * - User long press để highlight
 * - Display annotations overlay
 * - Offline support với OfflineSyncHelper (Phần 11)
 */
public class PDFReaderActivity extends AppCompatActivity {
    private static final String TAG = "PDFReaderActivity";
    
    private PDFView pdfView;
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private OfflineSyncHelper syncHelper;
    private AnnotationApiHandler apiHandler;
    private RemotePdfService remotePdfService;
    private com.se1853_jv.labverse.data.api.workflow.ReadingWorkflowApiHandler workflowApiHandler;
    private com.se1853_jv.labverse.domain.infrastructure.workflow.repo.ReadingWorkflowRepository workflowRepository;
    
    private String paperId;
    private String collectionId;
    private String userId;
    private String jwtToken;
    private String pdfUrl; // Firebase Storage URL from PaperResearch.dataUrl
    private int totalPages = 0; // Total pages in PDF
    
    private List<Note> loadedNotes = new ArrayList<>();
    private List<Highlight> loadedHighlights = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_reader);
        
        // Get data from intent
        paperId = getIntent().getStringExtra("paperId");
        collectionId = getIntent().getStringExtra("collectionId");
        pdfUrl = getIntent().getStringExtra("pdfUrl");
        
        // Get user info from SessionManager
        com.se1853_jv.labverse.data.utils.SessionManager sessionManager = 
            new com.se1853_jv.labverse.data.utils.SessionManager(this);
        userId = sessionManager.getUserId();
        jwtToken = sessionManager.getToken();
        
        if (paperId == null || pdfUrl == null) {
            Toast.makeText(this, "Missing paper ID or PDF URL", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initializeComponents();
        setupToolbar();
        setupPDFViewer();
        loadAnnotations();
    }

    private void initializeComponents() {
        pdfView = findViewById(R.id.pdfView);
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);
        syncHelper = new OfflineSyncHelper(this);
        apiHandler = new AnnotationApiHandler();
        remotePdfService = new RemotePdfService();
        workflowApiHandler = new com.se1853_jv.labverse.data.api.workflow.ReadingWorkflowApiHandler(this);
        
        // Get workflow repository from database
        var db = com.se1853_jv.labverse.domain.db.DatabaseClient.getInstance(this).getAppDatabase();
        workflowRepository = db.readingWorkflowRepository();
        
        // Log collectionId for debugging
        Log.d(TAG, "PDFReaderActivity onCreate - paperId: " + paperId + ", collectionId: " + collectionId);
        
        // Set default collectionId if null or empty (personal library)
        if (collectionId == null || collectionId.isEmpty()) {
            collectionId = "PERSONAL_LIBRARY"; // Special collection ID for personal library
            Log.d(TAG, "Using PERSONAL_LIBRARY for personal library");
            // Ensure personal library collection exists in database
            ensurePersonalLibraryCollectionExists();
        } else {
            Log.d(TAG, "Using collectionId from intent: " + collectionId);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("PDF Reader");
        }
    }

    private void setupPDFViewer() {
        // Show progress bar
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        // Download PDF from Firebase Storage and cache locally
        remotePdfService.downloadPdfFromUrl(
            pdfUrl,
            paperId,
            this,
            new RemotePdfService.DownloadCallback() {
                @Override
                public void onSuccess(File localFile) {
                    // Hide progress bar
                    runOnUiThread(() -> {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                    
                    // Load PDF from local file
                    loadPdfFromFile(localFile);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Error downloading PDF", e);
                    runOnUiThread(() -> {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        Toast.makeText(PDFReaderActivity.this, 
                            "Error loading PDF: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    });
                }
            }
        );
    }

    /**
     * Load PDF from local file
     */
    private void loadPdfFromFile(File pdfFile) {
        // Load last read page from database
        int lastReadPage = loadLastReadPage();
        
        pdfView.fromFile(pdfFile)
            .defaultPage(lastReadPage) // Start from last read page
            .enableSwipe(true)
            .swipeHorizontal(false) // Vertical scrolling (recommended for research papers)
            .enableDoubletap(true)
            .autoSpacing(true) // Add spacing between pages
            .pageFling(false) // Disable page fling for smooth scrolling
            .onPageChange(new OnPageChangeListener() {
                @Override
                public void onPageChanged(int page, int pageCount) {
                    // Update reading progress
                    totalPages = pageCount;
                    updateReadingProgress(page, pageCount);
                }
            })
            .onTap(new OnTapListener() {
                @Override
                public boolean onTap(MotionEvent e) {
                    // User tap → Show dialog to add note
                    float x = e.getX();
                    float y = e.getY();
                    showAddNoteDialog((int) x, (int) y, pdfView.getCurrentPage());
                    return true;
                }
            })
            .onLoad(nbPages -> {
                totalPages = nbPages;
                Log.d(TAG, "PDF loaded: " + nbPages + " pages");
            })
            .onError(t -> {
                Log.e(TAG, "Error loading PDF", t);
                Toast.makeText(PDFReaderActivity.this, 
                    "Error loading PDF: " + t.getMessage(), 
                    Toast.LENGTH_LONG).show();
            })
            .load();
        
        // Setup long press listener for highlight
        pdfView.setOnLongClickListener(v -> {
            // Get tap position from PDF viewer
            // Note: AndroidPdfViewer doesn't provide direct tap coordinates
            // You may need to use custom gesture detector
            showHighlightColorPicker();
            return true;
        });
    }

    /**
     * Show dialog để user nhập note content
     */
    private void showAddNoteDialog(int x, int y, int page) {
        EditText input = new EditText(this);
        input.setHint("Enter your note...");
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("Add Note")
            .setView(input)
            .setPositiveButton("Save", (dialog, which) -> {
                String noteContent = input.getText().toString().trim();
                if (!noteContent.isEmpty()) {
                    createNote(noteContent, (long) x, (long) y, page);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * Show color picker để user chọn highlight color
     */
    private void showHighlightColorPicker() {
        String[] colors = {"#FFFF00", "#00FF00", "#00FFFF", "#FF00FF", "#FF0000"};
        String[] colorNames = {"Yellow", "Green", "Cyan", "Magenta", "Red"};
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("Select Highlight Color")
            .setItems(colorNames, (dialog, which) -> {
                // Get current page
                int page = pdfView.getCurrentPage();
                // Create highlight (tọa độ sẽ được lấy từ gesture hoặc selection)
                createHighlight(colors[which], page);
            })
            .show();
    }

    /**
     * Create note với offline support
     */
    private void createNote(String content, long x, long y, int page) {
        Note note = Note.builder()
            .id(UUID.randomUUID().toString())
            .content(content)
            .coordinationX(x)
            .coordinationY(y)
            .pageNumber(page)
            .build();
        
        // Try to create via API if online, otherwise save for offline sync
        if (com.se1853_jv.labverse.data.utils.Connectivity.isInternetAvailable(this) && jwtToken != null) {
            // Create API request
            com.se1853_jv.labverse.data.api.annotation.AnnotationApi.CreateNoteRequest request = 
                new com.se1853_jv.labverse.data.api.annotation.AnnotationApi.CreateNoteRequest();
            request.paperId = paperId;
            request.collectionId = collectionId != null ? collectionId : "";
            request.content = content;
            request.coordinationX = (int) x;
            request.coordinationY = (int) y;
            request.pageNumber = page;
            
            // Call API
            apiHandler.createNote(jwtToken, request, new com.se1853_jv.labverse.data.api.ApiCallback<com.se1853_jv.labverse.data.api.annotation.AnnotationApi.NoteResponse>() {
                @Override
                public void onSuccess(com.se1853_jv.labverse.data.api.annotation.AnnotationApi.NoteResponse response) {
                    runOnUiThread(() -> {
                        // Update note with server ID
                        note.setId(response.id);
                        loadedNotes.add(note);
                        displayNoteOverlay(note);
                        Toast.makeText(PDFReaderActivity.this, "Note saved", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Log.e(TAG, "Error creating note via API: " + error);
                        // Fallback to offline sync
                        syncHelper.saveNote(note, "CREATE");
                        loadedNotes.add(note);
                        displayNoteOverlay(note);
                        Toast.makeText(PDFReaderActivity.this, "Note saved (will sync later)", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            // Offline: save for sync later
            syncHelper.saveNote(note, "CREATE");
            loadedNotes.add(note);
            displayNoteOverlay(note);
            Toast.makeText(this, "Note saved (will sync when online)", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Create highlight với offline support
     */
    private void createHighlight(String color, int page) {
        // Get coordinates from current page center (can be improved with text selection)
        // For now, using center of page as placeholder
        long x = 200L; // Center X
        long y = 300L; // Center Y
        
        Highlight highlight = Highlight.builder()
            .id(UUID.randomUUID().toString())
            .colorCode(color)
            .coordinationX(x)
            .coordinationY(y)
            .pageNumber(page)
            .build();
        
        // Try to create via API if online, otherwise save for offline sync
        if (com.se1853_jv.labverse.data.utils.Connectivity.isInternetAvailable(this) && jwtToken != null) {
            // Create API request
            com.se1853_jv.labverse.data.api.annotation.AnnotationApi.CreateHighlightRequest request = 
                new com.se1853_jv.labverse.data.api.annotation.AnnotationApi.CreateHighlightRequest();
            request.paperId = paperId;
            request.collectionId = collectionId != null ? collectionId : "";
            request.color = color;
            request.coordinationX = (int) x;
            request.coordinationY = (int) y;
            request.pageNumber = page;
            
            // Call API
            apiHandler.createHighlight(jwtToken, request, new com.se1853_jv.labverse.data.api.ApiCallback<com.se1853_jv.labverse.data.api.annotation.AnnotationApi.HighlightResponse>() {
                @Override
                public void onSuccess(com.se1853_jv.labverse.data.api.annotation.AnnotationApi.HighlightResponse response) {
                    runOnUiThread(() -> {
                        // Update highlight with server ID
                        highlight.setId(response.id);
                        loadedHighlights.add(highlight);
                        displayHighlightOverlay(highlight);
                        Toast.makeText(PDFReaderActivity.this, "Text highlighted", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Log.e(TAG, "Error creating highlight via API: " + error);
                        // Fallback to offline sync
                        syncHelper.saveHighlight(highlight, "CREATE");
                        loadedHighlights.add(highlight);
                        displayHighlightOverlay(highlight);
                        Toast.makeText(PDFReaderActivity.this, "Highlight saved (will sync later)", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            // Offline: save for sync later
            syncHelper.saveHighlight(highlight, "CREATE");
            loadedHighlights.add(highlight);
            displayHighlightOverlay(highlight);
            Toast.makeText(this, "Highlight saved (will sync when online)", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Display note overlay trên PDF
     * TODO: Implement actual overlay rendering
     */
    private void displayNoteOverlay(Note note) {
        // TODO: Render note icon/annotation tại tọa độ (note.coordinationX, note.coordinationY)
        // Có thể dùng custom view overlay hoặc annotation library
        Log.d(TAG, "Display note: " + note.getId() + " at page " + note.getPageNumber());
    }

    /**
     * Display highlight overlay trên PDF
     * TODO: Implement actual overlay rendering
     */
    private void displayHighlightOverlay(Highlight highlight) {
        // TODO: Render highlight tại tọa độ với màu highlight.colorCode
        Log.d(TAG, "Display highlight: " + highlight.getId() + " at page " + highlight.getPageNumber());
    }

    /**
     * Load annotations từ API hoặc local database
     */
    private void loadAnnotations() {
        // Check offline first
        if (!com.se1853_jv.labverse.data.utils.Connectivity.isInternetAvailable(this)) {
            // Load từ local Room DB
            loadAnnotationsFromLocal();
        } else {
            // Load từ API
            loadAnnotationsFromAPI();
        }
    }

    /**
     * Load annotations từ local Room DB (offline)
     */
    private void loadAnnotationsFromLocal() {
        // TODO: Query từ Room DB
        // NoteRepository và HighlightRepository
        Log.d(TAG, "Loading annotations from local database");
    }

    /**
     * Load annotations từ API
     */
    private void loadAnnotationsFromAPI() {
        if (jwtToken == null) {
            Log.w(TAG, "No JWT token, loading from local");
            loadAnnotationsFromLocal();
            return;
        }
        
        // collectionId can be null for personal collections
        String collectionIdParam = collectionId != null ? collectionId : "";
        
        // Load notes
        apiHandler.getNotes(jwtToken, paperId, collectionIdParam, userId, new com.se1853_jv.labverse.data.api.ApiCallback<List<com.se1853_jv.labverse.data.api.annotation.AnnotationApi.NoteResponse>>() {
            @Override
            public void onSuccess(List<com.se1853_jv.labverse.data.api.annotation.AnnotationApi.NoteResponse> notes) {
                runOnUiThread(() -> {
                    loadedNotes = notes != null ? convertToNotes(notes) : new ArrayList<>();
                    // Display notes
                    for (Note note : loadedNotes) {
                        displayNoteOverlay(note);
                    }
                    Log.d(TAG, "Loaded " + loadedNotes.size() + " notes from API");
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading notes: " + error);
                // Fallback to local
                loadAnnotationsFromLocal();
            }
        });
        
        // Load highlights
        apiHandler.getHighlights(jwtToken, paperId, collectionIdParam, userId, new com.se1853_jv.labverse.data.api.ApiCallback<List<com.se1853_jv.labverse.data.api.annotation.AnnotationApi.HighlightResponse>>() {
            @Override
            public void onSuccess(List<com.se1853_jv.labverse.data.api.annotation.AnnotationApi.HighlightResponse> highlights) {
                runOnUiThread(() -> {
                    loadedHighlights = highlights != null ? convertToHighlights(highlights) : new ArrayList<>();
                    // Display highlights
                    for (Highlight highlight : loadedHighlights) {
                        displayHighlightOverlay(highlight);
                    }
                    Log.d(TAG, "Loaded " + loadedHighlights.size() + " highlights from API");
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading highlights: " + error);
                // Fallback to local
                loadAnnotationsFromLocal();
            }
        });
    }

    /**
     * Ensure personal library collection exists in database (async)
     */
    private void ensurePersonalLibraryCollectionExists() {
        new Thread(() -> {
            ensurePersonalLibraryCollectionExistsSync();
        }).start();
    }
    
    /**
     * Ensure personal library collection exists in database (synchronous, must be called on background thread)
     */
    private void ensurePersonalLibraryCollectionExistsSync() {
        try {
            var db = com.se1853_jv.labverse.domain.db.DatabaseClient.getInstance(this).getAppDatabase();
            var collectionRepository = db.collectionRepository();
            
            // Check if personal library collection exists
            com.se1853_jv.labverse.domain.infrastructure.collection.model.Collections personalCollection = 
                collectionRepository.getById("PERSONAL_LIBRARY");
            
            if (personalCollection == null) {
                // Create personal library collection
                personalCollection = com.se1853_jv.labverse.domain.infrastructure.collection.model.Collections.builder()
                        .id("PERSONAL_LIBRARY")
                        .name("Personal Library")
                        .build();
                collectionRepository.create(personalCollection);
                Log.d(TAG, "Created personal library collection");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error ensuring personal library collection exists", e);
        }
    }
    
    /**
     * Ensure all foreign key entities exist before creating ReadingWorkflow
     * Returns true if all entities exist or were created successfully
     */
    private boolean ensureForeignKeysExist(String userId, String paperId, String collectionId) {
        try {
            var db = com.se1853_jv.labverse.domain.db.DatabaseClient.getInstance(this).getAppDatabase();
            
            // Ensure User exists
            var userRepository = db.userRepository();
            com.se1853_jv.labverse.domain.infrastructure.user.model.Users user = userRepository.getById(userId);
            if (user == null) {
                Log.w(TAG, "User not found in database: " + userId + ". Creating minimal user record.");
                // Create minimal user record from SessionManager
                com.se1853_jv.labverse.data.utils.SessionManager sessionManager = 
                    new com.se1853_jv.labverse.data.utils.SessionManager(this);
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
                com.se1853_jv.labverse.domain.infrastructure.role.model.Roles roleEntity = roleRepository.getById(roleId);
                if (roleEntity == null) {
                    // Create role if it doesn't exist
                    roleEntity = com.se1853_jv.labverse.domain.infrastructure.role.model.Roles.builder()
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
                user = com.se1853_jv.labverse.domain.infrastructure.user.model.Users.builder()
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
            
            // Ensure PaperResearch exists
            var paperRepository = db.paperRepository();
            com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch paper = paperRepository.getById(paperId);
            if (paper == null) {
                Log.w(TAG, "Paper not found in database: " + paperId + ". Creating minimal paper record.");
                // Create minimal paper record
                paper = com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch.builder()
                        .id(paperId)
                        .dataUrl(pdfUrl != null ? pdfUrl : "")
                        .description("")
                        .title("Paper " + paperId)
                        .authors("Unknown")
                        .journal("Unknown")
                        .publicationYear(2024)
                        .doi("")
                        .build();
                try {
                    paperRepository.create(paper);
                    Log.d(TAG, "Created minimal paper record: " + paperId);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to create paper: " + paperId, e);
                    return false;
                }
            }
            
            // Ensure Collection exists
            var collectionRepository = db.collectionRepository();
            com.se1853_jv.labverse.domain.infrastructure.collection.model.Collections collection = 
                collectionRepository.getById(collectionId);
            if (collection == null) {
                // Create collection if it doesn't exist
                if ("PERSONAL_LIBRARY".equals(collectionId)) {
                    collection = com.se1853_jv.labverse.domain.infrastructure.collection.model.Collections.builder()
                            .id("PERSONAL_LIBRARY")
                            .name("Personal Library")
                            .build();
                    collectionRepository.create(collection);
                    Log.d(TAG, "Created personal library collection");
                } else {
                    // For real collections, we need to create a minimal record
                    // This should ideally be loaded from backend, but we'll create a placeholder
                    collection = com.se1853_jv.labverse.domain.infrastructure.collection.model.Collections.builder()
                            .id(collectionId)
                            .name("Collection " + collectionId)
                            .build();
                    try {
                        collectionRepository.create(collection);
                        Log.d(TAG, "Created collection placeholder: " + collectionId);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to create collection: " + collectionId, e);
                        return false;
                    }
                }
            }
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error ensuring foreign keys exist", e);
            return false;
        }
    }

    /**
     * Load last read page from database
     */
    private int loadLastReadPage() {
        if (userId == null || paperId == null) {
            return 0;
        }
        
        try {
            String finalCollectionId = collectionId != null ? collectionId : "PERSONAL_LIBRARY";
            // Note: We don't need to ensure collection exists here since we're only reading
            ReadingWorkflow workflow = workflowRepository.getByCompositeKey(userId, paperId, finalCollectionId);
            if (workflow != null && workflow.getLastPage() != null) {
                int lastPage = workflow.getLastPage();
                Log.d(TAG, "Restoring last read page: " + lastPage);
                return lastPage;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading last read page", e);
        }
        
        return 0;
    }

    /**
     * Update reading progress
     */
    private void updateReadingProgress(int currentPage, int totalPages) {
        if (userId == null || paperId == null || totalPages == 0) {
            return;
        }
        
        // Make variables final for lambda
        final String finalUserId = userId;
        final String finalPaperId = paperId;
        final String finalCollectionId = collectionId != null ? collectionId : "PERSONAL_LIBRARY";
        final String finalJwtToken = jwtToken;
        
        // Calculate progress percentage (0-100)
        // Note: currentPage is 0-indexed, so we add 1 to get the actual page number
        // For a 2-page PDF: page 0 = 50%, page 1 = 100%
        int calculatedProgress = (int) Math.round(((currentPage + 1) * 100.0) / totalPages);
        if (calculatedProgress > 100) calculatedProgress = 100;
        if (calculatedProgress < 0) calculatedProgress = 0;
        
        final int progress = calculatedProgress;
        final int finalCurrentPage = currentPage;
        
        Log.d(TAG, "Reading progress: " + progress + "% (page " + currentPage + "/" + totalPages + ")");
        
        // Run on background thread
        new Thread(() -> {
            try {
                // Ensure all foreign key entities exist before creating workflow
                if (!ensureForeignKeysExist(finalUserId, finalPaperId, finalCollectionId)) {
                    Log.e(TAG, "Failed to ensure foreign keys exist, skipping workflow creation");
                    return;
                }
                
                // Get or create ReadingWorkflow
                ReadingWorkflow workflow = workflowRepository.getByCompositeKey(finalUserId, finalPaperId, finalCollectionId);
                
                com.se1853_jv.labverse.domain.enumerate.WorkflowStatus newStatus;
                if (progress == 0) {
                    newStatus = com.se1853_jv.labverse.domain.enumerate.WorkflowStatus.UNREAD;
                } else if (progress >= 100) {
                    newStatus = com.se1853_jv.labverse.domain.enumerate.WorkflowStatus.FINISHED;
                } else {
                    newStatus = com.se1853_jv.labverse.domain.enumerate.WorkflowStatus.READING;
                }
                
                if (workflow == null) {
                    // Create new workflow
                    workflow = ReadingWorkflow.builder()
                            .userId(finalUserId)
                            .paperId(finalPaperId)
                            .collectionId(finalCollectionId)
                            .status(newStatus)
                            .progress(progress)
                            .lastPage(finalCurrentPage)
                            .build();
                    workflowRepository.create(workflow);
                    Log.d(TAG, "Created new ReadingWorkflow");
                } else {
                    // Update existing workflow - create new instance with updated values
                    ReadingWorkflow updatedWorkflow = ReadingWorkflow.builder()
                            .userId(workflow.getUserId())
                            .paperId(workflow.getPaperId())
                            .collectionId(workflow.getCollectionId())
                            .status(newStatus)
                            .progress(progress)
                            .lastPage(finalCurrentPage)
                            .build();
                    workflowRepository.update(updatedWorkflow);
                    workflow = updatedWorkflow;
                    Log.d(TAG, "Updated ReadingWorkflow");
                }
                
                // Sync to backend via OfflineSyncHelper (handles online/offline)
                final ReadingWorkflow finalWorkflow = workflow;
                syncHelper.updateReadingProgress(finalWorkflow);
                
                // Also try direct API call if online
                if (com.se1853_jv.labverse.data.utils.Connectivity.isInternetAvailable(this) && finalJwtToken != null) {
                    com.se1853_jv.labverse.data.dto.request.ReadingWorkflowProgressRequest request = 
                        new com.se1853_jv.labverse.data.dto.request.ReadingWorkflowProgressRequest();
                    request.setCollectionId(finalCollectionId);
                    request.setPaperId(finalPaperId);
                    request.setUsersid(finalUserId);
                    request.setLastPage(finalCurrentPage);
                    request.setProgress(progress);
                    
                    workflowApiHandler.updateProgress(request, new com.se1853_jv.labverse.data.api.ApiCallback<String>() {
                        @Override
                        public void onSuccess(String result) {
                            Log.d(TAG, "Reading progress synced to backend successfully");
                            
                            // Trigger status recalculation if reading in a collection (not personal library)
                            if (finalCollectionId != null && !finalCollectionId.equals("PERSONAL_LIBRARY")) {
                                try {
                                    com.se1853_jv.labverse.data.api.collection.CollectionApiHandler collectionApiHandler = 
                                        new com.se1853_jv.labverse.data.api.collection.CollectionApiHandler();
                                    // finalCollectionId and finalPaperId from intent are already encoded
                                    // recalculatePaperStatus expects encoded IDs, so we use them directly
                                    Log.d(TAG, "Recalculating status with collectionId=" + finalCollectionId + ", paperId=" + finalPaperId);
                                    collectionApiHandler.recalculatePaperStatus(finalCollectionId, finalPaperId, 
                                        new com.se1853_jv.labverse.data.api.ApiCallback<Object>() {
                                            @Override
                                            public void onSuccess(Object result) {
                                                Log.d(TAG, "Collection paper status recalculated successfully");
                                            }
                                            
                                            @Override
                                            public void onError(String error) {
                                                Log.w(TAG, "Failed to recalculate collection paper status: " + error);
                                                // Non-critical, status will be recalculated on next collection view
                                            }
                                        });
                                } catch (Exception e) {
                                    Log.w(TAG, "Error triggering status recalculation: " + e.getMessage());
                                    // Non-critical
                                }
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Log.w(TAG, "Failed to sync reading progress to backend: " + error);
                            // Progress is already saved locally, will sync later
                        }
                    });
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error updating reading progress", e);
            }
        }).start();
    }

    /**
     * Convert API response to Note entity
     */
    private List<Note> convertToNotes(List<com.se1853_jv.labverse.data.api.annotation.AnnotationApi.NoteResponse> responses) {
        List<Note> notes = new ArrayList<>();
        for (var response : responses) {
            Note note = Note.builder()
                .id(response.id)
                .content(response.content)
                .coordinationX((long) response.coordinationX)
                .coordinationY((long) response.coordinationY)
                .pageNumber(response.pageNumber)
                .build();
            notes.add(note);
        }
        return notes;
    }

    /**
     * Convert API response to Highlight entity
     */
    private List<Highlight> convertToHighlights(List<com.se1853_jv.labverse.data.api.annotation.AnnotationApi.HighlightResponse> responses) {
        List<Highlight> highlights = new ArrayList<>();
        for (var response : responses) {
            Highlight highlight = Highlight.builder()
                .id(response.id)
                .colorCode(response.color)
                .coordinationX((long) response.coordinationX)
                .coordinationY((long) response.coordinationY)
                .pageNumber(response.pageNumber)
                .build();
            highlights.add(highlight);
        }
        return highlights;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pdf_reader, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.export_annotations) {
            exportAnnotations();
            return true;
        } else if (id == R.id.import_annotations) {
            importAnnotations();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Export annotations to JSON file
     */
    private void exportAnnotations() {
        if (collectionId == null) {
            Toast.makeText(this, "Cannot export: No collection ID", Toast.LENGTH_SHORT).show();
            return;
        }

        if (jwtToken == null) {
            Toast.makeText(this, "Cannot export: Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress dialog
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Exporting annotations...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        apiHandler.exportAnnotations(jwtToken, paperId, collectionId, 
            new com.se1853_jv.labverse.data.api.ApiCallback<com.se1853_jv.labverse.data.api.annotation.AnnotationApi.ExportAnnotationsResponse>() {
                @Override
                public void onSuccess(com.se1853_jv.labverse.data.api.annotation.AnnotationApi.ExportAnnotationsResponse exportData) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        saveExportToFile(exportData);
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(PDFReaderActivity.this, "Export failed: " + error, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error exporting annotations: " + error);
                    });
                }
            });
    }

    /**
     * Save exported annotations to JSON file
     * Uses app's external files directory (no permission needed)
     */
    private void saveExportToFile(com.se1853_jv.labverse.data.api.annotation.AnnotationApi.ExportAnnotationsResponse exportData) {
        try {
            com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(exportData);

            // Create file in app's external files directory (no permission needed)
            String fileName = "annotations_" + paperId + "_" + System.currentTimeMillis() + ".json";
            java.io.File exportDir = new java.io.File(getExternalFilesDir(null), "exports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            java.io.File exportFile = new java.io.File(exportDir, fileName);

            java.io.FileWriter writer = new java.io.FileWriter(exportFile);
            writer.write(json);
            writer.close();

            // Share file via Android ShareSheet
            android.content.Intent shareIntent = new android.content.Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/json");
            
            // Use FileProvider to share file
            android.net.Uri fileUri = androidx.core.content.FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                exportFile
            );
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Show success dialog with share option
            new MaterialAlertDialogBuilder(this)
                .setTitle("Export Successful")
                .setMessage("Annotations exported successfully!")
                .setPositiveButton("Share", (dialog, which) -> {
                    startActivity(Intent.createChooser(shareIntent, "Share annotations file"));
                })
                .setNegativeButton("OK", null)
                .show();

            Log.d(TAG, "Annotations exported to: " + exportFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Error saving export file", e);
            Toast.makeText(this, "Error saving file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Import annotations from JSON file
     */
    private void importAnnotations() {
        if (collectionId == null) {
            Toast.makeText(this, "Cannot import: No collection ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Open file picker
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/json");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select JSON file"), IMPORT_ANNOTATIONS_REQUEST_CODE);
    }

    private static final int IMPORT_ANNOTATIONS_REQUEST_CODE = 1001;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == IMPORT_ANNOTATIONS_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                loadAndImportAnnotations(fileUri);
            }
        }
    }

    /**
     * Load JSON file and import annotations
     */
    private void loadAndImportAnnotations(Uri fileUri) {
        try {
            // Read JSON file
            java.io.InputStream inputStream = getContentResolver().openInputStream(fileUri);
            if (inputStream == null) {
                Toast.makeText(this, "Cannot read file", Toast.LENGTH_SHORT).show();
                return;
            }

            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            reader.close();
            inputStream.close();

            // Parse JSON
            com.google.gson.Gson gson = new com.google.gson.Gson();
            com.se1853_jv.labverse.data.api.annotation.AnnotationApi.ExportAnnotationsResponse importData = 
                gson.fromJson(jsonBuilder.toString(), com.se1853_jv.labverse.data.api.annotation.AnnotationApi.ExportAnnotationsResponse.class);

            // Validate paperId and collectionId
            if (!importData.paperId.equals(paperId)) {
                Toast.makeText(this, "Cannot import: Paper ID mismatch", Toast.LENGTH_LONG).show();
                return;
            }

            if (!importData.collectionId.equals(collectionId)) {
                Toast.makeText(this, "Cannot import: Collection ID mismatch", Toast.LENGTH_LONG).show();
                return;
            }

            // Show progress dialog
            android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
            progressDialog.setMessage("Importing annotations...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            // Import via API
            if (jwtToken == null) {
                progressDialog.dismiss();
                Toast.makeText(this, "Cannot import: Not authenticated", Toast.LENGTH_SHORT).show();
                return;
            }

            apiHandler.importAnnotations(jwtToken, paperId, collectionId, importData,
                new com.se1853_jv.labverse.data.api.ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(PDFReaderActivity.this, "Annotations imported successfully", Toast.LENGTH_SHORT).show();
                            // Reload annotations to display imported ones
                            loadAnnotations();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(PDFReaderActivity.this, "Import failed: " + error, Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Error importing annotations: " + error);
                        });
                    }
                });

        } catch (Exception e) {
            Log.e(TAG, "Error reading import file", e);
            Toast.makeText(this, "Error reading file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}







