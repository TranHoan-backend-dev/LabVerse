package com.se1853_jv.labverse.presentation.team;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

public class TeamCreateActivity extends AppCompatActivity {

    private ImageView ivTeamIcon, ivCameraIcon;
    private EditText etTeamName, etDescription;
    private Spinner spinnerResearchField;
    private RadioGroup rgPrivacy;
    private RadioButton rbPublic, rbPrivate;
    private Button btnCreateTeam;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

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
                            ivTeamIcon.setImageURI(imageUri);
                            Toast.makeText(this, "Team icon selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        bindViews();
        setupToolbar();
        setupSpinner();
        handleEvents();
    }

    private void bindViews() {
        ivTeamIcon = findViewById(R.id.iv_team_icon);
        ivCameraIcon = findViewById(R.id.iv_camera_icon);
        etTeamName = findViewById(R.id.et_team_name);
        etDescription = findViewById(R.id.et_description);
        spinnerResearchField = findViewById(R.id.spinner_research_field);
        rgPrivacy = findViewById(R.id.rg_privacy);
        rbPublic = findViewById(R.id.rb_public);
        rbPrivate = findViewById(R.id.rb_private);
        btnCreateTeam = findViewById(R.id.btn_create_team);
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                researchFields) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(getResources().getColor(R.color.light_black, null));
                textView.setTextSize(16);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(getResources().getColor(R.color.light_black, null));
                textView.setTextSize(16);
                textView.setPadding(textView.getPaddingLeft(), 16, textView.getPaddingRight(), 16);
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerResearchField.setAdapter(adapter);
    }

    private void handleEvents() {
        ivCameraIcon.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        findViewById(R.id.tv_add_button).setOnClickListener(v -> {
            Toast.makeText(this, "Add members", Toast.LENGTH_SHORT).show();
            // Navigate to add members activity
        });

        btnCreateTeam.setOnClickListener(v -> {
            String teamName = etTeamName.getText() != null ? etTeamName.getText().toString().trim() : "";
            String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

            if (teamName.isEmpty()) {
                Toast.makeText(this, "Please enter team name", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isPublic = rbPublic.isChecked();
            String researchField = spinnerResearchField.getSelectedItem() != null ?
                    spinnerResearchField.getSelectedItem().toString() : "";

            // Create team logic here
            Toast.makeText(this, "Team created successfully", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}

