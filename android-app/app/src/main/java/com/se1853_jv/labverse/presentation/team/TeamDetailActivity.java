package com.se1853_jv.labverse.presentation.team;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.se1853_jv.labverse.R;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.team.TeamApiHandler;
import com.se1853_jv.labverse.data.api.user.UserApiHandler;
import com.se1853_jv.labverse.data.dto.request.AddTeamMemberRequest;
import com.se1853_jv.labverse.data.dto.request.UpdateMemberRoleRequest;
import com.se1853_jv.labverse.data.dto.response.TeamMemberResponse;
import com.se1853_jv.labverse.data.dto.response.TeamResponse;
import com.se1853_jv.labverse.data.dto.response.UserResponse;
import com.google.android.material.button.MaterialButton;
import com.se1853_jv.labverse.data.utils.Connectivity;
import com.se1853_jv.labverse.data.utils.SessionManager;
import com.se1853_jv.labverse.presentation.common.HeaderHelper;
import com.se1853_jv.labverse.presentation.team.adapter.TeamMemberAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TeamDetailActivity extends AppCompatActivity {

    private static final String TAG = "TeamDetailActivity";

    private TextView tvProjectName, tvProjectStart, btnAddMember, tvProjectMembers, tvMembersTitle;
    private TextView btnEditTeam, tvTeamMembersCount, tvPrivacyStatus;
    private View layoutPrivacyBadge;
    private ChipGroup chipGroupMembers;
    private RecyclerView recyclerViewMembers;
    private String currentFilter = "All Members"; // Track current filter
    
    private TeamApiHandler teamApiHandler;
    private UserApiHandler userApiHandler;
    private TeamMemberAdapter memberAdapter;
    private SessionManager sessionManager;
    private List<TeamMemberResponse> allMembers = new ArrayList<>();
    private String currentTeamId;
    private TeamResponse currentTeam;
    private MaterialButton btnJoinTeam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailScrollView), (v, insets) -> {
            var statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(statusBar.left, statusBar.top, statusBar.right, statusBar.bottom);
            return insets;
        });

               // Initialize API handlers
               teamApiHandler = new TeamApiHandler(this);
               userApiHandler = new UserApiHandler(this);
               sessionManager = new SessionManager(this);

        bindViews();
        setupRecyclerView();
        
        // Get team ID from intent
        currentTeamId = getIntent().getStringExtra("teamId");
        if (currentTeamId == null || currentTeamId.isEmpty()) {
            Toast.makeText(this, "Team ID not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load team details and members
        loadTeamDetails(currentTeamId);
        loadTeamMembers(currentTeamId);

        handleEvents();
        
        // Setup avatar and profile navigation click listeners
        HeaderHelper.setupProfileClickListeners(this);
        // Setup Lists navigation click listener
        HeaderHelper.setupListsNavigationClickListener(this);
    }

    private void bindViews() {
        tvProjectName = findViewById(R.id.tvProjectName);
        tvProjectStart = findViewById(R.id.tvProjectStart);
        tvProjectMembers = findViewById(R.id.tvProjectMembers);
        tvMembersTitle = findViewById(R.id.tvMembersTitle);
        tvTeamMembersCount = findViewById(R.id.tvTeamMembersCount);
        tvPrivacyStatus = findViewById(R.id.tvPrivacyStatus);
        layoutPrivacyBadge = findViewById(R.id.layoutPrivacyBadge);
        chipGroupMembers = findViewById(R.id.chipGroupMembers);
        btnAddMember = findViewById(R.id.btnAddMember);
               btnEditTeam = findViewById(R.id.btnEditTeam);
               recyclerViewMembers = findViewById(R.id.recyclerViewMembers);
               btnJoinTeam = findViewById(R.id.btnJoinTeam);
    }

    private void setupRecyclerView() {
        memberAdapter = new TeamMemberAdapter();
        memberAdapter.setOnMemberActionListener(new TeamMemberAdapter.OnMemberActionListener() {
            @Override
            public void onRemoveMember(TeamMemberResponse member) {
                showRemoveMemberDialog(member);
            }

            @Override
            public void onEditRole(TeamMemberResponse member) {
                showEditRoleDialog(member);
            }
        });
        
        // Set permissions: only creator or PI can remove members
        // Only PI can edit roles
        // This will be updated when team details are loaded
        memberAdapter.setCanRemoveMembers(false); // Will be set after loading team details
        memberAdapter.setCanEditRoles(false); // Will be set after loading team details
        
        recyclerViewMembers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMembers.setAdapter(memberAdapter);
    }

    private void loadTeamDetails(@NonNull String teamId) {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        teamApiHandler.getTeamById(teamId, new ApiCallback<TeamResponse>() {
            @Override
            public void onSuccess(TeamResponse teamResponse) {
                runOnUiThread(() -> {
                    currentTeam = teamResponse;
                    displayTeamDetails(teamResponse);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error loading team details: " + error);
                    Toast.makeText(TeamDetailActivity.this, 
                            "Failed to load team details: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void displayTeamDetails(TeamResponse team) {
        if (team == null) return;
        
        // Set team name
                if (tvProjectName != null) {
            tvProjectName.setText(team.getName() != null ? team.getName() : "Team");
                }

        // Set start date
                if (tvProjectStart != null) {
            String createdDate = team.getCreatedDate();
            if (createdDate != null && !createdDate.isEmpty()) {
                try {
                    Date date = null;
                    // Try different date formats
                    String[] formats = {
                        "yyyy-MM-dd'T'HH:mm:ss",
                        "yyyy-MM-dd'T'HH:mm:ss.SSS",
                        "yyyy-MM-dd",
                        "yyyy-MM-dd HH:mm:ss"
                    };
                    
                    for (String format : formats) {
                        try {
                            SimpleDateFormat inputFormat = new SimpleDateFormat(format, Locale.getDefault());
                            date = inputFormat.parse(createdDate);
                            break;
                        } catch (ParseException ignored) {
                            // Try next format
                        }
                    }
                    
                    if (date != null) {
                        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                        String formattedDate = outputFormat.format(date);
                        tvProjectStart.setText("Started: " + formattedDate);
                    } else {
                        // If parsing fails, try to extract date part
                        if (createdDate.length() >= 10) {
                            tvProjectStart.setText("Started: " + createdDate.substring(0, 10));
                        } else {
                            tvProjectStart.setText("Started: " + createdDate);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing date: " + createdDate, e);
                    tvProjectStart.setText("Started: " + createdDate);
                }
            } else {
                tvProjectStart.setText("Started: N/A");
            }
        }

        // Set member count in purple card
        if (tvProjectMembers != null) {
            int memberCount = team.getMemberCount() != null ? team.getMemberCount() : 0;
            tvProjectMembers.setText(memberCount + " Members");
        }
        
        // Set privacy status
        if (tvPrivacyStatus != null && layoutPrivacyBadge != null) {
            String privacy = team.getPrivacy() != null ? team.getPrivacy() : "PRIVATE";
            tvPrivacyStatus.setText(privacy);
            
            // Change background color based on privacy
            if ("PUBLIC".equalsIgnoreCase(privacy)) {
                layoutPrivacyBadge.setBackgroundResource(R.drawable.bg_chip_green);
            } else {
                layoutPrivacyBadge.setBackgroundResource(R.drawable.bg_chip_orange);
            }
            layoutPrivacyBadge.setVisibility(View.VISIBLE);
        }
        
        // Update member count in stats card (will be updated when members are loaded)
        updateMemberCountStats();
        
        // Show/hide Join button for PUBLIC teams
        updateJoinButtonVisibility();
        
        // Update adapter with creator ID and current user permissions
        updateAdapterPermissions();
    }
    
    private void updateAdapterPermissions() {
        if (memberAdapter == null || currentTeam == null) {
            return;
        }
        
        // Set team creator ID
        if (currentTeam.getCreatedById() != null) {
            memberAdapter.setTeamCreatorId(currentTeam.getCreatedById());
        }
        
        // Get current user ID and check if they can remove members
        String currentUserId = sessionManager.getUserId();
        if (currentUserId != null) {
            memberAdapter.setCurrentUserId(currentUserId);
            
            // Check if current user is creator or PI
            boolean isCreator = currentUserId.equals(currentTeam.getCreatedById());
            boolean isPI = false;
            
            // Check if current user is PI in the team
            if (allMembers != null) {
                for (TeamMemberResponse member : allMembers) {
                    if (currentUserId.equals(member.getUserId()) && 
                        "PI".equalsIgnoreCase(member.getRole())) {
                        isPI = true;
                        break;
                    }
                }
            }
            
            // Only creator or PI can remove members
            memberAdapter.setCanRemoveMembers(isCreator || isPI);
            // Only PI can edit roles (not creator, only PI)
            memberAdapter.setCanEditRoles(isPI);
        }
    }
    
    private void updateJoinButtonVisibility() {
        if (btnJoinTeam == null || currentTeam == null) {
            return;
        }
        
        boolean isPublic = "PUBLIC".equalsIgnoreCase(currentTeam.getPrivacy());
        boolean isMember = currentTeam.getIsMember() != null && currentTeam.getIsMember();
        
        if (isPublic && !isMember) {
            btnJoinTeam.setVisibility(View.VISIBLE);
            btnJoinTeam.setOnClickListener(v -> joinTeam());
        } else {
            btnJoinTeam.setVisibility(View.GONE);
        }
    }
    
    private void joinTeam() {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentTeam == null || currentTeamId == null) {
            Toast.makeText(this, "Team information not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if team is PUBLIC
        if (!"PUBLIC".equalsIgnoreCase(currentTeam.getPrivacy())) {
            Toast.makeText(this, "This team is private. You need an invitation to join.", Toast.LENGTH_LONG).show();
            return;
        }

        // Get current user ID
        String currentUserId = sessionManager.getUserId();
        if (currentUserId == null || currentUserId.isEmpty()) {
            // Try to get from API
            userApiHandler.getCurrentUser(new ApiCallback<UserResponse>() {
                @Override
                public void onSuccess(UserResponse userResponse) {
                    if (userResponse != null && userResponse.getId() != null) {
                        addCurrentUserToTeam(userResponse.getId());
                    } else {
                        Toast.makeText(TeamDetailActivity.this, "Unable to get user information", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(TeamDetailActivity.this, "Unable to get user information: " + error, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            addCurrentUserToTeam(currentUserId);
        }
    }

    private void addCurrentUserToTeam(String userId) {
        if (btnJoinTeam != null) {
            btnJoinTeam.setEnabled(false);
            btnJoinTeam.setText("Joining...");
        }

        AddTeamMemberRequest request = new AddTeamMemberRequest();
        request.setUserId(userId);
        request.setRole("STUDENT"); // Default role for self-join

        teamApiHandler.addTeamMember(currentTeamId, request, new ApiCallback<TeamMemberResponse>() {
            @Override
            public void onSuccess(TeamMemberResponse teamMemberResponse) {
                runOnUiThread(() -> {
                    if (btnJoinTeam != null) {
                        btnJoinTeam.setEnabled(true);
                        btnJoinTeam.setVisibility(View.GONE);
                    }
                    Toast.makeText(TeamDetailActivity.this, "Successfully joined team!", Toast.LENGTH_SHORT).show();
                    // Reload team details and members to update membership status
                    loadTeamDetails(currentTeamId);
                    loadTeamMembers(currentTeamId);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    if (btnJoinTeam != null) {
                        btnJoinTeam.setEnabled(true);
                        btnJoinTeam.setText("Join Team");
                    }
                    Log.e(TAG, "Error joining team: " + error);
                    Toast.makeText(TeamDetailActivity.this, "Failed to join team: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private void updateMemberCountStats() {
        if (tvTeamMembersCount != null) {
            int count = allMembers != null ? allMembers.size() : 0;
            tvTeamMembersCount.setText(String.valueOf(count));
        }
    }

    private void loadTeamMembers(@NonNull String teamId) {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        teamApiHandler.getTeamMembers(teamId, new ApiCallback<List<TeamMemberResponse>>() {
            @Override
            public void onSuccess(List<TeamMemberResponse> members) {
                runOnUiThread(() -> {
                    allMembers = members != null ? new ArrayList<>(members) : new ArrayList<>();
                    filterMembersByRole("All Members");
                    updateMemberCountStats();
                    // Update adapter permissions after loading members
                    updateAdapterPermissions();
                    Log.d(TAG, "Loaded " + allMembers.size() + " members");
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error loading team members: " + error);
                    Toast.makeText(TeamDetailActivity.this, 
                            "Failed to load members: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void filterMembersByRole(String filterText) {
        currentFilter = filterText; // Update current filter
        List<TeamMemberResponse> filtered = new ArrayList<>();
        
        if ("All Members".equals(filterText)) {
            filtered = new ArrayList<>(allMembers);
        } else if ("PIs".equals(filterText)) {
            for (TeamMemberResponse member : allMembers) {
                if (member.getRole() != null && 
                    (member.getRole().equalsIgnoreCase("PI") || 
                     member.getRole().equalsIgnoreCase("PRINCIPAL_INVESTIGATOR"))) {
                    filtered.add(member);
                }
            }
        } else if ("Researchers".equals(filterText)) {
            for (TeamMemberResponse member : allMembers) {
                if (member.getRole() != null && 
                    (member.getRole().equalsIgnoreCase("RESEARCHER") || 
                     member.getRole().equalsIgnoreCase("Postdoc") ||
                     member.getRole().equalsIgnoreCase("PhD"))) {
                    filtered.add(member);
                }
            }
        } else if ("Students".equals(filterText) || "Designers".equals(filterText) || "Developers".equals(filterText)) {
            // Map these to STUDENT role
            for (TeamMemberResponse member : allMembers) {
                if (member.getRole() != null && 
                    (member.getRole().equalsIgnoreCase("STUDENT") || 
                     member.getRole().equalsIgnoreCase("INTERN"))) {
                    filtered.add(member);
                }
            }
        } else {
            filtered = new ArrayList<>(allMembers);
        }
        
        memberAdapter.setMembers(filtered);
    }

    private void handleEvents() {
        chipGroupMembers.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            int checkedId = checkedIds.get(0);
            Chip chip = findViewById(checkedId);
            if (chip != null) {
                String filterText = chip.getText().toString();
                filterMembersByRole(filterText);
            }
        });

        if (btnAddMember != null) {
        btnAddMember.setOnClickListener(v -> {
                showAddMemberDialog();
            });
        }
        
        if (btnEditTeam != null) {
            btnEditTeam.setOnClickListener(v -> {
                if (currentTeamId == null || currentTeamId.isEmpty()) {
                    Toast.makeText(this, "Team ID not available", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    Intent intent = new Intent(TeamDetailActivity.this, TeamEditActivity.class);
                    intent.putExtra("teamId", currentTeamId);
                    startActivityForResult(intent, 100);
                } catch (Exception e) {
                    Log.e(TAG, "Error opening edit activity: " + e.getMessage(), e);
                    Toast.makeText(this, "Failed to open edit screen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Log.e(TAG, "btnEditTeam is null");
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Team was updated, reload team details and members
            if (currentTeamId != null) {
                loadTeamDetails(currentTeamId);
                loadTeamMembers(currentTeamId);
            }
        }
    }

    private void showAddMemberDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_team_member, null);
        
        TextInputEditText etUserEmail = dialogView.findViewById(R.id.etUserEmail);
        RadioGroup rgRole = dialogView.findViewById(R.id.rgRole);
        RadioButton rbPI = dialogView.findViewById(R.id.rbPI);
        RadioButton rbResearcher = dialogView.findViewById(R.id.rbResearcher);
        RadioButton rbStudent = dialogView.findViewById(R.id.rbStudent);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnAdd = dialogView.findViewById(R.id.btnAdd);
        
        // Set default role to RESEARCHER
        rbResearcher.setChecked(true);
        
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnAdd.setOnClickListener(v -> {
            String emailOrId = etUserEmail.getText() != null ? 
                    etUserEmail.getText().toString().trim() : "";
            
            if (TextUtils.isEmpty(emailOrId)) {
                etUserEmail.setError("Please enter email or user ID");
                etUserEmail.requestFocus();
                return;
            }
            
            // Determine selected role
            String role = "RESEARCHER"; // Default
            if (rbPI.isChecked()) {
                role = "PI";
            } else if (rbResearcher.isChecked()) {
                role = "RESEARCHER";
            } else if (rbStudent.isChecked()) {
                role = "STUDENT";
            }
            
            // Search user first, then add to team
            searchAndAddMember(emailOrId, role, dialog);
        });
        
        dialog.show();
    }
    
    private void searchAndAddMember(String emailOrId, String role, androidx.appcompat.app.AlertDialog dialog) {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading
        dialog.findViewById(R.id.btnAdd).setEnabled(false);
        ((Button) dialog.findViewById(R.id.btnAdd)).setText("Adding...");
        
        // Try to get user by email first
        userApiHandler.getUserByEmail(emailOrId, new ApiCallback<UserResponse>() {
            @Override
            public void onSuccess(UserResponse userResponse) {
                runOnUiThread(() -> {
                    if (userResponse != null && userResponse.getId() != null) {
                        // User found, add to team
                        addMemberToTeam(userResponse.getId(), role, dialog);
                    } else {
                        // Try by ID
                        userApiHandler.getUserById(emailOrId, new ApiCallback<UserResponse>() {
                            @Override
                            public void onSuccess(UserResponse userResponse) {
                                runOnUiThread(() -> {
                                    if (userResponse != null && userResponse.getId() != null) {
                                        addMemberToTeam(userResponse.getId(), role, dialog);
                                    } else {
                                        dialog.findViewById(R.id.btnAdd).setEnabled(true);
                                        ((Button) dialog.findViewById(R.id.btnAdd)).setText("Add Member");
                                        Toast.makeText(TeamDetailActivity.this, 
                                                "User not found", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                            
                            @Override
                            public void onError(String error) {
                                runOnUiThread(() -> {
                                    dialog.findViewById(R.id.btnAdd).setEnabled(true);
                                    ((Button) dialog.findViewById(R.id.btnAdd)).setText("Add Member");
                                    Toast.makeText(TeamDetailActivity.this, 
                                            "User not found: " + error, Toast.LENGTH_LONG).show();
                                });
                            }
                        });
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                // If email search fails, try by ID
                userApiHandler.getUserById(emailOrId, new ApiCallback<UserResponse>() {
                    @Override
                    public void onSuccess(UserResponse userResponse) {
                        runOnUiThread(() -> {
                            if (userResponse != null && userResponse.getId() != null) {
                                addMemberToTeam(userResponse.getId(), role, dialog);
                            } else {
                                dialog.findViewById(R.id.btnAdd).setEnabled(true);
                                ((Button) dialog.findViewById(R.id.btnAdd)).setText("Add Member");
                                Toast.makeText(TeamDetailActivity.this, 
                                        "User not found", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    
                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            dialog.findViewById(R.id.btnAdd).setEnabled(true);
                            ((Button) dialog.findViewById(R.id.btnAdd)).setText("Add Member");
                            Toast.makeText(TeamDetailActivity.this, 
                                    "User not found: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
                });
            }
        });
    }
    
    private void addMemberToTeam(String userId, String role, androidx.appcompat.app.AlertDialog dialog) {
        AddTeamMemberRequest request = new AddTeamMemberRequest();
        // Backend will decode userId if encoded, or use as-is if not encoded
        request.setUserId(userId);
        request.setRole(role);
        
        teamApiHandler.addTeamMember(currentTeamId, request, new ApiCallback<TeamMemberResponse>() {
            @Override
            public void onSuccess(TeamMemberResponse teamMemberResponse) {
                runOnUiThread(() -> {
                    dialog.dismiss();
                    
                    // Add to local list
                    allMembers.add(teamMemberResponse);
                    memberAdapter.addMember(teamMemberResponse);
                    
                    // Update member count in both places
                    updateMemberCountStats();
                    if (currentTeam != null) {
                        int newCount = allMembers.size();
                        currentTeam.setMemberCount(newCount);
                        if (tvProjectMembers != null) {
                            tvProjectMembers.setText(newCount + " Members");
                        }
                    }
                    
                    Toast.makeText(TeamDetailActivity.this, 
                            "Member added successfully", Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    dialog.findViewById(R.id.btnAdd).setEnabled(true);
                    ((Button) dialog.findViewById(R.id.btnAdd)).setText("Add Member");
                    Log.e(TAG, "Error adding member: " + error);
                    Toast.makeText(TeamDetailActivity.this, 
                            "Failed to add member: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showEditRoleDialog(TeamMemberResponse member) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_member_role, null);

        TextView tvMemberName = dialogView.findViewById(R.id.tvMemberName);
        RadioGroup rgRole = dialogView.findViewById(R.id.rgRole);
        RadioButton rbPI = dialogView.findViewById(R.id.rbPI);
        RadioButton rbResearcher = dialogView.findViewById(R.id.rbResearcher);
        RadioButton rbStudent = dialogView.findViewById(R.id.rbStudent);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnUpdate = dialogView.findViewById(R.id.btnUpdate);

        // Set member name
        if (tvMemberName != null) {
            String displayName = member.getUserFullName() != null && !member.getUserFullName().isEmpty()
                    ? member.getUserFullName()
                    : member.getUserName() != null ? member.getUserName() : "Unknown";
            tvMemberName.setText(displayName);
        }

        // Set current role
        String currentRole = member.getRole();
        if (currentRole != null) {
            if ("PI".equalsIgnoreCase(currentRole)) {
                rbPI.setChecked(true);
            } else if ("RESEARCHER".equalsIgnoreCase(currentRole)) {
                rbResearcher.setChecked(true);
            } else if ("STUDENT".equalsIgnoreCase(currentRole)) {
                rbStudent.setChecked(true);
            } else {
                rbResearcher.setChecked(true); // Default
            }
        } else {
            rbResearcher.setChecked(true); // Default
        }

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnUpdate.setOnClickListener(v -> {
            // Determine selected role
            String newRole = "RESEARCHER"; // Default
            if (rbPI.isChecked()) {
                newRole = "PI";
            } else if (rbResearcher.isChecked()) {
                newRole = "RESEARCHER";
            } else if (rbStudent.isChecked()) {
                newRole = "STUDENT";
            }

            // Check if role changed
            if (newRole.equalsIgnoreCase(currentRole)) {
                Toast.makeText(this, "Role unchanged", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }

            // Update role
            updateMemberRole(member, newRole, dialog);
        });

        dialog.show();
    }

    private void updateMemberRole(TeamMemberResponse member, String newRole, androidx.appcompat.app.AlertDialog dialog) {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentTeamId == null || member.getUserId() == null) {
            Toast.makeText(this, "Invalid member information", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button during update
        Button btnUpdate = dialog.findViewById(R.id.btnUpdate);
        if (btnUpdate != null) {
            btnUpdate.setEnabled(false);
            btnUpdate.setText("Updating...");
        }

        UpdateMemberRoleRequest request = new UpdateMemberRoleRequest();
        request.setRole(newRole);

        teamApiHandler.updateMemberRole(currentTeamId, member.getUserId(), request, new ApiCallback<TeamMemberResponse>() {
            @Override
            public void onSuccess(TeamMemberResponse updatedMember) {
                runOnUiThread(() -> {
                    dialog.dismiss();

                    // Update member in local list
                    for (int i = 0; i < allMembers.size(); i++) {
                        TeamMemberResponse m = allMembers.get(i);
                        if (m.getUserId() != null && m.getUserId().equals(member.getUserId())) {
                            allMembers.set(i, updatedMember);
                            break;
                        }
                    }

                    // Refresh adapter with current filter
                    filterMembersByRole(currentFilter != null ? currentFilter : "All Members");

                    Toast.makeText(TeamDetailActivity.this,
                            "Member role updated successfully", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    if (btnUpdate != null) {
                        btnUpdate.setEnabled(true);
                        btnUpdate.setText("Update Role");
                    }
                    Log.e(TAG, "Error updating member role: " + error);
                    Toast.makeText(TeamDetailActivity.this,
                            "Failed to update role: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showRemoveMemberDialog(TeamMemberResponse member) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Member")
                .setMessage("Are you sure you want to remove " + 
                           (member.getUserFullName() != null ? member.getUserFullName() : member.getUserName()) + 
                           " from this team?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    removeMember(member);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removeMember(TeamMemberResponse member) {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        // API expects userId, not team member id
        String userIdToRemove = member.getUserId();
        if (userIdToRemove == null || userIdToRemove.isEmpty()) {
            Toast.makeText(this, "Invalid member information", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Member userId is null or empty");
            return;
        }

        Log.d(TAG, "Removing member - userId: " + userIdToRemove + ", teamId: " + currentTeamId);
        final String finalUserIdToRemove = userIdToRemove; // Make final for use in inner class
        teamApiHandler.removeTeamMember(currentTeamId, userIdToRemove, new ApiCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                runOnUiThread(() -> {
                    // Remove from local list using userId
                    allMembers.removeIf(m -> finalUserIdToRemove != null && finalUserIdToRemove.equals(m.getUserId()));
                    memberAdapter.removeMember(member);
                    
                    // Update member count in both places
                    updateMemberCountStats();
                    if (currentTeam != null) {
                        int newCount = allMembers.size();
                        currentTeam.setMemberCount(newCount);
                        if (tvProjectMembers != null) {
                            tvProjectMembers.setText(newCount + " Members");
                        }
                    }
                    
                    Toast.makeText(TeamDetailActivity.this, 
                            "Member removed successfully", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error removing member: " + error);
                    Toast.makeText(TeamDetailActivity.this, 
                            "Failed to remove member: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload members when returning to this activity
        if (currentTeamId != null) {
            loadTeamMembers(currentTeamId);
        }
    }
}
