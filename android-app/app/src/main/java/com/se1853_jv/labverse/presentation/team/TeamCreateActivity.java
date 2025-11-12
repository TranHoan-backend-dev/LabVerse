package com.se1853_jv.labverse.presentation.team;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.team.TeamApiHandler;
import com.se1853_jv.labverse.data.dto.request.CreateTeamRequest;
import com.se1853_jv.labverse.data.dto.response.TeamResponse;
import com.se1853_jv.labverse.data.utils.Connectivity;
import com.se1853_jv.labverse.domain.db.DatabaseClient;
import com.se1853_jv.labverse.domain.infrastructure.team.model.Team;
import com.se1853_jv.labverse.domain.infrastructure.team.repo.TeamRepository;

public class TeamCreateActivity extends AppCompatActivity {

    private static final String TAG = "TeamCreateActivity";

    private ImageView ivTeamIcon, ivCameraIcon;
    private EditText etTeamName, etDescription;
    private Spinner spinnerResearchField;
    private RadioGroup rgPrivacy;
    private RadioButton rbPublic, rbPrivate;
    private Button btnCreateTeam;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;
    private String iconUrl; // Will be set after uploading to S3

    private TeamApiHandler teamApiHandler;
    private TeamRepository teamRepository;
    private boolean isCreating = false;

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
                            // For now, we'll leave iconUrl as null
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
            btnCreateTeam = findViewById(R.id.btn_create_team);
            
            // Validate all views are found
            if (ivTeamIcon == null || ivCameraIcon == null || etTeamName == null || 
                etDescription == null || spinnerResearchField == null || rgPrivacy == null ||
                rbPublic == null || rbPrivate == null || btnCreateTeam == null) {
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
        }
        toolbar.setNavigationOnClickListener(v -> finish());
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
                // TODO: Navigate to add members activity
            });
        }

        if (btnCreateTeam != null) {
            btnCreateTeam.setOnClickListener(v -> createTeam());
        } else {
            Log.e(TAG, "Create team button is null");
        }
    }

    private void createTeam() {
        if (isCreating) {
            return; // Prevent multiple submissions
        }

        // Validate form
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

        // Determine privacy
        String privacy = rbPublic.isChecked() ? "PUBLIC" : "PRIVATE";

        // Check connectivity
        boolean isOnline = Connectivity.isInternetAvailable(this);
        if (!isOnline) {
            Toast.makeText(this, "No internet connection. Please check your network.", Toast.LENGTH_LONG).show();
            return;
        }

        // Create request
        CreateTeamRequest request = new CreateTeamRequest();
        request.setName(teamName);
        request.setDescription(description.isEmpty() ? null : description);
        request.setResearchField(researchField.isEmpty() ? null : researchField);
        request.setPrivacy(privacy);
        request.setIconUrl(iconUrl); // Will be null if no image uploaded

        // Disable button and show loading
        isCreating = true;
        if (btnCreateTeam != null) {
            btnCreateTeam.setEnabled(false);
            btnCreateTeam.setText("Creating...");
        }

        // Call API
        teamApiHandler.createTeam(request, new ApiCallback<TeamResponse>() {
            @Override
            public void onSuccess(TeamResponse teamResponse) {
                runOnUiThread(() -> {
                    isCreating = false;
                    if (btnCreateTeam != null) {
                        btnCreateTeam.setEnabled(true);
                        btnCreateTeam.setText("Create Team");
                    }

                    if (teamResponse != null) {
                        // Save to database
                        saveTeamToDatabase(teamResponse);

                        // Show success message
                        Toast.makeText(TeamCreateActivity.this, 
                                "Team created successfully!", Toast.LENGTH_SHORT).show();

                        // Navigate back to TeamListActivity
                        Intent intent = new Intent(TeamCreateActivity.this, TeamListActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    isCreating = false;
                    if (btnCreateTeam != null) {
                        btnCreateTeam.setEnabled(true);
                        btnCreateTeam.setText("Create Team");
                    }

                    Log.e(TAG, "Error creating team: " + error);
                    Toast.makeText(TeamCreateActivity.this, 
                            "Failed to create team: " + error, Toast.LENGTH_LONG).show();
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
                Log.d(TAG, "Team saved to database: " + team.getId());
            } catch (Exception e) {
                Log.e(TAG, "Error saving team to database: " + e.getMessage());
            }
        }).start();
    }
}

