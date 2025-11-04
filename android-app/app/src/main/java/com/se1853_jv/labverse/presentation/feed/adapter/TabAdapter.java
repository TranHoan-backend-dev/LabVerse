package com.se1853_jv.labverse.presentation.feed.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.se1853_jv.labverse.presentation.feed.fragment.DiscoveryFragment;
import com.se1853_jv.labverse.presentation.feed.fragment.MyPaperFragment;
import com.se1853_jv.labverse.presentation.feed.fragment.TeamFragment;

public class TabAdapter extends FragmentStateAdapter {
    public TabAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return switch (position) {
            case 0 -> new DiscoveryFragment();
            case 1 -> new MyPaperFragment();
            default -> new TeamFragment();
        };
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
