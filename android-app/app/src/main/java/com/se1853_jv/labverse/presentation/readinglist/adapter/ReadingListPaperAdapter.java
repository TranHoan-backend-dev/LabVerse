package com.se1853_jv.labverse.presentation.readinglist.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;

import java.util.ArrayList;
import java.util.List;

public class ReadingListPaperAdapter extends RecyclerView.Adapter<ReadingListPaperAdapter.PaperViewHolder> {

    private List<PaperResearch> papers = new ArrayList<>();
    private OnPaperActionListener listener;

    public interface OnPaperActionListener {
        void onPaperClick(PaperResearch paper);
        void onRemovePaper(PaperResearch paper);
    }

    public void setOnPaperActionListener(OnPaperActionListener listener) {
        this.listener = listener;
    }

    public void setPapers(List<PaperResearch> newPapers) {
        this.papers.clear();
        if (newPapers != null) {
            this.papers.addAll(newPapers);
        }
        notifyDataSetChanged();
    }

    public void addPaper(PaperResearch paper) {
        this.papers.add(paper);
        notifyItemInserted(this.papers.size() - 1);
    }

    public void removePaper(PaperResearch paper) {
        int position = -1;
        for (int i = 0; i < papers.size(); i++) {
            if (papers.get(i).getId().equals(paper.getId())) {
                position = i;
                break;
            }
        }
        if (position >= 0) {
            this.papers.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public PaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_reading_list_paper_item, parent, false);
        return new PaperViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaperViewHolder holder, int position) {
        holder.bind(papers.get(position));
    }

    @Override
    public int getItemCount() {
        return papers.size();
    }

    class PaperViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPaperTitle;
        private TextView tvAuthors;
        private TextView tvJournalYear;
        private ImageButton btnRemove;

        public PaperViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPaperTitle = itemView.findViewById(R.id.tvPaperTitle);
            tvAuthors = itemView.findViewById(R.id.tvAuthors);
            tvJournalYear = itemView.findViewById(R.id.tvJournalYear);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }

        public void bind(PaperResearch paper) {
            // Set title
            if (tvPaperTitle != null) {
                tvPaperTitle.setText(paper.getTitle() != null ? paper.getTitle() : "Untitled Paper");
            }

            // Set authors
            if (tvAuthors != null) {
                tvAuthors.setText(paper.getAuthors() != null ? paper.getAuthors() : "Unknown Authors");
            }

            // Set journal and year
            if (tvJournalYear != null) {
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
                tvJournalYear.setText(journalInfo.length() > 0 ? journalInfo.toString() : "");
            }

            // Handle card click
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPaperClick(paper);
                }
            });

            // Handle remove button
            if (btnRemove != null) {
                btnRemove.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRemovePaper(paper);
                    }
                });
            }
        }
    }
}

