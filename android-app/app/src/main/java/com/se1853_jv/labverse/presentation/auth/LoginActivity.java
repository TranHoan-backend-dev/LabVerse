package com.se1853_jv.labverse.presentation.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.auth.AuthApiHandler;
import com.se1853_jv.labverse.data.dto.request.ForgotPasswordRequest;
import com.se1853_jv.labverse.data.dto.request.LoginRequest;
import com.se1853_jv.labverse.data.dto.response.AuthResponse;
import com.se1853_jv.labverse.data.utils.SessionManager;
import com.se1853_jv.labverse.presentation.feed.FeedActivity;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private CheckBox cbRememberMe;
    private MaterialButton btnSignIn, btnGoogleSignIn;
    private TextView btnLogin, btnRegister, tvForgotPassword, tvCreateAccount;
    private View loginIndicator, registerIndicator;
    
    private AuthApiHandler authApiHandler;
    private SessionManager sessionManager;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize API handler and session manager
        authApiHandler = new AuthApiHandler();
        sessionManager = new SessionManager(this);
        
        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToFeed();
            return;
        }

        // Initialize views
        initializeViews();

        // Set click listeners
        setupClickListeners();
        
        // Setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvCreateAccount = findViewById(R.id.tvCreateAccount);
        loginIndicator = findViewById(R.id.loginIndicator);
        registerIndicator = findViewById(R.id.registerIndicator);
    }

    private void setupClickListeners() {
        // Sign In button
        btnSignIn.setOnClickListener(v -> handleLogin());

        // Google Sign In button
        btnGoogleSignIn.setOnClickListener(v -> handleGoogleLogin());

        // Login tab
        btnLogin.setOnClickListener(v -> {
            // Already on login screen
            updateTabSelection(true);
        });

        // Register tab
        btnRegister.setOnClickListener(v -> {
            // Navigate to register screen
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        // Forgot password
        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());

        // Create account
        tvCreateAccount.setOnClickListener(v -> {
            // Navigate to register screen
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void updateTabSelection(boolean isLogin) {
        if (isLogin) {
            btnLogin.setTextColor(getResources().getColor(R.color.blue, null));
            btnLogin.setTypeface(null, android.graphics.Typeface.BOLD);
            loginIndicator.setBackgroundResource(R.drawable.tab_indicator_selected);
            
            btnRegister.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
            btnRegister.setTypeface(null, android.graphics.Typeface.NORMAL);
            registerIndicator.setBackgroundResource(R.drawable.tab_indicator_unselected);
        } else {
            btnRegister.setTextColor(getResources().getColor(R.color.blue, null));
            btnRegister.setTypeface(null, android.graphics.Typeface.BOLD);
            registerIndicator.setBackgroundResource(R.drawable.tab_indicator_selected);
            
            btnLogin.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
            btnLogin.setTypeface(null, android.graphics.Typeface.NORMAL);
            loginIndicator.setBackgroundResource(R.drawable.tab_indicator_unselected);
        }
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        // TODO: Implement actual authentication logic
        // For now, just navigate to the main screen
        performLogin(email, password);
    }

    private void performLogin(String email, String password) {
        // Show loading
        progressDialog.show();
        btnSignIn.setEnabled(false);
        
        // Create login request
        LoginRequest loginRequest = new LoginRequest(email, password);
        
        // Call API
        authApiHandler.login(loginRequest, new ApiCallback<AuthResponse>() {
            @Override
            public void onSuccess(AuthResponse response) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
            btnSignIn.setEnabled(true);
            
                    // Save user session
                    sessionManager.saveAuthResponse(response);
                    
                    // Show success message
                    Toast.makeText(LoginActivity.this, 
                            "Welcome back, " + response.getFullName() + "!", 
                            Toast.LENGTH_SHORT).show();
                    
            // Navigate to main screen
                    navigateToFeed();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    btnSignIn.setEnabled(true);
                    
                    // Show error message
                    String errorMessage = "Login failed. Please check your credentials.";
                    if (error != null && !error.isEmpty()) {
                        errorMessage = error;
                    }
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private void navigateToFeed() {
            Intent intent = new Intent(LoginActivity.this, FeedActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
    }

    private void handleGoogleLogin() {
        // TODO: Implement Google Sign-In
        Toast.makeText(this, "Google Sign-In functionality coming soon", Toast.LENGTH_SHORT).show();
    }

    private void handleForgotPassword() {
        String email = etEmail.getText().toString().trim();

        // Validate email
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Please enter your email first");
            etEmail.requestFocus();
            Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("We will send a new password to:\n" + email)
                .setPositiveButton("Send", (dialog, which) -> performForgotPassword(email))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performForgotPassword(String email) {
        // Show loading
        progressDialog.setMessage("Sending password reset email...");
        progressDialog.show();

        // Create request
        ForgotPasswordRequest request = new ForgotPasswordRequest(email);

        // Call API
        authApiHandler.forgotPassword(request, new ApiCallback<String>() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();

                    // Show success message
                    String successMessage = message != null ? message : "Password reset email sent successfully!";
                    new androidx.appcompat.app.AlertDialog.Builder(LoginActivity.this)
                            .setTitle("Email Sent")
                            .setMessage(successMessage + "\n\nPlease check your email for the new password.")
                            .setPositiveButton("OK", null)
                            .show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();

                    // Show error message
                    String errorMessage = "Failed to send password reset email. Please try again.";
                    if (error != null && error.contains("not found")) {
                        errorMessage = "This email is not registered in our system.";
                    } else if (error != null && !error.isEmpty()) {
                        errorMessage = error;
                    }
                    
                    new androidx.appcompat.app.AlertDialog.Builder(LoginActivity.this)
                            .setTitle("Error")
                            .setMessage(errorMessage)
                            .setPositiveButton("OK", null)
                            .show();
                });
            }
        });
    }
}

