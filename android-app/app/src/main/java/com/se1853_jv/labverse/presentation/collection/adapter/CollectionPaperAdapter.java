package com.se1853_jv.labverse.presentation.collection.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.dto.response.CollectionPaperDetailResponse;
import com.se1853_jv.labverse.domain.enumerate.AccessLevel;

import java.util.ArrayList;
import java.util.List;

public class CollectionPaperAdapter extends RecyclerView.Adapter<CollectionPaperAdapter.PaperViewHolder> {
    private List<CollectionPaperDetailResponse> papers = new ArrayList<>();
    private OnStatusClickListener statusClickListener;
    private OnRemoveClickListener removeClickListener;
    private OnPaperClickListener paperClickListener;
    private AccessLevel currentUserAccessLevel;

    public interface OnStatusClickListener {
        void onStatusClick(CollectionPaperDetailResponse paper);
    }

    public interface OnRemoveClickListener {
        void onRemoveClick(CollectionPaperDetailResponse paper);
    }

    public interface OnPaperClickListener {
        void onPaperClick(CollectionPaperDetailResponse paper);
    }

    public void setOnStatusClickListener(OnStatusClickListener listener) {
        this.statusClickListener = listener;
    }

    public void setOnRemoveClickListener(OnRemoveClickListener listener) {
        this.removeClickListener = listener;
    }

    public void setOnPaperClickListener(OnPaperClickListener listener) {
        this.paperClickListener = listener;
    }

    public void setCurrentUserAccessLevel(AccessLevel accessLevel) {
        this.currentUserAccessLevel = accessLevel;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_collection_paper_item, parent, false);
        return new PaperViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaperViewHolder holder, int position) {
        CollectionPaperDetailResponse paper = papers.get(position);
        holder.bind(paper, statusClickListener, removeClickListener, paperClickListener, currentUserAccessLevel);
    }

    @Override
    public int getItemCount() {
        return papers.size();
    }

    public void setPapers(List<CollectionPaperDetailResponse> newPapers) {
        this.papers = newPapers != null ? newPapers : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class PaperViewHolder extends RecyclerView.ViewHolder {
        private TextView textTitle;
        private TextView textAuthors;
        private TextView textJournal;
        private Chip chipPriority;
        private Chip chipStatus;
        private com.google.android.material.button.MaterialButton buttonRemove;

        public PaperViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_paper_title);
            textAuthors = itemView.findViewById(R.id.text_paper_authors);
            textJournal = itemView.findViewById(R.id.text_paper_journal);
            chipPriority = itemView.findViewById(R.id.chip_paper_priority);
            chipStatus = itemView.findViewById(R.id.chip_paper_status);
            buttonRemove = itemView.findViewById(R.id.button_remove_paper);
        }

        public void bind(CollectionPaperDetailResponse paper, OnStatusClickListener statusListener, 
                        OnRemoveClickListener removeListener, OnPaperClickListener paperListener,
                        AccessLevel currentUserAccessLevel) {
            // Handle title
            String title = paper.getTitle();
            if (title == null || title.isEmpty() || title.equals("Unknown Paper")) {
                title = "Loading paper details...";
            }
            textTitle.setText(title);
            
            // Set click listener on entire item view to open PDF reader
            // Child views (chips, buttons) will consume their own clicks
            itemView.setOnClickListener(v -> {
                if (paperListener != null) {
                    paperListener.onPaperClick(paper);
                }
            });
            
            // Make chips and buttons not clickable for parent (they handle their own clicks)
            // This prevents the itemView click from firing when clicking on these views
            chipStatus.setClickable(true);
            chipPriority.setClickable(true);
            if (buttonRemove.getVisibility() == View.VISIBLE) {
                buttonRemove.setClickable(true);
            }
            
            // Handle authors
            String authors = paper.getAuthors();
            if (authors == null || authors.isEmpty() || authors.equals("Unknown Authors")) {
                authors = "Authors not available";
            }
            textAuthors.setText(authors);
            
            // Handle journal info
            String journalInfo = "";
            String journal = paper.getJournal();
            if (journal != null && !journal.isEmpty() && !journal.equals("Unknown")) {
                journalInfo = journal;
            }
            Integer year = paper.getPublicationYear();
            if (year != null && year > 0) {
                if (!journalInfo.isEmpty()) {
                    journalInfo += " • ";
                }
                journalInfo += String.valueOf(year);
            }
            if (journalInfo.isEmpty()) {
                journalInfo = "Journal information not available";
            }
            textJournal.setText(journalInfo);
            
            // Handle priority with color
            String priorityText = paper.getPriority();
            if (priorityText == null || priorityText.isEmpty()) {
                priorityText = "MEDIUM";
            }
            chipPriority.setText(priorityText);
            chipPriority.setVisibility(View.VISIBLE);
            
            // Set color based on priority
            int priorityColor;
            switch (priorityText.toUpperCase()) {
                case "HIGH":
                    priorityColor = 0xFFF44336; // Red
                    break;
                case "MEDIUM":
                    priorityColor = 0xFFFF9800; // Orange
                    break;
                case "LOW":
                    priorityColor = 0xFF4CAF50; // Green
                    break;
                default:
                    priorityColor = 0xFF757575; // Gray
                    break;
            }
            chipPriority.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(priorityColor));
            chipPriority.setTextColor(0xFFFFFFFF); // White text
            
            if (paper.getStatus() != null) {
                chipStatus.setText(paper.getStatus());
                chipStatus.setVisibility(View.VISIBLE);
                
                // Set color based on status
                int statusColor;
                switch (paper.getStatus().toUpperCase()) {
                    case "TOREAD":
                        statusColor = 0xFF2196F3; // Blue
                        break;
                    case "READING":
                        statusColor = 0xFFFF9800; // Orange
                        break;
                    case "FINISHED":
                        statusColor = 0xFF4CAF50; // Green
                        break;
                    default:
                        statusColor = 0xFF757575; // Gray
                        break;
                }
                chipStatus.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(statusColor));
                chipStatus.setTextColor(0xFFFFFFFF); // White text
                
                // Make status chip clickable
                chipStatus.setOnClickListener(v -> {
                    if (statusListener != null) {
                        statusListener.onStatusClick(paper);
                    }
                });
            } else {
                chipStatus.setVisibility(View.GONE);
            }

            // Show/hide remove button based on access level
            // Only CONTRIBUTOR and AUTHOR can remove papers
            boolean canRemovePaper = currentUserAccessLevel == AccessLevel.AUTHOR || 
                                     currentUserAccessLevel == AccessLevel.CONTRIBUTOR;
            
            if (canRemovePaper && removeListener != null) {
                buttonRemove.setVisibility(View.VISIBLE);
                buttonRemove.setOnClickListener(v -> {
                    if (removeListener != null) {
                        removeListener.onRemoveClick(paper);
                    }
                });
            } else {
                buttonRemove.setVisibility(View.GONE);
            }
        }
    }
}

