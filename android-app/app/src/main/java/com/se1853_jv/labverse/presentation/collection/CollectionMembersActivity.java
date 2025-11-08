package com.se1853_jv.labverse.presentation.collection;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.collection.CollectionApiHandler;
import com.se1853_jv.labverse.data.api.user.UserApiHandler;
import com.se1853_jv.labverse.data.dto.request.UpdateMemberAccessRequest;
import com.se1853_jv.labverse.data.dto.response.CollectionResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionUserResponse;
import com.se1853_jv.labverse.data.dto.response.UserResponse;
import com.se1853_jv.labverse.data.utils.Connectivity;
import com.se1853_jv.labverse.data.utils.SessionManager;
import com.se1853_jv.labverse.domain.enumerate.AccessLevel;
import com.se1853_jv.labverse.presentation.collection.adapter.CollectionMemberAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionMembersActivity extends AppCompatActivity {
    private static final String TAG = "CollectionMembersActivity";

    private CollectionApiHandler apiHandler;
    private UserApiHandler userApiHandler;
    private RecyclerView recyclerMembers;
    private TextView textEmptyState;
    private CollectionMemberAdapter adapter;
    private CollectionResponse collection;
    private List<CollectionUserResponse> members = new ArrayList<>();
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_members);

        collection = (CollectionResponse) getIntent().getSerializableExtra("collection");
        if (collection == null) {
            Toast.makeText(this, "Collection not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        SessionManager sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();

        // Check if current user is AUTHOR - only AUTHOR can manage members
        com.se1853_jv.labverse.domain.enumerate.AccessLevel currentUserAccessLevel = collection.getCurrentUserAccessLevel();
        boolean isAuthor = currentUserAccessLevel != null && 
                          currentUserAccessLevel == com.se1853_jv.labverse.domain.enumerate.AccessLevel.AUTHOR;
        
        if (!isAuthor) {
            Toast.makeText(this, "Only collection authors can manage members", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiHandler = new CollectionApiHandler();
        userApiHandler = new UserApiHandler(this);
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        loadMembers();
    }

    private void initializeViews() {
        recyclerMembers = findViewById(R.id.recycler_members);
        textEmptyState = findViewById(R.id.text_empty_state);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Members - " + collection.getName());
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new CollectionMemberAdapter();
        adapter.setOnMemberClickListener(new CollectionMemberAdapter.OnMemberClickListener() {
            @Override
            public void onAccessLevelClick(CollectionUserResponse member) {
                showChangeAccessLevelDialog(member);
            }

            @Override
            public void onRemoveClick(CollectionUserResponse member) {
                showRemoveMemberDialog(member);
            }
        });
        recyclerMembers.setLayoutManager(new LinearLayoutManager(this));
        recyclerMembers.setAdapter(adapter);
    }

    private void loadMembers() {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        apiHandler.getMembers(collection.getId(), new ApiCallback<List<CollectionUserResponse>>() {
            @Override
            public void onSuccess(List<CollectionUserResponse> response) {
                runOnUiThread(() -> {
                    members.clear();
                    if (response != null) {
                        // Fetch user details for each member
                        fetchMemberDetails(response);
                    } else {
                        updateEmptyState();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    android.util.Log.e(TAG, "Error loading members: " + error);
                    Toast.makeText(CollectionMembersActivity.this,
                            "Failed to load members: " + error,
                            Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });
            }
        });
    }

    private void fetchMemberDetails(List<CollectionUserResponse> memberList) {
        members.clear();
        if (memberList == null || memberList.isEmpty()) {
            updateEmptyState();
            return;
        }

        members = new ArrayList<>(memberList);
        
        // Set current user's access level in adapter (should be AUTHOR since we checked in onCreate)
        com.se1853_jv.labverse.domain.enumerate.AccessLevel currentUserAccessLevel = collection.getCurrentUserAccessLevel();
        adapter.setCurrentUserAccessLevel(currentUserAccessLevel);
        adapter.setMembers(members, memberList, currentUserId);
        
        // Fetch user details for each member asynchronously
        final int totalMembers = memberList.size();
        final int[] loadedCount = {0};
        final Map<String, UserResponse> userDetailsMap = new HashMap<>();

        for (CollectionUserResponse member : memberList) {
            String memberId = member.getMemberId();
            
            // Fetch user details by ID
            userApiHandler.getUserById(memberId, new ApiCallback<UserResponse>() {
                @Override
                public void onSuccess(UserResponse userResponse) {
                    synchronized (userDetailsMap) {
                        userDetailsMap.put(memberId, userResponse);
                        loadedCount[0]++;
                        
                        // Update adapter when all users are loaded or incrementally
                        if (loadedCount[0] == totalMembers) {
                            runOnUiThread(() -> {
                                // Update adapter with user details
                                for (int i = 0; i < members.size(); i++) {
                                    CollectionUserResponse m = members.get(i);
                                    UserResponse details = userDetailsMap.get(m.getMemberId());
                                    if (details != null) {
                                        adapter.setUserDetails(m.getMemberId(), details);
                                    }
                                }
                            });
                        } else {
                            // Update incrementally
                            runOnUiThread(() -> {
                                adapter.setUserDetails(memberId, userResponse);
                            });
                        }
                    }
                }

                @Override
                public void onError(String error) {
                    synchronized (userDetailsMap) {
                        loadedCount[0]++;
                        android.util.Log.w(TAG, "Failed to fetch user details for member " + memberId + ": " + error);
                        // Continue even if one fails
                        if (loadedCount[0] == totalMembers) {
                            runOnUiThread(() -> {
                                // Update adapter with available user details
                                for (int i = 0; i < members.size(); i++) {
                                    CollectionUserResponse m = members.get(i);
                                    UserResponse details = userDetailsMap.get(m.getMemberId());
                                    if (details != null) {
                                        adapter.setUserDetails(m.getMemberId(), details);
                                    }
                                }
                            });
                        }
                    }
                }
            });
        }
        
        updateEmptyState();
    }

    private void showChangeAccessLevelDialog(CollectionUserResponse member) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Access Level");

        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_change_access_level, null);
        Spinner spinnerAccessLevel = dialogView.findViewById(R.id.spinner_access_level);

        // Setup Access Level spinner
        String[] accessLevels = {"READ_ONLY", "CONTRIBUTOR", "AUTHOR"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, accessLevels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccessLevel.setAdapter(adapter);

        // Set current access level
        AccessLevel currentLevel = member.getAccessLevel();
        if (currentLevel != null) {
            int position = java.util.Arrays.asList(accessLevels).indexOf(currentLevel.name());
            if (position >= 0) {
                spinnerAccessLevel.setSelection(position);
            }
        }

        builder.setView(dialogView);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String selectedLevel = (String) spinnerAccessLevel.getSelectedItem();
            AccessLevel newAccessLevel = AccessLevel.valueOf(selectedLevel);
            updateMemberAccess(member, newAccessLevel);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateMemberAccess(CollectionUserResponse member, AccessLevel newAccessLevel) {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        UpdateMemberAccessRequest request = new UpdateMemberAccessRequest();
        request.setUserId(currentUserId);
        request.setAccessLevel(newAccessLevel);

        apiHandler.updateMemberAccess(collection.getId(), member.getMemberId(), request,
                new ApiCallback<CollectionUserResponse>() {
                    @Override
                    public void onSuccess(CollectionUserResponse response) {
                        runOnUiThread(() -> {
                            Toast.makeText(CollectionMembersActivity.this,
                                    "Access level updated successfully",
                                    Toast.LENGTH_SHORT).show();
                            loadMembers(); // Refresh list
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            android.util.Log.e(TAG, "Error updating access level: " + error);
                            Toast.makeText(CollectionMembersActivity.this,
                                    "Failed to update access level: " + error,
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    private void showRemoveMemberDialog(CollectionUserResponse member) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Member")
                .setMessage("Are you sure you want to remove this member from the collection?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    removeMember(member);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removeMember(CollectionUserResponse member) {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        apiHandler.removeMember(collection.getId(), member.getMemberId(), new ApiCallback<Object>() {
            @Override
            public void onSuccess(Object response) {
                runOnUiThread(() -> {
                    Toast.makeText(CollectionMembersActivity.this,
                            "Member removed successfully",
                            Toast.LENGTH_SHORT).show();
                    loadMembers(); // Refresh list
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    android.util.Log.e(TAG, "Error removing member: " + error);
                    Toast.makeText(CollectionMembersActivity.this,
                            "Failed to remove member: " + error,
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateEmptyState() {
        if (members.isEmpty()) {
            textEmptyState.setVisibility(View.VISIBLE);
            recyclerMembers.setVisibility(View.GONE);
        } else {
            textEmptyState.setVisibility(View.GONE);
            recyclerMembers.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

