package com.se1853_jv.labverse.presentation.paper;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.paper.PaperApiHandler;
import com.se1853_jv.labverse.data.utils.Connectivity;
import com.se1853_jv.labverse.domain.db.DatabaseClient;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;
import com.se1853_jv.labverse.presentation.paper.adapter.PaperTabsAdapter;
import com.se1853_jv.labverse.presentation.paper.fragments.OverviewFragment;

@RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
public class PaperDetailsActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView tvPaperTitle, tvPaperAuthors, tvPaperJournal;
    private TabLayout tabLayout;
    private final PaperApiHandler apiHandler;

    public PaperDetailsActivity() {
        this.apiHandler = new PaperApiHandler();
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

        bindViews();
        setupToolbar();
        setupTabs();
        displayPaperData(pr);
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

    @SuppressLint("SetTextI18n")
    private void displayPaperData(PaperResearch paper) {
        if (paper != null) {
            tvPaperTitle.setText(paper.getTitle());
            tvPaperAuthors.setText(paper.getAuthors());
            tvPaperJournal.setText(paper.getPublicationYear() + " • " + paper.getJournal());
        }
        // co dinh noi dung ban dau la "Khong co du lieu"
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

        apiHandler.getPaperDetails("YjNjZGU2YTUtYWYyYi00ZDJjLTljYWYtN2UxODY3ZDY3OWI4", new ApiCallback<>() {
            @Override
            public void onSuccess(PaperResearch data) {
                var adapter = new PaperTabsAdapter(PaperDetailsActivity.this, data.getDescription());
                viewPager.setAdapter(adapter);

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


    }
}
