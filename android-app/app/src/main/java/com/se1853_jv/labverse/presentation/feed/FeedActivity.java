package com.se1853_jv.labverse.presentation.feed;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.reflect.TypeToken;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.utils.ParseFileUtils;
import com.se1853_jv.labverse.presentation.common.BaseActivity;
import com.se1853_jv.labverse.presentation.common.HeaderHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.se1853_jv.labverse.presentation.common.HeaderHelper;
import com.se1853_jv.labverse.presentation.feed.adapter.TabAdapter;
import com.se1853_jv.labverse.presentation.feed.entity.DiscoveryItem;
import com.se1853_jv.labverse.presentation.paper.ImportPaperManuallyActivity;

import java.util.List;

public class FeedActivity extends BaseActivity {
    private static final String PREFS_NAME = "FeedActivityPrefs";
    private static final String KEY_FAB_X = "fab_x";
    private static final String KEY_FAB_Y = "fab_y";

    private float dX, dY;
    private int lastAction;
    private View rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_common_ui_home);

        rootLayout = findViewById(R.id.ui_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ui_home), (v, insets) -> {
            var statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(statusBar.left, statusBar.top, statusBar.right, statusBar.bottom);
            return insets;
        });

        setupBottomNavbar(findViewById(R.id.ui_home), R.id.bottomNav);
        setupTabs();
        setupImportPaperButton();
        setupFabDragAndDrop();
        getMockData();
        handleFilterPapers();

        // Setup notification button click listener
        HeaderHelper.setupNotificationClickListener(this);
        // Load and update notification badge
        HeaderHelper.loadNotificationBadge(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh notification badge when returning to this activity
        HeaderHelper.loadNotificationBadge(this);
    }

    private TabLayoutMediator mediator;
    private ViewPager2 pager;
    private TabLayout tabLayout;

    private void setupTabs() {
        pager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayoutPaper);

        if (pager == null || tabLayout == null) {
            Log.e("FeedActivity", "ViewPager or TabLayout not found");
            return;
        }

        var adapter = new TabAdapter(FeedActivity.this);
        pager.setAdapter(adapter);

        // Set up TabLayoutMediator
        mediator = new TabLayoutMediator(tabLayout, pager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(ContextCompat.getString(FeedActivity.this, R.string.discovery));
                    break;
                case 1:
                    tab.setText(ContextCompat.getString(FeedActivity.this, R.string.my_papers));
                    break;
                case 2:
                    tab.setText(ContextCompat.getString(FeedActivity.this, R.string.teams));
                    break;
            }
        });
        mediator.attach();

        // Handle FAB visibility based on current tab
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                // Hide FAB when on Teams tab (position 2)
                FloatingActionButton fab = findViewById(R.id.fabImportPaper);
                if (fab != null) {
                    fab.setVisibility(position == 2 ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Do nothing
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Do nothing
            }
        });

        // Handle page changes
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                // Hide FAB when on Teams tab (position 2)
                FloatingActionButton fab = findViewById(R.id.fabImportPaper);
                if (fab != null) {
                    fab.setVisibility(position == 2 ? View.GONE : View.VISIBLE);
                }
            }
        });

        // Set initial FAB visibility
        updateFabVisibility(pager.getCurrentItem());
    }

    private void getMockData() {
        List<DiscoveryItem> items = ParseFileUtils.fromJsonAsset(
                FeedActivity.this,
                "feed/discovery.json",
                new TypeToken<List<DiscoveryItem>>() {
                }.getType()
        );
    }

    private void handleFilterPapers() {
        View searchBar = findViewById(R.id.search_bar);
        ImageButton btn = searchBar.findViewById(R.id.btn_filter);

        btn.setOnClickListener(v -> {
            var intent = new Intent(this, FilterActivity.class);
            startActivity(intent);
        });
    }

    private static final int REQUEST_CODE_IMPORT_PAPER = 1001;
    private static final float DRAG_THRESHOLD = 10f; // Minimum distance to consider as drag

    private FloatingActionButton fabImportPaper;
    private boolean isDragging = false;
    private float initialTouchX, initialTouchY;

    private void setupImportPaperButton() {
        fabImportPaper = findViewById(R.id.fabImportPaper);
        if (fabImportPaper != null) {
            fabImportPaper.setOnClickListener(v -> {
                android.util.Log.d("FeedActivity", "FAB clicked, isDragging: " + isDragging);
                // Only trigger click if not dragging
                if (!isDragging) {
                    try {
                        Intent intent = new Intent(FeedActivity.this, ImportPaperManuallyActivity.class);
                        startActivityForResult(intent, REQUEST_CODE_IMPORT_PAPER);
                        android.util.Log.d("FeedActivity", "Started ImportPaperManuallyActivity");
                    } catch (Exception e) {
                        android.util.Log.e("FeedActivity", "Error starting ImportPaperManuallyActivity: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    android.util.Log.d("FeedActivity", "Click ignored because isDragging is true");
                }
            });
        } else {
            android.util.Log.e("FeedActivity", "fabImportPaper is null!");
        }
    }

    private void setupFabDragAndDrop() {
        if (fabImportPaper == null || rootLayout == null) return;

        // Ensure FAB is visible first
        fabImportPaper.setVisibility(View.VISIBLE);

        // Wait for layout to be measured before removing constraints
        fabImportPaper.post(() -> {
            if (fabImportPaper == null || rootLayout == null) return;

            // Get screen dimensions
            int screenWidth = rootLayout.getWidth();
            int screenHeight = rootLayout.getHeight();
            View bottomNav = findViewById(R.id.bottomNav);
            int bottomNavHeight = bottomNav != null ? bottomNav.getHeight() : 0;
            int fabWidth = fabImportPaper.getWidth();
            int fabHeight = fabImportPaper.getHeight();

            if (screenWidth == 0 || screenHeight == 0 || fabWidth == 0 || fabHeight == 0) {
                // Layout not ready, try again later
                fabImportPaper.postDelayed(() -> setupFabDragAndDrop(), 100);
                return;
            }

            // Calculate default position (bottom right) - same as XML constraints
            float defaultX = screenWidth - fabWidth - dpToPx(24); // 24dp margin
            float defaultY = screenHeight - fabHeight - bottomNavHeight - dpToPx(80); // 80dp margin

            // Remove constraints to allow free movement
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) fabImportPaper.getLayoutParams();
            params.leftToLeft = ConstraintLayout.LayoutParams.UNSET;
            params.rightToRight = ConstraintLayout.LayoutParams.UNSET;
            params.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
            params.topToTop = ConstraintLayout.LayoutParams.UNSET;
            fabImportPaper.setLayoutParams(params);

            // Set default position first
            fabImportPaper.setX(defaultX);
            fabImportPaper.setY(defaultY);

            // Load saved position (will override default if exists)
            loadFabPosition();
        });

        fabImportPaper.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                // Only handle touch events that are actually on the FAB
                if (!isTouchInsideView(view, event)) {
                    return false;
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Store initial touch position
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        dX = view.getX() - event.getRawX();
                        dY = view.getY() - event.getRawY();
                        lastAction = MotionEvent.ACTION_DOWN;
                        isDragging = false;
                        // Don't consume ACTION_DOWN to allow click to work
                        return false;

                    case MotionEvent.ACTION_MOVE:
                        // Calculate distance moved
                        float deltaX = Math.abs(event.getRawX() - initialTouchX);
                        float deltaY = Math.abs(event.getRawY() - initialTouchY);

                        // Only start dragging if moved beyond threshold
                        if (deltaX > DRAG_THRESHOLD || deltaY > DRAG_THRESHOLD) {
                            isDragging = true;
                            lastAction = MotionEvent.ACTION_MOVE;

                            // Continue to prevent parent from intercepting
                            view.getParent().requestDisallowInterceptTouchEvent(true);

                            // Cancel any pending click
                            view.setPressed(false);
                            view.cancelLongPress();

                            // Calculate new position
                            float newX = event.getRawX() + dX;
                            float newY = event.getRawY() + dY;

                            // Get screen bounds
                            Rect displayRect = new Rect();
                            rootLayout.getWindowVisibleDisplayFrame(displayRect);
                            int statusBarHeight = displayRect.top;
                            int screenWidth = rootLayout.getWidth();
                            int screenHeight = rootLayout.getHeight();

                            // Get FAB dimensions
                            int fabWidth = view.getWidth();
                            int fabHeight = view.getHeight();

                            // Get bottom navbar height
                            View bottomNav = findViewById(R.id.bottomNav);
                            int bottomNavHeight = bottomNav != null ? bottomNav.getHeight() : 0;

                            // Constrain FAB within screen bounds
                            newX = Math.max(0, Math.min(newX, screenWidth - fabWidth));
                            newY = Math.max(statusBarHeight, Math.min(newY, screenHeight - fabHeight - bottomNavHeight));

                            // Update FAB position
                            view.setX(newX);
                            view.setY(newY);

                            return true; // Consume the event
                        }
                        // Not enough movement yet - don't consume, let click work normally
                        return false;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Allow parent to intercept again
                        view.getParent().requestDisallowInterceptTouchEvent(false);

                        if (isDragging) {
                            // Save position after drag
                            saveFabPosition(view.getX(), view.getY());
                            isDragging = false;
                            // Reset pressed state to prevent click
                            view.setPressed(false);
                            view.cancelLongPress();
                            return true; // Consume the event to prevent click
                        }
                        // If not dragging, reset flag and let onClick handle it naturally
                        isDragging = false;
                        return false; // Let the click event propagate normally

                    default:
                        return false;
                }
            }
        });
    }

    private void saveFabPosition(float x, float y) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(KEY_FAB_X, x);
        editor.putFloat(KEY_FAB_Y, y);
        editor.apply();
    }

    private void loadFabPosition() {
        if (fabImportPaper == null || rootLayout == null) return;

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        float savedX = prefs.getFloat(KEY_FAB_X, -1);
        float savedY = prefs.getFloat(KEY_FAB_Y, -1);

        if (savedX >= 0 && savedY >= 0) {
            // Wait for layout to be measured
            fabImportPaper.post(() -> {
                if (fabImportPaper == null || rootLayout == null) return;

                // Get screen bounds
                Rect displayRect = new Rect();
                rootLayout.getWindowVisibleDisplayFrame(displayRect);
                int screenWidth = rootLayout.getWidth();
                int screenHeight = rootLayout.getHeight();

                if (screenWidth == 0 || screenHeight == 0) return; // Layout not ready

                // Get FAB dimensions
                int fabWidth = fabImportPaper.getWidth();
                int fabHeight = fabImportPaper.getHeight();

                if (fabWidth == 0 || fabHeight == 0) return; // FAB not measured

                // Get bottom navbar height
                View bottomNav = findViewById(R.id.bottomNav);
                int bottomNavHeight = bottomNav != null ? bottomNav.getHeight() : 0;

                // Ensure saved position is within bounds
                float x = Math.max(0, Math.min(savedX, screenWidth - fabWidth));
                float y = Math.max(displayRect.top, Math.min(savedY, screenHeight - fabHeight - bottomNavHeight));

                // Apply saved position
                fabImportPaper.setX(x);
                fabImportPaper.setY(y);

                // Make sure FAB is visible
                fabImportPaper.setVisibility(View.VISIBLE);
            });
        } else {
            // No saved position, ensure FAB is visible at default position
            fabImportPaper.post(() -> {
                if (fabImportPaper != null) {
                    fabImportPaper.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void updateFabVisibility(int position) {
        if (fabImportPaper != null) {
            // Only show FAB on My Papers tab (position 1)
            if (position == 1) {
                fabImportPaper.show();
                fabImportPaper.setVisibility(View.VISIBLE); // Ensure visible
            } else {
                fabImportPaper.hide();
            }
        }
    }

    private int dpToPx(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private boolean isTouchInsideView(View view, MotionEvent event) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        int width = view.getWidth();
        int height = view.getHeight();

        float touchX = event.getRawX();
        float touchY = event.getRawY();

        return touchX >= x && touchX <= x + width && touchY >= y && touchY <= y + height;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMPORT_PAPER && resultCode == RESULT_OK) {
            // Paper was uploaded successfully, reload My Papers tab
            Log.d("FeedActivity", "Paper uploaded, reloading My Papers tab");
            // Switch to My Papers tab and trigger reload
            if (pager != null) {
                pager.setCurrentItem(1, false); // Switch to My Papers tab (index 1)
                // Fragment's onResume will be called automatically
            }
        }
    }
}
