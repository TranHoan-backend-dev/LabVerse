package com.se1853_jv.labverse.presentation.search.adapter;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.paper.PaperApiHandler;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;
import com.se1853_jv.labverse.presentation.paper.PdfReaderActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder> {
    private static final String TAG = "SearchResultAdapter";
    private List<PaperResearch> papers = new ArrayList<>();
    private Map<String, Integer> progressMap = new HashMap<>(); // paperId -> progress (0-100)
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
        holder.bind(paper, progressMap);
    }

    @Override
    public int getItemCount() {
        return papers.size();
    }

    public void setPapers(List<PaperResearch> papers) {
        this.papers = papers != null ? papers : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    /**
     * Get current papers list
     */
    public List<PaperResearch> getPapers() {
        return new ArrayList<>(papers); // Return a copy to prevent external modification
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
        this.progressMap.clear();
        notifyDataSetChanged();
    }
    
    /**
     * Set progress data for papers (called after loading from ReadingWorkflow)
     */
    public void setPaperProgressData(Map<String, Integer> progressMap) {
        this.progressMap = progressMap != null ? progressMap : new HashMap<>();
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
        private final ProgressBar progressBar;

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
            progressBar = itemView.findViewById(R.id.progress_bar);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    PaperResearch paper = papers.get(position);
                    openPDFReader(v, paper);
                }
            });

            textReadMore.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    PaperResearch paper = papers.get(position);
                    openPDFReader(v, paper);
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

        public void bind(PaperResearch paper, Map<String, Integer> progressMap) {
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
            
            // Handle reading progress from ReadingWorkflow
            if (progressBar != null) {
                Integer progress = progressMap != null ? progressMap.get(paper.getId()) : null;
                if (progress != null && progress > 0) {
                    // Show progress bar for papers that are being read (progress > 0, including 100%)
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(progress);
                } else {
                    // Hide progress bar for unread papers (progress = 0 or null)
                    progressBar.setVisibility(View.GONE);
                }
            }
        }

        private void openPDFReader(View v, PaperResearch paper) {
            String paperId = paper.getId();
            if (paperId == null || paperId.isEmpty()) {
                Toast.makeText(v.getContext(), "Paper ID not available", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if PDF URL is already available in the paper object
            String pdfUrl = paper.getDataUrl();
            if (pdfUrl != null && !pdfUrl.isEmpty()) {
                // PDF URL is available, open PDF Reader directly
                Intent intent = new Intent(v.getContext(), PdfReaderActivity.class);
                intent.putExtra("paperId", paperId);
                // Use PERSONAL_LIBRARY as collectionId for search papers (not in a collection)
                intent.putExtra("collectionId", "PERSONAL_LIBRARY");
                intent.putExtra("pdfUrl", pdfUrl);
                Log.d(TAG, "Opening PDF Reader with paperId=" + paperId + ", collectionId=PERSONAL_LIBRARY");
                v.getContext().startActivity(intent);
            } else {
                // PDF URL is not available, fetch paper details first
                Toast.makeText(v.getContext(), "Loading paper...", Toast.LENGTH_SHORT).show();
                
                PaperApiHandler paperApiHandler = new PaperApiHandler();
                paperApiHandler.getPaperDetails(paperId, new ApiCallback<PaperResearch>() {
                    @Override
                    public void onSuccess(PaperResearch paperResearch) {
                        // Post to UI thread to update UI
                        new Handler(Looper.getMainLooper()).post(() -> {
                            String fetchedPdfUrl = paperResearch.getDataUrl();
                            if (fetchedPdfUrl == null || fetchedPdfUrl.isEmpty()) {
                                Toast.makeText(v.getContext(),
                                        "PDF URL not available for this paper",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }

                            // Open PDF Reader Activity
                            Intent intent = new Intent(v.getContext(), PdfReaderActivity.class);
                            intent.putExtra("paperId", paperId);
                            // Use PERSONAL_LIBRARY as collectionId for search papers (not in a collection)
                            intent.putExtra("collectionId", "PERSONAL_LIBRARY");
                            intent.putExtra("pdfUrl", fetchedPdfUrl);
                            Log.d(TAG, "Opening PDF Reader with paperId=" + paperId + ", collectionId=PERSONAL_LIBRARY");
                            v.getContext().startActivity(intent);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        // Post to UI thread to update UI
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Log.e(TAG, "Error loading paper details: " + error);
                            Toast.makeText(v.getContext(),
                                    "Failed to load paper: " + error,
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                });
            }
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

