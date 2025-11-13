package com.se1853_jv.labverse.presentation.readinglist;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.user.UserApiHandler;
import com.se1853_jv.labverse.data.dto.response.UserResponse;
import com.se1853_jv.labverse.data.utils.Connectivity;

import java.util.regex.Pattern;

public class SelectUserForReadingListActivity extends AppCompatActivity {
    private static final String TAG = "SelectUserForRL";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private String readingListId;
    private UserApiHandler userApiHandler;

    private MaterialToolbar toolbar;
    private EditText editSearch;
    private CardView cardUserInfo;
    private ImageView imgAvatar;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private TextView tvUserUsername;
    private MaterialButton btnAddUser;
    private ProgressBar progressBar;
    private TextView textEmptyState;

    private UserResponse foundUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user);

        readingListId = getIntent().getStringExtra("readingListId");
        if (readingListId == null || readingListId.isEmpty()) {
            Toast.makeText(this, "Reading list ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userApiHandler = new UserApiHandler(this);

        initializeViews();
        setupToolbar();
        setupSearch();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        editSearch = findViewById(R.id.edit_search);
        cardUserInfo = findViewById(R.id.card_user_info);
        imgAvatar = findViewById(R.id.img_avatar);
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserEmail = findViewById(R.id.tv_user_email);
        tvUserUsername = findViewById(R.id.tv_user_username);
        btnAddUser = findViewById(R.id.btn_add_user);
        progressBar = findViewById(R.id.progress_bar);
        textEmptyState = findViewById(R.id.text_empty_state);
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupSearch() {
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    hideUserInfo();
                    textEmptyState.setText("Enter an email address or username to search for users");
                    textEmptyState.setVisibility(View.VISIBLE);
                    return;
                }

                // Check if it's a valid email (contains @)
                if (EMAIL_PATTERN.matcher(query).matches()) {
                    // Search user by email
                    searchUserByEmail(query);
                } else if (query.length() >= 3) {
                    // If it looks like a username (no @, at least 3 chars), search by username
                    searchUserByUsername(query);
                } else {
                    hideUserInfo();
                    textEmptyState.setText("Please enter at least 3 characters");
                    textEmptyState.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnAddUser.setOnClickListener(v -> {
            if (foundUser != null) {
                addUserToReadingList();
            }
        });
    }

    private void searchUserByEmail(String email) {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        hideUserInfo();

        userApiHandler.getUserByEmail(email, new ApiCallback<UserResponse>() {
            @Override
            public void onSuccess(UserResponse user) {
                runOnUiThread(() -> {
                    showLoading(false);
                    foundUser = user;
                    displayUserInfo(user);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    foundUser = null;
                    hideUserInfo();
                    textEmptyState.setText("User not found. Please check the email address.");
                    textEmptyState.setVisibility(View.VISIBLE);
                    Log.e(TAG, "Error searching user: " + error);
                });
            }
        });
    }

    private void searchUserByUsername(String username) {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        hideUserInfo();

        userApiHandler.getUserByUsername(username, new ApiCallback<UserResponse>() {
            @Override
            public void onSuccess(UserResponse user) {
                runOnUiThread(() -> {
                    showLoading(false);
                    foundUser = user;
                    displayUserInfo(user);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    foundUser = null;
                    hideUserInfo();
                    textEmptyState.setText("User not found. Please check the username.");
                    textEmptyState.setVisibility(View.VISIBLE);
                    Log.e(TAG, "Error searching user: " + error);
                });
            }
        });
    }

    private void displayUserInfo(UserResponse user) {
        if (user == null) {
            hideUserInfo();
            return;
        }

        // Set user info
        tvUserName.setText(user.getFullName() != null ? user.getFullName() : "Unknown");
        tvUserEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        tvUserUsername.setText(user.getUsername() != null ? "@" + user.getUsername() : "");

        // Load avatar if available
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            Glide.with(this)
                    .load(user.getAvatarUrl())
                    .placeholder(R.drawable.ic_person)
                    .circleCrop()
                    .into(imgAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.ic_person);
        }

        // Show card
        cardUserInfo.setVisibility(View.VISIBLE);
        textEmptyState.setVisibility(View.GONE);
    }

    private void hideUserInfo() {
        cardUserInfo.setVisibility(View.GONE);
        foundUser = null;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            textEmptyState.setVisibility(View.GONE);
        }
    }

    private void addUserToReadingList() {
        if (foundUser == null || foundUser.getId() == null) {
            Toast.makeText(this, "User information not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Return user ID to parent activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("userId", foundUser.getId());
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}

