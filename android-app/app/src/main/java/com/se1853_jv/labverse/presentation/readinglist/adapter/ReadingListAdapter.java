package com.se1853_jv.labverse.presentation.readinglist.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.dto.response.ReadingListResponse;

import java.util.ArrayList;
import java.util.List;

public class ReadingListAdapter extends RecyclerView.Adapter<ReadingListAdapter.ReadingListViewHolder> {

    private List<ReadingListResponse> readingLists = new ArrayList<>();
    private OnReadingListActionListener listener;

    public interface OnReadingListActionListener {
        void onViewList(ReadingListResponse list);
        void onShareList(ReadingListResponse list);
        void onMoreOptions(ReadingListResponse list, View anchorView);
    }

    public void setOnReadingListActionListener(OnReadingListActionListener listener) {
        this.listener = listener;
    }

    public void setReadingLists(List<ReadingListResponse> newLists) {
        this.readingLists.clear();
        if (newLists != null) {
            this.readingLists.addAll(newLists);
        }
        notifyDataSetChanged();
    }

    public void addReadingList(ReadingListResponse list) {
        this.readingLists.add(list);
        notifyItemInserted(this.readingLists.size() - 1);
    }

    public void removeReadingList(ReadingListResponse list) {
        int position = -1;
        for (int i = 0; i < readingLists.size(); i++) {
            if (readingLists.get(i).getId().equals(list.getId())) {
                position = i;
                break;
            }
        }
        if (position >= 0) {
            this.readingLists.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public ReadingListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_reading_list_card, parent, false);
        return new ReadingListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReadingListViewHolder holder, int position) {
        holder.bind(readingLists.get(position));
    }

    @Override
    public int getItemCount() {
        return readingLists.size();
    }

    class ReadingListViewHolder extends RecyclerView.ViewHolder {
        private TextView tvListName;
        private TextView tvListDescription;
        private TextView tvPaperCount;
        private TextView tvMemberCount;
        private ImageButton btnMoreOptions;
        private TextView btnView;
        private ImageButton btnShare;

        public ReadingListViewHolder(@NonNull View itemView) {
            super(itemView);
            tvListName = itemView.findViewById(R.id.tvListName);
            tvListDescription = itemView.findViewById(R.id.tvListDescription);
            tvPaperCount = itemView.findViewById(R.id.tvPaperCount);
            tvMemberCount = itemView.findViewById(R.id.tvMemberCount);
            btnMoreOptions = itemView.findViewById(R.id.btnMoreOptions);
            btnView = itemView.findViewById(R.id.btnView);
            btnShare = itemView.findViewById(R.id.btnShare);
        }

        public void bind(ReadingListResponse list) {
            // Set list name
            if (tvListName != null) {
                tvListName.setText(list.getName() != null ? list.getName() : "Untitled List");
            }

            // Set description (placeholder for now, can be added to API later)
            if (tvListDescription != null) {
                // Description không có trong API response hiện tại
                // Có thể để trống hoặc generate từ paper count
                tvListDescription.setVisibility(View.GONE);
            }

            // Set paper count
            if (tvPaperCount != null) {
                int paperCount = list.getPaperIdsList() != null ? list.getPaperIdsList().size() : 0;
                tvPaperCount.setText(paperCount + " paper" + (paperCount != 1 ? "s" : ""));
            }

            // Set member count
            if (tvMemberCount != null) {
                int memberCount = list.getUserIdsList() != null ? list.getUserIdsList().size() : 0;
                tvMemberCount.setText(memberCount + " member" + (memberCount != 1 ? "s" : ""));
            }

            // Handle card click
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewList(list);
                }
            });

            // Handle View button
            if (btnView != null) {
                btnView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onViewList(list);
                    }
                });
            }

            // Handle Share button
            if (btnShare != null) {
                btnShare.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onShareList(list);
                    }
                });
            }

            // Handle More Options button
            if (btnMoreOptions != null) {
                btnMoreOptions.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onMoreOptions(list, btnMoreOptions);
                    }
                });
            }
        }
    }
}

