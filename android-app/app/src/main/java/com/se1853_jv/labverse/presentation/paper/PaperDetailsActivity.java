package com.se1853_jv.labverse.presentation.paper;

import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.presentation.paper.adapter.DetailsAdapter;

@RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
public class PaperDetailsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_common_details);

        TextView header = findViewById(R.id.title);
        header.setText(ContextCompat.getString(this, R.string.paper_details));

        ViewPager2 pager2 = findViewById(R.id.viewPager);
        var adapter = new DetailsAdapter(PaperDetailsActivity.this);
        pager2.setAdapter(adapter);
    }
}
