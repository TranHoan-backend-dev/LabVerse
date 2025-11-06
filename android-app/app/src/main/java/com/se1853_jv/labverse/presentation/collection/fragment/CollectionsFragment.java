package com.se1853_jv.labverse.presentation.collection.fragment;

import android.content.Intent;
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

import com.google.android.material.button.MaterialButton;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.collection.CollectionApiHandler;
import com.se1853_jv.labverse.data.dto.response.CollectionResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionsPageResponse;
import com.se1853_jv.labverse.data.utils.Connectivity;
import com.se1853_jv.labverse.data.utils.SessionManager;
import com.se1853_jv.labverse.presentation.collection.CollectionDetailsActivity;
import com.se1853_jv.labverse.presentation.collection.CollectionsActivity;
import com.se1853_jv.labverse.presentation.collection.adapter.CollectionAdapter;

import java.util.ArrayList;
import java.util.List;

public class CollectionsFragment extends Fragment {
    private static final String TAG = "CollectionsFragment";

    private CollectionApiHandler apiHandler;
    private SessionManager sessionManager;
    
    // My Collections section
    private RecyclerView recyclerMyCollections;
    private CollectionAdapter adapterMyCollections;
    private TextView textEmptyMyCollections;
    
    // Shared Collections section
    private RecyclerView recyclerSharedCollections;
    private CollectionAdapter adapterSharedCollections;
    private TextView textEmptySharedCollections;
    
    // Buttons (only for PI)
    private MaterialButton buttonCreateCollection;
    private MaterialButton buttonInviteMembers;
    
    private List<CollectionResponse> myCollections = new ArrayList<>();
    private List<CollectionResponse> sharedCollections = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiHandler = new CollectionApiHandler();
        sessionManager = new SessionManager(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_fragment_collections_v2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerViews();
        setupButtons(view);
        checkUserRole();
        loadCollections();
    }

    private void initializeViews(View view) {
        recyclerMyCollections = view.findViewById(R.id.recycler_my_collections);
        recyclerSharedCollections = view.findViewById(R.id.recycler_shared_collections);
        textEmptyMyCollections = view.findViewById(R.id.text_empty_my_collections);
        textEmptySharedCollections = view.findViewById(R.id.text_empty_shared_collections);
        buttonCreateCollection = view.findViewById(R.id.button_create_collection);
        buttonInviteMembers = view.findViewById(R.id.button_invite_members);
    }

    private void setupRecyclerViews() {
        // Setup My Collections RecyclerView
        adapterMyCollections = new CollectionAdapter();
        adapterMyCollections.setIsOwner(true); // Owner role for My Collections
        adapterMyCollections.setOnCollectionClickListener(new CollectionAdapter.OnCollectionClickListener() {
            @Override
            public void onCollectionClick(CollectionResponse collection) {
                openCollectionDetails(collection);
            }

            @Override
            public void onOptionsClick(CollectionResponse collection, View anchor) {
                showCollectionOptions(collection, anchor, true);
            }
        });
        recyclerMyCollections.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerMyCollections.setAdapter(adapterMyCollections);

        // Setup Shared Collections RecyclerView
        adapterSharedCollections = new CollectionAdapter();
        adapterSharedCollections.setIsOwner(false); // Reader role for Shared Collections
        adapterSharedCollections.setOnCollectionClickListener(new CollectionAdapter.OnCollectionClickListener() {
            @Override
            public void onCollectionClick(CollectionResponse collection) {
                openCollectionDetails(collection);
            }

            @Override
            public void onOptionsClick(CollectionResponse collection, View anchor) {
                showCollectionOptions(collection, anchor, false);
            }
        });
        recyclerSharedCollections.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerSharedCollections.setAdapter(adapterSharedCollections);
    }

    private void setupButtons(View view) {
        buttonCreateCollection.setOnClickListener(v -> {
            if (getActivity() instanceof CollectionsActivity) {
                ((CollectionsActivity) getActivity()).showCreateCollectionDialog();
            }
        });

        buttonInviteMembers.setOnClickListener(v -> {
            if (getActivity() instanceof CollectionsActivity) {
                ((CollectionsActivity) getActivity()).showInviteMembersDialog();
            }
        });
    }

