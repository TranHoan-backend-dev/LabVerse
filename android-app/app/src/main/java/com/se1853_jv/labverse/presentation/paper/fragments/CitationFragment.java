package com.se1853_jv.labverse.presentation.paper.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.paper.PaperApiHandler;
import com.se1853_jv.labverse.domain.infrastructure.citation.model.Citation;

import java.util.List;

public class CitationFragment extends Fragment {
    private PaperApiHandler apiHandler = new PaperApiHandler();

    public CitationFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_citation_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d("CitationFragment", "onViewCreated");
        super.onViewCreated(view, savedInstanceState);

        bindingCitations(view, "YjNjZGU2YTUtYWYyYi00ZDJjLTljYWYtN2UxODY3ZDY3OWI4");

    }

    private void bindingPaperDetails(String id) {
    }

    private void bindingCitations(View view, String id) {
        Log.d("CitationFragment", "bindingCitations");
        apiHandler.getCitationsOfPaper(id, new ApiCallback<>() {
            @Override
            public void onSuccess(List<Citation> data) {
                requireActivity().runOnUiThread(() -> {
                    if (!data.isEmpty()) {
                        LinearLayout container = view.findViewById(R.id.container_citations);
                        LayoutInflater inflater = LayoutInflater.from(getContext());

                        data.forEach(c -> {
                            View itemView = inflater.inflate(R.layout.layout_citation_info, container, false);

                            TextView citationTitle = itemView.findViewById(R.id.title);
                            TextView citationAuthors = itemView.findViewById(R.id.authors);
                            TextView citationJournal = itemView.findViewById(R.id.journal);
                            TextView citationYear = itemView.findViewById(R.id.year);
                            TextView citationDoi = itemView.findViewById(R.id.doi);

                            citationTitle.setText(c.getTitle());
                            citationAuthors.setText(c.getAuthors());
                            citationJournal.setText(c.getJournal());
                            citationYear.setText(String.valueOf(c.getPublicationYear()));
                            citationDoi.setText(c.getDoi());

                            container.addView(itemView);
                        });
                        Log.d("CitationFragment", "Size of container: " + container.getChildCount());
                    }
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
