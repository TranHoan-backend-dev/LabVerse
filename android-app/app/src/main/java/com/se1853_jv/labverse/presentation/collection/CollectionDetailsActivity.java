package com.se1853_jv.labverse.presentation.collection;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.collection.CollectionApiHandler;
import com.se1853_jv.labverse.data.dto.response.CollectionPaperDetailResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionResponse;
import com.se1853_jv.labverse.data.utils.Connectivity;
import com.se1853_jv.labverse.presentation.collection.adapter.CollectionPaperAdapter;

import java.util.ArrayList;
import java.util.List;

public class CollectionDetailsActivity extends AppCompatActivity {
    private static final String TAG = "CollectionDetailsActivity";
    
    private CollectionApiHandler apiHandler;
    private RecyclerView recyclerPapers;
    private TextView textCollectionName;
    private TextView textEmptyState;
    private MaterialButton buttonAddPaper;
    private CollectionPaperAdapter adapter;
    
    private CollectionResponse collection;
    private List<CollectionPaperDetailResponse> papers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_details);

        collection = (CollectionResponse) getIntent().getSerializableExtra("collection");
        if (collection == null) {
            Toast.makeText(this, "Collection not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiHandler = new CollectionApiHandler();
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        loadPapers();
    }

    private void initializeViews() {
        recyclerPapers = findViewById(R.id.recycler_papers);
        textCollectionName = findViewById(R.id.text_collection_name);
        textEmptyState = findViewById(R.id.text_empty_state);
        buttonAddPaper = findViewById(R.id.button_add_paper);

        textCollectionName.setText(collection.getName());

        buttonAddPaper.setOnClickListener(v -> {
            Intent intent = new Intent(CollectionDetailsActivity.this, SelectPaperActivity.class);
            intent.putExtra("collection", collection);
            startActivityForResult(intent, 100);
        });
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new CollectionPaperAdapter();
        recyclerPapers.setLayoutManager(new LinearLayoutManager(this));
        recyclerPapers.setAdapter(adapter);
    }

    private void loadPapers() {
        if (!Connectivity.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        apiHandler.getPapersInCollection(collection.getId(), new ApiCallback<List<CollectionPaperDetailResponse>>() {
            @Override
            public void onSuccess(List<CollectionPaperDetailResponse> response) {
                runOnUiThread(() -> {
                    papers.clear();
                    if (response != null) {
                        papers.addAll(response);
                    }
                    adapter.setPapers(papers);
                    updateEmptyState();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    android.util.Log.e(TAG, "Error loading papers: " + error);
                    Toast.makeText(CollectionDetailsActivity.this,
                            "Failed to load papers: " + error,
                            Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });
            }
        });
    }

    private void updateEmptyState() {
        if (papers.isEmpty()) {
            textEmptyState.setVisibility(View.VISIBLE);
            recyclerPapers.setVisibility(View.GONE);
        } else {
            textEmptyState.setVisibility(View.GONE);
            recyclerPapers.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Refresh papers list after adding paper
            loadPapers();
        }
    }
}

