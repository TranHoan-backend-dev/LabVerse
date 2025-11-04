package com.se1853_jv.labverse.presentation.user;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.auth.AuthApiHandler;
import com.se1853_jv.labverse.data.api.user.UserApiHandler;
import com.se1853_jv.labverse.data.dto.response.UserResponse;
import com.se1853_jv.labverse.data.utils.SessionManager;
import com.se1853_jv.labverse.presentation.auth.LoginActivity;


public class UserActivity extends AppCompatActivity implements EditProfileDialog.OnProfileUpdatedListener {

    private ImageView ivAvatar;
    private TextView tvFullName, tvUsername, tvEmail, tvRole;
    private MaterialButton btnLogout, btnEdit, btnChangePassword;
    
    private SessionManager sessionManager;
    private AuthApiHandler authApiHandler;
    private UserApiHandler userApiHandler;
    private ProgressDialog progressDialog;
    private UserResponse currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_profile);

            // Initialize
            sessionManager = new SessionManager(this);
            authApiHandler = new AuthApiHandler();
            userApiHandler = new UserApiHandler(this);
            
            // Check if user is logged in
            if (!sessionManager.isLoggedIn()) {
                navigateToLogin();
                return;
            }

            // Setup progress dialog first
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            
            // Initialize views
            initializeViews();
            
            // Setup click listeners
            setupClickListeners();
            
            // Load user data from API after views are ready
            loadUserDataFromAPI();
        } catch (Exception e) {
            android.util.Log.e("UserActivity", "Error in onCreate", e);
            Toast.makeText(this, "Error loading profile. Please try again.", Toast.LENGTH_SHORT).show();
            // Fallback: show session data
            if (sessionManager != null && sessionManager.isLoggedIn()) {
                loadUserDataFromSession();
            } else {
                navigateToLogin();
            }
        }
    }

    private void initializeViews() {
        try {
            ivAvatar = findViewById(R.id.ivAvatar);
            tvFullName = findViewById(R.id.tvFullName);
            tvUsername = findViewById(R.id.tvUsername);
            tvEmail = findViewById(R.id.tvEmail);
            tvRole = findViewById(R.id.tvRole);
            btnLogout = findViewById(R.id.btnLogout);
            btnEdit = findViewById(R.id.btnEdit);
            btnChangePassword = findViewById(R.id.btnChangePassword);
        } catch (Exception e) {
            android.util.Log.e("UserActivity", "Error initializing views", e);
            Toast.makeText(this, "Error loading profile screen", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadUserDataFromAPI() {
        try {
            if (progressDialog != null) {
                progressDialog.setMessage("Loading profile...");
                progressDialog.show();
            }
            
            userApiHandler.getCurrentUser(new ApiCallback<UserResponse>() {
                @Override
                public void onSuccess(UserResponse user) {
                    runOnUiThread(() -> {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        if (user != null) {
                            currentUser = user;
                            displayUserData(user);
                        } else {
                            loadUserDataFromSession();
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        // Fallback to session data if API fails
                        loadUserDataFromSession();
                        android.util.Log.e("UserActivity", "Error loading user: " + error);
                    });
                }
            });
        } catch (Exception e) {
            android.util.Log.e("UserActivity", "Error in loadUserDataFromAPI", e);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            loadUserDataFromSession();
        }
    }

    private void loadUserDataFromSession() {
        try {
            // Load user info from session as fallback
            String fullName = sessionManager.getFullName();
            String username = sessionManager.getUsername();
            String email = sessionManager.getEmail();
            String role = sessionManager.getRole();
            String userId = sessionManager.getUserId();
            String avatarUrl = sessionManager.getAvatarUrl();

            // Set text with null checks
            if (fullName != null && tvFullName != null) tvFullName.setText(fullName);
            if (username != null && tvUsername != null) tvUsername.setText(username);
            if (email != null && tvEmail != null) tvEmail.setText(email);
            if (role != null && tvRole != null) tvRole.setText(role);

            // Load avatar
            if (ivAvatar != null && avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(this)
                        .load(avatarUrl)
                        .placeholder(R.mipmap.avt_mock_round)
                        .error(R.mipmap.avt_mock_round)
                        .circleCrop()
                        .into(ivAvatar);
            }
        } catch (Exception e) {
            android.util.Log.e("UserActivity", "Error loading session data", e);
        }
    }

    private void displayUserData(UserResponse user) {
        if (user == null) {
            loadUserDataFromSession();
            return;
        }
        
        try {
            // Set text
            if (user.getFullName() != null && tvFullName != null) {
                tvFullName.setText(user.getFullName());
            }
            if (user.getUsername() != null && tvUsername != null) {
                tvUsername.setText(user.getUsername());
            }
            if (user.getEmail() != null && tvEmail != null) {
                tvEmail.setText(user.getEmail());
            }
            if (user.getRole() != null && tvRole != null) {
                tvRole.setText(user.getRole());
            }

            // Load avatar
            if (ivAvatar != null && user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                Glide.with(this)
                        .load(user.getAvatarUrl())
                        .placeholder(R.mipmap.avt_mock_round)
                        .error(R.mipmap.avt_mock_round)
                        .circleCrop()
                        .into(ivAvatar);
            }
            
            // Update session with latest data
            updateSessionFromUserResponse(user);
        } catch (Exception e) {
            android.util.Log.e("UserActivity", "Error displaying user data", e);
        }
    }


    private void updateSessionFromUserResponse(UserResponse user) {
        // Update session manager with latest user data
        if (user.getFullName() != null) {
            sessionManager.updateFullName(user.getFullName());
        }
        if (user.getAvatarUrl() != null) {
            sessionManager.updateAvatarUrl(user.getAvatarUrl());
        }
    }

    private void setupClickListeners() {
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> showLogoutConfirmation());
        }
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> showEditProfileDialog());
        }
        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        }
    }
    
    private void showChangePasswordDialog() {
        try {
            ChangePasswordDialog dialog = new ChangePasswordDialog();
            dialog.show(getSupportFragmentManager(), "ChangePasswordDialog");
        } catch (Exception e) {
            android.util.Log.e("UserActivity", "Error showing change password dialog", e);
            Toast.makeText(this, "Error opening change password dialog", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditProfileDialog() {
        try {
            if (currentUser == null) {
                Toast.makeText(this, "Loading user data...", Toast.LENGTH_SHORT).show();
                loadUserDataFromAPI();
                return;
            }
            
            EditProfileDialog dialog = EditProfileDialog.newInstance(currentUser);
            dialog.show(getSupportFragmentManager(), "EditProfileDialog");
        } catch (Exception e) {
            android.util.Log.e("UserActivity", "Error showing edit dialog", e);
            Toast.makeText(this, "Error opening edit dialog", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onProfileUpdated(UserResponse updatedUser) {
        // Update current user
        currentUser = updatedUser;
        
        // Update display
        displayUserData(updatedUser);
        
        // Show success message
        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        // Show loading
        progressDialog.show();
        btnLogout.setEnabled(false);

        // Call logout API (optional, but good practice)
        authApiHandler.logout(new ApiCallback<String>() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    btnLogout.setEnabled(true);
                    
                    // Clear session
                    sessionManager.logout();
                    
                    // Show success message
                    Toast.makeText(UserActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    
                    // Navigate to login
                    navigateToLogin();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    btnLogout.setEnabled(true);
                    
                    // Even if API fails, clear local session
                    sessionManager.logout();
                    
                    // Show message
                    Toast.makeText(UserActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                    
                    // Navigate to login
                    navigateToLogin();
                });
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(UserActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
        EdgeToEdge.enable(this);
        setContentView(R.layout.layout_common_ui_home);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

//        AppDatabase db = DatabaseClient.getInstance(this).getAppDatabase();
//        UserRepository userRepo = db.userRepository();
//
//        new Thread(() -> {
//            Roles role = new Roles("1", Role.INTERN);
//            db.roleRepository().create(role);
//
//            Users user = new Users();
//            user.setId("1");
//            user.setEmail("test@example.com");
//            user.setPassword("123456");
//            user.setName("Test User");
//            user.setUsername("testuser");
//            user.setCreatedDate(System.currentTimeMillis());
//            user.setUpdatedDate(System.currentTimeMillis());
//            user.setRoleId("1");
//
//            userRepo.create(user);
//
//            Users users = userRepo.getById("1");
//
//            runOnUiThread(() -> {
//                Toast.makeText(
//                        this,
//                        users.toString(),
//                        Toast.LENGTH_LONG
//                ).show();
//            });
//        }).start();
    }
}
