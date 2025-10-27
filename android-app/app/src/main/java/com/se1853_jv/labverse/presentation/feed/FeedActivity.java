package com.se1853_jv.labverse.presentation.feed;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.tag.TagApiHandler;
import com.se1853_jv.labverse.domain.infrastructure.tag.model.Tag;
import com.se1853_jv.labverse.presentation.feed.tabs.TabAdapter;

import java.util.List;

public class FeedActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private final TagApiHandler apiHandler;

    public FeedActivity() {
        this.apiHandler = new TagApiHandler();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.feedActivity), (v, insets) -> {
            var statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(statusBar.left, statusBar.top, statusBar.right, statusBar.bottom);
            return insets;
        });
        bindViews();
        bindingDataForTagChips();
        setupTabs();
    }

    private void bindViews() {
        tabLayout = findViewById(R.id.tabLayoutPaper);
    }

    private void setupTabs() {
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.d("TabSelected", "Tab được chọn: " + position);
            }
        });

        var adapter = new TabAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Discovery");
                    break;
                case 1:
                    tab.setText("Saved");
                    break;
                case 2:
                    tab.setText("Team");
                    break;
            }
        }).attach();
    }

    private void bindingDataForTagChips() {
        apiHandler.getTheFiveMostPopularTags(new ApiCallback<>() {
            @Override
            public void onSuccess(List<Tag> data) {
                if (data != null) {
                    runOnUiThread(() -> {
                        LinearLayout view = findViewById(R.id.tags_view);
                        view.removeAllViews();

                        for (var tag : data) {
                            var tv = new TextView(FeedActivity.this);
                            tv.setId(View.generateViewId());
                            tv.setText(tag.getName());
                            tv.setBackground(ContextCompat.getDrawable(FeedActivity.this, R.drawable.bg_chip_gray));
                            tv.setTextColor(ContextCompat.getColor(FeedActivity.this, R.color.black));
                            tv.setTextSize(12);
                            tv.setPaddingRelative(10, 4, 10, 4);
                            tv.setBackgroundTintMode(PorterDuff.Mode.SRC_IN);

                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            params.setMarginEnd(8);
                            tv.setLayoutParams(params);

                            view.addView(tv);
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                        Toast.makeText(FeedActivity.this, "Error when fetching data", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
