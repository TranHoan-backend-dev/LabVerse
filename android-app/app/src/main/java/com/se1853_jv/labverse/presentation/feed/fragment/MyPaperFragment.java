package com.se1853_jv.labverse.presentation.feed.fragment;

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

        item = getMockData(view);

        buildStatisticCards(getLayoutInflater(), view);
        buildMainContent(view);
        handleSwitchTabs(view);
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

        buildAddedPaperCard(item.getSummary(), container, inflater);
        buildRecentLyReadCard(item.getSummary(), container, inflater);
        buildFavoritesCard(item.getSummary(), container, inflater);

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
        var adapter = new MyPaperContentAdapter(requireActivity(), item.getPapers());
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
