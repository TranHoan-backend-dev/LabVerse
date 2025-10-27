package com.se1853_jv.labverse.presentation.paper;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.paper.PaperApiHandler;
import com.se1853_jv.labverse.data.api.tag.TagApiHandler;
import com.se1853_jv.labverse.data.utils.Connectivity;
import com.se1853_jv.labverse.domain.db.DatabaseClient;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;
import com.se1853_jv.labverse.domain.infrastructure.tag.model.Tag;
import com.se1853_jv.labverse.presentation.paper.adapter.PaperTabsAdapter;

import java.util.List;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaperDetailsActivity extends AppCompatActivity {
    Toolbar toolbar;
    TextView tvPaperTitle, tvPaperAuthors, tvPaperJournal;
    TabLayout tabLayout;
    final PaperApiHandler paperApiHandler;
    final TagApiHandler tagApiHandler;

    public PaperDetailsActivity() {
        this.paperApiHandler = new PaperApiHandler();
        this.tagApiHandler = new TagApiHandler();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper_details);
        PaperResearch pr = null;
        if (!Connectivity.isInternetAvailable(this)) {
            if (!Connectivity.isApiActive("")) {
                var db = DatabaseClient.getInstance(this).getAppDatabase();
                var id = getIntent().getStringExtra("id");
                pr = db.paperRepository().getById(id);
            } else {
                pr = new PaperResearch();
            }
        }

        bindingViews();
        setupToolbar();
        setupTabs();
        displayPaperData(pr);
    }

    private void bindingViews() {
        toolbar = findViewById(R.id.toolbarPaperDetails);
        tvPaperTitle = findViewById(R.id.tvPaperTitle);
        tvPaperAuthors = findViewById(R.id.tvPaperAuthors);
        tvPaperJournal = findViewById(R.id.tvPaperJournal);
        tabLayout = findViewById(R.id.tabLayoutPaper);
    }

    private void bindingData(@NonNull PaperResearch data) {
        tvPaperTitle.setText(data.getTitle());
        tvPaperAuthors.setText(data.getAuthors());
        tvPaperJournal.setText(data.getJournal());
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

    @SuppressLint("SetTextI18n")
    private void displayPaperData(PaperResearch paper) {
        if (paper != null) {
            tvPaperTitle.setText(paper.getTitle());
            tvPaperAuthors.setText(paper.getAuthors());
            tvPaperJournal.setText(paper.getPublicationYear() + " • " + paper.getJournal());
        }
    }

    /**
     * Trình bày nội dung cho từng tab và trình bày dữ liệu cho header
     */
    private void setupTabs() {
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        LinearLayout tagsList = findViewById(R.id.layoutTags);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.d("TabSelected", "Tab được chọn: " + position);
            }
        });

        paperApiHandler.getPaperDetails("YjNjZGU2YTUtYWYyYi00ZDJjLTljYWYtN2UxODY3ZDY3OWI4", new ApiCallback<>() {
            @Override
            public void onSuccess(PaperResearch data) {
                // trình bày nội dung cho mô tả paper
                bindingData(data);

                // ném dữ liệu cho tab overview
                var adapter = new PaperTabsAdapter(PaperDetailsActivity.this, data.getDescription());
                viewPager.setAdapter(adapter);

                // trình bày nội dung tiêu đề cho các tab
                new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Overview");
                            break;
                        case 1:
                            tab.setText("References");
                            break;
                        case 2:
                            tab.setText("Citation");
                            break;
                    }
                }).attach();
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                        Toast.makeText(PaperDetailsActivity.this, "Error when fetching data", Toast.LENGTH_SHORT).show());
            }
        });

        // Ném dữ liệu cho phần chip tag của header
        tagApiHandler.getTagsByPaperId("YjNjZGU2YTUtYWYyYi00ZDJjLTljYWYtN2UxODY3ZDY3OWI4", new ApiCallback<>() {
            @Override
            public void onSuccess(List<Tag> data) {
                int[] bg = {R.drawable.bg_tag_blue, R.drawable.bg_tag_purple, R.drawable.bg_tag_green, R.drawable.bg_tag_skin};
                int[] textColor = {R.color.blue, R.color.purple, R.color.fourth_green, R.color.yellow};

                if (data == null) {
                    var tv = new TextView(PaperDetailsActivity.this);
                    tv.setId(View.generateViewId());
                    tv.setText(R.string.no_data);
                } else {
                    for (var tag : data) {
                        // tạo chip cho tag
                        var tv = new TextView(PaperDetailsActivity.this);

                        // Lấy vị trí màu sắc tương ứng
                        var position = data.indexOf(tag) > bg.length ? data.indexOf(tag) % bg.length : data.indexOf(tag);
                        tv.setId(View.generateViewId());
                        tv.setText(tag.getName());
                        tv.setBackground(ContextCompat.getDrawable(PaperDetailsActivity.this, bg[position]));

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(0, 0, 8, 0);
                        tv.setLayoutParams(params);

                        var horizontalPadding = (int) TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()
                        );
                        var verticalPadding = (int) TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()
                        );
                        tv.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);

                        tv.setTextSize(12);
                        tv.setTextColor(textColor[position]);

                        tagsList.addView(tv);
                    }
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                        Toast.makeText(PaperDetailsActivity.this, "Error when fetching data", Toast.LENGTH_SHORT).show());
            }
        });
    }

}
