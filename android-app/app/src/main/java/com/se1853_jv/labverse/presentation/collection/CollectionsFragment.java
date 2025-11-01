package com.se1853_jv.labverse.presentation.collection;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.collection.CollectionApiHandler;
import com.se1853_jv.labverse.data.dto.response.CollectionResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionsPageResponse;
import com.se1853_jv.labverse.data.utils.Connectivity;
import com.se1853_jv.labverse.presentation.collection.adapter.CollectionAdapter;

import java.util.ArrayList;
import java.util.List;

public class CollectionsFragment extends Fragment {
    private static final String TAG = "CollectionsFragment";
    
    private CollectionApiHandler apiHandler;
    private RecyclerView recyclerCollections;
    private CollectionAdapter adapter;
    private MaterialCardView cardActiveCollection;
    private TextView textActiveTitle;
    private TextView textActiveInfo;
    private TextView textActiveTimestamp;
    private TextView textEmptyState;
    
    private CollectionResponse activeCollection;
    private List<CollectionResponse> allCollections = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiHandler = new CollectionApiHandler();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_fragment_collections, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupRecyclerView();
        setupActiveCollectionCard(view);
        setupButtons(view);
        loadCollections();
    }

    private void initializeViews(View view) {
        recyclerCollections = view.findViewById(R.id.recycler_collections);
        cardActiveCollection = view.findViewById(R.id.card_active_collection);
        textActiveTitle = view.findViewById(R.id.text_active_title);
        textActiveInfo = view.findViewById(R.id.text_active_info);
        textActiveTimestamp = view.findViewById(R.id.text_active_timestamp);
        textEmptyState = view.findViewById(R.id.text_empty_state);
    }

    private void setupRecyclerView() {
        adapter = new CollectionAdapter();
        adapter.setOnCollectionClickListener(new CollectionAdapter.OnCollectionClickListener() {
            @Override
            public void onCollectionClick(CollectionResponse collection) {
                openCollectionDetails(collection);
            }

            @Override
            public void onOptionsClick(CollectionResponse collection, View anchor) {
                showCollectionOptions(collection, anchor);
            }
        });

        recyclerCollections.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerCollections.setAdapter(adapter);
    }

    private void setupActiveCollectionCard(View view) {
        cardActiveCollection.setOnClickListener(v -> {
            if (activeCollection != null) {
                openCollectionDetails(activeCollection);
            }
        });

        view.findViewById(R.id.button_active_options)
                .setOnClickListener(v -> {
                    if (activeCollection != null) {
                        showCollectionOptions(activeCollection, v);
                    }
                });
    }

    private void setupButtons(View view) {
        view.findViewById(R.id.button_create_collection).setOnClickListener(v -> {
            if (getActivity() instanceof CollectionsActivity) {
                ((CollectionsActivity) getActivity()).showCreateCollectionDialog();
            }
        });
        
        view.findViewById(R.id.button_invite_members).setOnClickListener(v -> {
            if (getActivity() instanceof CollectionsActivity) {
                ((CollectionsActivity) getActivity()).showInviteMembersDialog();
            }
        });
    }

    private void loadCollections() {
        if (!Connectivity.isInternetAvailable(requireContext())) {
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        apiHandler.getCollections(0, 100, new ApiCallback<CollectionsPageResponse>() {
            @Override
            public void onSuccess(CollectionsPageResponse response) {
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    allCollections = response.getContent() != null ? response.getContent() : new ArrayList<>();
                    displayCollections();
                });
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    android.util.Log.e(TAG, "Error loading collections: " + error);
                    Toast.makeText(requireContext(), 
                            "Failed to load collections: " + error, 
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void displayCollections() {
        if (allCollections.isEmpty()) {
            textEmptyState.setVisibility(View.VISIBLE);
            recyclerCollections.setVisibility(View.GONE);
            cardActiveCollection.setVisibility(View.GONE);
            return;
        }

        textEmptyState.setVisibility(View.GONE);
        recyclerCollections.setVisibility(View.VISIBLE);

        // Set first collection as active
        activeCollection = allCollections.get(0);
        updateActiveCollectionCard();

        // Set remaining collections in RecyclerView
        List<CollectionResponse> remainingCollections = allCollections.size() > 1 
                ? allCollections.subList(1, allCollections.size()) 
                : new ArrayList<>();
        adapter.setCollections(remainingCollections);
    }

    private void updateActiveCollectionCard() {
        if (activeCollection == null) {
            cardActiveCollection.setVisibility(View.GONE);
            return;
        }

        cardActiveCollection.setVisibility(View.VISIBLE);
        textActiveTitle.setText(activeCollection.getName());
        textActiveInfo.setText("0 papers • 0 members"); // TODO: Get actual counts
        textActiveTimestamp.setText("Recently updated");
    }

    private void showCollectionOptions(CollectionResponse collection, View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add("View Details");
        popup.getMenu().add("Manage Members");
        popup.getMenu().add("Edit Collection");
        popup.getMenu().add("Delete Collection");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if ("View Details".equals(title)) {
                openCollectionDetails(collection);
            } else if ("Manage Members".equals(title)) {
                if (getActivity() instanceof CollectionsActivity) {
                    ((CollectionsActivity) getActivity()).showInviteMembersDialog();
                }
            } else if ("Edit Collection".equals(title)) {
                if (getActivity() instanceof CollectionsActivity) {
                    ((CollectionsActivity) getActivity()).showEditCollectionDialog(collection);
                }
            } else if ("Delete Collection".equals(title)) {
                if (getActivity() instanceof CollectionsActivity) {
                    ((CollectionsActivity) getActivity()).showDeleteCollectionDialog(collection);
                }
            }
            return true;
        });
        popup.show();
    }

    private void openCollectionDetails(CollectionResponse collection) {
        // TODO: Navigate to collection details screen
        Toast.makeText(requireContext(), "Opening collection: " + collection.getName(), Toast.LENGTH_SHORT).show();
    }

    public void refreshCollections() {
        loadCollections();
    }
}


