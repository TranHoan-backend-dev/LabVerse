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
import com.se1853_jv.labverse.presentation.readinglist.ReadingListActivity;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class BaseActivity extends AppCompatActivity {
    AppCompatImageButton homeBtn, searchBtn, readingListBtn, collectionBtn, profileBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupBottomNavbar(@NonNull ViewGroup container, int index) {
        Log.e("Huhu", "Hehe");
        View parent = container.findViewById(index);

        Log.e("Huhu", parent.toString());
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
        Log.e("Hehe", "Start here");
        homeBtn.setOnClickListener(v -> {
            Log.e("Hehe", "handleNavigatingToHomeScreen");
            if (!(this instanceof FeedActivity)) {
                Log.e("Hehe", "But not go here");
                Intent intent = new Intent(this, FeedActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void handleNavigatingToReadingListScreen() {
        readingListBtn.setOnClickListener(v -> {
            Log.e("Hehe", "handleNavigatingToReadingListScreen");
            if (!(this instanceof ReadingListActivity)) {
                Intent intent = new Intent(this, ReadingListActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void handleNavigatingToCollectionScreen() {
        collectionBtn.setOnClickListener(v -> {
            Log.e("Hehe", "handleNavigatingToCollectionScreen");
            if (!(this instanceof CollectionsActivity)) {
                Intent intent = new Intent(this, CollectionsActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void handleNavigatingToProfileScreen() {
        profileBtn.setOnClickListener(v -> {
            Log.e("Hehe", "handleNavigatingToProfileScreen");
            if (!(this instanceof ProfileActivity)) {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
