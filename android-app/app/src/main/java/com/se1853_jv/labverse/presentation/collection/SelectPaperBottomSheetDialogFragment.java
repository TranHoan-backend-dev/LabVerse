package com.se1853_jv.labverse.presentation.collection;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.collection.CollectionApiHandler;
import com.se1853_jv.labverse.data.api.paper.PaperApiHandler;
import com.se1853_jv.labverse.data.dto.request.CollectionPaperRequest;
import com.se1853_jv.labverse.data.dto.response.CollectionPaperResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionResponse;
import com.se1853_jv.labverse.data.utils.Connectivity;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;
import com.se1853_jv.labverse.presentation.collection.adapter.SelectPaperAdapter;

import java.util.ArrayList;
import java.util.List;

public class SelectPaperBottomSheetDialogFragment extends BottomSheetDialogFragment {
    private static final String TAG = "SelectPaperBottomSheet";
    private static final String ARG_COLLECTION = "collection";
    
    private PaperApiHandler paperApiHandler;
    private CollectionApiHandler collectionApiHandler;
    private RecyclerView recyclerPapers;
    private EditText editSearch;
    private TextView textEmptyState;
    private SelectPaperAdapter adapter;
    
    private CollectionResponse collection;
    private List<PaperResearch> allPapers = new ArrayList<>();
    private List<PaperResearch> filteredPapers = new ArrayList<>();
    
    private OnPaperAddedListener onPaperAddedListener;

    public interface OnPaperAddedListener {
        void onPaperAdded();
    }

    public static SelectPaperBottomSheetDialogFragment newInstance(CollectionResponse collection) {
        SelectPaperBottomSheetDialogFragment fragment = new SelectPaperBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_COLLECTION, collection);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnPaperAddedListener(OnPaperAddedListener listener) {
        this.onPaperAddedListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_select_paper, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        collection = (CollectionResponse) getArguments().getSerializable(ARG_COLLECTION);
        if (collection == null) {
            Toast.makeText(getContext(), "Collection not found", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        paperApiHandler = new PaperApiHandler();
        collectionApiHandler = new CollectionApiHandler();
        initializeViews(view);
        setupRecyclerView();
        setupSearch();
        loadPapers();
    }

    private void initializeViews(View view) {
        recyclerPapers = view.findViewById(R.id.recycler_papers);
        editSearch = view.findViewById(R.id.edit_search);
        textEmptyState = view.findViewById(R.id.text_empty_state);
    }

    private void setupRecyclerView() {
        adapter = new SelectPaperAdapter(new ArrayList<>(), paper -> {
            addPaperToCollection(paper);
        });
        recyclerPapers.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerPapers.setAdapter(adapter);
    }

    private void setupSearch() {
        if (editSearch != null) {
            editSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterPapers(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void loadPapers() {
        if (getContext() == null || !Connectivity.isInternetAvailable(getContext())) {
            Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        paperApiHandler.getAllPapers(null, new ApiCallback<List<PaperResearch>>() {
            @Override
            public void onSuccess(List<PaperResearch> response) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        allPapers.clear();
                        if (response != null) {
                            allPapers.addAll(response);
                        }
                        filterPapers(editSearch != null ? editSearch.getText().toString() : "");
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        android.util.Log.e(TAG, "Error loading papers: " + error);
                        Toast.makeText(getContext(),
                                "Failed to load papers: " + error,
                                Toast.LENGTH_SHORT).show();
                        updateEmptyState();
                    });
                }
            }
        });
    }

    private void filterPapers(String query) {
        if (query == null || query.trim().isEmpty()) {
            filteredPapers.clear();
            filteredPapers.addAll(allPapers);
        } else {
            String lowerQuery = query.toLowerCase();
            filteredPapers.clear();
            for (PaperResearch paper : allPapers) {
                if (paper.getTitle() != null && paper.getTitle().toLowerCase().contains(lowerQuery) ||
                    paper.getAuthors() != null && paper.getAuthors().toLowerCase().contains(lowerQuery) ||
                    paper.getJournal() != null && paper.getJournal().toLowerCase().contains(lowerQuery)) {
                    filteredPapers.add(paper);
                }
            }
        }
        adapter.setPapers(filteredPapers);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredPapers.isEmpty()) {
            textEmptyState.setVisibility(View.VISIBLE);
            recyclerPapers.setVisibility(View.GONE);
            if (editSearch != null && editSearch.getText().toString().trim().isEmpty()) {
                textEmptyState.setText("No papers found");
            } else {
                textEmptyState.setText("No papers match your search");
            }
        } else {
            textEmptyState.setVisibility(View.GONE);
            recyclerPapers.setVisibility(View.VISIBLE);
        }
    }

    private void addPaperToCollection(PaperResearch paper) {
        if (getContext() == null || !Connectivity.isInternetAvailable(getContext())) {
            Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionPaperRequest request = new CollectionPaperRequest();
        request.setCollectionId(collection.getId());
        request.setPaperId(paper.getId());
        request.setPriority("MEDIUM");
        request.setStatus("ToRead");

        collectionApiHandler.addPaperToCollection(request, new ApiCallback<CollectionPaperResponse>() {
            @Override
            public void onSuccess(CollectionPaperResponse response) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(),
                                "Paper added to collection successfully",
                                Toast.LENGTH_SHORT).show();
                        // Remove paper from list to prevent duplicate selection
                        allPapers.remove(paper);
                        filterPapers(editSearch != null ? editSearch.getText().toString() : "");
                        
                        // Notify parent activity to refresh
                        if (onPaperAddedListener != null) {
                            onPaperAddedListener.onPaperAdded();
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        android.util.Log.e(TAG, "Error adding paper: " + error);
                        Toast.makeText(getContext(),
                                "Failed to add paper: " + error,
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
}

