package com.se1853_jv.labverse.presentation.feed.fragment;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.tag.TagApiHandler;
import com.se1853_jv.labverse.domain.infrastructure.tag.model.Tag;
import com.se1853_jv.labverse.presentation.feed.tabs.TabAdapter;

import java.util.List;

public class FeedFragment extends Fragment {
    private TabLayout tabLayout;
    private final TagApiHandler apiHandler;

    public FeedFragment() {
        this.apiHandler = new TagApiHandler();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayoutPaper);
        bindingDataForTagChips();
        setupTabs(viewPager);
    }

    private void setupTabs(ViewPager2 viewPager) {
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.d("TabSelected", "Tab được chọn: " + position);
            }
        });

        var adapter = new TabAdapter(requireActivity());
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
                    requireActivity().runOnUiThread(() -> {
                        LinearLayout view = requireActivity().findViewById(R.id.tags_view);
                        view.removeAllViews();

                        for (var tag : data) {
                            var tv = new TextView(requireActivity());
                            tv.setId(View.generateViewId());
                            tv.setText(tag.getName());
                            tv.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.bg_chip_gray));
                            tv.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black));
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
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireActivity(), "Error when fetching data", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
