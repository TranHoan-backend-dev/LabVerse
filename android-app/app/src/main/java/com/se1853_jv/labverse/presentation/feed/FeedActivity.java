package com.se1853_jv.labverse.presentation.feed;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.presentation.common.HeaderHelper;
import com.se1853_jv.labverse.presentation.feed.adapter.ContentAdapter;
import com.se1853_jv.labverse.presentation.feed.adapter.TabAdapter;

public class FeedActivity extends AppCompatActivity {
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_common_home);
        tabLayout = findViewById(R.id.tabLayoutPaper);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.feedActivity), (v, insets) -> {
            var statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(statusBar.left, statusBar.top, statusBar.right, statusBar.bottom);
            return insets;
        });

        ViewPager2 pager2 = findViewById(R.id.viewPagerPaper);
        setupTabs(pager2);

        ViewPager2 pager3 = findViewById(R.id.viewPager);
        ContentAdapter contentAdapter = new ContentAdapter(FeedActivity.this);
        pager3.setAdapter(contentAdapter);

        // Setup avatar and profile navigation click listeners
        HeaderHelper.setupProfileClickListeners(this);
        // Setup Lists navigation click listener
        HeaderHelper.setupListsNavigationClickListener(this);

    }

    private void setupTabs(@NonNull ViewPager2 viewPager) {
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.d("TabSelected", "Tab được chọn: " + position);
            }
        });

        var adapter = new TabAdapter(FeedActivity.this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
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

}
