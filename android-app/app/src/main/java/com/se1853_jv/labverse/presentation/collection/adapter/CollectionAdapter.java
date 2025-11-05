package com.se1853_jv.labverse.presentation.collection.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.dto.response.CollectionResponse;

import java.util.ArrayList;
import java.util.List;

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.CollectionViewHolder> {
    private List<CollectionResponse> collections = new ArrayList<>();
    private OnCollectionClickListener listener;

    public interface OnCollectionClickListener {
        void onCollectionClick(CollectionResponse collection);
        void onOptionsClick(CollectionResponse collection, View anchor);
    }

    public void setOnCollectionClickListener(OnCollectionClickListener listener) {
        this.listener = listener;
    }

    public void setCollections(List<CollectionResponse> collections) {
        this.collections = collections != null ? collections : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CollectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_collection_item, parent, false);
        return new CollectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CollectionViewHolder holder, int position) {
        CollectionResponse collection = collections.get(position);
        holder.bind(collection, listener);
    }

    @Override
    public int getItemCount() {
        return collections.size();
    }

    static class CollectionViewHolder extends RecyclerView.ViewHolder {
        private final TextView textTitle;
        private final TextView textInfo;
        private final TextView textTimestamp;
        private final ImageView buttonOptions;

        public CollectionViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_collection_title);
            textInfo = itemView.findViewById(R.id.text_collection_info);
            textTimestamp = itemView.findViewById(R.id.text_collection_timestamp);
            buttonOptions = itemView.findViewById(R.id.button_collection_options);
        }

        public void bind(CollectionResponse collection, OnCollectionClickListener listener) {
            textTitle.setText(collection.getName());
            
            // Display actual counts from API response
            long paperCount = collection.getPaperCount() != null ? collection.getPaperCount() : 0;
            long memberCount = collection.getMemberCount() != null ? collection.getMemberCount() : 0;
            textInfo.setText(paperCount + " papers • " + memberCount + " members");
            
            textTimestamp.setText("Recently updated");
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCollectionClick(collection);
                }
            });

            buttonOptions.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOptionsClick(collection, buttonOptions);
                }
            });
        }
    }
}


