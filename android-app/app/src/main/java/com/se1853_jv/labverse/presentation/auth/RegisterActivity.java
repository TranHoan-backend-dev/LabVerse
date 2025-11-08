package com.se1853_jv.labverse.presentation.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.auth.AuthApiHandler;
import com.se1853_jv.labverse.data.dto.request.GoogleLoginRequest;
import com.se1853_jv.labverse.data.dto.response.AuthResponse;
import com.se1853_jv.labverse.data.utils.SessionManager;
import com.se1853_jv.labverse.domain.enumerate.Role;
import com.se1853_jv.labverse.presentation.feed.FeedActivity;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etUsername, etEmail, etPassword, etConfirmPassword;
    private AutoCompleteTextView actvRole;
    private MaterialButton btnCreateAccount, btnGoogleSignIn;
    private TextView btnLogin, btnRegister, tvLoginHere;
    private View loginIndicator, registerIndicator;

    private Map<String, Role> roleMap;
    private AuthApiHandler authApiHandler;
    private SessionManager sessionManager;
    private ProgressDialog progressDialog;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9002;
    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize API handler and session manager
        authApiHandler = new AuthApiHandler();
        sessionManager = new SessionManager(this);

        // Initialize views
        initializeViews();

        // Setup role dropdown
        setupRoleDropdown();

        // Set click listeners
        setupClickListeners();

        // Setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating account...");
        progressDialog.setCancelable(false);

        // Configure Google Sign-In
        configureGoogleSignIn();
    }

    private void initializeViews() {
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        actvRole = findViewById(R.id.actvRole);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginHere = findViewById(R.id.tvLoginHere);
        loginIndicator = findViewById(R.id.loginIndicator);
        registerIndicator = findViewById(R.id.registerIndicator);
    }

    private void setupRoleDropdown() {
        // Create role map for display names to Role enum
        roleMap = new HashMap<>();
        String piRole = getString(R.string.role_principal_investigator);
        String researcherRole = getString(R.string.role_researcher);
        String internRole = getString(R.string.role_student);

        roleMap.put(piRole, Role.PRINCIPAL_INVESTIGATOR);
        roleMap.put(researcherRole, Role.RESEARCHER);
        roleMap.put(internRole, Role.INTERN);

        // Create adapter with role options
        String[] roles = new String[]{
                piRole,           // Principal Investigator (PI)
                researcherRole,   // Researcher (Postdoc/PhD)
                internRole       // Student
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                roles
        );
        actvRole.setAdapter(adapter);
    }

    private void setupClickListeners() {
        // Create Account button
        btnCreateAccount.setOnClickListener(v -> handleRegister());

        // Google Sign In button
        btnGoogleSignIn.setOnClickListener(v -> handleGoogleRegister());

        // Login tab
        btnLogin.setOnClickListener(v -> navigateToLogin());

        // Login here link
        tvLoginHere.setOnClickListener(v -> navigateToLogin());
    }

    private void navigateToLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void handleRegister() {
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String roleText = actvRole.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }

        if (fullName.length() < 3) {
            etFullName.setError("Full name must be at least 3 characters");
            etFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }

        if (username.length() < 4) {
            etUsername.setError("Username must be at least 4 characters");
            etUsername.requestFocus();
            return;
        }

        // Check if username contains only alphanumeric and underscore
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            etUsername.setError("Username can only contain letters, numbers, and underscore");
            etUsername.requestFocus();
            return;
        }

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

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(roleText)) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            actvRole.requestFocus();
            return;
        }

        Role selectedRole = roleMap.get(roleText);
        if (selectedRole == null) {
            Toast.makeText(this, "Invalid role selected", Toast.LENGTH_SHORT).show();
            return;
        }


        // All validations passed
        performRegistration(fullName, username, email, password, selectedRole);
    }

    private void performRegistration(String fullName, String username, String email,
                                     String password, Role role) {
        // Show loading
        progressDialog.show();
        btnCreateAccount.setEnabled(false);

        // Map Role enum to backend role name
        String roleName = mapRoleToBackendName(role);

        // Create register request
        com.se1853_jv.labverse.data.dto.request.RegisterRequest registerRequest =
                new com.se1853_jv.labverse.data.dto.request.RegisterRequest(
                        email, password, fullName, username, roleName);

        // Call API
        authApiHandler.register(registerRequest, new ApiCallback<AuthResponse>() {
            @Override
            public void onSuccess(AuthResponse response) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    btnCreateAccount.setEnabled(true);

                    // Save user session
                    sessionManager.saveAuthResponse(response);

                    // Show success message
                    Toast.makeText(RegisterActivity.this,
                            "Welcome to LabVerse, " + response.getFullName() + "!",
                            Toast.LENGTH_SHORT).show();

                    // Navigate to main screen
                    navigateToFeed();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    btnCreateAccount.setEnabled(true);

                    // Show error message
                    String errorMessage = "Registration failed. Please try again.";
                    if (error != null && error.contains("Email")) {
                        errorMessage = "This email is already registered.";
                    } else if (error != null && error.contains("Username")) {
                        errorMessage = "This username is already taken.";
                    } else if (error != null && !error.isEmpty()) {
                        errorMessage = error;
                    }
                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Map Role enum to backend role name
     */
    private String mapRoleToBackendName(Role role) {
        switch (role) {
            case PRINCIPAL_INVESTIGATOR:
                return "PI";
            case RESEARCHER:
                return "RESEARCHER";
            case INTERN:
                return "STUDENT";
            default:
                return "RESEARCHER";
        }
    }

    private void navigateToFeed() {
        Intent intent = new Intent(RegisterActivity.this, FeedActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void configureGoogleSignIn() {
        // Configure Google Sign-In to request the user's ID, email address, and basic profile
        // Note: You need to get your Web Client ID from Google Cloud Console
        // The Web Client ID is different from Android Client ID - you need the Web Client ID for ID tokens
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void handleGoogleRegister() {
        // Đăng xuất khỏi Google Sign-In trước để luôn hiển thị dialog chọn tài khoản
        // Điều này cho phép người dùng chọn tài khoản Google khác mỗi lần đăng ký
        googleSignInClient.signOut()
                .addOnCompleteListener(this, task -> {
                    // Sau khi đăng xuất, mở dialog đăng nhập mới
                    // Dialog sẽ hiển thị danh sách tất cả các tài khoản Google để chọn
                    Intent signInIntent = googleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null && account.getIdToken() != null) {
                // Got the ID token, now send it to the backend
                String idToken = account.getIdToken();
                performGoogleRegister(idToken);
            } else {
                Toast.makeText(this, "Google Sign-Up failed: Unable to get account information", Toast.LENGTH_SHORT).show();
            }
        } catch (ApiException e) {
            Log.e(TAG, "Google Sign-In failed", e);
            String errorMessage = "Google Sign-Up failed";
            if (e.getStatusCode() == 12500) {
                errorMessage = "Google Sign-Up was cancelled";
            } else if (e.getStatusCode() == 10) {
                errorMessage = "Google Play Services is not available. Please update Google Play Services.";
            } else if (e.getStatusCode() == 7) {
                errorMessage = "Network error. Please check your internet connection.";
            }
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    private void performGoogleRegister(String idToken) {
        // Show loading
        progressDialog.setMessage("Signing up with Google...");
        progressDialog.show();
        btnGoogleSignIn.setEnabled(false);

        // Create Google login request (same endpoint handles both login and register)
        GoogleLoginRequest request = new GoogleLoginRequest(idToken);

        // Call API
        authApiHandler.googleLogin(request, new ApiCallback<AuthResponse>() {
            @Override
            public void onSuccess(AuthResponse response) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    btnGoogleSignIn.setEnabled(true);

                    // Save user session
                    sessionManager.saveAuthResponse(response);

                    // Show success message
                    Toast.makeText(RegisterActivity.this,
                            "Welcome to LabVerse, " + response.getFullName() + "!",
                            Toast.LENGTH_SHORT).show();

                    // Navigate to main screen
                    navigateToFeed();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    btnGoogleSignIn.setEnabled(true);

                    // Show error message
                    String errorMessage = "Google Sign-Up failed. Please try again.";
                    if (error != null && !error.isEmpty()) {
                        errorMessage = error;
                    }
                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}

