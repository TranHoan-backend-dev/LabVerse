package com.se1853_jv.labverse.presentation.paper;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.presentation.paper.adapter.ImportBibtexAdapter;

public class ImportPaperByBibtexActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_common_ui_details);

        setupToolbarAndNavbar();
        setupViewPager();
        handleBackEvent();
    }

    private void setupToolbarAndNavbar() {
        View header = findViewById(R.id.title_view);
        header.setBackgroundColor(ContextCompat.getColor(header.getContext(), R.color.purple));

        TextView title = header.findViewById(R.id.title);
        title.setText(getResources().getString(R.string.import_bibtex));

        ImageView backBtn = header.findViewById(R.id.back_btn);
        backBtn.setBackgroundColor(ContextCompat.getColor(header.getContext(), R.color.purple));

        ImageView rightBtn = header.findViewById(R.id.right_btn);
        rightBtn.setImageDrawable(ContextCompat.getDrawable(header.getContext(), R.drawable.ic_help_circle));
        rightBtn.setColorFilter(ContextCompat.getColor(header.getContext(), android.R.color.white), PorterDuff.Mode.SRC_IN);

        View bottomView = findViewById(R.id.view);
        bottomView.setVisibility(View.GONE);

        View bottomNavbar = findViewById(R.id.bottom_navbar);
        bottomNavbar.setVisibility(View.GONE);
    }

    private void handleBackEvent() {
        View header = findViewById(R.id.title_view);
        ImageButton btn = header.findViewById(R.id.back_btn);
        btn.setOnClickListener(v -> finish());
    }

    private void setupViewPager() {
        ViewPager2 pager2 = findViewById(R.id.viewPager);
        var adapter = new ImportBibtexAdapter(this);
        pager2.setAdapter(adapter);
    }
}
