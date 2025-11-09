package com.se1853_jv.labverse.presentation.feed.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.presentation.feed.entity.Paper;
import com.se1853_jv.labverse.presentation.paper.PaperDetailsActivity;

import java.util.List;

public class MyPaperMainContentFragment extends Fragment {
    private int CURRENT_PAGE = 0; // Changed from static to instance variable
    private final static int PAGE_SIZE = 5;
    private List<Paper> papers;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_mypaper_main_content, container, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            papers = (List<Paper>) getArguments().getSerializable("papers");
        }

        // Reset to first page when new data is loaded
        CURRENT_PAGE = 0;
        
        buildPaperCard(view, getLayoutInflater());
        buildPagination(view);
    }

    @SuppressLint("SetTextI18n")
    private void buildPaperCard(@NonNull View view, LayoutInflater inflater) {
        LinearLayout layout = view.findViewById(R.id.card_container);
        layout.removeAllViews();

        if (papers == null || papers.isEmpty()) {
            // Show empty state or return
            return;
        }

        // Calculate the end index to avoid IndexOutOfBoundsException
        int startIndex = CURRENT_PAGE * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, papers.size());
        
        // Reset CURRENT_PAGE if it's out of bounds
        if (startIndex >= papers.size()) {
            CURRENT_PAGE = 0;
            startIndex = 0;
            endIndex = Math.min(PAGE_SIZE, papers.size());
        }

        // Get the sublist for current page
        List<Paper> papersToShow = papers.subList(startIndex, endIndex);

        for (var paper : papersToShow) {
            View card = inflater.inflate(R.layout.layout_mypaper_card, layout, false);

            TextView title = card.findViewById(R.id.title);
            title.setText(paper.getTitle());

            TextView metadata = card.findViewById(R.id.metadata);
            metadata.setText(paper.getAuthors() + " • " + paper.getJournal() + " • " + paper.getYear());

            if (paper.getStatus().equalsIgnoreCase("in progress")) {
                View status = card.findViewById(R.id.in_progress);
                status.setVisibility(View.VISIBLE);
            } else if (paper.getStatus().equalsIgnoreCase("read")) {
                View status = card.findViewById(R.id.read);
                status.setVisibility(View.VISIBLE);
            } else {
                View status = card.findViewById(R.id.status_continue);
                status.setVisibility(View.VISIBLE);
            }
            deleteCard(card, layout);
            navigateToReadPdfScreen(card, paper.getId());

            layout.addView(card);
        }
    }

    private void deleteCard(@NonNull View view, LinearLayout parent) {
        ImageButton btn = view.findViewById(R.id.delete_btn);
        btn.setOnClickListener(v -> {
            parent.removeView(view);
            // TODO: them goi api xoa that trong server
        });
    }

    private void buildPagination(@NonNull View view) {
        updatePageInfo(view);

        ImageButton btnPrev = view.findViewById(R.id.btn_prev);
        ImageButton btnNext = view.findViewById(R.id.btn_next);

        btnPrev.setOnClickListener(v -> {
            if (CURRENT_PAGE > 0) {
                CURRENT_PAGE--;
                buildPaperCard(view, getLayoutInflater());
                updatePageInfo(view);
            } else {
                Toast.makeText(requireContext(), "Đang ở trang đầu tiên", Toast.LENGTH_SHORT).show();
            }
        });

        btnNext.setOnClickListener(v -> {
            var numberOfPage = getNumberOfPage();
            int nextPage = CURRENT_PAGE + 1;
            if (nextPage < numberOfPage) {
                CURRENT_PAGE = nextPage;
                buildPaperCard(view, getLayoutInflater());
                updatePageInfo(view);
            } else {
                Toast.makeText(requireContext(), "Đã đến trang cuối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePageInfo(@NonNull View view) {
        var numberOfPage = getNumberOfPage();
        var title = String.format("Page %s of %s", CURRENT_PAGE + 1, numberOfPage);

        TextView tv = view.findViewById(R.id.tv_page_info);
        tv.setText(title);
    }

    private int getNumberOfPage() {
        if (papers == null || papers.isEmpty()) return 1;
        int totalPages = (int) Math.ceil((double) papers.size() / PAGE_SIZE);
        return Math.max(1, totalPages); // At least 1 page
    }

    private void navigateToReadPdfScreen(@NonNull View parent, String id) {
        Button btn = parent.findViewById(R.id.continue_to_read_btn);
        btn.setOnClickListener(v -> {
            // TODO: wait for reading pdf activity
//            var intent = new Intent(requireContext(), PaperDetailsActivity.class);
//            intent.putExtra("id", id);
//            startActivity(intent);
        });
    }
}
