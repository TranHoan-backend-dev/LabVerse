package com.se1853_jv.labverse.presentation.readinglist;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputEditText;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.readinglist.ReadingListApiHandler;
import com.se1853_jv.labverse.data.dto.request.CreateReadingListRequest;
import com.se1853_jv.labverse.data.dto.response.ReadingListResponse;
import com.se1853_jv.labverse.data.utils.Connectivity;
import com.se1853_jv.labverse.data.utils.SessionManager;
import com.se1853_jv.labverse.presentation.common.HeaderHelper;
import com.se1853_jv.labverse.presentation.readinglist.ReadingListDetailActivity;
import com.se1853_jv.labverse.presentation.readinglist.adapter.ReadingListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ReadingListsActivity extends AppCompatActivity {
    private static final String TAG = "ReadingListsActivity";

    private RecyclerView recyclerViewReadingLists;
    private LinearLayout emptyState;
    private MaterialButton btnCreateFirstList;
    private FloatingActionButton fabCreateList;
    private MaterialButton btnNewList;
    private MaterialButton btnFilter;
    private MaterialButton btnSort;
    private EditText etSearch;

    private ReadingListApiHandler readingListApiHandler;
    private ReadingListAdapter adapter;
    private SessionManager sessionManager;
    private List<ReadingListResponse> allReadingLists = new ArrayList<>();
    private String currentUserId;
    private String currentSortBy = "name"; // "name", "papers", "members", "date"
    private boolean sortAscending = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_lists);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scrollContent), (v, insets) -> {
            var statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(statusBar.left, statusBar.top, statusBar.right, statusBar.bottom);
            return insets;
        });

        // Initialize
        readingListApiHandler = new ReadingListApiHandler(this);
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();

        bindViews();
        setupRecyclerView();
        handleEvents();

        // Setup header
        HeaderHelper.setupProfileClickListeners(this);
        HeaderHelper.setupListsNavigationClickListener(this);

        // Load reading lists
        loadReadingLists();
    }

    private void bindViews() {
        recyclerViewReadingLists = findViewById(R.id.recyclerViewReadingLists);
        emptyState = findViewById(R.id.emptyState);
        btnCreateFirstList = findViewById(R.id.btnCreateFirstList);
        fabCreateList = findViewById(R.id.fabCreateList);
        btnNewList = findViewById(R.id.btnNewList);
        btnFilter = findViewById(R.id.btnFilter);
        btnSort = findViewById(R.id.btnSort);
        
        // Get search input from included layout
        View searchSection = findViewById(R.id.searchSection);
        if (searchSection != null) {
            etSearch = searchSection.findViewById(R.id.input_search);
            if (etSearch != null) {
                etSearch.setHint("Search reading lists...");
            }
        }
    }

    private void setupRecyclerView() {
        adapter = new ReadingListAdapter();
        adapter.setOnReadingListActionListener(new ReadingListAdapter.OnReadingListActionListener() {
            @Override
            public void onViewList(ReadingListResponse list) {
                Intent intent = new Intent(ReadingListsActivity.this, ReadingListDetailActivity.class);
                intent.putExtra("readingList", list);
                startActivity(intent);
            }

            @Override
            public void onShareList(ReadingListResponse list) {
                // TODO: Implement share functionality
                Toast.makeText(ReadingListsActivity.this, "Share list: " + list.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMoreOptions(ReadingListResponse list, View anchorView) {
                showMoreOptionsMenu(list, anchorView);
            }
        });

        recyclerViewReadingLists.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReadingLists.setAdapter(adapter);
    }

    private void handleEvents() {
        // Search
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterReadingLists(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Create List buttons
        if (btnNewList != null) {
            btnNewList.setOnClickListener(v -> showCreateReadingListDialog());
        }

        if (fabCreateList != null) {
            fabCreateList.setOnClickListener(v -> showCreateReadingListDialog());
        }

        if (btnCreateFirstList != null) {
            btnCreateFirstList.setOnClickListener(v -> showCreateReadingListDialog());
        }

        // Filter button (placeholder)
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> {
                Toast.makeText(this, "Filter functionality coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        // Sort button
        if (btnSort != null) {
            btnSort.setOnClickListener(v -> showSortDialog());
        }
    }

    private void loadReadingLists() {
        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        readingListApiHandler.getReadingListsByUser(currentUserId, new ApiCallback<List<ReadingListResponse>>() {
            @Override
            public void onSuccess(List<ReadingListResponse> lists) {
                runOnUiThread(() -> {
                    allReadingLists = lists != null ? lists : new ArrayList<>();
                    applyFiltersAndSort();
                    updateEmptyState();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error loading reading lists: " + error);
                    Toast.makeText(ReadingListsActivity.this,
                            "Failed to load reading lists: " + error, Toast.LENGTH_LONG).show();
                    updateEmptyState();
                });
            }
        });
    }

    private void filterReadingLists(String query) {
        if (query.isEmpty()) {
            applyFiltersAndSort();
            return;
        }

        List<ReadingListResponse> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (ReadingListResponse list : allReadingLists) {
            if (list.getName() != null && list.getName().toLowerCase().contains(lowerQuery)) {
                filtered.add(list);
            }
        }

        adapter.setReadingLists(filtered);
        updateEmptyState();
    }

    private void applyFiltersAndSort() {
        List<ReadingListResponse> sorted = new ArrayList<>(allReadingLists);
        
        // Sort
        Collections.sort(sorted, new Comparator<ReadingListResponse>() {
            @Override
            public int compare(ReadingListResponse a, ReadingListResponse b) {
                int result = 0;
                switch (currentSortBy) {
                    case "name":
                        String nameA = a.getName() != null ? a.getName() : "";
                        String nameB = b.getName() != null ? b.getName() : "";
                        result = nameA.compareToIgnoreCase(nameB);
                        break;
                    case "papers":
                        int papersA = a.getPaperIdsList() != null ? a.getPaperIdsList().size() : 0;
                        int papersB = b.getPaperIdsList() != null ? b.getPaperIdsList().size() : 0;
                        result = Integer.compare(papersA, papersB);
                        break;
                    case "members":
                        int membersA = a.getUserIdsList() != null ? a.getUserIdsList().size() : 0;
                        int membersB = b.getUserIdsList() != null ? b.getUserIdsList().size() : 0;
                        result = Integer.compare(membersA, membersB);
                        break;
                    case "date":
                        // Sort by created date (newest first by default)
                        String dateA = a.getCreatedAt() != null ? a.getCreatedAt() : "";
                        String dateB = b.getCreatedAt() != null ? b.getCreatedAt() : "";
                        result = dateB.compareTo(dateA); // Reverse for newest first
                        break;
                }
                return sortAscending ? result : -result;
            }
        });

        adapter.setReadingLists(sorted);
    }

    private void showSortDialog() {
        String[] sortOptions = {"Name", "Papers", "Members", "Date"};
        int currentIndex = 0;
        switch (currentSortBy) {
            case "name": currentIndex = 0; break;
            case "papers": currentIndex = 1; break;
            case "members": currentIndex = 2; break;
            case "date": currentIndex = 3; break;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Sort by")
                .setSingleChoiceItems(sortOptions, currentIndex, (dialog, which) -> {
                    switch (which) {
                        case 0: currentSortBy = "name"; break;
                        case 1: currentSortBy = "papers"; break;
                        case 2: currentSortBy = "members"; break;
                        case 3: currentSortBy = "date"; break;
                    }
                    applyFiltersAndSort();
                    dialog.dismiss();
                })
                .setPositiveButton("Ascending", (dialog, which) -> {
                    sortAscending = true;
                    applyFiltersAndSort();
                })
                .setNegativeButton("Descending", (dialog, which) -> {
                    sortAscending = false;
                    applyFiltersAndSort();
                })
                .show();
    }

    private void showCreateReadingListDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_reading_list, null);

        TextInputEditText etListName = dialogView.findViewById(R.id.etListName);
        ImageButton btnClose = dialogView.findViewById(R.id.btnClose);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        MaterialButton btnAccept = dialogView.findViewById(R.id.btnAccept);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAccept.setOnClickListener(v -> {
            String listName = etListName.getText() != null ? etListName.getText().toString().trim() : "";
            if (listName.isEmpty()) {
                etListName.setError("List name is required");
                etListName.requestFocus();
                return;
            }

            createReadingList(listName, dialog);
        });

        dialog.show();
    }

    private void createReadingList(String listName, AlertDialog dialog) {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        CreateReadingListRequest request = new CreateReadingListRequest();
        request.setName(listName);
        // Optionally add current user to the list
        if (currentUserId != null) {
            List<String> userIds = new ArrayList<>();
            userIds.add(currentUserId);
            request.setUserIdsList(userIds);
        }

        readingListApiHandler.createReadingList(request, new ApiCallback<ReadingListResponse>() {
            @Override
            public void onSuccess(ReadingListResponse list) {
                runOnUiThread(() -> {
                    dialog.dismiss();
                    allReadingLists.add(list);
                    adapter.addReadingList(list);
                    updateEmptyState();
                    Toast.makeText(ReadingListsActivity.this,
                            "Reading list created successfully!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error creating reading list: " + error);
                    Toast.makeText(ReadingListsActivity.this,
                            "Failed to create reading list: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showMoreOptionsMenu(ReadingListResponse list, View anchorView) {
        androidx.appcompat.widget.PopupMenu popupMenu = new androidx.appcompat.widget.PopupMenu(this, anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.menu_reading_list_options, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit) {
                // TODO: Implement edit
                Toast.makeText(ReadingListsActivity.this, "Edit: " + list.getName(), Toast.LENGTH_SHORT).show();
                return true;
            } else if (item.getItemId() == R.id.action_delete) {
                showDeleteConfirmation(list);
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void showDeleteConfirmation(ReadingListResponse list) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Reading List")
                .setMessage("Are you sure you want to delete \"" + list.getName() + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteReadingList(list))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteReadingList(ReadingListResponse list) {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        readingListApiHandler.deleteReadingList(list.getId(), new ApiCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                runOnUiThread(() -> {
                    allReadingLists.remove(list);
                    adapter.removeReadingList(list);
                    updateEmptyState();
                    Toast.makeText(ReadingListsActivity.this,
                            "Reading list deleted successfully", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error deleting reading list: " + error);
                    Toast.makeText(ReadingListsActivity.this,
                            "Failed to delete reading list: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void updateEmptyState() {
        if (emptyState != null && recyclerViewReadingLists != null) {
            if (adapter.getItemCount() == 0) {
                emptyState.setVisibility(View.VISIBLE);
                recyclerViewReadingLists.setVisibility(View.GONE);
            } else {
                emptyState.setVisibility(View.GONE);
                recyclerViewReadingLists.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload reading lists when returning to this activity
        loadReadingLists();
    }
}

