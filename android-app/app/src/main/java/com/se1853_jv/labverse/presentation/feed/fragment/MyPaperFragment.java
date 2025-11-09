package com.se1853_jv.labverse.presentation.feed.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.gson.reflect.TypeToken;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.utils.ParseFileUtils;
import com.se1853_jv.labverse.presentation.feed.adapter.MyPaperContentAdapter;
import com.se1853_jv.labverse.presentation.feed.entity.MyPaperItem;
import com.se1853_jv.labverse.presentation.feed.entity.Summary;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class MyPaperFragment extends Fragment {
    MyPaperItem item;
    Button recentlyAdded, recentlyRead, favorites;
    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_mypaper, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recentlyAdded = view.findViewById(R.id.recently_added);
        favorites = view.findViewById(R.id.favorites);
        recentlyRead = view.findViewById(R.id.recently_read);

        this.view = view;
        item = getMockData();

        buildStatisticCards(getLayoutInflater());
        buildMainContent();
        handleSwitchTabs();
    }

    private MyPaperItem getMockData() {
        return ParseFileUtils.fromJsonAsset(
                view.getContext(),
                "feed/my-paper.json",
                new TypeToken<MyPaperItem>() {
                }.getType()
        );
    }

    // <editor-fold> desc="Build statistic cards"
    private void buildStatisticCards(@NonNull LayoutInflater inflater) {
        LinearLayout container = view.findViewById(R.id.cards);
        container.removeAllViews();

        var context = view.getContext();

        buildPapersCard(item.getSummary(), container, inflater, context);
        buildCollectionsCard(item.getSummary(), container, inflater, context);
        buildTeamProjectsCard(item.getSummary(), container, inflater, context);
    }

    private void buildPapersCard(@NonNull Summary summary, LinearLayout container, @NonNull LayoutInflater inflater, @NonNull Context context) {
        View card = inflater.inflate(R.layout.layout_mypaper_stat_card, container, false);
        card.setBackgroundColor(ContextCompat.getColor(context, R.color.blue_50));

        TextView statValue = card.findViewById(R.id.tv_stat_value);
        statValue.setText(String.valueOf(summary.getPapers()));
        statValue.setTextColor(ContextCompat.getColor(context, R.color.blue_600));

        TextView statLabel = card.findViewById(R.id.tv_stat_label);
        statLabel.setText(R.string.papers);
        statLabel.setTextColor(ContextCompat.getColor(context, R.color.blue_400));
        container.addView(card);
    }

    private void buildCollectionsCard(@NonNull Summary summary, LinearLayout container, @NonNull LayoutInflater inflater, @NonNull Context context) {
        View card = inflater.inflate(R.layout.layout_mypaper_stat_card, container, false);
        card.setBackgroundColor(ContextCompat.getColor(context, R.color.first_green));

        TextView statValue = card.findViewById(R.id.tv_stat_value);
        statValue.setText(String.valueOf(summary.getCollections()));
        statValue.setTextColor(ContextCompat.getColor(context, R.color.seventh_green));

        TextView statLabel = card.findViewById(R.id.tv_stat_label);
        statLabel.setText(R.string.collections);
        statLabel.setTextColor(ContextCompat.getColor(context, R.color.third_green));
        container.addView(card);
    }

    private void buildTeamProjectsCard(@NonNull Summary summary, LinearLayout container, @NonNull LayoutInflater inflater, @NonNull Context context) {
        View card = inflater.inflate(R.layout.layout_mypaper_stat_card, container, false);
        card.setBackgroundColor(ContextCompat.getColor(context, R.color.skin));

        TextView statValue = card.findViewById(R.id.tv_stat_value);
        statValue.setText(String.valueOf(summary.getTeamProjects()));
        statValue.setTextColor(ContextCompat.getColor(context, R.color.yellow));

        TextView statLabel = card.findViewById(R.id.tv_stat_label);
        statLabel.setText(R.string.team_projects);
        statLabel.setTextColor(ContextCompat.getColor(context, R.color.brown));
        container.addView(card);
    }
    // </editor-fold>

    private void buildMainContent() {
        ViewPager2 mainContent = view.findViewById(R.id.main_content);
        var adapter = new MyPaperContentAdapter(requireActivity(), item.getPapers());
        mainContent.setAdapter(adapter);
    }

    private void handleSwitchTabs() {
        switchToRecentlyAdded();
        switchToRecentlyRead();
        switchToFavorites();
    }

    private void switchToRecentlyAdded() {
        recentlyAdded.setOnClickListener(v -> {
            Log.e("Hehe", "switchToRecentlyAdded");
            recentlyAdded.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.blue_400));
            recentlyAdded.setTextColor(ContextCompat.getColor(view.getContext(), R.color.white));

            changeRecentlyReadTabColorIntoDefault();
            changeFavoritesTabColorIntoDefault();
            buildMainContent();
        });
    }

    private void switchToRecentlyRead() {
        recentlyRead.setOnClickListener(v -> {
            Log.e("Hehe", "switchToRecentlyRead");
            recentlyRead.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.blue_400));
            recentlyRead.setTextColor(ContextCompat.getColor(view.getContext(), R.color.white));

            changeRecentlyAddedTabColorIntoDefault();
            changeFavoritesTabColorIntoDefault();
            buildMainContent();
        });
    }

    private void switchToFavorites() {
        favorites.setOnClickListener(v -> {
            Log.e("Hehe", "switchToFavorites");
            favorites.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.blue_400));
            favorites.setTextColor(ContextCompat.getColor(view.getContext(), R.color.white));

            changeRecentlyReadTabColorIntoDefault();
            changeRecentlyAddedTabColorIntoDefault();
            buildMainContent();
        });
    }

    private void changeRecentlyAddedTabColorIntoDefault() {
        recentlyAdded.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.gray_200));
        recentlyAdded.setTextColor(ContextCompat.getColor(view.getContext(), R.color.black));
    }

    private void changeRecentlyReadTabColorIntoDefault() {
        recentlyRead.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.gray_200));
        recentlyRead.setTextColor(ContextCompat.getColor(view.getContext(), R.color.black));
    }

    private void changeFavoritesTabColorIntoDefault() {
        favorites.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.gray_200));
        favorites.setTextColor(ContextCompat.getColor(view.getContext(), R.color.black));
    }
}
