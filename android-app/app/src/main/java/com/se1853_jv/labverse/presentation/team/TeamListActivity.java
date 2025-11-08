package com.se1853_jv.labverse.presentation.team;

import android.content.Intent;
import android.os.Bundle;
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
import com.se1853_jv.labverse.presentation.common.HeaderHelper;

public class TeamListActivity extends AppCompatActivity {

    private ChipGroup chipGroupFilters;
    private ImageButton btnSort;
    private MaterialButton btnCreateTeam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scrollContent), (v, insets) -> {
            var statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(statusBar.left, statusBar.top, statusBar.right, statusBar.bottom);
            return insets;
        });

        bindViews();
        handleEvents();
        setupTeamCards();

        // Setup avatar and profile navigation click listeners
        HeaderHelper.setupProfileClickListeners(this);
        // Setup Lists navigation click listener (navigate to this screen)
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
    }

    private void bindViews() {
        chipGroupFilters = findViewById(R.id.chipGroupFilters);
        btnSort = findViewById(R.id.btnSort);
        btnCreateTeam = findViewById(R.id.btnCreateTeam);
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
                Toast.makeText(this, "Filter: " + filterText, Toast.LENGTH_SHORT).show();
                // Apply filter logic here
            }
        });

        btnSort.setOnClickListener(v -> {
            Toast.makeText(this, "Sort options", Toast.LENGTH_SHORT).show();
            // Show sort dialog
        });

        // Create Team button click
        btnCreateTeam.setOnClickListener(v -> {
            Intent intent = new Intent(this, TeamCreateActivity.class);
            startActivity(intent);
        });
    }

    private void setupTeamCards() {
        // Setup click listeners and populate data for all team cards

        // Team Card 1
        setupTeamCard(R.id.teamCard1, "team_1",
                "LabVerse – Research Paper Management",
                "Lead: Dr. Nguyen Van Minh",
                "AI-powered research workflow platform connecting multi-disciplinary teams.",
                "8 Members", "3 Departments", "2 Locations");

        // Team Card 2
        setupTeamCard(R.id.teamCard2, "team_2",
                "AI Research Lab",
                "Lead: Prof. Tran Thi Mai",
                "Advanced machine learning and deep learning research team working on computer vision.",
                "12 Members", "2 Departments", "1 Location");

        // Team Card 3
        setupTeamCard(R.id.teamCard3, "team_3",
                "Biomedical Innovation Group",
                "Lead: Dr. Le Van Duc",
                "Medical research team focusing on biotechnology and pharmaceutical development.",
                "15 Members", "4 Departments", "3 Locations");

        // Team Card 4 (Recommended)
        setupTeamCard(R.id.teamCard4, "team_4",
                "Environmental Science Team",
                "Lead: Assoc. Prof. Pham Thi Lan",
                "Environmental research and sustainable development projects across multiple institutions.",
                "10 Members", "3 Departments", "2 Locations");
    }

    private void setupTeamCard(int cardViewId, String teamId, String teamName,
                               String teamLead, String description,
                               String members, String departments, String locations) {
        // Find the include view first
        View includeView = findViewById(cardViewId);
        if (includeView == null) {
            Toast.makeText(this, "Cannot find team card view", Toast.LENGTH_SHORT).show();
            return;
        }

        // Find MaterialCardView with id teamCard - it's the root of the included layout
        MaterialCardView teamCard = includeView.findViewById(R.id.teamCard);
        if (teamCard == null) {
            // If not found, try casting the include view itself
            if (includeView instanceof MaterialCardView) {
                teamCard = (MaterialCardView) includeView;
            } else {
                Toast.makeText(this, "Cannot find MaterialCardView in team card", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Store teamId in tag for later retrieval
        teamCard.setTag(teamId);

        // Set click listener directly on the card
        // MaterialCardView already has clickable and foreground set in XML
        teamCard.setClickable(true);
        teamCard.setFocusable(true);

        // Store teamId in a final variable for lambda
        final String finalTeamId = teamId;

        teamCard.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(TeamListActivity.this, TeamDetailActivity.class);
                intent.putExtra("teamId", finalTeamId);
                startActivity(intent);
            } catch (Exception e) {
                // Log error and show toast
                Log.e("TeamListActivity", "Error in team card click: " + e.getMessage());
                String errorMsg = e.getClass().getSimpleName();
                if (e.getMessage() != null) {
                    errorMsg += ": " + e.getMessage();
                }
                Toast.makeText(TeamListActivity.this,
                        "Error: " + errorMsg,
                        Toast.LENGTH_LONG).show();
            }
        });

        // Make child views non-clickable so click events go to the card
        android.view.ViewGroup cardContent = (android.view.ViewGroup) teamCard.getChildAt(0);
        if (cardContent != null) {
            cardContent.setClickable(false);
            cardContent.setFocusable(false);
        }

        // Populate TextView data - find views from the card itself
        TextView tvTeamName = teamCard.findViewById(R.id.tvTeamName);
        if (tvTeamName != null) {
            tvTeamName.setText(teamName);
            tvTeamName.setClickable(false);
            tvTeamName.setFocusable(false);
        }

        TextView tvTeamLead = teamCard.findViewById(R.id.tvTeamLead);
        if (tvTeamLead != null) {
            tvTeamLead.setText(teamLead);
            tvTeamLead.setClickable(false);
            tvTeamLead.setFocusable(false);
        }

        TextView tvTeamDescription = teamCard.findViewById(R.id.tvTeamDescription);
        if (tvTeamDescription != null) {
            tvTeamDescription.setText(description);
            tvTeamDescription.setClickable(false);
            tvTeamDescription.setFocusable(false);
        }

        TextView tvTeamMembers = teamCard.findViewById(R.id.tvTeamMembers);
        if (tvTeamMembers != null) {
            tvTeamMembers.setText(members);
            tvTeamMembers.setClickable(false);
            tvTeamMembers.setFocusable(false);
        }

        TextView tvTeamDepartments = teamCard.findViewById(R.id.tvTeamDepartments);
        if (tvTeamDepartments != null) {
            tvTeamDepartments.setText(departments);
            tvTeamDepartments.setClickable(false);
            tvTeamDepartments.setFocusable(false);
        }

        TextView tvTeamLocations = teamCard.findViewById(R.id.tvTeamLocations);
        if (tvTeamLocations != null) {
            tvTeamLocations.setText(locations);
            tvTeamLocations.setClickable(false);
            tvTeamLocations.setFocusable(false);
        }
    }
}

