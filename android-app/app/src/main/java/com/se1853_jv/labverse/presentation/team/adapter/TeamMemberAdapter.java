package com.se1853_jv.labverse.presentation.team.adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.dto.response.TeamMemberResponse;

import java.util.ArrayList;
import java.util.List;

public class TeamMemberAdapter extends RecyclerView.Adapter<TeamMemberAdapter.MemberViewHolder> {

    private List<TeamMemberResponse> members = new ArrayList<>();
    private OnMemberActionListener listener;
    private boolean canRemoveMembers = false; // Set to true if current user is admin/PI
    private boolean canEditRoles = false; // Set to true if current user is PI
    private String teamCreatorId; // ID of team creator
    private String currentUserId; // ID of current user

    public interface OnMemberActionListener {
        void onRemoveMember(TeamMemberResponse member);
        void onEditRole(TeamMemberResponse member);
    }

    public void setOnMemberActionListener(OnMemberActionListener listener) {
        this.listener = listener;
    }

    public void setCanRemoveMembers(boolean canRemove) {
        this.canRemoveMembers = canRemove;
        notifyDataSetChanged(); // Refresh to update button visibility
    }

    public void setCanEditRoles(boolean canEdit) {
        this.canEditRoles = canEdit;
        notifyDataSetChanged(); // Refresh to update button visibility
    }

