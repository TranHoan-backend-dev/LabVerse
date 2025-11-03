package com.se1853_jv.labverse.presentation.paper.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.se1853_jv.labverse.presentation.paper.fragments.ImportBibtexFragment;

public class ImportBibtexAdapter extends FragmentStateAdapter {
    public ImportBibtexAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return new ImportBibtexFragment();
    }

    @Override
    public int getItemCount() {
        return 1;
    }
}
