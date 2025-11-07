package com.se1853_jv.labverse.presentation.feed;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.reflect.TypeToken;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.utils.ParseFileUtils;
import com.se1853_jv.labverse.presentation.common.BaseActivity;
import com.se1853_jv.labverse.presentation.feed.adapter.TabAdapter;
import com.se1853_jv.labverse.presentation.feed.entity.DiscoveryItem;

import java.util.List;

public class FeedActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_common_ui_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ui_home), (v, insets) -> {
            var statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(statusBar.left, statusBar.top, statusBar.right, statusBar.bottom);
            return insets;
        });

        setupBottomNavbar(findViewById(R.id.ui_home), R.id.bottomNav);
        setupTabs();
        getMockData();
        handleFilterPapers();
    }

    private void setupTabs() {
        ViewPager2 pager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayoutPaper);

        var adapter = new TabAdapter(FeedActivity.this);
        pager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, pager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(ContextCompat.getString(FeedActivity.this, R.string.discovery));
                    break;
                case 1:
                    tab.setText(ContextCompat.getString(FeedActivity.this, R.string.my_papers));
                    break;
                case 2:
                    tab.setText(ContextCompat.getString(FeedActivity.this, R.string.teams));
                    break;
            }
        }).attach();
    }

    private void getMockData() {
        List<DiscoveryItem> items = ParseFileUtils.fromJsonAsset(
                FeedActivity.this,
                "feed/discovery.json",
                new TypeToken<List<DiscoveryItem>>() {
                }.getType()
        );
    }

    private void handleFilterPapers() {
        View searchBar = findViewById(R.id.search_bar);
        ImageButton btn = searchBar.findViewById(R.id.btn_filter);

        btn.setOnClickListener(v -> {
            var intent = new Intent(this, FilterActivity.class);
            startActivity(intent);
        });
    }
}
