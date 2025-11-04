package com.se1853_jv.labverse.presentation.paper.adapter;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.se1853_jv.labverse.presentation.paper.fragments.ReferenceFragment;
import com.se1853_jv.labverse.presentation.paper.fragments.OverviewFragment;
import com.se1853_jv.labverse.presentation.paper.fragments.CitationFragment;

@RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
public class PaperTabsAdapter extends FragmentStateAdapter {
    private final String overviewDescription;

    public PaperTabsAdapter(@NonNull FragmentActivity fragmentActivity, String description) {
        super(fragmentActivity);
        this.overviewDescription = description;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Log.d("Hehe", "GO here");
        return switch (position) {
            case 0 -> {
                var fragment = new OverviewFragment();
                var bundle = new Bundle();
                bundle.putString("description", overviewDescription);
                fragment.setArguments(bundle);
                yield fragment;
            }
            case 1 -> new ReferenceFragment();
            default -> new CitationFragment();
        };
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
