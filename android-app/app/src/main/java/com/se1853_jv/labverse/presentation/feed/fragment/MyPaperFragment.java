package com.se1853_jv.labverse.presentation.feed.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.reflect.TypeToken;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.utils.ParseFileUtils;
import com.se1853_jv.labverse.presentation.feed.entity.MyPaperItem;

public class MyPaperFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MyPaperItem item = getMockData(view);
        Log.d("MyPaper", item.toString());
    }

    private MyPaperItem getMockData(@NonNull View view) {
        return ParseFileUtils.fromJsonAsset(
                view.getContext(),
                "feed/my-paper.json",
                new TypeToken<MyPaperItem>() {
                }.getType()
        );
    }
}
