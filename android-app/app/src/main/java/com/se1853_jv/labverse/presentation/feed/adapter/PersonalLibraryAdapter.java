package com.se1853_jv.labverse.presentation.feed.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonalLibraryAdapter extends RecyclerView.Adapter<PersonalLibraryAdapter.PaperViewHolder> {
    private List<PaperResearch> papers = new ArrayList<>();
    private Map<String, Integer> paperProgressMap = new HashMap<>(); // paperId -> progress (0-100)
    private boolean showProgressBar = false; // Control whether to show progress bar
    private OnPaperClickListener clickListener;

    public interface OnPaperClickListener {
        void onPaperClick(PaperResearch paper);
    }

    public void setOnPaperClickListener(OnPaperClickListener listener) {
        this.clickListener = listener;
    }

    public void setPapers(List<PaperResearch> papers) {
        this.papers = papers != null ? papers : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Set progress data for papers (for recently_read tab)
     */
    public void setPaperProgressData(Map<String, Integer> progressMap) {
        this.paperProgressMap = progressMap != null ? progressMap : new HashMap<>();
        notifyDataSetChanged();
    }

    /**
     * Set whether to show progress bar (for recently_read tab)
     */
    public void setShowProgressBar(boolean show) {
        this.showProgressBar = show;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_personal_library_card, parent, false);
        return new PaperViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaperViewHolder holder, int position) {
        PaperResearch paper = papers.get(position);
        Integer progress = paperProgressMap.get(paper.getId());
        holder.bind(paper, clickListener, showProgressBar, progress);
    }

    @Override
    public int getItemCount() {
        return papers.size();
    }

    static class PaperViewHolder extends RecyclerView.ViewHolder {
        private TextView textTitle;
        private TextView textAuthors;
        private TextView textJournal;
        private ProgressBar progressBar;

        public PaperViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_paper_title);
            textAuthors = itemView.findViewById(R.id.text_paper_authors);
            textJournal = itemView.findViewById(R.id.text_paper_journal);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }

        public void bind(PaperResearch paper, OnPaperClickListener listener, boolean showProgress, Integer progress) {
            // Set title
            textTitle.setText(paper.getTitle() != null ? paper.getTitle() : "Untitled Paper");

            // Set authors
            textAuthors.setText(paper.getAuthors() != null ? paper.getAuthors() : "Unknown Authors");

            // Set journal and year
            StringBuilder journalInfo = new StringBuilder();
            if (paper.getJournal() != null && !paper.getJournal().isEmpty()) {
                journalInfo.append(paper.getJournal());
            }
            if (paper.getPublicationYear() != null && paper.getPublicationYear() > 0) {
                if (journalInfo.length() > 0) {
                    journalInfo.append(" • ");
                }
                journalInfo.append(paper.getPublicationYear());
            }
            textJournal.setText(journalInfo.length() > 0 ? journalInfo.toString() : "Unknown Journal");

            // Handle progress bar visibility and progress
            if (progressBar != null) {
                if (showProgress && progress != null && progress > 0) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(progress);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }

            // Handle card click
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPaperClick(paper);
                }
            });
        }
    }
}

