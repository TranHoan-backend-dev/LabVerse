package com.se1853_jv.labverse.presentation.collection;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.se1853_jv.labverse.data.dto.request.UpdateCollectionRequest;
import com.se1853_jv.labverse.data.dto.response.CollectionResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionPaperResponse;
import com.se1853_jv.labverse.data.dto.response.UserResponse;
import com.se1853_jv.labverse.data.utils.Connectivity;
import com.se1853_jv.labverse.presentation.collection.adapter.CollectionsPagerAdapter;
import com.se1853_jv.labverse.presentation.collection.fragment.CollectionsFragment;
import com.se1853_jv.labverse.presentation.common.BaseActivity;
import com.se1853_jv.labverse.presentation.common.HeaderHelper;

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
        
        // Setup notification button click listener
        HeaderHelper.setupNotificationClickListener(this);
        // Load and update notification badge
        HeaderHelper.loadNotificationBadge(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh notification badge when returning to this activity
        HeaderHelper.loadNotificationBadge(this);
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
                updateCollection(collection, name);
            } else {
                Toast.makeText(this, "Collection name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    public void showDeleteCollectionDialog(@NonNull CollectionResponse collection) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Collection")
                .setMessage("Are you sure you want to delete \"" + collection.getName() + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteCollection(collection);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String title = collection != null 
            ? "Invite Team Members to " + collection.getName()
            : "Invite Team Members";
        builder.setTitle(title);

        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_invite_members, null);
        EditText editEmail = dialogView.findViewById(R.id.edit_member_email);
        Spinner spinnerCollection = dialogView.findViewById(R.id.spinner_collection);
        Spinner spinnerAccessLevel = dialogView.findViewById(R.id.spinner_access_level);
        TextView textCollectionLabel = dialogView.findViewById(R.id.text_collection_label);

        // Setup Access Level spinner
        String[] accessLevels = {"READ_ONLY", "CONTRIBUTOR", "AUTHOR"};
        ArrayAdapter<String> accessLevelAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, accessLevels);
        accessLevelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccessLevel.setAdapter(accessLevelAdapter);
        spinnerAccessLevel.setSelection(1); // Default to CONTRIBUTOR

        // Setup Collection spinner if collection is null
        CollectionsFragment frag = getFragment();
        List<CollectionResponse> myCollections = frag != null ? frag.getMyCollections() : null;
        
        if (collection == null && myCollections != null && !myCollections.isEmpty()) {
            // Show collection selector
            textCollectionLabel.setVisibility(View.VISIBLE);
            spinnerCollection.setVisibility(View.VISIBLE);
            
            ArrayAdapter<CollectionResponse> collectionAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, myCollections) {
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    TextView textView = (TextView) super.getView(position, convertView, parent);
                    textView.setText(getItem(position).getName());
                    return textView;
                }

                @Override
                public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
                    textView.setText(getItem(position).getName());
                    return textView;
                }
            };
            collectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCollection.setAdapter(collectionAdapter);
        } else {
            // Hide collection selector if collection is already selected
            textCollectionLabel.setVisibility(View.GONE);
            spinnerCollection.setVisibility(View.GONE);
        }

        builder.setView(dialogView);
        builder.setPositiveButton("Send Invite", (dialog, which) -> {
            String email = editEmail.getText().toString().trim();
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get selected collection
            CollectionResponse selectedCollection = collection;
            if (selectedCollection == null && spinnerCollection.getVisibility() == View.VISIBLE) {
                selectedCollection = (CollectionResponse) spinnerCollection.getSelectedItem();
            }

            if (selectedCollection == null) {
                Toast.makeText(this, "Please select a collection", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get selected access level
            String selectedAccessLevel = (String) spinnerAccessLevel.getSelectedItem();
            com.se1853_jv.labverse.domain.enumerate.AccessLevel accessLevel = 
                com.se1853_jv.labverse.domain.enumerate.AccessLevel.valueOf(selectedAccessLevel);

            inviteMember(email, selectedCollection, accessLevel);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    public void showInviteMembersDialog() {
        // Show dialog without pre-selected collection, user will choose from spinner
        CollectionsFragment frag = getFragment();
        if (frag != null) {
            List<CollectionResponse> myCollections = frag.getMyCollections();
            if (myCollections == null || myCollections.isEmpty()) {
                Toast.makeText(this, "Please create a collection first", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        showInviteMembersDialog(null); // Pass null to show collection selector
    }

    private void inviteMember(String email, CollectionResponse collection, 
                             com.se1853_jv.labverse.domain.enumerate.AccessLevel accessLevel) {
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
                request.setIsAuthor(accessLevel == com.se1853_jv.labverse.domain.enumerate.AccessLevel.AUTHOR);
                request.setAccessLevel(accessLevel);

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


    private void updateCollection(CollectionResponse collection, String newName) {
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

        UpdateCollectionRequest request = new UpdateCollectionRequest();
        request.setName(newName);
        request.setUserId(userId);

        apiHandler.updateCollection(collection.getId(), request, new ApiCallback<CollectionResponse>() {
            @Override
            public void onSuccess(CollectionResponse response) {
                runOnUiThread(() -> {
                    Toast.makeText(CollectionsActivity.this,
                            "Collection updated successfully",
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
                    Log.e(TAG, "Error updating collection: " + error);
                    Toast.makeText(CollectionsActivity.this,
                            "Failed to update collection: " + error,
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void deleteCollection(CollectionResponse collection) {
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

        apiHandler.deleteCollection(collection.getId(), userId, new ApiCallback<Object>() {
            @Override
            public void onSuccess(Object response) {
                runOnUiThread(() -> {
                    Toast.makeText(CollectionsActivity.this,
                            "Collection deleted successfully",
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
                    Log.e(TAG, "Error deleting collection: " + error);
                    Toast.makeText(CollectionsActivity.this,
                            "Failed to delete collection: " + error,
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
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

