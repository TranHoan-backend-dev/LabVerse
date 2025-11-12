package com.se1853_jv.labverse.presentation.team;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.team.TeamApiHandler;
import com.se1853_jv.labverse.data.dto.request.UpdateTeamRequest;
import com.se1853_jv.labverse.data.dto.response.TeamResponse;
import com.se1853_jv.labverse.data.utils.Connectivity;
import com.se1853_jv.labverse.domain.db.DatabaseClient;
import com.se1853_jv.labverse.domain.infrastructure.team.model.Team;
import com.se1853_jv.labverse.domain.infrastructure.team.repo.TeamRepository;

public class TeamEditActivity extends AppCompatActivity {

    private static final String TAG = "TeamEditActivity";

    private ImageView ivTeamIcon, ivCameraIcon;
    private android.widget.EditText etTeamName, etDescription;
    private Spinner spinnerResearchField;
    private RadioGroup rgPrivacy;
    private RadioButton rbPublic, rbPrivate;
    private Button btnUpdateTeam;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;
    private String iconUrl;

    private TeamApiHandler teamApiHandler;
    private TeamRepository teamRepository;
    private String teamId;
    private TeamResponse currentTeam;
    private boolean isUpdating = false;

    private final String[] researchFields = {
            "Computer Science",
            "Artificial Intelligence",
            "Machine Learning",
            "Data Science",
            "Biomedical Engineering",
            "Environmental Science",
            "Chemistry",
            "Physics"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_create);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            var statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(statusBar.left, statusBar.top, statusBar.right, statusBar.bottom);
            return insets;
        });

        // Get team ID from intent
        teamId = getIntent().getStringExtra("teamId");
        if (teamId == null || teamId.isEmpty()) {
            Toast.makeText(this, "Team ID not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            selectedImageUri = imageUri;
                            ivTeamIcon.setImageURI(imageUri);
                            Toast.makeText(this, "Team icon selected", Toast.LENGTH_SHORT).show();
                            // TODO: Upload image to S3 and get URL
                        }
                    }
                }
        );

        // Initialize API handler and database
        teamApiHandler = new TeamApiHandler(this);
        teamRepository = DatabaseClient.getInstance(this).getAppDatabase().teamRepository();

        bindViews();
        setupToolbar();
        setupSpinner();
        setupPrivacyRadioGroup();
        loadTeamData();
        handleEvents();
    }

    private void bindViews() {
        try {
            ivTeamIcon = findViewById(R.id.iv_team_icon);
            ivCameraIcon = findViewById(R.id.iv_camera_icon);
            etTeamName = findViewById(R.id.et_team_name);
            etDescription = findViewById(R.id.et_description);
            spinnerResearchField = findViewById(R.id.spinner_research_field);
            rgPrivacy = findViewById(R.id.rg_privacy);
            rbPublic = findViewById(R.id.rb_public);
            rbPrivate = findViewById(R.id.rb_private);
            btnUpdateTeam = findViewById(R.id.btn_create_team);
            
            if (btnUpdateTeam != null) {
                btnUpdateTeam.setText("Update Team");
            }
            
            if (ivTeamIcon == null || ivCameraIcon == null || etTeamName == null || 
                etDescription == null || spinnerResearchField == null || rgPrivacy == null ||
                rbPublic == null || rbPrivate == null || btnUpdateTeam == null) {
                Log.e(TAG, "One or more views not found in layout");
                Toast.makeText(this, "Error: Layout initialization failed", Toast.LENGTH_LONG).show();
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error binding views: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing form: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Edit Team");
        }
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void setupSpinner() {
        if (spinnerResearchField == null) {
            Log.e(TAG, "Spinner is null");
            return;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                researchFields);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerResearchField.setAdapter(adapter);
    }

    private void setupPrivacyRadioGroup() {
        if (rgPrivacy == null) {
            Log.e(TAG, "RadioGroup is null");
            return;
        }
        
        // Add listener to ensure proper RadioGroup behavior
        rgPrivacy.setOnCheckedChangeListener((group, checkedId) -> {
            Log.d(TAG, "RadioGroup checked changed: " + checkedId);
            if (checkedId == R.id.rb_public) {
                Log.d(TAG, "Public selected");
            } else if (checkedId == R.id.rb_private) {
                Log.d(TAG, "Private selected");
            } else {
                Log.d(TAG, "No radio button selected (checkedId: -1)");
            }
        });
        
        // Also make RelativeLayouts clickable to ensure proper interaction
        View rlPublic = findViewById(R.id.rl_public);
        View rlPrivate = findViewById(R.id.rl_private);
        
        if (rlPublic != null) {
            rlPublic.setOnClickListener(v -> {
                Log.d(TAG, "Public RelativeLayout clicked");
                rgPrivacy.check(R.id.rb_public);
            });
        }
        
        if (rlPrivate != null) {
            rlPrivate.setOnClickListener(v -> {
                Log.d(TAG, "Private RelativeLayout clicked");
                rgPrivacy.check(R.id.rb_private);
            });
        }
    }

    private void loadTeamData() {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        teamApiHandler.getTeamById(teamId, new ApiCallback<TeamResponse>() {
            @Override
            public void onSuccess(TeamResponse teamResponse) {
                runOnUiThread(() -> {
                    currentTeam = teamResponse;
                    populateForm(teamResponse);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error loading team: " + error);
                    Toast.makeText(TeamEditActivity.this, 
                            "Failed to load team: " + error, Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        });
    }

    private void populateForm(TeamResponse team) {
        if (team == null) return;

        // Set team name
        if (etTeamName != null && team.getName() != null) {
            etTeamName.setText(team.getName());
        }

        // Set description
        if (etDescription != null && team.getDescription() != null) {
            etDescription.setText(team.getDescription());
        }

        // Set research field
        if (spinnerResearchField != null && team.getResearchField() != null) {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerResearchField.getAdapter();
            if (adapter != null) {
                int position = adapter.getPosition(team.getResearchField());
                if (position >= 0) {
                    spinnerResearchField.setSelection(position);
                }
            }
        }

        // Set privacy - ensure radio button is set correctly
        if (rgPrivacy != null && rbPublic != null && rbPrivate != null) {
            String privacyStr = team.getPrivacy() != null ? team.getPrivacy().trim() : null;
            Log.d(TAG, "Setting privacy from API: " + privacyStr);
            
            // Use RadioGroup's check method instead of individual RadioButton setChecked
            // This ensures proper RadioGroup behavior
            if (privacyStr != null && "PUBLIC".equalsIgnoreCase(privacyStr)) {
                rgPrivacy.check(R.id.rb_public);
                Log.d(TAG, "Set to PUBLIC via RadioGroup");
            } else {
                // Default to PRIVATE (including null case)
                rgPrivacy.check(R.id.rb_private);
                Log.d(TAG, "Set to PRIVATE via RadioGroup (value: " + privacyStr + ")");
            }
        }

        // Set icon
        if (ivTeamIcon != null && team.getIconUrl() != null && !team.getIconUrl().isEmpty()) {
            Glide.with(this)
                    .load(team.getIconUrl())
                    .placeholder(R.mipmap.avt_mock_round)
                    .into(ivTeamIcon);
            iconUrl = team.getIconUrl();
        }
    }

    private void handleEvents() {
        if (ivCameraIcon != null) {
            ivCameraIcon.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                imagePickerLauncher.launch(intent);
            });
        }

        TextView btnAddMembers = findViewById(R.id.tv_add_button);
        if (btnAddMembers != null) {
            btnAddMembers.setOnClickListener(v -> {
                Toast.makeText(this, "Add members", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnUpdateTeam != null) {
            btnUpdateTeam.setOnClickListener(v -> updateTeam());
        }
    }

    private void updateTeam() {
        if (isUpdating) {
            return;
        }

        if (etTeamName == null || etDescription == null || spinnerResearchField == null || 
            rgPrivacy == null || rbPublic == null || rbPrivate == null) {
            Log.e(TAG, "One or more views are null");
            Toast.makeText(this, "Error: Form fields not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        String teamName = etTeamName.getText() != null ? etTeamName.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String researchField = "";
        if (spinnerResearchField.getSelectedItem() != null) {
            researchField = spinnerResearchField.getSelectedItem().toString().trim();
        }

        if (TextUtils.isEmpty(teamName)) {
            etTeamName.setError("Team name is required");
            etTeamName.requestFocus();
            return;
        }

        // Get selected privacy - check which radio button is actually checked
        String privacy;
        int checkedId = rgPrivacy.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_public) {
            privacy = "PUBLIC";
        } else if (checkedId == R.id.rb_private) {
            privacy = "PRIVATE";
        } else {
            // Fallback to checking radio buttons directly
            privacy = rbPublic.isChecked() ? "PUBLIC" : "PRIVATE";
        }
        
        Log.d(TAG, "Updating team with privacy: " + privacy + " (checkedId: " + checkedId + ")");

        boolean isOnline = Connectivity.isInternetAvailable(this);
        if (!isOnline) {
            Toast.makeText(this, "No internet connection. Please check your network.", Toast.LENGTH_LONG).show();
            return;
        }

        UpdateTeamRequest request = new UpdateTeamRequest();
        request.setName(teamName);
        request.setDescription(description.isEmpty() ? null : description);
        request.setResearchField(researchField.isEmpty() ? null : researchField);
        request.setPrivacy(privacy);
        request.setIconUrl(iconUrl);

        isUpdating = true;
        if (btnUpdateTeam != null) {
            btnUpdateTeam.setEnabled(false);
            btnUpdateTeam.setText("Updating...");
        }

        teamApiHandler.updateTeam(teamId, request, new ApiCallback<TeamResponse>() {
            @Override
            public void onSuccess(TeamResponse teamResponse) {
                runOnUiThread(() -> {
                    isUpdating = false;
                    if (btnUpdateTeam != null) {
                        btnUpdateTeam.setEnabled(true);
                        btnUpdateTeam.setText("Update Team");
                    }

                    if (teamResponse != null) {
                        saveTeamToDatabase(teamResponse);
                        Toast.makeText(TeamEditActivity.this,
                                "Team updated successfully!", Toast.LENGTH_SHORT).show();
                        
                        // Return result to TeamDetailActivity
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("teamUpdated", true);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    isUpdating = false;
                    if (btnUpdateTeam != null) {
                        btnUpdateTeam.setEnabled(true);
                        btnUpdateTeam.setText("Update Team");
                    }
                    Log.e(TAG, "Error updating team: " + error);
                    Toast.makeText(TeamEditActivity.this,
                            "Failed to update team: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void saveTeamToDatabase(TeamResponse teamResponse) {
        new Thread(() -> {
            try {
                Team team = Team.builder()
                        .id(teamResponse.getId())
                        .name(teamResponse.getName())
                        .description(teamResponse.getDescription())
                        .researchField(teamResponse.getResearchField())
                        .privacy(teamResponse.getPrivacy() != null && !teamResponse.getPrivacy().isEmpty() 
                                ? teamResponse.getPrivacy() : "PRIVATE")
                        .iconUrl(teamResponse.getIconUrl())
                        .createdDate(teamResponse.getCreatedDate())
                        .updatedDate(teamResponse.getUpdatedDate())
                        .createdById(teamResponse.getCreatedById())
                        .createdByName(teamResponse.getCreatedByName())
                        .createdByEmail(teamResponse.getCreatedByEmail())
                        .memberCount(teamResponse.getMemberCount())
                        .isMember(teamResponse.getIsMember())
                        .build();

                teamRepository.insertAll(java.util.Collections.singletonList(team));
                Log.d(TAG, "Team updated in database: " + team.getId());
            } catch (Exception e) {
                Log.e(TAG, "Error saving team to database: " + e.getMessage());
            }
        }).start();
    }
}

