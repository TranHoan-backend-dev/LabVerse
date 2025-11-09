package com.se1853_jv.labverse.presentation.search;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.se1853_jv.labverse.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FilterDialogFragment extends BottomSheetDialogFragment {
    public interface FilterDialogListener {
        void onFiltersApplied(FilterData filterData);
    }

    public static class FilterData {
        public String author;
        public String journal;
        public Integer yearFrom;
        public Integer yearTo;
        public List<String> keywords;
    }

    private FilterDialogListener listener;
    private TextInputEditText authorInput;
    private AutoCompleteTextView journalDropdown;
    private TextInputEditText yearFromInput;
    private TextInputEditText yearToInput;
    private TextInputEditText keywordsInput;
    private final String[] journalItems = {"All Journals", "Nature", "Science", "IEEE Transactions", "ACM Computing Surveys", "Cell", "PNAS"};

    public static FilterDialogFragment newInstance() {
        return new FilterDialogFragment();
    }

    public void setListener(FilterDialogListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_filter_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authorInput = view.findViewById(R.id.input_author);
        journalDropdown = view.findViewById(R.id.spinner_journal);
        yearFromInput = view.findViewById(R.id.input_year_from);
        yearToInput = view.findViewById(R.id.input_year_to);
        keywordsInput = view.findViewById(R.id.input_tags);

        ImageButton closeBtn = view.findViewById(R.id.btn_close);
        Button resetBtn = view.findViewById(R.id.btn_reset);
        Button applyBtn = view.findViewById(R.id.btn_apply);

        closeBtn.setOnClickListener(v -> dismiss());

        resetBtn.setOnClickListener(v -> resetFilters());

        applyBtn.setOnClickListener(v -> applyFilters());

        setupJournalDropdown();
    }

    private void setupJournalDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                journalItems
        );
        journalDropdown.setAdapter(adapter);
        journalDropdown.setText(journalItems[0], false); // Set default to "All Journals"
    }

    private void resetFilters() {
        if (authorInput != null) authorInput.setText("");
        if (journalDropdown != null) journalDropdown.setText(journalItems[0], false);
        if (yearFromInput != null) yearFromInput.setText("");
        if (yearToInput != null) yearToInput.setText("");
        if (keywordsInput != null) keywordsInput.setText("");
    }

    private void applyFilters() {
        FilterData filterData = new FilterData();

        // Get author
        String author = authorInput != null ? Objects.requireNonNull(authorInput.getText()).toString().trim() : "";
        if (!author.isEmpty()) {
            filterData.author = author;
        }

        // Get journal
        String journal = journalDropdown != null ? journalDropdown.getText().toString().trim() : "";
        if (!journal.isEmpty() && !journal.equals("All Journals")) {
            filterData.journal = journal;
        }

        // Get year range
        String yearFromStr = yearFromInput != null ? Objects.requireNonNull(yearFromInput.getText()).toString().trim() : "";
        String yearToStr = yearToInput != null ? Objects.requireNonNull(yearToInput.getText()).toString().trim() : "";

        if (!yearFromStr.isEmpty()) {
            try {
                int yearFrom = Integer.parseInt(yearFromStr);
                int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
                if (yearFrom < 0 || yearFrom > currentYear) {
                    Toast.makeText(requireContext(), "Year from must be between 0 and " + currentYear, Toast.LENGTH_SHORT).show();
                    return;
                }
                filterData.yearFrom = yearFrom;
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid year format", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (!yearToStr.isEmpty()) {
            try {
                int yearTo = Integer.parseInt(yearToStr);
                int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
                if (yearTo < 0 || yearTo > currentYear) {
                    Toast.makeText(requireContext(), "Year to must be between 0 and " + currentYear, Toast.LENGTH_SHORT).show();
                    return;
                }
                filterData.yearTo = yearTo;
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid year format", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Validate year range
        if (filterData.yearFrom != null && filterData.yearTo != null && filterData.yearFrom > filterData.yearTo) {
            Toast.makeText(requireContext(), "Year from must be less than or equal to year to", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get keywords
        String keywordsStr = keywordsInput != null ? Objects.requireNonNull(keywordsInput.getText()).toString().trim() : "";
        if (!keywordsStr.isEmpty()) {
            String[] keywordArray = keywordsStr.split(",");
            filterData.keywords = new ArrayList<>();
            for (String keyword : keywordArray) {
                String trimmed = keyword.trim();
                if (!trimmed.isEmpty()) {
                    filterData.keywords.add(trimmed);
                }
            }
        }

        // Check if at least one filter is applied
        if (filterData.author == null && filterData.journal == null &&
                filterData.yearFrom == null && filterData.yearTo == null &&
                (filterData.keywords == null || filterData.keywords.isEmpty())) {
            Toast.makeText(requireContext(), "Please fill at least one filter field", Toast.LENGTH_SHORT).show();
            return;
        }

        if (listener != null) {
            listener.onFiltersApplied(filterData);
        }

        dismiss();
    }
}