    public void setTeamCreatorId(String creatorId) {
        this.teamCreatorId = creatorId;
        notifyDataSetChanged(); // Refresh to update button visibility
    }

    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
        notifyDataSetChanged(); // Refresh to update button visibility
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_team_member_item, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        TeamMemberResponse member = members.get(position);
        holder.bind(member);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public void setMembers(List<TeamMemberResponse> members) {
        this.members = members != null ? new ArrayList<>(members) : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addMember(TeamMemberResponse member) {
        members.add(member);
        notifyItemInserted(members.size() - 1);
    }

    public void removeMember(TeamMemberResponse member) {
        int position = -1;
        String userIdToRemove = member.getUserId();
        if (userIdToRemove == null) {
            // Fallback to id if userId is null
            userIdToRemove = member.getId();
        }
        
        final String finalUserId = userIdToRemove;
        for (int i = 0; i < members.size(); i++) {
            TeamMemberResponse m = members.get(i);
            if ((m.getUserId() != null && m.getUserId().equals(finalUserId)) ||
                (m.getUserId() == null && m.getId() != null && m.getId().equals(finalUserId))) {
                position = i;
                break;
            }
        }
        if (position >= 0) {
            members.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void filterByRole(String role) {
        // This will be handled by the activity, but we can add filtering logic here if needed
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivAvatar;
        private TextView tvMemberName;
        private TextView tvMemberRole;
        private TextView tvMemberTitle;
        private TextView tvMemberInstitution;
        private TextView tvMemberLocation;
        private ImageButton btnMessage;
        private ImageButton btnCall;
        private ImageButton btnMail;
        private ImageButton btnEditRole;
        private ImageButton btnKick;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvMemberRole = itemView.findViewById(R.id.tvMemberRole);
            tvMemberTitle = itemView.findViewById(R.id.tvMemberTitle);
            tvMemberInstitution = itemView.findViewById(R.id.tvMemberInstitution);
            tvMemberLocation = itemView.findViewById(R.id.tvMemberLocation);
            btnMessage = itemView.findViewById(R.id.btnMessage);
            btnCall = itemView.findViewById(R.id.btnCall);
            btnMail = itemView.findViewById(R.id.btnMail);
            btnEditRole = itemView.findViewById(R.id.btnEditRole);
            btnKick = itemView.findViewById(R.id.btnKick);
        }

        public void bind(TeamMemberResponse member) {
            // Set avatar
            if (member.getUserAvatarUrl() != null && !member.getUserAvatarUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(member.getUserAvatarUrl())
                        .placeholder(R.mipmap.avt_mock_round)
                        .circleCrop()
                        .into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.mipmap.avt_mock_round);
            }

            // Set name
            if (tvMemberName != null) {
                String displayName = member.getUserFullName() != null && !member.getUserFullName().isEmpty()
                        ? member.getUserFullName()
                        : member.getUserName() != null ? member.getUserName() : "Unknown";
                tvMemberName.setText(displayName);
            }

            // Set role badge
            if (tvMemberRole != null) {
                String role = member.getRole() != null ? member.getRole() : "MEMBER";
                tvMemberRole.setText(role);
                
                // Set background color based on role
                int bgResId;
                switch (role.toUpperCase()) {
                    case "PI":
                        bgResId = R.drawable.bg_chip_orange;
                        break;
                    case "RESEARCHER":
                        bgResId = R.drawable.bg_chip_purple;
                        break;
                    case "STUDENT":
                        bgResId = R.drawable.bg_chip_green;
                        break;
                    default:
                        bgResId = R.drawable.bg_chip_gray;
                        break;
                }
                tvMemberRole.setBackgroundResource(bgResId);
            }

            // Set title/position (using email as fallback)
            if (tvMemberTitle != null) {
                String title = member.getUserEmail() != null ? member.getUserEmail() : "Team Member";
                tvMemberTitle.setText(title);
            }

            // Set institution (placeholder for now)
            if (tvMemberInstitution != null) {
                tvMemberInstitution.setText("Hanoi University"); // TODO: Get from user profile
            }

            // Set location (placeholder for now)
            if (tvMemberLocation != null) {
                tvMemberLocation.setText("Hanoi"); // TODO: Get from user profile
            }

            // Handle action buttons
            if (btnMessage != null) {
                btnMessage.setOnClickListener(v -> {
                    Toast.makeText(itemView.getContext(), "Message " + member.getUserFullName(), Toast.LENGTH_SHORT).show();
                    // TODO: Open messaging app or chat
                });
            }

            if (btnCall != null) {
                btnCall.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    // TODO: Add phone number from user profile
                    itemView.getContext().startActivity(intent);
                });
            }

            if (btnMail != null) {
                btnMail.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:" + member.getUserEmail()));
                    if (intent.resolveActivity(itemView.getContext().getPackageManager()) != null) {
                        itemView.getContext().startActivity(intent);
                    } else {
                        Toast.makeText(itemView.getContext(), "No email app found", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // Show/hide edit role button
            // Show edit role button only if:
            // - User has permission to edit roles (canEditRoles - PI only)
            // - Member is NOT the creator
            // - Member is NOT a PI
            if (btnEditRole != null) {
                boolean isCreator = false;
                boolean isPI = false;
                
                // Check if member is team creator
                if (teamCreatorId != null && member.getUserId() != null) {
                    isCreator = teamCreatorId.equals(member.getUserId());
                }
                
                // Check if member is PI
                String role = member.getRole();
                if (role != null) {
                    isPI = "PI".equalsIgnoreCase(role);
                }
                
                // Show edit role button only if user is PI and member is not creator/PI
                if (canEditRoles && !isCreator && !isPI) {
                    btnEditRole.setVisibility(View.VISIBLE);
                    btnEditRole.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onEditRole(member);
                        }
                    });
                } else {
                    btnEditRole.setVisibility(View.GONE);
                }
            }

            // Show/hide kick button based on permissions
            // Hide kick button for:
            // 1. Team creator
            // 2. PI members
            // Only show for non-creator, non-PI members
            if (btnKick != null) {
                boolean isCreator = false;
                boolean isPI = false;
                
                // Check if member is team creator
                if (teamCreatorId != null && member.getUserId() != null) {
                    isCreator = teamCreatorId.equals(member.getUserId());
                }
                
                // Check if member is PI
                String role = member.getRole();
                if (role != null) {
                    isPI = "PI".equalsIgnoreCase(role);
                }
                
                // Show kick button only if:
                // - User has permission to remove members (canRemoveMembers)
                // - Member is NOT the creator
                // - Member is NOT a PI
                if (canRemoveMembers && !isCreator && !isPI) {
                    btnKick.setVisibility(View.VISIBLE);
                    btnKick.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onRemoveMember(member);
                        }
                    });
                } else {
                    btnKick.setVisibility(View.GONE);
                }
            }

            // Long press to remove member (if allowed) - keep as backup
            // Only allow long press for non-creator, non-PI members
            if (canRemoveMembers) {
                boolean isCreator = false;
                boolean isPI = false;
                
                if (teamCreatorId != null && member.getUserId() != null) {
                    isCreator = teamCreatorId.equals(member.getUserId());
                }
                
                String role = member.getRole();
                if (role != null) {
                    isPI = "PI".equalsIgnoreCase(role);
                }
                
                if (!isCreator && !isPI) {
                    itemView.setOnLongClickListener(v -> {
                        if (listener != null) {
                            listener.onRemoveMember(member);
                        }
                        return true;
                    });
                } else {
                    itemView.setOnLongClickListener(null);
                }
            }
        }
    }
}

