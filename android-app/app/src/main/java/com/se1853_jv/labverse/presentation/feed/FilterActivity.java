package com.se1853_jv.labverse.presentation.feed;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.se1853_jv.labverse.R;

import java.time.LocalDate;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
public class FilterActivity extends AppCompatActivity {
    private final String[] items = {"Nature", "Science", "IEEE Transactions", "ACM Computing Surveys"};
    private TextInputEditText author, yearFrom, yearTo, keywordsOrTags;
    private AutoCompleteTextView journalDropdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_filter_bottom_sheet);

        bindingView();
        handleCloseEvent();
        buildDropdownItems();
        handleResetContent();
        handleSubmitFilter();
    }

    private void bindingView() {
        journalDropdown = findViewById(R.id.spinner_journal);
        author = findViewById(R.id.input_author);
        yearFrom = findViewById(R.id.input_year_from);
        yearTo = findViewById(R.id.input_year_to);
    }

    private void handleCloseEvent() {
        ImageButton btn = findViewById(R.id.btn_close);
        btn.setOnClickListener(v -> {
            finish();
        });
    }

    private void buildDropdownItems() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                items
        );
        journalDropdown.setAdapter(adapter);
    }

    private void handleResetContent() {
        Button resetBtn = findViewById(R.id.btn_reset);
        resetBtn.setOnClickListener(v -> {
            author.setText("");
            yearFrom.setText("");
            yearTo.setText("");
            keywordsOrTags.setText("");
            journalDropdown.setText("");
        });
    }

    private void handleSubmitFilter() {
        Button submitBtn = findViewById(R.id.btn_apply);
        submitBtn.setOnClickListener(v -> {
            checkEmpty();
            validateYear();
            // TODO: xu ly phan filter
        });
    }

    private void checkEmpty() {
        if (Objects.requireNonNull(author.getText()).toString().isEmpty() &&
                Objects.requireNonNull(yearFrom.getText()).toString().isEmpty() &&
                Objects.requireNonNull(yearTo.getText()).toString().isEmpty() &&
                Objects.requireNonNull(keywordsOrTags.getText()).toString().isEmpty() &&
                journalDropdown.getText().toString().isEmpty()) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Must fill at least one field", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void validateYear() {
        var yearFrom = Integer.parseInt(Objects.requireNonNull(this.yearFrom.getText()).toString());
        var yearTo = Integer.parseInt(Objects.requireNonNull(this.yearTo.getText()).toString());
        if (yearFrom > yearTo) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Year from must be smaller than year to", Toast.LENGTH_SHORT).show();
            });
        }
        if (yearTo > LocalDate.now().getYear()) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Year to must be smaller than current year", Toast.LENGTH_SHORT).show();
            });
        }
        if (String.valueOf(yearFrom).length() != 4 || String.valueOf(yearTo).length() != 4) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Year must be 4 digits", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
