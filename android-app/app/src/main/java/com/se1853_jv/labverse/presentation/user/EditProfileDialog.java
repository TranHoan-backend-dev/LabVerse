package com.se1853_jv.labverse.presentation.user;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.user.UserApiHandler;
import com.se1853_jv.labverse.data.dto.request.UpdateProfileRequest;
import com.se1853_jv.labverse.data.dto.response.UserResponse;

public class EditProfileDialog extends DialogFragment {
    
    private TextInputEditText etFullName, etUsername, etAvatarUrl;
    private MaterialButton btnSave, btnCancel;
    
    private UserApiHandler userApiHandler;
    private OnProfileUpdatedListener listener;
    private UserResponse currentUser;
    
    public interface OnProfileUpdatedListener {
        void onProfileUpdated(UserResponse updatedUser);
    }

    public static EditProfileDialog newInstance(UserResponse user) {
        EditProfileDialog dialog = new EditProfileDialog();
        Bundle args = new Bundle();
        // Pass user data as individual strings
        if (user != null) {
            args.putString("fullName", user.getFullName());
            args.putString("username", user.getUsername());
            args.putString("avatarUrl", user.getAvatarUrl());
        }
        dialog.setArguments(args);
        return dialog;
    }
    
    public void setCurrentUser(UserResponse user) {
        this.currentUser = user;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnProfileUpdatedListener) {
            listener = (OnProfileUpdatedListener) context;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Reconstruct UserResponse from Bundle
        if (getArguments() != null) {
            currentUser = new UserResponse();
            currentUser.setFullName(getArguments().getString("fullName", ""));
            currentUser.setUsername(getArguments().getString("username", ""));
            currentUser.setAvatarUrl(getArguments().getString("avatarUrl", ""));
        }

        Context context = getContext();
        if (context == null) {
            context = getActivity();
        }
        
        if (context == null) {
            return super.onCreateDialog(savedInstanceState);
        }

        userApiHandler = new UserApiHandler(context);

        android.app.Activity activity = getActivity();
        if (activity == null) {
            return super.onCreateDialog(savedInstanceState);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Edit Profile");
        
        android.view.LayoutInflater inflater = activity.getLayoutInflater();
        android.view.View view = inflater.inflate(R.layout.dialog_edit_profile, null);
        
        initializeViews(view);
        loadCurrentData();
        setupClickListeners();
        
        builder.setView(view);
        return builder.create();
    }

    private void initializeViews(android.view.View view) {
        etFullName = view.findViewById(R.id.etFullName);
        etUsername = view.findViewById(R.id.etUsername);
        etAvatarUrl = view.findViewById(R.id.etAvatarUrl);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);
    }

    private void loadCurrentData() {
        if (currentUser != null) {
            if (currentUser.getFullName() != null) {
                etFullName.setText(currentUser.getFullName());
            }
            if (currentUser.getUsername() != null) {
                etUsername.setText(currentUser.getUsername());
            }
            if (currentUser.getAvatarUrl() != null) {
                etAvatarUrl.setText(currentUser.getAvatarUrl());
            }
        }
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> dismiss());
        
        btnSave.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String avatarUrl = etAvatarUrl.getText().toString().trim();

            // Validation
            if (TextUtils.isEmpty(fullName)) {
                etFullName.setError("Full name is required");
                etFullName.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(username)) {
                etUsername.setError("Username is required");
                etUsername.requestFocus();
                return;
            }

            if (username.length() < 3) {
                etUsername.setError("Username must be at least 3 characters");
                etUsername.requestFocus();
                return;
            }

            // Create request
            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setFullName(fullName);
            request.setUsername(username);
            if (!avatarUrl.isEmpty()) {
                request.setAvatarUrl(avatarUrl);
            }

            // Show loading
            btnSave.setEnabled(false);
            btnSave.setText("Saving...");

            // Call API
            userApiHandler.updateProfile(request, new ApiCallback<UserResponse>() {
                @Override
                public void onSuccess(UserResponse response) {
                    android.app.Activity activity = getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(() -> {
                            if (btnSave != null) {
                                btnSave.setEnabled(true);
                                btnSave.setText("Save");
                            }
                            
                            Context context = getContext();
                            if (context == null) context = activity;
                            if (context != null) {
                                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                            }
                            
                            if (listener != null) {
                                listener.onProfileUpdated(response);
                            }
                            
                            dismiss();
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    android.app.Activity activity = getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(() -> {
                            if (btnSave != null) {
                                btnSave.setEnabled(true);
                                btnSave.setText("Save");
                            }
                            
                            String errorMessage = "Failed to update profile";
                            if (error != null && error.contains("Username already taken")) {
                                errorMessage = "Username is already taken";
                            } else if (error != null && !error.isEmpty()) {
                                errorMessage = error;
                            }
                            
                            Context context = getContext();
                            if (context == null) context = activity;
                            if (context != null) {
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });
        });
    }
}

