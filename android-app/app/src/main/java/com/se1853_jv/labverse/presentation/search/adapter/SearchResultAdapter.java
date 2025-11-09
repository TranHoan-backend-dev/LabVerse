package com.se1853_jv.labverse.presentation.search.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;
import com.se1853_jv.labverse.presentation.paper.PaperDetailsActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder> {
    private List<PaperResearch> papers = new ArrayList<>();
    private final int[] tagColors = {
            R.color.tag_color_1, R.color.tag_color_2, R.color.tag_color_3,
            R.color.tag_color_4, R.color.tag_color_5, R.color.tag_color_6
    };
    private final Random random = new Random();

    @NonNull
    @Override
    public SearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_search_paper_card, parent, false);
        return new SearchResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResultViewHolder holder, int position) {
        PaperResearch paper = papers.get(position);
        holder.bind(paper);
    }

    @Override
    public int getItemCount() {
        return papers.size();
    }

    public void setPapers(List<PaperResearch> papers) {
        this.papers = papers != null ? papers : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addPapers(List<PaperResearch> papers) {
        if (papers != null && !papers.isEmpty()) {
            int startPosition = this.papers.size();
            this.papers.addAll(papers);
            notifyItemRangeInserted(startPosition, papers.size());
        }
    }

    public void clear() {
        this.papers.clear();
        notifyDataSetChanged();
    }

    class SearchResultViewHolder extends RecyclerView.ViewHolder {
        private final TextView textPaperTitle;
        private final TextView textPublicationDetails;
        private final TextView textAuthorAffiliation;
        private final LinearLayout tagsContainer;
        private final TextView textViews;
        private final TextView textCitations;
        private final TextView textReadMore;
        private final ImageButton bookmarkIcon;

        public SearchResultViewHolder(@NonNull View itemView) {
            super(itemView);
            textPaperTitle = itemView.findViewById(R.id.text_paper_title);
            textPublicationDetails = itemView.findViewById(R.id.text_publication_details);
            textAuthorAffiliation = itemView.findViewById(R.id.text_author_affiliation);
            tagsContainer = itemView.findViewById(R.id.tags_container);
            textViews = itemView.findViewById(R.id.text_views);
            textCitations = itemView.findViewById(R.id.text_citations);
            textReadMore = itemView.findViewById(R.id.text_read_more);
            bookmarkIcon = itemView.findViewById(R.id.bookmark_icon);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    PaperResearch paper = papers.get(position);
                    openPaperDetails(v, paper);
                }
            });

            textReadMore.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    PaperResearch paper = papers.get(position);
                    openPaperDetails(v, paper);
                }
            });

            bookmarkIcon.setOnClickListener(v -> {
                // TODO: Implement bookmark functionality
                boolean isBookmarked = bookmarkIcon.getTag() != null && (boolean) bookmarkIcon.getTag();
                bookmarkIcon.setTag(!isBookmarked);
                // Update icon based on bookmark state
                // You can change the drawable resource here
            });
        }

        public void bind(PaperResearch paper) {
            // Set title
            textPaperTitle.setText(paper.getTitle() != null ? paper.getTitle() : "No Title");

            // Set publication details (Journal • Year)
            String journal = paper.getJournal() != null ? paper.getJournal() : "Unknown Journal";
            Integer year = paper.getPublicationYear();
            String yearStr = year != null ? String.valueOf(year) : "Unknown";
            textPublicationDetails.setText(journal + " • " + yearStr);

            // Set author and affiliation
            String authors = paper.getAuthors() != null ? paper.getAuthors() : "Unknown Author";
            // For now, we'll just show the authors. In a real app, you might have affiliation data
            textAuthorAffiliation.setText(authors);

            // Set keywords/tags
            tagsContainer.removeAllViews();
            if (paper.getKeyword() != null && !paper.getKeyword().isEmpty()) {
                for (String keyword : paper.getKeyword()) {
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        Chip chip = new Chip(itemView.getContext());
                        chip.setText(keyword);
                        chip.setChipBackgroundColorResource(getRandomTagColor());
                        chip.setTextColor(itemView.getContext().getColor(android.R.color.white));
                        chip.setChipMinHeight(itemView.getContext().getResources().getDimensionPixelSize(R.dimen.chip_min_height));
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(0, 0, 8, 0);
                        chip.setLayoutParams(params);
                        tagsContainer.addView(chip);
                    }
                }
            }

            // Set engagement metrics (mock data for now)
            // In a real app, these would come from the API
            // For now, we'll hide these or show mock data
            // You can add viewCount and citationCount to PaperResearch model if available
            int views = random.nextInt(5000) + 100;
            int citations = random.nextInt(500) + 50;
            textViews.setText(formatViews(views));
            textCitations.setText(String.valueOf(citations));
        }

        private void openPaperDetails(View v, PaperResearch paper) {
            Intent intent = new Intent(v.getContext(), PaperDetailsActivity.class);
            intent.putExtra("paper_id", paper.getId());
            v.getContext().startActivity(intent);
        }

        private int getRandomTagColor() {
            return tagColors[random.nextInt(tagColors.length)];
        }

        private String formatViews(int number) {
            if (number >= 1000) {
                return String.format("%.1f", number / 1000.0) + "k";
            }
            return String.valueOf(number);
        }
    }
}

