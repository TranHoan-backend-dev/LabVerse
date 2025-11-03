package com.se1853_jv.labverse.presentation.collection;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.collection.CollectionApiHandler;
import com.se1853_jv.labverse.data.dto.request.CollectionRequest;
import com.se1853_jv.labverse.data.dto.request.CollectionPaperRequest;
import com.se1853_jv.labverse.data.dto.response.CollectionResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionPaperResponse;
import com.se1853_jv.labverse.data.utils.Connectivity;
import com.se1853_jv.labverse.presentation.collection.adapter.CollectionsPagerAdapter;
import com.se1853_jv.labverse.presentation.feed.FeedActivity;
import com.se1853_jv.labverse.presentation.user.UserActivity;

public class CollectionsActivity extends AppCompatActivity {
    private static final String TAG = "CollectionsActivity";
    
    private CollectionApiHandler apiHandler;
    private CollectionsFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_common_ui_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.feedActivity), (v, insets) -> {
            var statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(statusBar.left, statusBar.top, statusBar.right, statusBar.bottom);
            return insets;
        });

        // Hide search bar
        hideSearchBar();

        apiHandler = new CollectionApiHandler();
        setupViewPager();
        setupBottomNavigation();
    }

    private void hideSearchBar() {
        View searchBox = findViewById(R.id.search_box);
        if (searchBox != null) {
            searchBox.setVisibility(View.GONE);
        }
    }

    private void setupViewPager() {
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        var adapter = new CollectionsPagerAdapter(this);
        viewPager.setAdapter(adapter);
        
        // Get fragment after adapter is set and fragment is created
        viewPager.post(() -> {
            fragment = (CollectionsFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.viewPager);
            if (fragment == null) {
                // Try alternative method
                var fragments = getSupportFragmentManager().getFragments();
                for (var frag : fragments) {
                    if (frag instanceof CollectionsFragment) {
                        fragment = (CollectionsFragment) frag;
                        break;
                    }
                }
            }
        });
    }
    
    private CollectionsFragment getFragment() {
        if (fragment == null) {
            var fragments = getSupportFragmentManager().getFragments();
            for (var frag : fragments) {
                if (frag instanceof CollectionsFragment) {
                    fragment = (CollectionsFragment) frag;
                    break;
                }
            }
        }
        return fragment;
    }

    private void setupBottomNavigation() {
        View bottomNav = findViewById(R.id.bottomNav);
        
        bottomNav.findViewById(R.id.nav_home).setOnClickListener(v -> {
            startActivity(new Intent(this, FeedActivity.class));
            finish();
        });

        bottomNav.findViewById(R.id.nav_search).setOnClickListener(v -> {
            // TODO: Implement search activity
        });

        bottomNav.findViewById(R.id.nav_lists).setOnClickListener(v -> {
            // TODO: Implement lists activity
        });

        bottomNav.findViewById(R.id.nav_collections).setOnClickListener(v -> {
            // Already in CollectionsActivity
        });

        bottomNav.findViewById(R.id.nav_profile).setOnClickListener(v -> {
            startActivity(new Intent(this, UserActivity.class));
            finish();
        });
    }

    public void showEditCollectionDialog(CollectionResponse collection) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Collection");

        View dialogView = getLayoutInflater().inflate(R.layout.layout_collection_dialog_create_collection, null);
        EditText editName = dialogView.findViewById(R.id.edit_collection_name);
        editName.setText(collection.getName());
        
        builder.setView(dialogView);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = editName.getText().toString().trim();
            if (!name.isEmpty()) {
                // TODO: Implement update collection API
                Toast.makeText(this, "Update functionality will be implemented soon", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    public void showDeleteCollectionDialog(CollectionResponse collection) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Collection")
                .setMessage("Are you sure you want to delete \"" + collection.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // TODO: Implement delete collection API
                    Toast.makeText(this, "Delete functionality will be implemented soon", Toast.LENGTH_SHORT).show();
                    CollectionsFragment frag = getFragment();
                    if (frag != null) {
                        frag.refreshCollections();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void showCreateCollectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Collection");

        View dialogView = getLayoutInflater().inflate(R.layout.layout_collection_dialog_create_collection, null);
        EditText editName = dialogView.findViewById(R.id.edit_collection_name);
        
        builder.setView(dialogView);
        builder.setPositiveButton("Create", (dialog, which) -> {
            String name = editName.getText().toString().trim();
            if (!name.isEmpty()) {
                createCollection(name);
            } else {
                Toast.makeText(this, "Collection name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void createCollection(String name) {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionRequest request = new CollectionRequest(name);
        apiHandler.createCollection(request, new ApiCallback<CollectionResponse>() {
            @Override
            public void onSuccess(CollectionResponse response) {
                runOnUiThread(() -> {
                    Toast.makeText(CollectionsActivity.this, 
                            "Collection created successfully", 
                            Toast.LENGTH_SHORT).show();
                    CollectionsFragment frag = getFragment();
                    if (frag != null) {
                        frag.refreshCollections();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error creating collection: " + error);
                    Toast.makeText(CollectionsActivity.this, 
                            "Failed to create collection: " + error, 
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    public void showInviteMembersDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Invite Team Members");

        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_invite_members, null);
        EditText editEmail = dialogView.findViewById(R.id.edit_member_email);
        
        builder.setView(dialogView);
        builder.setPositiveButton("Send Invite", (dialog, which) -> {
            String email = editEmail.getText().toString().trim();
            if (!email.isEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                inviteMember(email);
            } else {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void inviteMember(String email) {
        // TODO: Implement API call to invite member
        Toast.makeText(this, "Invite functionality will be implemented soon", Toast.LENGTH_SHORT).show();
    }

    public void showAddPaperDialog(CollectionResponse collection) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Paper to Collection");

        View dialogView = getLayoutInflater().inflate(R.layout.layout_collection_dialog_add_paper, null);
        EditText editPaperId = dialogView.findViewById(R.id.edit_paper_id);
        Spinner spinnerPriority = dialogView.findViewById(R.id.spinner_priority);
        Spinner spinnerStatus = dialogView.findViewById(R.id.spinner_status);

        // Setup Priority spinner
        String[] priorities = {"HIGH", "MEDIUM", "LOW"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priorities);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);
        spinnerPriority.setSelection(1); // Default to MEDIUM

        // Setup Status spinner
        String[] statuses = {"ToRead", "Reading", "Finished"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);
        spinnerStatus.setSelection(0); // Default to ToRead

        builder.setView(dialogView);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String paperId = editPaperId.getText().toString().trim();
            if (!paperId.isEmpty()) {
                String priority = (String) spinnerPriority.getSelectedItem();
                String status = (String) spinnerStatus.getSelectedItem();
                addPaperToCollection(collection, paperId, priority, status);
            } else {
                Toast.makeText(this, "Paper ID cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void addPaperToCollection(CollectionResponse collection, String paperId, String priority, String status) {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionPaperRequest request = new CollectionPaperRequest();
        request.setCollectionId(collection.getId());
        request.setPaperId(paperId);
        request.setPriority(priority);
        request.setStatus(status);

        apiHandler.addPaperToCollection(request, new ApiCallback<CollectionPaperResponse>() {
            @Override
            public void onSuccess(CollectionPaperResponse response) {
                runOnUiThread(() -> {
                    Toast.makeText(CollectionsActivity.this,
                            "Paper added to collection successfully",
                            Toast.LENGTH_SHORT).show();
                    CollectionsFragment frag = getFragment();
                    if (frag != null) {
                        frag.refreshCollections();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error adding paper to collection: " + error);
                    Toast.makeText(CollectionsActivity.this,
                            "Failed to add paper: " + error,
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}

