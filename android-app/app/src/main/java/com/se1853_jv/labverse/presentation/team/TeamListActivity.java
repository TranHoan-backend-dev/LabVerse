package com.se1853_jv.labverse.presentation.team;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.team.TeamApiHandler;
import com.se1853_jv.labverse.data.dto.response.TeamResponse;
import com.se1853_jv.labverse.data.dto.response.TeamsPageResponse;
import com.google.android.material.button.MaterialButton;
import com.se1853_jv.labverse.data.api.user.UserApiHandler;
import com.se1853_jv.labverse.data.dto.request.AddTeamMemberRequest;
import com.se1853_jv.labverse.data.dto.response.TeamMemberResponse;
import com.se1853_jv.labverse.data.dto.response.UserResponse;
import com.se1853_jv.labverse.data.utils.Connectivity;
import com.se1853_jv.labverse.data.utils.SessionManager;
import com.se1853_jv.labverse.domain.db.DatabaseClient;
import com.se1853_jv.labverse.domain.infrastructure.team.model.Team;
import com.se1853_jv.labverse.domain.infrastructure.team.repo.TeamRepository;
import com.se1853_jv.labverse.presentation.common.HeaderHelper;

import java.util.ArrayList;
import java.util.List;

public class TeamListActivity extends AppCompatActivity {

    private static final String TAG = "TeamListActivity";

    private ChipGroup chipGroupFilters;
    private ImageButton btnSort;
    private MaterialButton btnCreateTeam;
    private TextView tvSectionTitle;
    private TextView tvRecommended;

