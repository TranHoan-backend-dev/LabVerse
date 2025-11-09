package com.se1853_jv.labverse.presentation.feed;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.reflect.TypeToken;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.utils.ParseFileUtils;
import com.se1853_jv.labverse.presentation.common.BaseActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.se1853_jv.labverse.presentation.feed.adapter.TabAdapter;
import com.se1853_jv.labverse.presentation.feed.entity.DiscoveryItem;
import com.se1853_jv.labverse.presentation.paper.ImportPaperManuallyActivity;
import com.se1853_jv.labverse.presentation.team.TeamListActivity;

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
        setupImportPaperButton();
        getMockData();
    }

    private TabLayoutMediator mediator;
    private ViewPager2 pager;
    private TabLayout tabLayout;

    private void setupTabs() {
        pager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayoutPaper);

        if (pager == null || tabLayout == null) {
            Log.e("FeedActivity", "ViewPager or TabLayout not found");
            return;
        }

        var adapter = new TabAdapter(FeedActivity.this);
        pager.setAdapter(adapter);

        // Set up TabLayoutMediator
        mediator = new TabLayoutMediator(tabLayout, pager, (tab, position) -> {
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
        });
        mediator.attach();

        // Add tab selection listener to intercept Teams tab clicks
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                try {
                    int position = tab.getPosition();
                    if (position == 2) { // Teams tab
                        // Temporarily detach mediator to prevent ViewPager sync
                        try {
                            if (mediator != null) {
                                mediator.detach();
                            }
                        } catch (Exception e) {
                            Log.e("FeedActivity", "Error detaching mediator: " + e.getMessage());
                        }
                        
                        // Get current page before navigation
                        final int currentPage;
                        if (pager != null) {
                            int tempPage = pager.getCurrentItem();
                            currentPage = (tempPage == 2) ? 0 : tempPage; // If already on Teams, go to Discovery
                        } else {
                            currentPage = 0;
                        }
                        
                        // Navigate to TeamListActivity
                        Intent intent = new Intent(FeedActivity.this, TeamListActivity.class);
                        startActivity(intent);
                        
                        // Reset ViewPager and tab selection after navigation
                        if (tabLayout != null && pager != null) {
                            final ViewPager2 finalPager = pager;
                            final TabLayout finalTabLayout = tabLayout;
                            final TabLayoutMediator finalMediator = mediator;
                            
                            tabLayout.post(() -> {
                                try {
                                    // Reset ViewPager to previous page
                                    if (finalPager != null) {
                                        finalPager.setCurrentItem(currentPage, false);
                                    }
                                    
                                    // Reset tab selection to previous tab
                                    if (finalTabLayout != null) {
                                        TabLayout.Tab prevTab = finalTabLayout.getTabAt(currentPage);
                                        if (prevTab != null) {
                                            finalTabLayout.selectTab(prevTab, false);
                                        }
                                    }
                                    
                                    // Reattach mediator
                                    if (finalMediator != null) {
                                        finalMediator.attach();
                                    }
                                } catch (Exception e) {
                                    Log.e("FeedActivity", "Error resetting tabs: " + e.getMessage());
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    Log.e("FeedActivity", "Error in onTabSelected: " + e.getMessage(), e);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Do nothing
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                try {
                    int position = tab.getPosition();
                    if (position == 2) { // Teams tab
                        // Navigate to TeamListActivity when reselected
                        Intent intent = new Intent(FeedActivity.this, TeamListActivity.class);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    Log.e("FeedActivity", "Error in onTabReselected: " + e.getMessage(), e);
                }
            }
        });

        // Prevent ViewPager from swiping to Teams tab (position 2)
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                try {
                    if (position == 2) { // Teams tab
                        // Temporarily detach mediator
                        try {
                            if (mediator != null) {
                                mediator.detach();
                            }
                        } catch (Exception e) {
                            Log.e("FeedActivity", "Error detaching mediator in page callback: " + e.getMessage());
                        }
                        
                        // Navigate to TeamListActivity
                        Intent intent = new Intent(FeedActivity.this, TeamListActivity.class);
                        startActivity(intent);
                        
                        // Go back to previous page
                        if (pager != null && tabLayout != null) {
                            pager.post(() -> {
                                try {
                                    if (pager != null) {
                                        pager.setCurrentItem(0, false);
                                    }
                                    
                                    // Reset tab selection
                                    if (tabLayout != null) {
                                        TabLayout.Tab prevTab = tabLayout.getTabAt(0);
                                        if (prevTab != null) {
                                            tabLayout.selectTab(prevTab, false);
                                        }
                                    }
                                    
                                    // Reattach mediator
                                    if (mediator != null) {
                                        mediator.attach();
                                    }
                                } catch (Exception e) {
                                    Log.e("FeedActivity", "Error resetting in page callback: " + e.getMessage());
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    Log.e("FeedActivity", "Error in onPageSelected: " + e.getMessage(), e);
                }
            }
        });
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

    private static final int REQUEST_CODE_IMPORT_PAPER = 1001;
    
    private void setupImportPaperButton() {
        FloatingActionButton fabImportPaper = findViewById(R.id.fabImportPaper);
        if (fabImportPaper != null) {
            fabImportPaper.setOnClickListener(v -> {
                Intent intent = new Intent(FeedActivity.this, ImportPaperManuallyActivity.class);
                startActivityForResult(intent, REQUEST_CODE_IMPORT_PAPER);
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMPORT_PAPER && resultCode == RESULT_OK) {
            // Paper was uploaded successfully, reload My Papers tab
            Log.d("FeedActivity", "Paper uploaded, reloading My Papers tab");
            // Switch to My Papers tab and trigger reload
            if (pager != null) {
                pager.setCurrentItem(1, false); // Switch to My Papers tab (index 1)
                // Fragment's onResume will be called automatically
            }
        }
    }
}
