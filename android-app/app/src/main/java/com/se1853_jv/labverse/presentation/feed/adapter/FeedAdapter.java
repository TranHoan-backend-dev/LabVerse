package com.se1853_jv.labverse.presentation.feed.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.se1853_jv.labverse.presentation.feed.fragment.FeedFragment;

public class FeedAdapter extends FragmentStateAdapter {
    public FeedAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return new FeedFragment();
    }

    @Override
    public int getItemCount() {
        return 1;
    }
}
