package com.se1853_jv.labverse.presentation.team;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.presentation.common.HeaderHelper;

public class TeamDetailActivity extends AppCompatActivity {

    private TextView tvProjectName, tvProjectStart, btnAddMember;
    private TextView tvProjectMembers;
    private ChipGroup chipGroupMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailScrollView), (v, insets) -> {
            var statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(statusBar.left, statusBar.top, statusBar.right, statusBar.bottom);
            return insets;
        });

        bindViews();
        
        // Get team ID from intent
        String teamId = getIntent().getStringExtra("teamId");
        if (teamId != null) {
            loadTeamDetails(teamId);
        } else {
            // Default team if no ID provided
            loadTeamDetails("team_1");
        }

        handleEvents();
        
        // Setup avatar and profile navigation click listeners
        HeaderHelper.setupProfileClickListeners(this);
        // Setup Lists navigation click listener
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
        tvProjectName = findViewById(R.id.tvProjectName);
        tvProjectStart = findViewById(R.id.tvProjectStart);
        tvProjectMembers = findViewById(R.id.tvProjectMembers);
        chipGroupMembers = findViewById(R.id.chipGroupMembers);
        btnAddMember = findViewById(R.id.btnAddMember);
    }

    @SuppressLint("SetTextI18n")
    private void loadTeamDetails(@NonNull String teamId) {
        // Load team details from API or database
        // For now, using mock data based on teamId
        
        switch (teamId) {
            case "team_1":
                if (tvProjectName != null) {
                    tvProjectName.setText("LabVerse – Research Paper Management System");
                }
                if (tvProjectStart != null) {
                    tvProjectStart.setText("Started: Sep 18, 2025");
                }
                if (tvProjectMembers != null) {
                    tvProjectMembers.setText("8 Members");
                }
                break;
            case "team_2":
                if (tvProjectName != null) {
                    tvProjectName.setText("AI Research Lab");
                }
                if (tvProjectStart != null) {
                    tvProjectStart.setText("Started: Aug 15, 2025");
                }
                if (tvProjectMembers != null) {
                    tvProjectMembers.setText("12 Members");
                }
                break;
            case "team_3":
                if (tvProjectName != null) {
                    tvProjectName.setText("Biomedical Innovation Group");
                }
                if (tvProjectStart != null) {
                    tvProjectStart.setText("Started: Jul 10, 2025");
                }
                if (tvProjectMembers != null) {
                    tvProjectMembers.setText("15 Members");
                }
                break;
            case "team_4":
                if (tvProjectName != null) {
                    tvProjectName.setText("Environmental Science Team");
                }
                if (tvProjectStart != null) {
                    tvProjectStart.setText("Started: Sep 1, 2025");
                }
                if (tvProjectMembers != null) {
                    tvProjectMembers.setText("10 Members");
                }
                break;
            default:
                if (tvProjectName != null) {
                    tvProjectName.setText("LabVerse – Research Paper Management System");
                }
                break;
        }
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
                Toast.makeText(this, "Filter: " + filterText, Toast.LENGTH_SHORT).show();
                // Filter members by role
            }
        });

        btnAddMember.setOnClickListener(v -> {
            Toast.makeText(this, "Add member", Toast.LENGTH_SHORT).show();
            // Navigate to add member activity
        });
    }
}

