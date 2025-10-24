package com.se1853_jv.labverse.presentation.paper.fragments;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.paper.PaperApiHandler;
import com.se1853_jv.labverse.domain.infrastructure.citation.model.Citation;

import java.util.ArrayList;
import java.util.List;

public class CitationFragment extends Fragment {
    private final PaperApiHandler apiHandler = new PaperApiHandler();
    private List<Citation> citations = new ArrayList<>();

    public CitationFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_tab_citation, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d("CitationFragment", "onViewCreated");
        super.onViewCreated(view, savedInstanceState);

        bindingCitations(view, "YjNjZGU2YTUtYWYyYi00ZDJjLTljYWYtN2UxODY3ZDY3OWI4");
        handleCopyCitation(view);
    }

    private void bindingPaperDetails(String id) {
    }

    private void bindingCitations(View view, String id) {
        Log.d("CitationFragment", "bindingCitations");
        apiHandler.getCitationsOfPaper(id, new ApiCallback<>() {
            @Override
            public void onSuccess(List<Citation> data) {
                citations = data;
                requireActivity().runOnUiThread(() -> {
                    if (!data.isEmpty()) {
                        LinearLayout container = view.findViewById(R.id.container_citations);
                        LayoutInflater inflater = LayoutInflater.from(getContext());

                        data.forEach(c -> {
                            View itemView = inflater.inflate(R.layout.layout_citation_info, container, false);

                            TextView citationTitle = itemView.findViewById(R.id.title);
                            TextView citationAuthors = itemView.findViewById(R.id.authors);
                            TextView citationJournal = itemView.findViewById(R.id.journal);
                            TextView citationYear = itemView.findViewById(R.id.year);
                            TextView citationDoi = itemView.findViewById(R.id.doi);

                            citationTitle.setText(c.getTitle());
                            citationAuthors.setText(c.getAuthors());
                            citationJournal.setText(c.getJournal());
                            citationYear.setText(String.valueOf(c.getPublicationYear()));
                            citationDoi.setText(c.getDoi());

                            // luu du lieu goc
                            citationTitle.setTag(c.getTitle());
                            citationAuthors.setTag(c.getAuthors());
                            citationJournal.setTag(c.getJournal());
                            citationYear.setTag(String.valueOf(c.getPublicationYear()));
                            citationDoi.setTag(c.getDoi());

                            handleChangeStyle(itemView);

                            container.addView(itemView);
                        });
                        Log.d("CitationFragment", "Size of container: " + container.getChildCount());
                    }
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void handleChangeStyle(@NonNull View view) {
        MaterialButton style = view.findViewById(R.id.btnStyle);

        style.setOnClickListener(v -> {
            var popup = new PopupMenu(
                    new ContextThemeWrapper(requireContext(), R.style.PopupMenuWhiteBackground),
                    v
            );
            popup.getMenuInflater().inflate(R.menu.menu_style, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                TextView originalTitle = view.findViewById(R.id.title);
                TextView originalAuthors = view.findViewById(R.id.authors);
                TextView originalJournal = view.findViewById(R.id.journal);
                TextView originalYear = view.findViewById(R.id.year);
                TextView originalDoi = view.findViewById(R.id.doi);

                // lay du lieu goc
                var rawTitle = (String) originalTitle.getTag();
                var rawAuthors = (String) originalAuthors.getTag();
                var rawJournal = (String) originalJournal.getTag();
                var rawYear = (String) originalYear.getTag();
                var rawDoi = (String) originalDoi.getTag();

                if (item.getItemId() == R.id.apa) {
                    style.setText("APA Style");

                    applyAPA(originalTitle, originalAuthors, originalJournal, originalYear, originalDoi,
                            rawTitle, rawAuthors, rawJournal, rawYear, rawDoi);

                    return true;
                }
                if (item.getItemId() == R.id.mla) {
                    style.setText("MLA Style");

                    applyMLA(originalTitle, originalAuthors, originalJournal, originalYear, originalDoi,
                            rawTitle, rawAuthors, rawJournal, rawYear, rawDoi);

                    return true;
                }
                if (item.getItemId() == R.id.bibtex) {
                    style.setText("BibTex Style");

                    applyBibTex(originalTitle, originalAuthors, originalJournal, originalYear, originalDoi,
                            rawTitle, rawAuthors, rawJournal, rawYear, rawDoi);

                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private void handleCopyCitation(@NonNull View parent) {
        View view = parent.findViewById(R.id.citation_format);
        MaterialButton apaButton = view.findViewById(R.id.apa);
        MaterialButton mlaButton = view.findViewById(R.id.mla);
        MaterialButton bibtexButton = view.findViewById(R.id.bibtex);

        apaButton.setOnClickListener(v -> {
            List<Citation> data = citations.stream().map(this::deepCopy).toList();

            data.forEach(c -> {
                c.setTitle("*" + c.getTitle() + "*");
                c.setAuthors(formatAPAAuthors(c.getAuthors()));
                c.setDoi("https://doi.org/" + extractDOI(c.getDoi()));
                c.setJournal("*" + c.getJournal() + "*");
                c.setPublicationYear("(" + c.getPublicationYear() + ")");
            });
            copyToTheClipboard(data);
        });

        mlaButton.setOnClickListener(v -> {
            List<Citation> data = citations.stream().map(this::deepCopy).toList();

            data.forEach(c -> {
                c.setAuthors(formatMLAAuthors(c.getAuthors()));
                c.setDoi("https://doi.org/" + extractDOI(c.getDoi()));
                c.setJournal("*" + c.getJournal() + "*");
                c.setPublicationYear(c.getPublicationYear());
            });
            copyToTheClipboard(data);
        });

        bibtexButton.setOnClickListener(v -> {
            List<Citation> data = citations.stream().map(this::deepCopy).toList();

            data.forEach(c -> {
                c.setAuthors(formatBibtexAuthors(c.getAuthors()));
                c.setDoi(extractDOI(c.getDoi()));
                c.setJournal(c.getJournal());
                c.setPublicationYear(c.getPublicationYear());
            });
            copyToTheClipboard(data);
        });
    }

    // <editor-fold> desc="Re-format data when changing styles"
    @NonNull
    private String extractDOI(@NonNull String doi) {
        if (doi.startsWith("https://doi.org/")) {
            return doi.substring("https://doi.org/".length());
        }
        return doi;
    }

    @NonNull
    private String formatAPAAuthors(@NonNull String authors) {
        return authors.replaceAll("(\\w+),\\s*(\\w+)", "$1, $2.").replace(".,", ".");
    }

    @NonNull
    private String formatMLAAuthors(@NonNull String authors) {
        if (authors.contains(","))
            return authors.split(",").length > 3 ? authors.split(",")[0] + ", et al." : authors;
        return authors;
    }

    @NonNull
    private String formatBibtexAuthors(@NonNull String authors) {
        if (authors.contains("et al")) return authors.replace("et al", "and others");
        return authors;
    }
    // </editor-fold>

    // <editor-fold> desc="Handle citation loading when changing styles"
    @SuppressLint("SetTextI18n")
    private void applyAPA(@NonNull TextView title, @NonNull TextView authors, @NonNull TextView journal, @NonNull TextView year, @NonNull TextView doi,
                          String t, String a, String j, String y, String d) {
        title.setText("*" + t + "*");
        authors.setText(formatAPAAuthors(a));
        journal.setText("*" + j + "*");
        year.setText("(" + y + ")");
        doi.setText("https://doi.org/" + extractDOI(d));
    }

    @SuppressLint("SetTextI18n")
    private void applyMLA(@NonNull TextView title, @NonNull TextView authors, @NonNull TextView journal, @NonNull TextView year, @NonNull TextView doi,
                          String t, String a, String j, String y, String d) {
        title.setText(t);
        authors.setText(formatMLAAuthors(a));
        journal.setText("*" + j + "*");
        year.setText(y);
        doi.setText("https://doi.org/" + extractDOI(d));
    }

    private void applyBibTex(@NonNull TextView title, @NonNull TextView authors, @NonNull TextView journal, @NonNull TextView year, @NonNull TextView doi,
                             String t, String a, String j, String y, String d) {
        title.setText(t);
        authors.setText(formatBibtexAuthors(a));
        journal.setText(j);
        year.setText(y);
        doi.setText(extractDOI(d));
    }
    // </editor-fold>

    private Citation deepCopy(@NonNull Citation citation) {
        return Citation.builder()
                .id(citation.getId())
                .title(citation.getTitle())
                .authors(citation.getAuthors())
                .journal(citation.getJournal())
                .publicationYear(citation.getPublicationYear())
                .doi(citation.getDoi())
                .build();
    }

    private void copyToTheClipboard(List<Citation> data) {
        var clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        var gson = new Gson();
        var json = gson.toJson(data);
        var clip = ClipData.newPlainText("citation", json);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(requireContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }
}
