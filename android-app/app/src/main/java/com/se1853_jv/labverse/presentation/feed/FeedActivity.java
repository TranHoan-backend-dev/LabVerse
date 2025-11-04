package com.se1853_jv.labverse.presentation.feed;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.reflect.TypeToken;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.presentation.feed.adapter.FeedAdapter;
import com.se1853_jv.labverse.presentation.user.UserActivity;
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

        // Setup bottom navigation
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        // Profile navigation
        View navProfile = findViewById(R.id.nav_profile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(FeedActivity.this, UserActivity.class);
                startActivity(intent);
            });
        }

        // Home navigation (already on home, just scroll to top if needed)
        View navHome = findViewById(R.id.nav_home);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                // Already on home screen, do nothing or scroll to top
            });
        }
        setupBottomNavbar(findViewById(R.id.ui_home), R.id.bottomNav);
        setupTabs();
        getMockData();
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
}
