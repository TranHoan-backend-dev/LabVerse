package com.se1853_jv.labverse.presentation.feed;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.presentation.feed.adapter.FeedAdapter;
import com.se1853_jv.labverse.presentation.user.UserActivity;

public class FeedActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_common_home);

        ViewPager2 pager2 = findViewById(R.id.viewPager1);
        FeedAdapter adapter = new FeedAdapter(FeedActivity.this);
        pager2.setAdapter(adapter);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.feedActivity), (v, insets) -> {
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
    }
}
