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

import java.util.ArrayList;
import java.util.List;

public class CollectionPaperAdapter extends RecyclerView.Adapter<CollectionPaperAdapter.PaperViewHolder> {
    private List<CollectionPaperDetailResponse> papers = new ArrayList<>();
    private OnStatusClickListener statusClickListener;

    public interface OnStatusClickListener {
        void onStatusClick(CollectionPaperDetailResponse paper);
    }

    public void setOnStatusClickListener(OnStatusClickListener listener) {
        this.statusClickListener = listener;
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
        holder.bind(paper, statusClickListener);
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

        public PaperViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_paper_title);
            textAuthors = itemView.findViewById(R.id.text_paper_authors);
            textJournal = itemView.findViewById(R.id.text_paper_journal);
            chipPriority = itemView.findViewById(R.id.chip_paper_priority);
            chipStatus = itemView.findViewById(R.id.chip_paper_status);
        }

        public void bind(CollectionPaperDetailResponse paper, OnStatusClickListener listener) {
            // Handle title
            String title = paper.getTitle();
            if (title == null || title.isEmpty() || title.equals("Unknown Paper")) {
                title = "Loading paper details...";
            }
            textTitle.setText(title);
            
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
                    if (listener != null) {
                        listener.onStatusClick(paper);
                    }
                });
            } else {
                chipStatus.setVisibility(View.GONE);
            }
        }
    }
}

