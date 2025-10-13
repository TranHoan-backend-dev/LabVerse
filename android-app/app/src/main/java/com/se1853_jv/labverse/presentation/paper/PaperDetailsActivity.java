package com.se1853_jv.labverse.presentation.paper;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;

public class PaperDetailsActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView tvPaperTitle, tvPaperAuthors, tvPaperJournal;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper_details);

        String paperId = getIntent().getStringExtra("paperId");
        Log.d("PaperDetails", "Paper ID: " + paperId);

        bindViews();
        setupToolbar();
        setupTabs();
    }

    private void bindViews() {
        toolbar = findViewById(R.id.toolbarPaperDetails);
        tvPaperTitle = findViewById(R.id.tvPaperTitle);
        tvPaperAuthors = findViewById(R.id.tvPaperAuthors);
        tvPaperJournal = findViewById(R.id.tvPaperJournal);
        tabLayout = findViewById(R.id.tabLayoutPaper);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayPaperData(PaperResearch paper) {
        tvPaperTitle.setText(paper.getTitle());
        tvPaperAuthors.setText(paper.getAuthors());
        tvPaperJournal.setText(paper.getJournal());
    }

    private void setupTabs() {
        // Ánh xạ viewPager
        ViewPager2 viewPager = findViewById(R.id.viewPager);


        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Xử lý khi tab được chọn
                Log.d("TabSelected", "Tab được chọn: " + position);
            }
        });

        // Tạo adapter
        PaperTabsAdapter adapter = new PaperTabsAdapter(this);
        viewPager.setAdapter(adapter);

        // Kết nối TabLayout với ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Overview");
                    break;
                case 1:
                    tab.setText("Citation");
                    break;
                case 2:
                    tab.setText("References");
                    break;
            }
        }).attach();
    }

}
