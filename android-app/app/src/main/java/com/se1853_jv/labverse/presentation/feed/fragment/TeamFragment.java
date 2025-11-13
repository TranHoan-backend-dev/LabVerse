package com.se1853_jv.labverse.presentation.feed.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.team.TeamApiHandler;
import com.se1853_jv.labverse.data.api.user.UserApiHandler;
import com.se1853_jv.labverse.data.dto.request.AddTeamMemberRequest;
import com.se1853_jv.labverse.data.dto.response.TeamMemberResponse;
import com.se1853_jv.labverse.data.dto.response.TeamResponse;
import com.se1853_jv.labverse.data.dto.response.TeamsPageResponse;
import com.se1853_jv.labverse.data.dto.response.UserResponse;
import com.se1853_jv.labverse.data.utils.Connectivity;
import com.se1853_jv.labverse.data.utils.SessionManager;
import com.se1853_jv.labverse.domain.db.DatabaseClient;
import com.se1853_jv.labverse.domain.infrastructure.team.model.Team;
import com.se1853_jv.labverse.domain.infrastructure.team.repo.TeamRepository;
import com.se1853_jv.labverse.presentation.team.TeamCreateActivity;
import com.se1853_jv.labverse.presentation.team.TeamDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class TeamFragment extends Fragment {
    private static final String TAG = "TeamFragment";

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
    private View rootView;
    
    private ActivityResultLauncher<Intent> createTeamLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Register ActivityResultLauncher for creating team
        createTeamLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                    // Reload teams after creating a new team
                    loadTeams();
                }
            }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_teams, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = view;

        // Initialize API handler and database
        teamApiHandler = new TeamApiHandler(requireContext());
        userApiHandler = new UserApiHandler(requireContext());
        teamRepository = DatabaseClient.getInstance(requireContext()).getAppDatabase().teamRepository();
        sessionManager = new SessionManager(requireContext());

        bindViews();
        handleEvents();

        // Load teams from API or database
        loadTeams();
    }

    private void bindViews() {
        if (rootView == null) return;
        
        chipGroupFilters = rootView.findViewById(R.id.chipGroupFilters);
        btnSort = rootView.findViewById(R.id.btnSort);
        btnCreateTeam = rootView.findViewById(R.id.btnCreateTeam);
        tvSectionTitle = rootView.findViewById(R.id.tvSectionTitle);
        tvRecommended = rootView.findViewById(R.id.tvRecommended);
    }

    private void handleEvents() {
        if (chipGroupFilters != null) {
            chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty()) {
                    return;
                }
                int checkedId = checkedIds.get(0);
                Chip chip = rootView.findViewById(checkedId);
                if (chip != null) {
                    String filterText = chip.getText().toString();
                    filterTeams(filterText);
                }
            });
        }

        if (btnSort != null) {
            btnSort.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Sort options", Toast.LENGTH_SHORT).show();
                // TODO: Show sort dialog
            });
        }

        if (btnCreateTeam != null) {
            btnCreateTeam.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), TeamCreateActivity.class);
                createTeamLauncher.launch(intent);
            });
        }
    }

    private void loadTeams() {
        if (isLoading) return;
        isLoading = true;

        // Check connectivity
        boolean isOnline = Connectivity.isInternetAvailable(requireContext());

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
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> displayTeams());
                    }
                    
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
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Failed to load teams: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
                
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
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
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
                            Toast.makeText(requireContext(), "No teams found", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        isLoading = false;
                        Log.e(TAG, "Error loading teams from database: " + e.getMessage());
                        Toast.makeText(requireContext(), "Error loading teams: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
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
            filteredActive = new ArrayList<>(activeTeams);
            filteredRecommended = new ArrayList<>(recommendedTeams);
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
        if (rootView == null) return;

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
                View cardView = rootView.findViewById(cardId);
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
            View card4 = rootView.findViewById(R.id.teamCard4);
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
        if (rootView == null) return;
        
        View includeView = rootView.findViewById(cardViewId);
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
                Intent intent = new Intent(requireContext(), TeamDetailActivity.class);
                intent.putExtra("teamId", team.getId());
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error in team card click: " + e.getMessage());
                Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        if (!Connectivity.isInternetAvailable(requireContext())) {
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if team is PUBLIC
        if (!"PUBLIC".equalsIgnoreCase(team.getPrivacy())) {
            Toast.makeText(requireContext(), "This team is private. You need an invitation to join.", Toast.LENGTH_LONG).show();
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
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Unable to get user information", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                }

                @Override
                public void onError(String error) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Unable to get user information: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
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
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Successfully joined team!", Toast.LENGTH_SHORT).show();
                        // Reload teams to update membership status
                        loadTeams();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.e(TAG, "Error joining team: " + error);
                        Toast.makeText(requireContext(), "Failed to join team: " + error, Toast.LENGTH_LONG).show();
                    });
                }
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

    @Override
    public void onResume() {
        super.onResume();
        // Reload teams when fragment becomes visible (e.g., after creating a team)
        loadTeams();
    }
}
