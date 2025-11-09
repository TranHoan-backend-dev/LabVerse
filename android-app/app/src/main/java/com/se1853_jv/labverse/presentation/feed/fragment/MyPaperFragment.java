package com.se1853_jv.labverse.presentation.feed.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.gson.reflect.TypeToken;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.paper.PaperApiHandler;
import com.se1853_jv.labverse.data.utils.Connectivity;
import com.se1853_jv.labverse.data.utils.ParseFileUtils;
import com.se1853_jv.labverse.data.utils.SessionManager;
import com.se1853_jv.labverse.data.utils.EncoderUtils;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;
import com.se1853_jv.labverse.presentation.feed.adapter.MyPaperContentAdapter;
import com.se1853_jv.labverse.presentation.feed.entity.MyPaperItem;
import com.se1853_jv.labverse.presentation.feed.entity.Paper;
import com.se1853_jv.labverse.presentation.feed.entity.Summary;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class MyPaperFragment extends Fragment {
    MyPaperItem item;
    Button recentlyAdded, recentlyRead, favorites;
    PaperApiHandler paperApiHandler;
    SessionManager sessionManager;
    List<Paper> papers = new ArrayList<>();
    View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_mypaper, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = view;
        
        recentlyAdded = view.findViewById(R.id.recently_added);
        favorites = view.findViewById(R.id.favorites);
        recentlyRead = view.findViewById(R.id.recently_read);

        // Initialize API handler and session manager
        paperApiHandler = new PaperApiHandler(requireContext());
        sessionManager = new SessionManager(requireContext());

        // Load papers from API
        loadPapersFromAPI(view);

        handleSwitchTabs(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload papers when fragment becomes visible
        Log.d("MyPaperFragment", "onResume called - reloading papers");
        if (rootView != null && paperApiHandler != null && sessionManager != null) {
            // Small delay to ensure fragment is fully visible
            rootView.post(() -> {
                if (isResumed() && getActivity() != null) {
                    loadPapersFromAPI(rootView);
                }
            });
        } else {
            Log.w("MyPaperFragment", "Cannot reload: rootView=" + (rootView != null) + 
                    ", paperApiHandler=" + (paperApiHandler != null) + 
                    ", sessionManager=" + (sessionManager != null));
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        // Reload when tab becomes visible
        if (isVisibleToUser && rootView != null && paperApiHandler != null && sessionManager != null) {
            Log.d("MyPaperFragment", "Tab became visible, reloading papers");
            loadPapersFromAPI(rootView);
        }
    }

    private void loadPapersFromAPI(@NonNull View view) {
        if (!Connectivity.isInternetAvailable(requireContext())) {
            Toast.makeText(requireContext(), "No internet connection. Using mock data.", Toast.LENGTH_SHORT).show();
            loadMockData(view);
            return;
        }

        // Get current user ID
        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            Log.e("MyPaperFragment", "User ID is null or empty");
            Toast.makeText(requireContext(), "User not logged in. Using mock data.", Toast.LENGTH_SHORT).show();
            loadMockData(view);
            return;
        }

        Log.d("MyPaperFragment", "Loading papers for userId: " + userId);
        
        // userId từ SessionManager đã được encode từ backend khi login
        // Không cần encode lại, sử dụng trực tiếp
        Log.d("MyPaperFragment", "Using userId directly (already encoded from backend): " + userId);
        
        // Load papers by user ID from API
        paperApiHandler.getPapersByUserId(userId, new ApiCallback<List<PaperResearch>>() {
            @Override
            public void onSuccess(List<PaperResearch> paperResearchList) {
                if (getActivity() == null) return;
                
                Log.d("MyPaperFragment", "Papers loaded successfully: " + (paperResearchList != null ? paperResearchList.size() : 0) + " papers");
                
                getActivity().runOnUiThread(() -> {
                    // Convert PaperResearch to Paper entity
                    papers = convertToPaperList(paperResearchList);
                    Log.d("MyPaperFragment", "Converted to Paper list: " + papers.size() + " papers");
                    
                    // Create summary from papers
                    Summary summary = createSummary(papers);
                    item = new MyPaperItem(papers, null, summary);
                    
                    // Update UI
                    buildStatisticCards(getLayoutInflater(), view);
                    buildMainContent(view);
                });
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    Log.e("MyPaperFragment", "Error loading papers: " + error);
                    Log.e("MyPaperFragment", "userId used: " + sessionManager.getUserId());
                    Log.e("MyPaperFragment", "encoded userId: " + (sessionManager.getUserId() != null ? EncoderUtils.encode(sessionManager.getUserId()) : "null"));
                    Toast.makeText(requireContext(), "Failed to load papers: " + error, Toast.LENGTH_LONG).show();
                    // Fallback to mock data
                    loadMockData(view);
                });
            }
        });
    }

    private List<Paper> convertToPaperList(List<PaperResearch> paperResearchList) {
        if (paperResearchList == null || paperResearchList.isEmpty()) {
            return new ArrayList<>();
        }
        
        return paperResearchList.stream()
                .map(this::convertToPaper)
                .collect(Collectors.toList());
    }

    private Paper convertToPaper(PaperResearch paperResearch) {
        Paper paper = new Paper();
        paper.setId(paperResearch.getId());
        paper.setTitle(paperResearch.getTitle() != null ? paperResearch.getTitle() : "Untitled");
        paper.setAuthors(paperResearch.getAuthors() != null ? paperResearch.getAuthors() : "Unknown");
        paper.setJournal(paperResearch.getJournal() != null ? paperResearch.getJournal() : "Unknown");
        paper.setYear(paperResearch.getPublicationYear() != null ? paperResearch.getPublicationYear() : 0);
        paper.setStatus("in progress"); // Default status, can be updated later
        return paper;
    }

    private Summary createSummary(List<Paper> papers) {
        Summary summary = new Summary();
        summary.setRecentlyAdded(papers.size());
        summary.setRecentlyRead(0); // TODO: Track read papers
        summary.setFavorites(0); // TODO: Track favorite papers
        return summary;
    }

    private void loadMockData(@NonNull View view) {
        item = getMockData(view);
        papers = item.getPapers() != null ? item.getPapers() : new ArrayList<>();
        buildStatisticCards(getLayoutInflater(), view);
        buildMainContent(view);
    }

    private MyPaperItem getMockData(@NonNull View view) {
        return ParseFileUtils.fromJsonAsset(
                view.getContext(),
                "feed/my-paper.json",
                new TypeToken<MyPaperItem>() {
                }.getType()
        );
    }

    // <editor-fold> desc="Build statistic cards"
    private void buildStatisticCards(@NonNull LayoutInflater inflater, @NonNull View view) {
        LinearLayout container = view.findViewById(R.id.cards);
        container.removeAllViews();

        // Get summary from item or create default
        Summary summary = (item != null && item.getSummary() != null) ? item.getSummary() : createSummary(papers);

        buildAddedPaperCard(summary, container, inflater);
        buildRecentLyReadCard(summary, container, inflater);
        buildFavoritesCard(summary, container, inflater);

    }

    private void buildAddedPaperCard(@NonNull Summary summary, LinearLayout container, @NonNull LayoutInflater inflater) {
        View card = inflater.inflate(R.layout.layout_mypaper_stat_card, container, false);
        TextView statValue = card.findViewById(R.id.tv_stat_value);
        statValue.setText(String.valueOf(summary.getRecentlyAdded()));

        TextView statLabel = card.findViewById(R.id.tv_stat_label);
        statLabel.setText(R.string.added_paper);
        container.addView(card);
    }

    private void buildRecentLyReadCard(@NonNull Summary summary, LinearLayout container, @NonNull LayoutInflater inflater) {
        View card = inflater.inflate(R.layout.layout_mypaper_stat_card, container, false);
        TextView statValue = card.findViewById(R.id.tv_stat_value);
        statValue.setText(String.valueOf(summary.getRecentlyRead()));

        TextView statLabel = card.findViewById(R.id.tv_stat_label);
        statLabel.setText(R.string.read_paper);
        container.addView(card);
    }

    private void buildFavoritesCard(@NonNull Summary summary, LinearLayout container, @NonNull LayoutInflater inflater) {
        View card = inflater.inflate(R.layout.layout_mypaper_stat_card, container, false);
        TextView statValue = card.findViewById(R.id.tv_stat_value);
        statValue.setText(String.valueOf(summary.getFavorites()));

        TextView statLabel = card.findViewById(R.id.tv_stat_label);
        statLabel.setText(R.string.favorites);
        container.addView(card);
    }
    // </editor-fold>

    private void buildMainContent(@NonNull View view) {
        ViewPager2 mainContent = view.findViewById(R.id.main_content);
        // Use papers from API or mock data
        List<Paper> papersToDisplay = papers != null && !papers.isEmpty() ? papers : 
                (item != null && item.getPapers() != null ? item.getPapers() : new ArrayList<>());
        var adapter = new MyPaperContentAdapter(requireActivity(), papersToDisplay);
        mainContent.setAdapter(adapter);
    }

    private void handleSwitchTabs(@NonNull View view) {
        switchToRecentlyAdded(view);
        switchToRecentlyRead(view);
        switchToFavorites(view);
    }

    private void switchToRecentlyAdded(View view) {
        recentlyAdded.setOnClickListener(v -> {
            Log.e("Hehe", "switchToRecentlyAdded");
            recentlyAdded.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.blue));
            recentlyAdded.setTextColor(ContextCompat.getColor(view.getContext(), R.color.white));

            changeRecentlyReadTabColorIntoDefault(view);
            changeFavoritesTabColorIntoDefault(view);
            buildMainContent(view);
        });
    }

    private void switchToRecentlyRead(View view) {
        recentlyRead.setOnClickListener(v -> {
            Log.e("Hehe", "switchToRecentlyRead");
            recentlyRead.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.blue));
            recentlyRead.setTextColor(ContextCompat.getColor(view.getContext(), R.color.white));

            changeRecentlyAddedTabColorIntoDefault(view);
            changeFavoritesTabColorIntoDefault(view);
            buildMainContent(view);
        });
    }

    private void switchToFavorites(View view) {
        favorites.setOnClickListener(v -> {
            Log.e("Hehe", "switchToFavorites");
            favorites.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.blue));
            favorites.setTextColor(ContextCompat.getColor(view.getContext(), R.color.white));

            changeRecentlyReadTabColorIntoDefault(view);
            changeRecentlyAddedTabColorIntoDefault(view);
            buildMainContent(view);
        });
    }

    private void changeRecentlyAddedTabColorIntoDefault(@NonNull View view) {
        recentlyAdded.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.dark_gray));
        recentlyAdded.setTextColor(ContextCompat.getColor(view.getContext(), R.color.black));
    }

    private void changeRecentlyReadTabColorIntoDefault(@NonNull View view) {
        recentlyRead.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.dark_gray));
        recentlyRead.setTextColor(ContextCompat.getColor(view.getContext(), R.color.black));
    }

    private void changeFavoritesTabColorIntoDefault(@NonNull View view) {
        favorites.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.dark_gray));
        favorites.setTextColor(ContextCompat.getColor(view.getContext(), R.color.black));
    }
}
