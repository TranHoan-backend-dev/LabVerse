package com.se1853_jv.labverse.presentation.library.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.presentation.library.data.LibraryMockDataProvider;
import com.se1853_jv.labverse.presentation.library.model.LibraryPaper;
import com.se1853_jv.labverse.presentation.paper.PaperDetailsActivity;

import java.util.ArrayList;
import java.util.List;

public class LibraryPaperAdapter extends RecyclerView.Adapter<LibraryPaperAdapter.PaperViewHolder> {
    
    private List<LibraryPaper> papers = new ArrayList<>();
    private Context context;
    private LibraryMockDataProvider dataProvider;
    
    public LibraryPaperAdapter(Context context) {
        this.context = context;
        this.dataProvider = LibraryMockDataProvider.getInstance();
    }
    
    public void setPapers(List<LibraryPaper> papers) {
        this.papers = papers != null ? papers : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public PaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_library_paper_card, parent, false);
        return new PaperViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PaperViewHolder holder, int position) {
        LibraryPaper paper = papers.get(position);
        holder.bind(paper);
    }
    
    @Override
    public int getItemCount() {
        return papers.size();
    }
    
    class PaperViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private Chip statusChip;
        private TextView titleText;
        private TextView metaText;
        private TextView pdfText;
        private TextView readCountText;
        private TextView citationsText;
        private ImageButton bookmarkButton;
        private ImageButton shareButton;
        private ProgressBar progressBar;
        private Button startReadingButton;
        private Button reviewNotesButton;
        
        public PaperViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.paper_card);
            statusChip = itemView.findViewById(R.id.chip_status);
            titleText = itemView.findViewById(R.id.text_title);
            metaText = itemView.findViewById(R.id.text_meta);
            pdfText = itemView.findViewById(R.id.text_pdf);
            readCountText = itemView.findViewById(R.id.text_read);
            citationsText = itemView.findViewById(R.id.text_citations);
            bookmarkButton = itemView.findViewById(R.id.btn_bookmark);
            shareButton = itemView.findViewById(R.id.btn_share);
            progressBar = itemView.findViewById(R.id.progress_bar);
            startReadingButton = itemView.findViewById(R.id.btn_start_reading);
            reviewNotesButton = itemView.findViewById(R.id.btn_review_notes);
        }
        
        public void bind(LibraryPaper paper) {
            // Set title
            titleText.setText(paper.getTitle());
            
            // Set meta info (journal, year, authors)
            metaText.setText(paper.getJournal() + " • " + paper.getYear() + " • Authors: " + paper.getAuthors());
            
            // Set PDF text
            pdfText.setText("📄 PDF");
            
            // Set read count
            readCountText.setText("👁 " + paper.getReadCount() + " reads");
            
            // Set citations
            citationsText.setText("📚 " + paper.getCitationCount() + " citations");
            
            // Set status chip
            statusChip.setText(paper.getStatus());
            statusChip.setChipBackgroundColorResource(getStatusColor(paper.getStatusColor()));
            
            // Set bookmark icon
            bookmarkButton.setImageResource(paper.isFavorite() ? 
                    R.drawable.ic_star : R.drawable.ic_bookmark);
            
            // Show/hide progress bar
            if (paper.getProgress() > 0 && paper.getProgress() < 100) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(paper.getProgress());
            } else {
                progressBar.setVisibility(View.GONE);
            }
            
            // Set button text based on status
            if ("Finished".equals(paper.getStatus())) {
                startReadingButton.setVisibility(View.GONE);
                reviewNotesButton.setVisibility(View.VISIBLE);
            } else {
                startReadingButton.setVisibility(View.VISIBLE);
                reviewNotesButton.setVisibility(View.GONE);
                startReadingButton.setText("Reading".equals(paper.getStatus()) ? 
                        "Continue Reading" : "Start Reading");
            }
            
            // Click listeners
            cardView.setOnClickListener(v -> {
                Intent intent = new Intent(context, PaperDetailsActivity.class);
                intent.putExtra("paper_id", paper.getId());
                context.startActivity(intent);
            });
            
            bookmarkButton.setOnClickListener(v -> {
                // Toggle favorite in data provider
                dataProvider.toggleFavorite(paper.getId());
                // Update UI
                notifyItemChanged(getAdapterPosition());
            });
            
            shareButton.setOnClickListener(v -> {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, paper.getTitle());
                shareIntent.putExtra(Intent.EXTRA_TEXT, paper.getTitle() + " by " + paper.getAuthors());
                context.startActivity(Intent.createChooser(shareIntent, "Share paper"));
            });
            
            startReadingButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, PaperDetailsActivity.class);
                intent.putExtra("paper_id", paper.getId());
                context.startActivity(intent);
            });
            
            reviewNotesButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, PaperDetailsActivity.class);
                intent.putExtra("paper_id", paper.getId());
                context.startActivity(intent);
            });
        }
        
        private int getStatusColor(String color) {
            switch (color) {
                case "blue":
                    return R.color.blue_50;
                case "yellow":
                    return R.color.skin;
                case "green":
                    return R.color.first_green;
                default:
                    return R.color.gray_200;
            }
        }
    }
}
