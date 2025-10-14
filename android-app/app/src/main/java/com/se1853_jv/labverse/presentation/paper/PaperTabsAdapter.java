package com.se1853_jv.labverse.presentation.paper;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.se1853_jv.labverse.presentation.paper.fragments.CitationFragment;
import com.se1853_jv.labverse.presentation.paper.fragments.OverviewFragment;
import com.se1853_jv.labverse.presentation.paper.fragments.ReferenceFragment;

public class PaperTabsAdapter extends FragmentStateAdapter {
    public PaperTabsAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Log.d("Hehe", "GO here");
        return switch (position) {
            case 0 -> new OverviewFragment();
            case 1 -> new CitationFragment();
            default -> new ReferenceFragment();
        };
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
