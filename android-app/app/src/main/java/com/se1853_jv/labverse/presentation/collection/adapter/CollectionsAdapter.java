package com.se1853_jv.labverse.presentation.collection.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.se1853_jv.labverse.presentation.collection.CollectionsFragment;

public class CollectionsAdapter extends FragmentStateAdapter {
    public CollectionsAdapter(@NonNull FragmentActivity fragmentActivity) {
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


