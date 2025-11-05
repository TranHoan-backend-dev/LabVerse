package com.se1853_jv.labverse.presentation.collection;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.collection.CollectionApiHandler;
import com.se1853_jv.labverse.data.api.user.UserApiHandler;
import com.se1853_jv.labverse.data.dto.request.CollectionRequest;
import com.se1853_jv.labverse.data.dto.request.CollectionPaperRequest;
import com.se1853_jv.labverse.data.dto.request.CollectionUserRequest;
import com.se1853_jv.labverse.data.dto.response.CollectionResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionPaperResponse;
import com.se1853_jv.labverse.data.dto.response.UserResponse;
import com.se1853_jv.labverse.data.utils.Connectivity;
import com.se1853_jv.labverse.presentation.collection.adapter.CollectionsPagerAdapter;
import com.se1853_jv.labverse.presentation.collection.fragment.CollectionsFragment;
import com.se1853_jv.labverse.presentation.common.BaseActivity;

import java.util.List;

public class CollectionsActivity extends BaseActivity {
    private static final String TAG = "CollectionsActivity";

    private CollectionApiHandler apiHandler;
    private UserApiHandler userApiHandler;
    private CollectionsFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_common_ui_home);
        setupBottomNavbar(findViewById(R.id.ui_home), R.id.bottomNav); // dùng cái này để tiêm phương thức xử lý navbar

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ui_home), (v, insets) -> {
            var statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(statusBar.left, statusBar.top, statusBar.right, statusBar.bottom);
            return insets;
        });

        // Hide search bar
        hideSearchBar();

        apiHandler = new CollectionApiHandler();
        userApiHandler = new UserApiHandler(this);
        setupViewPager();
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

    public void showEditCollectionDialog(@NonNull CollectionResponse collection) {
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

    public void showDeleteCollectionDialog(@NonNull CollectionResponse collection) {
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

        // Get userId from session
        com.se1853_jv.labverse.data.utils.SessionManager sessionManager =
                new com.se1853_jv.labverse.data.utils.SessionManager(this);
        String userId = sessionManager.getUserId();

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Note: userId from session should be used as-is (backend will decode it)
        CollectionRequest request = new CollectionRequest(name, userId);
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

    public void showInviteMembersDialog(CollectionResponse collection) {
        if (collection == null) {
            Toast.makeText(this, "Please select a collection first", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Invite Team Members to " + collection.getName());

        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_invite_members, null);
        EditText editEmail = dialogView.findViewById(R.id.edit_member_email);

        builder.setView(dialogView);
        builder.setPositiveButton("Send Invite", (dialog, which) -> {
            String email = editEmail.getText().toString().trim();
            if (!email.isEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                inviteMember(email, collection);
            } else {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    public void showInviteMembersDialog() {
        // Show dialog to select collection from My Collections
        CollectionsFragment frag = getFragment();
        if (frag != null) {
            // Get first collection from My Collections (PI can only invite to their own collections)
            List<CollectionResponse> myCollections = frag.getMyCollections();
            if (myCollections != null && !myCollections.isEmpty()) {
                // For now, use the first collection. In the future, can add a selection dialog
                showInviteMembersDialog(myCollections.get(0));
            } else {
                Toast.makeText(this, "Please create a collection first", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please select a collection first", Toast.LENGTH_SHORT).show();
        }
    }

    private void inviteMember(String email, CollectionResponse collection) {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        // Step 1: Find user by email
        userApiHandler.getUserByEmail(email, new ApiCallback<UserResponse>() {
            @Override
            public void onSuccess(UserResponse userResponse) {
                // Step 2: Add user to collection
                CollectionUserRequest request = new CollectionUserRequest();
                request.setCollectionId(collection.getId());
                request.setMemberId(userResponse.getId());
                request.setIsAuthor(false); // Default to non-author, can be changed later

                apiHandler.addMemberToCollection(request, new ApiCallback<Object>() {
                    @Override
                    public void onSuccess(Object response) {
                        runOnUiThread(() -> {
                            Toast.makeText(CollectionsActivity.this,
                                    "Member invited successfully to " + collection.getName(),
                                    Toast.LENGTH_SHORT).show();
                            // Refresh collections to show updated member count
                            CollectionsFragment frag = getFragment();
                            if (frag != null) {
                                frag.refreshCollections();
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Log.e(TAG, "Error inviting member: " + error);
                            Toast.makeText(CollectionsActivity.this,
                                    "Failed to invite member: " + error,
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error finding user: " + error);
                    Toast.makeText(CollectionsActivity.this,
                            "User not found with email: " + email + ". Please make sure the user has registered.",
                            Toast.LENGTH_LONG).show();
                });
            }
        });
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

