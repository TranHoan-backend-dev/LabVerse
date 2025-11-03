package com.se1853_jv.labverse.presentation.feed.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.se1853_jv.labverse.presentation.feed.fragment.DiscoveryFragment;

public class ContentAdapter extends FragmentStateAdapter {
    
    public ContentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return DiscoveryFragment as default content
        return new DiscoveryFragment();
    }

    @Override
    public int getItemCount() {
        return 1;
    }
}

