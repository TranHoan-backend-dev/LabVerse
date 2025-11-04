package com.se1853_jv.labverse.presentation.feed.fragment;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.gson.reflect.TypeToken;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.tag.TagApiHandler;
import com.se1853_jv.labverse.data.utils.ParseFileUtils;
import com.se1853_jv.labverse.domain.infrastructure.tag.model.Tag;
import com.se1853_jv.labverse.presentation.feed.entity.DiscoveryItem;

import java.util.List;

public class DiscoveryFragment extends Fragment {
    private final TagApiHandler apiHandler;
    private List<DiscoveryItem> list;
    private static int CURRENT_PAGE = 0;
    private final static int PAGE_SIZE = 5;

    private final int[] BG_COLORS = new int[]
            {
                    R.drawable.bg_chip_gray, R.drawable.bg_chip_green,
                    R.drawable.bg_chip_skin, R.drawable.bg_chip_semi_purple
            };

    private final int[] TXT_COLORS = new int[]
            {
                    R.color.black, R.color.white,
                    R.color.yellow, R.color.purple
            };

    public DiscoveryFragment() {
        this.apiHandler = new TagApiHandler();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_discovery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list = getMockData(view);
        displayDiscoveryItemCard(getLayoutInflater(), view);
        buildPagination(view);
        bindingDataForTagChips();
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

    private List<DiscoveryItem> getMockData(@NonNull View view) {
        return ParseFileUtils.fromJsonAsset(
                view.getContext(),
                "feed/discovery.json",
                new TypeToken<List<DiscoveryItem>>() {
                }.getType()
        );
    }

    private void displayDiscoveryItemCard(@NonNull LayoutInflater inflater, @NonNull View view) {
        LinearLayout container = view.findViewById(R.id.content);
        container.removeAllViews();

        if (list == null || list.isEmpty()) return;

        for (DiscoveryItem item : list.subList(CURRENT_PAGE, Math.min(list.size(), CURRENT_PAGE + PAGE_SIZE))) {
            View cardView = inflater.inflate(R.layout.layout_feed_paper_card, container, false);

            LinearLayout tagsContainer = cardView.findViewById(R.id.tags);
            tagsContainer.removeAllViews();
            if (item.getTags() != null && !item.getTags().isEmpty()) {
                for (int i = 0; i < item.getTags().size(); i++) {
                    Tag tag = item.getTags().get(i);
                    tagsContainer.addView(buildChip(cardView.getContext(), tag.getName(), i));
                }
            }

            buildTitle(item.getTitle(), cardView);
            buildDescription(item.getSummary(), cardView);
            buildMetadata(item.getJournal() + " · " + item.getTimeAgo() + " · " + item.getCitations(), cardView);
            buildFooter(item.getAuthor().getAvatarUrl(), item.getAuthor().getName(), cardView);

            container.addView(cardView);
        }
    }

    @NonNull
    private TextView buildChip(Context context, String content, int position) {
        var tv = new TextView(context);
        tv.setId(View.generateViewId());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 10, 4);
        var horizontalPadding = (int) (8 * context.getResources().getDisplayMetrics().density);
        var verticalPadding = (int) (4 * context.getResources().getDisplayMetrics().density);
        tv.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
        tv.setLayoutParams(params);

        var bgColor = BG_COLORS[position % BG_COLORS.length];
        var textColor = TXT_COLORS[position % TXT_COLORS.length];

        tv.setTextColor(ContextCompat.getColor(context, textColor));
        tv.setText(content);
        tv.setTextSize(12);
        tv.setBackground(ContextCompat.getDrawable(context, bgColor));
        return tv;
    }

    // <editor-fold> desc="build data card view"
    private void buildTitle(String title, @NonNull View parent) {
        TextView tv = parent.findViewById(R.id.title);
        tv.setText(title);
    }

    private void buildDescription(String description, @NonNull View parent) {
        TextView tv = parent.findViewById(R.id.description);
        tv.setText(description);
    }

    private void buildMetadata(String metadata, @NonNull View parent) {
        TextView tv = parent.findViewById(R.id.metadata);
        tv.setText(metadata);
    }

    private void buildFooter(String imageUrl, String username, @NonNull View parent) {
        TextView tv = parent.findViewById(R.id.username);
        tv.setText(username == null ? ContextCompat.getString(parent.getContext(), R.string.user) : username);

        System.out.println(imageUrl);

        ImageView iv = parent.findViewById(R.id.avt);
        iv.setImageResource(R.mipmap.avt_mock_round);
    }
    // </editor-fold>

    // <editor-fold> desc="pagination handling"
    private void buildPagination(@NonNull View view) {
        updatePageInfo(view);

        ImageButton btnPrev = view.findViewById(R.id.btn_prev);
        ImageButton btnNext = view.findViewById(R.id.btn_next);

        btnPrev.setOnClickListener(v -> {
            if (CURRENT_PAGE > 0) {
                CURRENT_PAGE--;
                displayDiscoveryItemCard(getLayoutInflater(), view);
                updatePageInfo(view);
            } else {
                Toast.makeText(requireContext(), "Đang ở trang đầu tiên", Toast.LENGTH_SHORT).show();
            }
        });

        btnNext.setOnClickListener(v -> {
            var numberOfPage = getNumberOfPage();
            if (CURRENT_PAGE < numberOfPage - 1) {
                CURRENT_PAGE++;
                displayDiscoveryItemCard(getLayoutInflater(), view);
                updatePageInfo(view);
            } else {
                Toast.makeText(requireContext(), "Đã đến trang cuối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePageInfo(@NonNull View view) {
        var numberOfPage = getNumberOfPage();
        var title = String.format("Page %s of %s", CURRENT_PAGE + 1, numberOfPage);

        TextView tv = view.findViewById(R.id.tv_page_info);
        tv.setText(title);
    }

    private int getNumberOfPage() {
        if (list == null || list.isEmpty()) return 1;
        return (int) Math.ceil((double) list.size() / PAGE_SIZE);
    }
    // </editor-fold>
}
