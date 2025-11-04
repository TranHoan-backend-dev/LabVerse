package com.se1853_jv.labverse.presentation.feed.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.se1853_jv.labverse.presentation.feed.entity.Paper;
import com.se1853_jv.labverse.presentation.feed.fragment.FavoritesFragment;
import com.se1853_jv.labverse.presentation.feed.fragment.MyPaperMainContentFragment;
import com.se1853_jv.labverse.presentation.feed.fragment.RecentlyReadFragment;

import java.util.ArrayList;
import java.util.List;

public class MyPaperContentAdapter extends FragmentStateAdapter {
    private final List<Paper> papers;

    public MyPaperContentAdapter(@NonNull FragmentActivity fragmentActivity, List<Paper> papers) {
        super(fragmentActivity);
        this.papers = papers;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
//        return switch (position) {
//            case 0 -> {
        var fragment = new MyPaperMainContentFragment();
        var bundle = new Bundle();
        bundle.putSerializable("papers", new ArrayList<>(papers));
        fragment.setArguments(bundle);
        return fragment;
//                yield fragment;
//            }
//            case 1 -> new RecentlyReadFragment();
//            default -> new FavoritesFragment();
//    };
}

@Override
public int getItemCount() {
    return 1;
}
}
