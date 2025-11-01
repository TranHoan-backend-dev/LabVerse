package com.se1853_jv.labverse.presentation.paper.adapter;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.se1853_jv.labverse.presentation.paper.fragments.PaperDetailsFragment;

@RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
public class DetailsAdapter extends FragmentStateAdapter {
    public DetailsAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return new PaperDetailsFragment();
    }

    @Override
    public int getItemCount() {
        return 1;
    }
}
