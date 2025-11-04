package com.se1853_jv.labverse.presentation.collection.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;

import java.util.ArrayList;
import java.util.List;

public class SelectPaperAdapter extends RecyclerView.Adapter<SelectPaperAdapter.PaperViewHolder> {
    private List<PaperResearch> papers = new ArrayList<>();
    private OnPaperSelectedListener listener;

    public interface OnPaperSelectedListener {
        void onPaperSelected(PaperResearch paper);
    }

    public SelectPaperAdapter(List<PaperResearch> papers, OnPaperSelectedListener listener) {
        this.papers = papers != null ? papers : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public PaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_select_paper_item, parent, false);
        return new PaperViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaperViewHolder holder, int position) {
        PaperResearch paper = papers.get(position);
        holder.bind(paper, listener);
    }

    @Override
    public int getItemCount() {
        return papers.size();
    }

    public void setPapers(List<PaperResearch> papers) {
        this.papers = papers != null ? papers : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class PaperViewHolder extends RecyclerView.ViewHolder {
        private TextView textTitle;
        private TextView textAuthors;
        private TextView textJournal;
        private MaterialButton buttonAdd;

        public PaperViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_paper_title);
            textAuthors = itemView.findViewById(R.id.text_paper_authors);
            textJournal = itemView.findViewById(R.id.text_paper_journal);
            buttonAdd = itemView.findViewById(R.id.button_add);
        }

        public void bind(PaperResearch paper, OnPaperSelectedListener listener) {
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
            
            buttonAdd.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPaperSelected(paper);
                }
            });
        }
    }
}

