package com.se1853_jv.labverse.presentation.collection.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.dto.response.CollectionUserResponse;
import com.se1853_jv.labverse.data.dto.response.UserResponse;
import com.se1853_jv.labverse.domain.enumerate.AccessLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionMemberAdapter extends RecyclerView.Adapter<CollectionMemberAdapter.MemberViewHolder> {
    private List<CollectionUserResponse> members = new ArrayList<>();
    private List<CollectionUserResponse> originalMembers = new ArrayList<>(); // Store original list with member IDs
    private Map<String, UserResponse> userDetailsMap = new HashMap<>(); // Map memberId -> UserResponse
    private String currentUserId;
    private AccessLevel currentUserAccessLevel; // Access level of the current user viewing this screen
    private OnMemberClickListener listener;

    public interface OnMemberClickListener {
        void onAccessLevelClick(CollectionUserResponse member);
        void onRemoveClick(CollectionUserResponse member);
    }

    public void setOnMemberClickListener(OnMemberClickListener listener) {
        this.listener = listener;
    }

    public void setMembers(List<CollectionUserResponse> members, List<CollectionUserResponse> originalMembers, String currentUserId) {
        this.members = members != null ? members : new ArrayList<>();
        this.originalMembers = originalMembers != null ? originalMembers : new ArrayList<>();
        this.currentUserId = currentUserId;
        notifyDataSetChanged();
    }

    public void setCurrentUserAccessLevel(AccessLevel accessLevel) {
        this.currentUserAccessLevel = accessLevel;
        notifyDataSetChanged();
    }

    public void setUserDetails(String memberId, UserResponse userResponse) {
        if (userDetailsMap == null) {
            userDetailsMap = new HashMap<>();
        }
        userDetailsMap.put(memberId, userResponse);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_collection_member_item, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        CollectionUserResponse member = members.get(position);
        UserResponse userDetails = userDetailsMap.get(member.getMemberId());
        boolean isCurrentUser = currentUserId != null && currentUserId.equals(member.getMemberId());
        
        // Check if current user (viewer) is AUTHOR - only AUTHOR can manage members
        boolean currentUserIsAuthor = currentUserAccessLevel != null && currentUserAccessLevel == AccessLevel.AUTHOR;
        
        holder.bind(member, userDetails, isCurrentUser, currentUserIsAuthor, listener);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageAvatar;
        private final TextView textName;
        private final TextView textEmail;
        private final Chip chipAccessLevel;
        private final View buttonChangeAccess;
        private final View buttonRemove;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            imageAvatar = itemView.findViewById(R.id.image_member_avatar);
            textName = itemView.findViewById(R.id.text_member_name);
            textEmail = itemView.findViewById(R.id.text_member_email);
            chipAccessLevel = itemView.findViewById(R.id.chip_access_level);
            buttonChangeAccess = itemView.findViewById(R.id.button_change_access);
            buttonRemove = itemView.findViewById(R.id.button_remove_member);
        }

        public void bind(CollectionUserResponse member, UserResponse userDetails, 
                       boolean isCurrentUser, boolean currentUserIsAuthor, 
                       OnMemberClickListener listener) {
            // Display user name - use member ID for now
            // TODO: Fetch user details from UserService if needed
            String displayName = "Member " + member.getMemberId().substring(0, Math.min(8, member.getMemberId().length()));
            if (userDetails != null && userDetails.getFullName() != null && !userDetails.getFullName().isEmpty()) {
                displayName = userDetails.getFullName();
            } else if (userDetails != null && userDetails.getUsername() != null && !userDetails.getUsername().isEmpty()) {
                displayName = userDetails.getUsername();
            }
            textName.setText(displayName);
            if (isCurrentUser) {
                textName.setText(displayName + " (You)");
            }

            // Display email or member ID
            String email = userDetails != null ? userDetails.getEmail() : null;
            if (email != null && !email.isEmpty()) {
                textEmail.setText(email);
                textEmail.setVisibility(android.view.View.VISIBLE);
            } else {
                // Show member ID if no email
                textEmail.setText("ID: " + member.getMemberId().substring(0, Math.min(12, member.getMemberId().length())));
                textEmail.setVisibility(android.view.View.VISIBLE);
            }

            // Display avatar
            String avatarUrl = userDetails != null ? userDetails.getAvatarUrl() : null;
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(avatarUrl)
                        .placeholder(R.mipmap.avt_mock_round)
                        .error(R.mipmap.avt_mock_round)
                        .circleCrop()
                        .into(imageAvatar);
            } else {
                imageAvatar.setImageResource(R.mipmap.avt_mock_round);
            }

            // Display access level chip
            AccessLevel accessLevel = member.getAccessLevel();
            if (accessLevel == null) {
                // Fallback to isAuthor for backward compatibility
                accessLevel = (member.getIsAuthor() != null && member.getIsAuthor()) 
                    ? AccessLevel.AUTHOR : AccessLevel.CONTRIBUTOR;
            }

            chipAccessLevel.setText(accessLevel.name());
            chipAccessLevel.setVisibility(android.view.View.VISIBLE);

            // Set color based on access level
            int chipColor;
            int textColor;
            switch (accessLevel) {
                case AUTHOR:
                    chipColor = 0xFF7CCA97; // Green
                    textColor = 0xFF1B5E20; // Dark green
                    break;
                case CONTRIBUTOR:
                    chipColor = 0xFFFF9800; // Orange
                    textColor = 0xFFFFFFFF; // White
                    break;
                case READ_ONLY:
                    chipColor = 0xFF757575; // Gray
                    textColor = 0xFFFFFFFF; // White
                    break;
                default:
                    chipColor = 0xFF757575;
                    textColor = 0xFFFFFFFF;
                    break;
            }
            chipAccessLevel.setChipBackgroundColor(ColorStateList.valueOf(chipColor));
            chipAccessLevel.setTextColor(textColor);

            // Show/hide action buttons
            // Only show buttons if current user (viewer) is AUTHOR and not viewing themselves
            boolean canManage = currentUserIsAuthor && !isCurrentUser;
            buttonChangeAccess.setVisibility(canManage ? android.view.View.VISIBLE : android.view.View.GONE);
            buttonRemove.setVisibility(canManage ? android.view.View.VISIBLE : android.view.View.GONE);

            // Set click listeners
            chipAccessLevel.setOnClickListener(v -> {
                if (canManage && listener != null) {
                    listener.onAccessLevelClick(member);
                }
            });

            buttonChangeAccess.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAccessLevelClick(member);
                }
            });

            buttonRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveClick(member);
                }
            });
        }
    }
}

