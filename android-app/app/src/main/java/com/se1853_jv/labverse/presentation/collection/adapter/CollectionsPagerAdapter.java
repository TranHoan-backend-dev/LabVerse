package com.se1853_jv.labverse.presentation.collection.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.se1853_jv.labverse.presentation.collection.fragment.CollectionsFragment;

/**
 * Adapter for ViewPager2 to manage CollectionsFragment pages.
 * For RecyclerView adapter, see CollectionAdapter.
 */
public class CollectionsPagerAdapter extends FragmentStateAdapter {
    public CollectionsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return new CollectionsFragment();
    }

    @Override
    public int getItemCount() {
        return 1;
    }
}

