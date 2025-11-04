package com.se1853_jv.labverse.presentation.paper.fragments;

import static com.se1853_jv.labverse.data.Constants.PAPER_ENDPOINT_URL;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaperDetailsFragment extends Fragment {
    Toolbar toolbar;
    TextView tvPaperTitle, tvPaperAuthors, tvPaperJournal;
    TabLayout tabLayout;
    final PaperApiHandler paperApiHandler;
    final TagApiHandler tagApiHandler;
    ViewPager2 viewPager;
    LinearLayout tagsList;

    public PaperDetailsFragment() {
        this.paperApiHandler = new PaperApiHandler();
        this.tagApiHandler = new TagApiHandler();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_paper_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindingView(view);

        var pr = new AtomicReference<PaperResearch>();
        Future<Boolean> apiStatus = Executors
                .newSingleThreadExecutor()
                .submit(() -> Connectivity.isApiActive(PAPER_ENDPOINT_URL.concat("health")));
        Future<Boolean> internetStatus = Executors
                .newSingleThreadExecutor()
                .submit(() -> Connectivity.isInternetAvailable(view.getContext()));

        try {
            if (!internetStatus.get()) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    var db = DatabaseClient.getInstance(view.getContext()).getAppDatabase();
                    var id = requireActivity().getIntent().getStringExtra("id");
                    pr.set(db.paperRepository().getById(id));
                });
            } else if (apiStatus.get()) {
                setupToolbar();
                setupTabs();
                displayPaperData(pr.get());
            } else if (!apiStatus.get()) {
                hidingView(view);
                LinearLayout parent = view.findViewById(R.id.contentLayout);
                parent.addView(createNoDataTitle(view));
            }
        } catch (ExecutionException | InterruptedException e) {
            Log.e("asdasd", e.getMessage(), e);
            hidingView(view);
            LinearLayout parent = view.findViewById(R.id.contentLayout);
            parent.addView(createNoDataTitle(view));
        }
    }

    private void bindingView(@NonNull View view) {
        viewPager = view.findViewById(R.id.viewPager);
        tagsList = view.findViewById(R.id.layoutTags);
        toolbar = view.findViewById(R.id.toolbarPaperDetails);
        tvPaperTitle = view.findViewById(R.id.tvPaperTitle);
        tvPaperAuthors = view.findViewById(R.id.tvPaperAuthors);
        tvPaperJournal = view.findViewById(R.id.tvPaperJournal);
        tabLayout = view.findViewById(R.id.tabLayoutPaper);
    }

    private void hidingView(@NonNull View view) {
        viewPager.setVisibility(View.GONE);

        LinearLayout header = view.findViewById(R.id.layoutPaperInfo);
        header.setVisibility(View.GONE);

        LinearLayout tabSection = view.findViewById(R.id.tab_section);
        tabSection.setVisibility(View.GONE);
    }

    @NonNull
    private TextView createNoDataTitle(@NonNull View view) {
        TextView tv = new TextView(view.getContext());
        tv.setText(ContextCompat.getString(view.getContext(), R.string.no_data));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER;
        tv.setLayoutParams(params);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(30);
        return tv;
    }

    private void bindingData(@NonNull PaperResearch data) {
        tvPaperTitle.setText(data.getTitle());
        tvPaperAuthors.setText(data.getAuthors());
        tvPaperJournal.setText(data.getJournal());
    }

    private void setupToolbar() {
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        var actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            requireActivity().finish();
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
                var adapter = new PaperTabsAdapter(requireActivity(), data.getDescription());
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
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireActivity(), "Error when fetching data", Toast.LENGTH_SHORT).show());
            }
        });

        // Ném dữ liệu cho phần chip tag của header
        tagApiHandler.getTagsByPaperId("YjNjZGU2YTUtYWYyYi00ZDJjLTljYWYtN2UxODY3ZDY3OWI4", new ApiCallback<>() {
            @Override
            public void onSuccess(List<Tag> data) {
                int[] bg = {R.drawable.bg_tag_blue, R.drawable.bg_tag_purple, R.drawable.bg_tag_green, R.drawable.bg_tag_skin};
                int[] textColor = {R.color.blue, R.color.purple, R.color.fourth_green, R.color.yellow};

                if (data == null) {
                    var tv = new TextView(requireActivity());
                    tv.setId(View.generateViewId());
                    tv.setText(R.string.no_data);
                } else {
                    for (var tag : data) {
                        // tạo chip cho tag
                        var tv = new TextView(requireActivity());

                        // Lấy vị trí màu sắc tương ứng
                        var position = data.indexOf(tag) > bg.length ? data.indexOf(tag) % bg.length : data.indexOf(tag);
                        tv.setId(View.generateViewId());
                        tv.setText(tag.getName());
                        tv.setBackground(ContextCompat.getDrawable(requireActivity(), bg[position]));

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
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireActivity(), "Error when fetching data", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
