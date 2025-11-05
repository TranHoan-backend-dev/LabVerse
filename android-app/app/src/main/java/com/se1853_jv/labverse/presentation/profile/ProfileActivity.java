package com.se1853_jv.labverse.presentation.profile;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.se1853_jv.labverse.R;

import android.content.Intent;

import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.auth.AuthApiHandler;
import com.se1853_jv.labverse.data.api.user.UserApiHandler;
import com.se1853_jv.labverse.data.dto.request.UpdateProfileRequest;
import com.se1853_jv.labverse.data.dto.response.UserResponse;
import com.se1853_jv.labverse.data.utils.SessionManager;
import com.se1853_jv.labverse.presentation.auth.LoginActivity;
import com.se1853_jv.labverse.presentation.common.BaseActivity;
import com.se1853_jv.labverse.presentation.user.fragment.ChangePasswordDialogFragment;

public class ProfileActivity extends BaseActivity {

    private final String TAG = "ProfileActivity";

    private TextInputEditText etFullName, etEmail, etAffiliation;
    private AutoCompleteTextView spinnerResearchField;
    private SwitchMaterial switchPushNotifications, switchEmailUpdates, switchCollaboration;
    private MaterialButton btnSaveChanges, btnLogout;
    private ImageView ivProfileAvatar;
    private TextView tvProfileName, tvProfileTitle;
    private LinearLayout layoutChangePassword;

