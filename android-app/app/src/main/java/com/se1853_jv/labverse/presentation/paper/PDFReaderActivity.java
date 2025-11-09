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
import com.se1853_jv.labverse.data.service.firebase.FirebaseService;
import com.se1853_jv.labverse.data.sync.OfflineSyncHelper;
import com.se1853_jv.labverse.domain.infrastructure.annotation.model.Highlight;
import com.se1853_jv.labverse.domain.infrastructure.annotation.model.Note;

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
    private FirebaseService firebaseService;
    
    private String paperId;
    private String collectionId;
    private String userId;
    private String jwtToken;
    private String pdfUrl; // Firebase Storage URL from PaperResearch.dataUrl
    
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
        firebaseService = new FirebaseService();
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
        firebaseService.downloadPdfFromFirebase(
            pdfUrl,
            paperId,
            this,
            new FirebaseService.DownloadCallback() {
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
        pdfView.fromFile(pdfFile)
            .defaultPage(0)
            .enableSwipe(true)
            .swipeHorizontal(false) // Vertical scrolling (recommended for research papers)
            .enableDoubletap(true)
            .autoSpacing(true) // Add spacing between pages
            .pageFling(false) // Disable page fling for smooth scrolling
            .onPageChange(new OnPageChangeListener() {
                @Override
                public void onPageChanged(int page, int pageCount) {
                    // Update reading progress
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
        
        // Save với offline support - DÙNG PHẦN 11!
        syncHelper.saveNote(note, "CREATE");
        
        // Display note overlay trên PDF
        loadedNotes.add(note);
        displayNoteOverlay(note);
        
        Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
    }

    /**
     * Create highlight với offline support
     */
    private void createHighlight(String color, int page) {
        // TODO: Get actual coordinates from user selection
        // For now, using placeholder coordinates
        Highlight highlight = Highlight.builder()
            .id(UUID.randomUUID().toString())
            .colorCode(color)
            .coordinationX(100L) // TODO: Get from selection
            .coordinationY(200L) // TODO: Get from selection
            .pageNumber(page)
            .build();
        
        // Save với offline support - DÙNG PHẦN 11!
        syncHelper.saveHighlight(highlight, "CREATE");
        
        // Display highlight overlay trên PDF
        loadedHighlights.add(highlight);
        displayHighlightOverlay(highlight);
        
        Toast.makeText(this, "Text highlighted", Toast.LENGTH_SHORT).show();
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
     * Update reading progress
     */
    private void updateReadingProgress(int currentPage, int totalPages) {
        // TODO: Update ReadingWorkflow với lastPage và progress
        int progress = (int) ((currentPage * 100.0) / totalPages);
        Log.d(TAG, "Reading progress: " + progress + "% (page " + currentPage + "/" + totalPages + ")");
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







