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
        holder.bind(paper);
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
        private TextView textPriority;
        private Chip chipStatus;

        public PaperViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_paper_title);
            textAuthors = itemView.findViewById(R.id.text_paper_authors);
            textJournal = itemView.findViewById(R.id.text_paper_journal);
            textPriority = itemView.findViewById(R.id.text_paper_priority);
            chipStatus = itemView.findViewById(R.id.chip_paper_status);
        }

        public void bind(CollectionPaperDetailResponse paper) {
            textTitle.setText(paper.getTitle() != null ? paper.getTitle() : "Untitled Paper");
            textAuthors.setText(paper.getAuthors() != null ? paper.getAuthors() : "Unknown Authors");
            
            String journalInfo = "";
            if (paper.getJournal() != null && !paper.getJournal().isEmpty()) {
                journalInfo = paper.getJournal();
            }
            if (paper.getPublicationYear() != null && paper.getPublicationYear() > 0) {
                if (!journalInfo.isEmpty()) {
                    journalInfo += " • ";
                }
                journalInfo += String.valueOf(paper.getPublicationYear());
            }
            textJournal.setText(journalInfo.isEmpty() ? "Unknown" : journalInfo);
            
            textPriority.setText(paper.getPriority() != null ? paper.getPriority() : "MEDIUM");
            
            if (paper.getStatus() != null) {
                chipStatus.setText(paper.getStatus());
                chipStatus.setVisibility(View.VISIBLE);
            } else {
                chipStatus.setVisibility(View.GONE);
            }
        }
    }
}