    private TeamApiHandler teamApiHandler;
    private UserApiHandler userApiHandler;
    private TeamRepository teamRepository;
    private SessionManager sessionManager;
    private List<TeamResponse> allTeams = new ArrayList<>();
    private List<TeamResponse> activeTeams = new ArrayList<>();
    private List<TeamResponse> recommendedTeams = new ArrayList<>();
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scrollContent), (v, insets) -> {
            var statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(statusBar.left, statusBar.top, statusBar.right, statusBar.bottom);
            return insets;
        });

        // Initialize API handler and database
        teamApiHandler = new TeamApiHandler(this);
        userApiHandler = new UserApiHandler(this);
        teamRepository = DatabaseClient.getInstance(this).getAppDatabase().teamRepository();
        sessionManager = new SessionManager(this);

        bindViews();
        handleEvents();

        // Setup avatar and profile navigation click listeners
        HeaderHelper.setupProfileClickListeners(this);
        HeaderHelper.setupListsNavigationClickListener(this);
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

        // Load teams from API or database
        loadTeams();
    }

    private void bindViews() {
        chipGroupFilters = findViewById(R.id.chipGroupFilters);
        btnSort = findViewById(R.id.btnSort);
        btnCreateTeam = findViewById(R.id.btnCreateTeam);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);
        tvRecommended = findViewById(R.id.tvRecommended);
    }

    private void handleEvents() {
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            int checkedId = checkedIds.get(0);
            Chip chip = findViewById(checkedId);
            if (chip != null) {
                String filterText = chip.getText().toString();
                filterTeams(filterText);
            }
        });

        btnSort.setOnClickListener(v -> {
            Toast.makeText(this, "Sort options", Toast.LENGTH_SHORT).show();
            // TODO: Show sort dialog
        });

        // Create Team button click
        btnCreateTeam.setOnClickListener(v -> {
            Intent intent = new Intent(this, TeamCreateActivity.class);
            startActivity(intent);
        });
    }

    private void loadTeams() {
        if (isLoading) return;
        isLoading = true;

        // Check connectivity
        boolean isOnline = Connectivity.isInternetAvailable(this);

        if (isOnline) {
            // Load from API
            loadTeamsFromAPI();
        } else {
            // Load from database
            loadTeamsFromDatabase();
        }
    }

    private void loadTeamsFromAPI() {
        Log.d(TAG, "Loading teams from API...");

        teamApiHandler.getTeams(null, null, null, 0, 100, new ApiCallback<TeamsPageResponse>() {
            @Override
            public void onSuccess(TeamsPageResponse response) {
                isLoading = false;
                if (response != null && response.getContent() != null) {
                    allTeams = response.getContent();

                    // Save to database
                    saveTeamsToDatabase(allTeams);

                    // Categorize teams
                    categorizeTeams();

                    // Display teams
                    displayTeams();

                    Log.d(TAG, "Teams loaded from API: " + allTeams.size());
                } else {
                    // If API returns empty, try loading from database
                    loadTeamsFromDatabase();
                }
            }

            @Override
            public void onError(String error) {
                isLoading = false;
                Log.e(TAG, "Error loading teams from API: " + error);
                Toast.makeText(TeamListActivity.this, "Failed to load teams: " + error, Toast.LENGTH_SHORT).show();

                // Try loading from database as fallback
                loadTeamsFromDatabase();
            }
        });
    }

    private void loadTeamsFromDatabase() {
        Log.d(TAG, "Loading teams from database...");

        new Thread(() -> {
            try {
                List<Team> teams = teamRepository.getAll();

                runOnUiThread(() -> {
                    isLoading = false;
                    if (teams != null && !teams.isEmpty()) {
                        // Convert Team entities to TeamResponse
                        allTeams = convertToTeamResponses(teams);

                        // Categorize teams
                        categorizeTeams();

                        // Display teams
                        displayTeams();

                        Log.d(TAG, "Teams loaded from database: " + allTeams.size());
                    } else {
                        Toast.makeText(TeamListActivity.this, "No teams found", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    isLoading = false;
                    Log.e(TAG, "Error loading teams from database: " + e.getMessage());
                    Toast.makeText(TeamListActivity.this, "Error loading teams: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void saveTeamsToDatabase(List<TeamResponse> teamResponses) {
        new Thread(() -> {
            try {
                List<Team> teams = convertToTeams(teamResponses);
                teamRepository.insertAll(teams);
                Log.d(TAG, "Teams saved to database: " + teams.size());
            } catch (Exception e) {
                Log.e(TAG, "Error saving teams to database: " + e.getMessage());
            }
        }).start();
    }

    private void categorizeTeams() {
        activeTeams.clear();
        recommendedTeams.clear();

        for (TeamResponse team : allTeams) {
            // Active teams are teams where user is a member
            if (team.getIsMember() != null && team.getIsMember()) {
                activeTeams.add(team);
            } else {
                // Recommended teams are public teams where user is not a member
                if ("PUBLIC".equals(team.getPrivacy())) {
                    recommendedTeams.add(team);
                }
            }
        }

        Log.d(TAG, "Active teams: " + activeTeams.size() + ", Recommended teams: " + recommendedTeams.size());
    }

    private void filterTeams(String filterText) {
        List<TeamResponse> filteredActive = new ArrayList<>();
        List<TeamResponse> filteredRecommended = new ArrayList<>();

        if ("All".equals(filterText)) {
            filteredActive = activeTeams;
            filteredRecommended = recommendedTeams;
        } else {
            // Filter by research field
            for (TeamResponse team : activeTeams) {
                if (filterText.equalsIgnoreCase(team.getResearchField()) ||
                    (team.getResearchField() != null && team.getResearchField().contains(filterText))) {
                    filteredActive.add(team);
                }
            }
            for (TeamResponse team : recommendedTeams) {
                if (filterText.equalsIgnoreCase(team.getResearchField()) ||
                    (team.getResearchField() != null && team.getResearchField().contains(filterText))) {
                    filteredRecommended.add(team);
                }
            }
        }

        // Update display with filtered teams
        displayFilteredTeams(filteredActive, filteredRecommended);
    }

    private void displayTeams() {
        displayFilteredTeams(activeTeams, recommendedTeams);
    }

    private void displayFilteredTeams(List<TeamResponse> active, List<TeamResponse> recommended) {
        // Display active teams (up to 3 cards)
        int activeCount = Math.min(active.size(), 3);
        for (int i = 0; i < activeCount; i++) {
            TeamResponse team = active.get(i);
            int cardId = getCardIdForIndex(i);
            if (cardId != 0) {
                setupTeamCard(cardId, team);
            }
        }

        // Hide unused active team cards
        for (int i = activeCount; i < 3; i++) {
            int cardId = getCardIdForIndex(i);
            if (cardId != 0) {
                View cardView = findViewById(cardId);
                if (cardView != null) {
                    cardView.setVisibility(View.GONE);
                }
            }
        }

        // Display recommended teams (card 4)
        if (!recommended.isEmpty()) {
            TeamResponse team = recommended.get(0);
            setupTeamCard(R.id.teamCard4, team);
            if (tvRecommended != null) {
                tvRecommended.setVisibility(View.VISIBLE);
            }
        } else {
            View card4 = findViewById(R.id.teamCard4);
            if (card4 != null) {
                card4.setVisibility(View.GONE);
            }
            if (tvRecommended != null) {
                tvRecommended.setVisibility(View.GONE);
            }
        }
    }

    private int getCardIdForIndex(int index) {
        switch (index) {
            case 0: return R.id.teamCard1;
            case 1: return R.id.teamCard2;
            case 2: return R.id.teamCard3;
            default: return 0;
        }
    }

    private void setupTeamCard(int cardViewId, TeamResponse team) {
        View includeView = findViewById(cardViewId);
        if (includeView == null) {
            return;
        }

        includeView.setVisibility(View.VISIBLE);

        MaterialCardView teamCard = includeView.findViewById(R.id.teamCard);
        if (teamCard == null) {
            if (includeView instanceof MaterialCardView) {
                teamCard = (MaterialCardView) includeView;
            } else {
                return;
            }
        }

        // Store team ID
        teamCard.setTag(team.getId());

        // Set click listener
        teamCard.setClickable(true);
        teamCard.setFocusable(true);
        teamCard.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(TeamListActivity.this, TeamDetailActivity.class);
                intent.putExtra("teamId", team.getId());
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error in team card click: " + e.getMessage());
                Toast.makeText(TeamListActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Populate data
        TextView tvTeamName = teamCard.findViewById(R.id.tvTeamName);
        if (tvTeamName != null) {
            tvTeamName.setText(team.getName());
        }

        TextView tvTeamLead = teamCard.findViewById(R.id.tvTeamLead);
        if (tvTeamLead != null) {
            String leadText = "Lead: " + (team.getCreatedByName() != null ? team.getCreatedByName() : "Unknown");
            tvTeamLead.setText(leadText);
        }

        TextView tvTeamDescription = teamCard.findViewById(R.id.tvTeamDescription);
        if (tvTeamDescription != null) {
            tvTeamDescription.setText(team.getDescription() != null ? team.getDescription() : "");
        }

        TextView tvTeamMembers = teamCard.findViewById(R.id.tvTeamMembers);
        if (tvTeamMembers != null) {
            int memberCount = team.getMemberCount() != null ? team.getMemberCount() : 0;
            tvTeamMembers.setText(memberCount + " Members");
        }

        TextView tvTeamDepartments = teamCard.findViewById(R.id.tvTeamDepartments);
        if (tvTeamDepartments != null) {
            // Departments info not available in API, set default
            tvTeamDepartments.setText("N/A");
        }

        TextView tvTeamLocations = teamCard.findViewById(R.id.tvTeamLocations);
        if (tvTeamLocations != null) {
            // Locations info not available in API, set default
            tvTeamLocations.setText("N/A");
        }

        // Show Join button for PUBLIC teams that user is not a member of
        MaterialButton btnJoinTeam = teamCard.findViewById(R.id.btnJoinTeam);
        if (btnJoinTeam != null) {
            boolean isPublic = "PUBLIC".equalsIgnoreCase(team.getPrivacy());
            boolean isMember = team.getIsMember() != null && team.getIsMember();

            if (isPublic && !isMember) {
                btnJoinTeam.setVisibility(View.VISIBLE);
                btnJoinTeam.setOnClickListener(v -> {
                    joinTeam(team);
                });
            } else {
                btnJoinTeam.setVisibility(View.GONE);
            }
        }
    }

    private void joinTeam(TeamResponse team) {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if team is PUBLIC
        if (!"PUBLIC".equalsIgnoreCase(team.getPrivacy())) {
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
                        addCurrentUserToTeam(team, userResponse.getId());
                    } else {
                        Toast.makeText(TeamListActivity.this, "Unable to get user information", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(TeamListActivity.this, "Unable to get user information: " + error, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            addCurrentUserToTeam(team, currentUserId);
        }
    }

    private void addCurrentUserToTeam(TeamResponse team, String userId) {
        AddTeamMemberRequest request = new AddTeamMemberRequest();
        request.setUserId(userId);
        request.setRole("STUDENT"); // Default role for self-join

        teamApiHandler.addTeamMember(team.getId(), request, new ApiCallback<TeamMemberResponse>() {
            @Override
            public void onSuccess(TeamMemberResponse teamMemberResponse) {
                runOnUiThread(() -> {
                    Toast.makeText(TeamListActivity.this, "Successfully joined team!", Toast.LENGTH_SHORT).show();
                    // Reload teams to update membership status
                    loadTeams();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error joining team: " + error);
                    Toast.makeText(TeamListActivity.this, "Failed to join team: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private List<Team> convertToTeams(List<TeamResponse> responses) {
        List<Team> teams = new ArrayList<>();
        for (TeamResponse response : responses) {
            Team team = Team.builder()
                    .id(response.getId())
                    .name(response.getName())
                    .description(response.getDescription())
                    .researchField(response.getResearchField())
                    .privacy(response.getPrivacy() != null ? response.getPrivacy() : "PRIVATE")
                    .iconUrl(response.getIconUrl())
                    .createdDate(response.getCreatedDate())
                    .updatedDate(response.getUpdatedDate())
                    .createdById(response.getCreatedById())
                    .createdByName(response.getCreatedByName())
                    .createdByEmail(response.getCreatedByEmail())
                    .memberCount(response.getMemberCount())
                    .isMember(response.getIsMember())
                    .build();
            teams.add(team);
        }
        return teams;
    }

    private List<TeamResponse> convertToTeamResponses(List<Team> teams) {
        List<TeamResponse> responses = new ArrayList<>();
        for (Team team : teams) {
            TeamResponse response = new TeamResponse();
            response.setId(team.getId());
            response.setName(team.getName());
            response.setDescription(team.getDescription());
            response.setResearchField(team.getResearchField());
            response.setPrivacy(team.getPrivacy());
            response.setIconUrl(team.getIconUrl());
            response.setCreatedDate(team.getCreatedDate());
            response.setUpdatedDate(team.getUpdatedDate());
            response.setCreatedById(team.getCreatedById());
            response.setCreatedByName(team.getCreatedByName());
            response.setCreatedByEmail(team.getCreatedByEmail());
            response.setMemberCount(team.getMemberCount());
            response.setIsMember(team.getIsMember());
            responses.add(response);
        }
        return responses;
    }
}