    private void checkUserRole() {
        String role = sessionManager.getRole();
        boolean isPI = "PI".equals(role);
        
        // Only PI can create collections and invite members
        if (!isPI) {
            buttonCreateCollection.setVisibility(View.GONE);
            buttonInviteMembers.setVisibility(View.GONE);
            
            // Hide "My Collections" section completely for non-PI users
            View view = getView();
            if (view != null) {
                TextView textMyCollectionsHeader = view.findViewById(R.id.text_my_collections_header);
                if (textMyCollectionsHeader != null) {
                    textMyCollectionsHeader.setVisibility(View.GONE);
                }
            }
            
            // Hide empty state message and RecyclerView for My Collections
            textEmptyMyCollections.setVisibility(View.GONE);
            recyclerMyCollections.setVisibility(View.GONE);
        }
    }

    private void loadCollections() {
        if (!Connectivity.isInternetAvailable(requireContext())) {
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Encode userId before sending to API (if not already encoded)
        // Note: userId from session might already be encoded from AuthResponse
        String encodedUserId = userId; // Use as-is for now, adjust if needed based on backend response format
        
        // Only load My Collections if user is PI
        String role = sessionManager.getRole();
        boolean isPI = "PI".equals(role);
        if (isPI) {
            // Load My Collections (collections where user is author)
            loadMyCollections(encodedUserId);
        }
        
        // Load Shared Collections (collections where user is not author)
        loadSharedCollections(encodedUserId);
    }

    private void loadMyCollections(String userId) {
        apiHandler.getMyCollections(userId, new ApiCallback<CollectionsPageResponse>() {
            @Override
            public void onSuccess(CollectionsPageResponse response) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    myCollections = response.getContent() != null ? response.getContent() : new ArrayList<>();
                    displayMyCollections();
                });
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    android.util.Log.e(TAG, "Error loading my collections: " + error);
                    Toast.makeText(requireContext(),
                            "Failed to load my collections: " + error,
                            Toast.LENGTH_SHORT).show();
                    displayMyCollections(); // Show empty state
                });
            }
        });
    }

    private void loadSharedCollections(String userId) {
        apiHandler.getSharedCollections(userId, new ApiCallback<CollectionsPageResponse>() {
            @Override
            public void onSuccess(CollectionsPageResponse response) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    sharedCollections = response.getContent() != null ? response.getContent() : new ArrayList<>();
                    displaySharedCollections();
                });
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    android.util.Log.e(TAG, "Error loading shared collections: " + error);
                    Toast.makeText(requireContext(),
                            "Failed to load shared collections: " + error,
                            Toast.LENGTH_SHORT).show();
                    displaySharedCollections(); // Show empty state
                });
            }
        });
    }

    private void displayMyCollections() {
        // Only show My Collections section for PI users
        String role = sessionManager.getRole();
        boolean isPI = "PI".equals(role);
        
        if (!isPI) {
            textEmptyMyCollections.setVisibility(View.GONE);
            recyclerMyCollections.setVisibility(View.GONE);
            return;
        }
        
        if (myCollections.isEmpty()) {
            textEmptyMyCollections.setVisibility(View.VISIBLE);
            recyclerMyCollections.setVisibility(View.GONE);
        } else {
            textEmptyMyCollections.setVisibility(View.GONE);
            recyclerMyCollections.setVisibility(View.VISIBLE);
            adapterMyCollections.setCollections(myCollections);
        }
    }

    private void displaySharedCollections() {
        if (sharedCollections.isEmpty()) {
            textEmptySharedCollections.setVisibility(View.VISIBLE);
            recyclerSharedCollections.setVisibility(View.GONE);
        } else {
            textEmptySharedCollections.setVisibility(View.GONE);
            recyclerSharedCollections.setVisibility(View.VISIBLE);
            adapterSharedCollections.setCollections(sharedCollections);
        }
    }

    private void showCollectionOptions(CollectionResponse collection, View anchor, boolean isMyCollection) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        
        if (isMyCollection) {
            // Only PI can manage their own collections
            popup.getMenu().add("Manage Members");
            popup.getMenu().add("Edit Collection");
            popup.getMenu().add("Delete Collection");
        } else {
            // Shared collections - users can only view
            popup.getMenu().add("View Details");
        }

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if ("Manage Members".equals(title)) {
                if (getActivity() instanceof CollectionsActivity) {
                    ((CollectionsActivity) getActivity()).showInviteMembersDialog(collection);
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
        Intent intent = new Intent(requireContext(), CollectionDetailsActivity.class);
        intent.putExtra("collection", collection);
        startActivity(intent);
    }

    public void refreshCollections() {
        loadCollections();
    }

    public List<CollectionResponse> getMyCollections() {
        return myCollections;
    }

    public List<CollectionResponse> getSharedCollections() {
        return sharedCollections;
    }
}
