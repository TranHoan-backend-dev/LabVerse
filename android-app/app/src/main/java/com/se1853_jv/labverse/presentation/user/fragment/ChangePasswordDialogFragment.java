package com.se1853_jv.labverse.presentation.user.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.user.UserApiHandler;
import com.se1853_jv.labverse.data.dto.request.ChangePasswordRequest;

public class ChangePasswordDialogFragment extends DialogFragment {

    private TextInputEditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private MaterialButton btnSave, btnCancel;

    private UserApiHandler userApiHandler;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
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
        builder.setTitle("Change Password");

        android.view.LayoutInflater inflater = activity.getLayoutInflater();
        android.view.View view = inflater.inflate(R.layout.dialog_change_password, null);

        initializeViews(view);
        setupClickListeners();

        builder.setView(view);
        return builder.create();
    }

    private void initializeViews(@NonNull android.view.View view) {
        etCurrentPassword = view.findViewById(R.id.etCurrentPassword);
        etNewPassword = view.findViewById(R.id.etNewPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);
    }

    @SuppressLint("SetTextI18n")
    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // Validation
            if (TextUtils.isEmpty(currentPassword)) {
                etCurrentPassword.setError("Current password is required");
                etCurrentPassword.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(newPassword)) {
                etNewPassword.setError("New password is required");
                etNewPassword.requestFocus();
                return;
            }

            if (newPassword.length() < 6) {
                etNewPassword.setError("New password must be at least 6 characters");
                etNewPassword.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(confirmPassword)) {
                etConfirmPassword.setError("Please confirm your new password");
                etConfirmPassword.requestFocus();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                etConfirmPassword.setError("Passwords do not match");
                etConfirmPassword.requestFocus();
                return;
            }

            if (currentPassword.equals(newPassword)) {
                etNewPassword.setError("New password must be different from current password");
                etNewPassword.requestFocus();
                return;
            }

            // Create request
            ChangePasswordRequest request = new ChangePasswordRequest(currentPassword, newPassword);

            // Show loading
            btnSave.setEnabled(false);
            btnSave.setText("Changing...");

            // Call API
            userApiHandler.changePassword(request, new ApiCallback<String>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccess(String message) {
                    android.app.Activity activity = getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(() -> {
                            if (btnSave != null) {
                                btnSave.setEnabled(true);
                                btnSave.setText("Change Password");
                            }

                            Context context = getContext();
                            if (context == null) {
                                context = activity;
                            } else {
                                Toast.makeText(context,
                                        message != null ? message : "Password changed successfully!",
                                        Toast.LENGTH_SHORT).show();
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
                                btnSave.setText("Change Password");
                            }

                            String errorMessage = "Failed to change password";
                            if (error != null && error.contains("Current password is incorrect")) {
                                errorMessage = "Current password is incorrect";
                                etCurrentPassword.setError("Current password is incorrect");
                                etCurrentPassword.requestFocus();
                            } else if (error != null && error.contains("must be different")) {
                                errorMessage = "New password must be different from current password";
                                etNewPassword.setError(errorMessage);
                                etNewPassword.requestFocus();
                            } else if (error != null && !error.isEmpty()) {
                                errorMessage = error;
                            }

                            Context context = getContext();
                            if (context == null) {
                                context = activity;
                            } else {
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });
        });
    }
}

