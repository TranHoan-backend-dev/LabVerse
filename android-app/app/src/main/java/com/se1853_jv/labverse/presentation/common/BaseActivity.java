package com.se1853_jv.labverse.presentation.common;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.presentation.collection.CollectionsActivity;
import com.se1853_jv.labverse.presentation.feed.FeedActivity;
import com.se1853_jv.labverse.presentation.profile.ProfileActivity;
import com.se1853_jv.labverse.presentation.readinglist.ReadingListsActivity;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class BaseActivity extends AppCompatActivity {
    AppCompatImageButton homeBtn, searchBtn, readingListBtn, collectionBtn, profileBtn;
    final String TAG = "BaseActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupBottomNavbar(@NonNull ViewGroup container, int index) {
        View parent = container.findViewById(index);

        homeBtn = parent.findViewById(R.id.home_btn);
        searchBtn = parent.findViewById(R.id.search_btn);
        readingListBtn = parent.findViewById(R.id.reading_list_btn);
        collectionBtn = parent.findViewById(R.id.collections_btn);
        profileBtn = parent.findViewById(R.id.profile_btn);

        handleNavigatingToHomeScreen();
        // TODO: search modal
        handleNavigatingToReadingListScreen();
        handleNavigatingToCollectionScreen();
        handleNavigatingToProfileScreen();
    }

    private void handleNavigatingToHomeScreen() {
        homeBtn.setOnClickListener(v -> {
            Log.e(TAG, "handleNavigatingToHomeScreen");
            if (!(this instanceof FeedActivity)) {
                var intent = new Intent(this, FeedActivity.class);
                startActivity(intent);
            }
        });
    }

    private void handleNavigatingToReadingListScreen() {
        readingListBtn.setOnClickListener(v -> {
            Log.e(TAG, "handleNavigatingToReadingListScreen");
            if (!this.getClass().equals(ReadingListsActivity.class)) {
                var intent = new Intent(this, ReadingListsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void handleNavigatingToCollectionScreen() {
        collectionBtn.setOnClickListener(v -> {
            Log.e(TAG, "handleNavigatingToCollectionScreen");
            if (!(this instanceof CollectionsActivity)) {
                var intent = new Intent(this, CollectionsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void handleNavigatingToProfileScreen() {
        profileBtn.setOnClickListener(v -> {
            Log.e(TAG, "handleNavigatingToProfileScreen");
            if (!(this instanceof ProfileActivity)) {
                var intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
            }
        });
    }
}
