package com.se1853_jv.labverse.presentation.paper;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.presentation.common.adapter.DetailsAdapter;
import com.se1853_jv.labverse.presentation.feed.FeedActivity;
import com.se1853_jv.labverse.presentation.user.UserActivity;
import com.se1853_jv.labverse.presentation.common.BaseActivity;
import com.se1853_jv.labverse.presentation.paper.adapter.DetailsAdapter;

@RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
public class PaperDetailsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_common_ui_details);
        setupBottomNavbar(findViewById(R.id.details), R.id.bottom_navbar);

        TextView headerTv = findViewById(R.id.title);
        headerTv.setText(ContextCompat.getString(this, R.string.paper_details));

        ViewPager2 pager2 = findViewById(R.id.viewPager);
        var adapter = new DetailsAdapter(PaperDetailsActivity.this);
        pager2.setAdapter(adapter);

        // Setup bottom navigation
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        // Profile navigation
        View navProfile = findViewById(R.id.nav_profile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(PaperDetailsActivity.this, UserActivity.class);
                startActivity(intent);
            });
        }

        // Home navigation
        View navHome = findViewById(R.id.nav_home);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(PaperDetailsActivity.this, FeedActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            });
        }
        View header = findViewById(R.id.title_view);
        ImageButton backBtn = header.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(v -> finish());
    }
}
