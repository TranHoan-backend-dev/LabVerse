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
import com.se1853_jv.labverse.data.dto.response.CollectionResponse;
import com.se1853_jv.labverse.domain.enumerate.AccessLevel;

import java.util.ArrayList;
import java.util.List;

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.CollectionViewHolder> {
    private List<CollectionResponse> collections = new ArrayList<>();
    private OnCollectionClickListener listener;
    private boolean isOwner; // true for My Collections, false for Shared Collections

    public interface OnCollectionClickListener {
        void onCollectionClick(CollectionResponse collection);
        void onOptionsClick(CollectionResponse collection, View anchor);
    }

    public void setOnCollectionClickListener(OnCollectionClickListener listener) {
        this.listener = listener;
    }

    public void setCollections(List<CollectionResponse> collections) {
        this.collections = collections != null ? collections : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setIsOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }

    @NonNull
    @Override
    public CollectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_collection_item, parent, false);
        return new CollectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CollectionViewHolder holder, int position) {
        CollectionResponse collection = collections.get(position);
        holder.bind(collection, listener, isOwner);
    }

    @Override
    public int getItemCount() {
        return collections.size();
    }

    static class CollectionViewHolder extends RecyclerView.ViewHolder {
        private final TextView textTitle;
        private final TextView textInfo;
        private final ImageView imageCreatorAvatar;
        private final TextView textCreatorName;
        private final ImageView buttonOptions;
        private final Chip chipRole;

        public CollectionViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_collection_title);
            textInfo = itemView.findViewById(R.id.text_collection_info);
            imageCreatorAvatar = itemView.findViewById(R.id.image_creator_avatar);
            textCreatorName = itemView.findViewById(R.id.text_creator_name);
            buttonOptions = itemView.findViewById(R.id.button_collection_options);
            chipRole = itemView.findViewById(R.id.chip_collection_role);
        }

        public void bind(CollectionResponse collection, OnCollectionClickListener listener, boolean isOwner) {
            textTitle.setText(collection.getName());
            
            // Display actual counts from API response
            long paperCount = collection.getPaperCount() != null ? collection.getPaperCount() : 0;
            long memberCount = collection.getMemberCount() != null ? collection.getMemberCount() : 0;
            textInfo.setText(paperCount + " papers • " + memberCount + " members");
            
            // Display creator name and avatar
            String creatorName = collection.getCreatorName();
            String creatorAvatarUrl = collection.getCreatorAvatarUrl();
            
            if (creatorName != null && !creatorName.isEmpty()) {
                textCreatorName.setText(creatorName);
                textCreatorName.setVisibility(View.VISIBLE);
            } else {
                textCreatorName.setText("Unknown");
                textCreatorName.setVisibility(View.VISIBLE);
            }
            
            // Load avatar with Glide, fallback to mock if no URL
            if (creatorAvatarUrl != null && !creatorAvatarUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(creatorAvatarUrl)
                        .placeholder(R.mipmap.avt_mock_round)
                        .error(R.mipmap.avt_mock_round)
                        .circleCrop()
                        .into(imageCreatorAvatar);
            } else {
                imageCreatorAvatar.setImageResource(R.mipmap.avt_mock_round);
            }
            
            // Set access level chip based on currentUserAccessLevel
            AccessLevel accessLevel = collection.getCurrentUserAccessLevel();
            
            // Fallback: if accessLevel is null, use isOwner to determine
            if (accessLevel == null) {
                accessLevel = isOwner ? AccessLevel.AUTHOR : AccessLevel.CONTRIBUTOR;
            }
            
            // Display access level
            chipRole.setText(accessLevel.name());
            chipRole.setVisibility(View.VISIBLE);
            
            // Set color based on access level
            int chipColor;
            int textColor;
            switch (accessLevel) {
                case AUTHOR:
                    chipColor = android.graphics.Color.parseColor("#7CCA97"); // Green
                    textColor = android.graphics.Color.parseColor("#1B5E20"); // Dark green
                    break;
                case CONTRIBUTOR:
                    chipColor = android.graphics.Color.parseColor("#FF9800"); // Orange
                    textColor = android.graphics.Color.parseColor("#FFFFFF"); // White
                    break;
                case READ_ONLY:
                    chipColor = android.graphics.Color.parseColor("#757575"); // Gray
                    textColor = android.graphics.Color.parseColor("#FFFFFF"); // White
                    break;
                default:
                    chipColor = android.graphics.Color.parseColor("#8CA5D3"); // Blue
                    textColor = android.graphics.Color.parseColor("#1976D2"); // Dark blue
                    break;
            }
            chipRole.setChipBackgroundColor(ColorStateList.valueOf(chipColor));
            chipRole.setTextColor(textColor);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCollectionClick(collection);
                }
            });

            buttonOptions.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOptionsClick(collection, buttonOptions);
                }
            });
        }
    }
}


