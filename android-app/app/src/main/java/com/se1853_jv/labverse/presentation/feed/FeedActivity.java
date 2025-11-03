package com.se1853_jv.labverse.presentation.feed;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.reflect.TypeToken;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.utils.ParseFileUtils;
import com.se1853_jv.labverse.presentation.feed.adapter.TabAdapter;
import com.se1853_jv.labverse.presentation.feed.entity.DiscoveryItem;

import java.util.List;

public class FeedActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_common_ui_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.feedActivity), (v, insets) -> {
            var statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(statusBar.left, statusBar.top, statusBar.right, statusBar.bottom);
            return insets;
        });
        getMockData();
        setupTabs();
    }

    private void setupTabs() {
        ViewPager2 pager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayoutPaper);

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.d("TabSelected", "Tab được chọn: " + position);
            }
        });

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
        if (items == null) return;
        for (DiscoveryItem item : items) {
            Log.d("MockData", item.toString());
        }
    }
}
