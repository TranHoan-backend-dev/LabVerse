package com.se1853_jv.labverse.presentation.paper.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.se1853_jv.labverse.R;

public class OverviewFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_paperdetails_tab_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            var data = getArguments().getString("description");
            Log.d("Data", data == null || data.isBlank() ? "Null" : data);
            TextView description = view.findViewById(R.id.tvDescription);
            if (data != null && !data.isBlank()) {
                description.setText(data);
            }
        }
    }
    // TODO: xu ly khau chuyen tiep sang read pdf file
}
