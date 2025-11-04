package com.se1853_jv.labverse.presentation.common;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.presentation.profile.ProfileActivity;
import com.se1853_jv.labverse.presentation.team.TeamListActivity;

/**
 * Helper class to setup header and navigation components across activities
 */
public class HeaderHelper {

    /**
     * Setup avatar click listener to navigate to ProfileActivity
     * @param activity The activity that contains the header
     */
    public static void setupAvatarClickListener(AppCompatActivity activity) {
        View headerView = activity.findViewById(R.id.header);
        ImageView avatar = null;

        if (headerView != null) {
            // If header is included with id="header", find avatar in the included view
            avatar = headerView.findViewById(R.id.avatar);
        } else {
            // If header is directly in the layout, find it directly
            avatar = activity.findViewById(R.id.avatar);
        }

        if (avatar != null) {
            avatar.setOnClickListener(v -> {
                Intent intent = new Intent(activity, ProfileActivity.class);
                activity.startActivity(intent);
            });
        }
    }

    /**
     * Setup avatar click listener with a custom view root
     * Useful when header is included but doesn't have an ID
     * @param activity The activity
     * @param rootView The root view that contains the header (usually activity root)
     */
    public static void setupAvatarClickListener(AppCompatActivity activity, View rootView) {
        ImageView avatar = rootView.findViewById(R.id.avatar);
        if (avatar != null) {
            avatar.setOnClickListener(v -> {
                Intent intent = new Intent(activity, ProfileActivity.class);
                activity.startActivity(intent);
            });
        }
    }

    /**
     * Setup profile navigation click listener in bottom navigation
     * @param activity The activity that contains the bottom navigation
     */
    public static void setupProfileNavigationClickListener(AppCompatActivity activity) {
        View bottomNavView = activity.findViewById(R.id.bottomNav);
        LinearLayout navProfile = null;

        if (bottomNavView != null) {
            // If bottom nav is included with id="bottomNav", find nav_profile in the included view
            navProfile = bottomNavView.findViewById(R.id.nav_profile);
        } else {
            // If bottom nav is directly in the layout, find it directly
            navProfile = activity.findViewById(R.id.nav_profile);
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(activity, ProfileActivity.class);
                activity.startActivity(intent);
            });
        }
    }

    /**
     * Setup both avatar and profile navigation click listeners
     * @param activity The activity
     */
    public static void setupProfileClickListeners(AppCompatActivity activity) {
        setupAvatarClickListener(activity);
        setupProfileNavigationClickListener(activity);
    }

    /**
     * Setup Lists navigation click listener in bottom navigation
     * @param activity The activity that contains the bottom navigation
     */
    public static void setupListsNavigationClickListener(AppCompatActivity activity) {
        View bottomNavView = activity.findViewById(R.id.bottomNav);
        LinearLayout navLists = null;

        if (bottomNavView != null) {
            // If bottom nav is included with id="bottomNav", find nav_lists in the included view
            navLists = bottomNavView.findViewById(R.id.nav_lists);
        } else {
            // If bottom nav is directly in the layout, find it directly
            navLists = activity.findViewById(R.id.nav_lists);
        }

        if (navLists != null) {
            navLists.setOnClickListener(v -> {
                Intent intent = new Intent(activity, TeamListActivity.class);
                activity.startActivity(intent);
            });
        }
    }
}