    private UserApiHandler userApiHandler;
    private AuthApiHandler authApiHandler;
    private SessionManager sessionManager;
    private UserResponse currentUser;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profileScrollView), (v, insets) -> {
            var statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(statusBar.left, statusBar.top, statusBar.right, statusBar.bottom);
            return insets;
        });

        userApiHandler = new UserApiHandler(this);
        authApiHandler = new AuthApiHandler();
        sessionManager = new SessionManager(this);

        bindViews();
        setupToolbar();
        loadUserData();
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
        btnLogout = findViewById(R.id.btnLogout);
        ivProfileAvatar = findViewById(R.id.ivProfileAvatar);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileTitle = findViewById(R.id.tvProfileTitle);
        layoutChangePassword = findViewById(R.id.layoutChangePassword);
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

    private void loadUserData() {
        if (isLoading) return;

        isLoading = true;
        Log.d(TAG, "Loading user data...");

        // Show loading state (you can add a progress bar if needed)
        setLoadingState(true);

        userApiHandler.getCurrentUser(new ApiCallback<>() {
            @Override
            public void onSuccess(UserResponse user) {
                isLoading = false;
                currentUser = user;
                displayUserData(user);
                setLoadingState(false);
                Log.d(TAG, "User data loaded successfully: " + user.getUsername());
            }

            @Override
            public void onError(String error) {
                isLoading = false;
                setLoadingState(false);
                Log.e(TAG, "Error loading user data: " + error);
                Toast.makeText(ProfileActivity.this, "Failed to load profile: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoadingState(boolean loading) {
        // Disable/enable fields during loading
        etFullName.setEnabled(!loading);
        etAffiliation.setEnabled(!loading);
        spinnerResearchField.setEnabled(!loading);
        btnSaveChanges.setEnabled(!loading);
    }

    @SuppressLint("SetTextI18n")
    private void displayUserData(UserResponse user) {
        // Display full name
        if (!TextUtils.isEmpty(user.getFullName())) {
            etFullName.setText(user.getFullName());
            tvProfileName.setText(user.getFullName());
        } else if (!TextUtils.isEmpty(user.getUsername())) {
            etFullName.setText(user.getUsername());
            tvProfileName.setText(user.getUsername());
        }

        // Display email (read-only, cannot be changed)
        if (!TextUtils.isEmpty(user.getEmail())) {
            etEmail.setText(user.getEmail());
        }
        // Make email field read-only since it cannot be updated
        etEmail.setFocusable(false);
        etEmail.setClickable(false);

        // Display affiliation (if available in UserResponse, otherwise leave empty)
        // Note: Affiliation might not be in UserResponse, you may need to add it to the backend
        if (etAffiliation != null) {
            // etAffiliation.setText(user.getAffiliation()); // Uncomment when backend supports it
        }

        // Display username as title
        if (!TextUtils.isEmpty(user.getUsername())) {
            tvProfileTitle.setText("@" + user.getUsername());
        } else {
            tvProfileTitle.setText("User");
        }

        // Display avatar
        if (!TextUtils.isEmpty(user.getAvatarUrl())) {
            Glide.with(this)
                    .load(user.getAvatarUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.mipmap.avt_mock_round)
                    .error(R.mipmap.avt_mock_round)
                    .circleCrop()
                    .into(ivProfileAvatar);
        } else {
            // Set default avatar
            ivProfileAvatar.setImageResource(R.mipmap.avt_mock_round);
        }
    }

    private void handleEvents() {
        btnSaveChanges.setOnClickListener(v -> saveProfile());

        // Avatar change listeners
        ivProfileAvatar.setOnClickListener(v -> {
            Toast.makeText(this, "Change avatar", Toast.LENGTH_SHORT).show();
            // Handle avatar change - open image picker
            // TODO: Implement image picker and upload to Firebase Storage
        });

        findViewById(R.id.btnChangeAvatar).setOnClickListener(v -> {
            Toast.makeText(this, "Change avatar", Toast.LENGTH_SHORT).show();
            // Handle avatar change - open image picker
            // TODO: Implement image picker and upload to Firebase Storage
        });

        // Change Password click listener
        if (layoutChangePassword != null) {
            layoutChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        }

        // Logout button click listener
        btnLogout.setOnClickListener(v -> handleLogout());
    }

    private void showChangePasswordDialog() {
        ChangePasswordDialogFragment dialog = new ChangePasswordDialogFragment();
        dialog.show(getSupportFragmentManager(), "ChangePasswordDialog");
    }

    private void handleLogout() {
        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    @SuppressLint("SetTextI18n")
    private void performLogout() {
        // Disable button and show loading
        btnLogout.setEnabled(false);
        btnLogout.setText("Logging out...");

        // Call logout API
        authApiHandler.logout(new ApiCallback<>() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    // Clear session data
                    sessionManager.logout();

                    // Show success message
                    Toast.makeText(ProfileActivity.this,
                            message != null ? message : "Logged out successfully",
                            Toast.LENGTH_SHORT).show();

                    // Navigate to login screen
                    navigateToLogin();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    // Even if API fails, clear session and logout locally
                    sessionManager.logout();

                    Log.w(TAG, "Logout API error but proceeding with local logout: " + error);

                    // Navigate to login screen
                    navigateToLogin();
                });
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @SuppressLint("SetTextI18n")
    private void saveProfile() {
        // Validate and save profile data
        String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";

        if (fullName.isEmpty()) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }

        // Create update request
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName(fullName);

        // Only update username if it's different from current
        // Note: Username update is optional and requires validation on backend
        if (currentUser != null && !TextUtils.isEmpty(currentUser.getUsername())) {
            request.setUsername(currentUser.getUsername());
        }

        // Avatar URL - keep current if not changed
        if (currentUser != null && !TextUtils.isEmpty(currentUser.getAvatarUrl())) {
            request.setAvatarUrl(currentUser.getAvatarUrl());
        }

        // Disable button and show loading
        btnSaveChanges.setEnabled(false);
        btnSaveChanges.setText("Saving...");

        // Call API
        userApiHandler.updateProfile(request, new ApiCallback<>() {
            @Override
            public void onSuccess(UserResponse updatedUser) {
                currentUser = updatedUser;
                displayUserData(updatedUser);
                btnSaveChanges.setEnabled(true);
                btnSaveChanges.setText("Save Changes");
                Toast.makeText(ProfileActivity.this, "Profile saved successfully", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Profile updated successfully");
            }

            @Override
            public void onError(String error) {
                btnSaveChanges.setEnabled(true);
                btnSaveChanges.setText("Save Changes");
                Log.e(TAG, "Error updating profile: " + error);
                Toast.makeText(ProfileActivity.this, "Failed to save profile: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}

