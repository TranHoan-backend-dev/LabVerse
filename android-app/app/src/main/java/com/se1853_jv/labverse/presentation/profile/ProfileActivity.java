package com.se1853_jv.labverse.presentation.profile;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.presentation.common.BaseActivity;

public class ProfileActivity extends BaseActivity {

    private TextInputEditText etFullName;
    private TextInputEditText etEmail;
    private TextInputEditText etAffiliation;
    private android.widget.AutoCompleteTextView spinnerResearchField;
    private SwitchMaterial switchPushNotifications;
    private SwitchMaterial switchEmailUpdates;
    private SwitchMaterial switchCollaboration;
    private MaterialButton btnSaveChanges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profileScrollView), (v, insets) -> {
            var statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(statusBar.left, statusBar.top, statusBar.right, statusBar.bottom);
            return insets;
        });

        bindViews();
        setupToolbar();
        handleEvents();
    }

    private void bindViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etAffiliation = findViewById(R.id.etAffiliation);
        spinnerResearchField = findViewById(R.id.spinnerResearchField);
        switchPushNotifications = findViewById(R.id.switchPushNotifications);
        switchEmailUpdates = findViewById(R.id.switchEmailUpdates);
        switchCollaboration = findViewById(R.id.switchCollaboration);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        if (item.getItemId() == R.id.action_more) {
            Toast.makeText(this, "More options", Toast.LENGTH_SHORT).show();
            // Show more options menu
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleEvents() {
        btnSaveChanges.setOnClickListener(v -> {
            // Validate and save profile data
            String fullName = etFullName.getText() != null ? etFullName.getText().toString() : "";
            String email = etEmail.getText() != null ? etEmail.getText().toString() : "";
            String affiliation = etAffiliation.getText() != null ? etAffiliation.getText().toString() : "";

            if (fullName.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill in required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save profile logic here
            Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        });

        // Avatar change listeners
        findViewById(R.id.ivProfileAvatar).setOnClickListener(v -> {
            Toast.makeText(this, "Change avatar", Toast.LENGTH_SHORT).show();
            // Handle avatar change - open image picker
        });

        findViewById(R.id.btnChangeAvatar).setOnClickListener(v -> {
            Toast.makeText(this, "Change avatar", Toast.LENGTH_SHORT).show();
            // Handle avatar change - open image picker
        });

        // Security section click listeners - change password (first LinearLayout in Security card)
        // The LinearLayout with "Change Password" text doesn't have an ID, so we find parent and set click listener
        // We can access it through the Security card's first child LinearLayout
    }
}

