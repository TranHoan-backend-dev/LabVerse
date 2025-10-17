package com.se1853_jv.labverse.presentation.paper;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.paper.PaperApiHandler;
import com.se1853_jv.labverse.data.utils.Connectivity;
import com.se1853_jv.labverse.domain.db.AppDatabase;
import com.se1853_jv.labverse.domain.db.DatabaseClient;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;

import java.util.HashMap;
import java.util.Map;

public class PaperDetailsActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView tvPaperTitle, tvPaperAuthors, tvPaperJournal;
    private TabLayout tabLayout;
    private static final String TAG = "FIREBASE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper_details);
        PaperResearch pr = null;
        if (!Connectivity.isInternetAvailable(this)) {
            if (!Connectivity.isApiActive("")) {
                var db = DatabaseClient.getInstance(this).getAppDatabase();
                var id = getIntent().getStringExtra("id");
                pr = db.paperRepository().getById(id);
            } else {
                pr = new PaperResearch();
            }
        }

        bindViews();
        setupToolbar();
        setupTabs();
        displayPaperData(pr);

//        PaperApiHandler apiHandler = new PaperApiHandler();
//        apiHandler.getDetails("b3cde6a5-af2b-4d2c-9caf-7e1867d679b8");
        FirebaseApp.initializeApp(this);

        var db = FirebaseFirestore.getInstance();
//        Map<String, Object> user = new HashMap<>();
//        user.put("name", "Cậu");
//        user.put("email", "test@example.com");
//        user.put("role", "developer");
//
//        db.collection("users")
//                .add(user)
//                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
//                        Log.d(TAG, "Document added: " + documentReference.getId());
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w(TAG, "Error adding document", e);
//                    }
//                });

        // --- Đọc dữ liệu ---
        db.collection("users")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot result) {
                        for (QueryDocumentSnapshot document : result) {
                            Log.d(TAG, document.getId() + " => " + document.getData());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error getting documents", e);
                    }
                });
    }

    private void bindViews() {
        toolbar = findViewById(R.id.toolbarPaperDetails);
        tvPaperTitle = findViewById(R.id.tvPaperTitle);
        tvPaperAuthors = findViewById(R.id.tvPaperAuthors);
        tvPaperJournal = findViewById(R.id.tvPaperJournal);
        tabLayout = findViewById(R.id.tabLayoutPaper);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
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

    @SuppressLint("SetTextI18n")
    private void displayPaperData(PaperResearch paper) {
        if (paper != null) {
            tvPaperTitle.setText(paper.getTitle());
            tvPaperAuthors.setText(paper.getAuthors());
            tvPaperJournal.setText(paper.getPublicationYear() + " • " + paper.getJournal());
        }
        // co dinh noi dung ban dau la "Khong co du lieu"
    }

    private void setupTabs() {
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.d("TabSelected", "Tab được chọn: " + position);
            }
        });

        var adapter = new PaperTabsAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Overview");
                    break;
                case 1:
                    tab.setText("Citation");
                    break;
                case 2:
                    tab.setText("References");
                    break;
            }
        }).attach();
    }

}
