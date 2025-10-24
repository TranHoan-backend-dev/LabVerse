package com.se1853_jv.labverse.presentation.paper;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.paper.CrossRefApiHandler;
import com.se1853_jv.labverse.domain.infrastructure.BibEntry;
import com.se1853_jv.labverse.data.utils.ParseFileUtils;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.AccessLevel;
import lombok.experimental.*;

@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ImportPaperByBibtexActivity extends AppCompatActivity {
    @NonFinal
    MaterialButton chooseFileBtn;
    @NonFinal
    MaterialButton importBtn;
    @NonFinal
    ActivityResultLauncher<Intent> filePickerLauncher;
    @NonFinal
    List<BibEntry> entries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_bibtex);

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        var uri = result.getData().getData(); // dia chi truu tuong cua du lieu
                        var bibContent = readBibFile(uri);
                        entries = ParseFileUtils.parseBibEntries(bibContent);
                        runOnUiThread(() -> displayPreview(entries));
                    }
                }
        );
        bindView();
        handleImportFileEvent();
        handleImportEvent();
    }

    private void bindView() {
        chooseFileBtn = findViewById(R.id.btn_choose_file);
        importBtn = findViewById(R.id.btn_import);
    }

    private void handleImportFileEvent() {
        Log.e("Guess", "Hehe");

        chooseFileBtn.setOnClickListener(v -> {
            var intent = new Intent(Intent.ACTION_GET_CONTENT);

            intent.setType("*/*"); // mo moi loai file
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            filePickerLauncher.launch(Intent.createChooser(intent, "Choose a file"));
        });
    }

    // <editor-fold> desc="read file module"
    @NonNull
    private String readBibFile(Uri uri) {
        var builder = new StringBuilder();

        // anh xa dang dia chi vat ly roi doc file do
        // content resolver xac dinh provider can thiet de xu ly (media, downloads,...)
        try (var inputStream = getContentResolver().openInputStream(uri);
             var reader = new java.io.BufferedReader(new java.io.InputStreamReader(Objects.requireNonNull(inputStream)))) {

            String line;
            var lineCount = 0;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');

                if (++lineCount > 50000) {
                    Log.w("ReadBibFile", "File quá lớn, dừng đọc sớm để tránh tràn bộ nhớ");
                    break;
                }
            }

        } catch (Exception e) {
            Log.e("ReadBibFile", "Error reading file: " + e.getMessage(), e);
            return "";
        }

        return builder.toString();
    }

    @SuppressLint("SetTextI18n")
    private void displayPreview(@NonNull List<BibEntry> entries) {
        LinearLayout previewContainer = findViewById(R.id.pdfSummaryWrapper);
        previewContainer.removeAllViews();

        for (var e : entries) {
            MaterialCardView card = buildCardView();

            LinearLayout body = buildParentForBibTexContent();
            body.addView(buildIcon());

            LinearLayout mainContent = new LinearLayout(this);
            mainContent.setOrientation(LinearLayout.VERTICAL);
            mainContent.setPadding(16, 0, 0, 0);

            mainContent.addView(buildTitle(e));
            mainContent.addView(buildAuthorsAndYear(e));

            body.addView(mainContent);

            // Bọc nội dung và nút xóa trong FrameLayout
            FrameLayout wrapper = new FrameLayout(this);
            wrapper.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            ));
            wrapper.addView(body);
            wrapper.addView(buildDeleteBadge(card));

            card.addView(wrapper);
            previewContainer.addView(card);
        }

        importBtn.setClickable(true);
        importBtn.bringToFront();
        importBtn.setElevation(10);
    }

    @NonNull
    private TextView buildTitle(@NonNull BibEntry e) {
        var title = new TextView(this);
        title.setText(e.getTitle());
        title.setTextSize(16);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        return title;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    private TextView buildAuthorsAndYear(@NonNull BibEntry e) {
        var authorsAndYear = new TextView(this);
        authorsAndYear.setText(e.getAuthor() + " (" + e.getYear() + ")");
        authorsAndYear.setTextSize(14);
        return authorsAndYear;
    }

    @NonNull
    private ImageView buildIcon() {
        var icon = new ImageView(this);
        var width = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
        var height = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(width, height);
        icon.setLayoutParams(iconParams);
        icon.setImageResource(R.drawable.ic_pdf_file);
        icon.setContentDescription(getString(R.string.pdf_file));
        return icon;
    }

    @NonNull
    private LinearLayout buildParentForBibTexContent() {
        var body = new LinearLayout(this);
        body.setOrientation(LinearLayout.HORIZONTAL);
        body.setPadding(16, 16, 16, 16);
        body.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        return body;
    }

    @NonNull
    private MaterialCardView buildCardView() {
        var card = new MaterialCardView(this);
        card.setId(View.generateViewId());
        card.setContentPadding(16, 16, 16, 16);
        card.setUseCompatPadding(true);
        ConstraintLayout.LayoutParams cardParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        card.setLayoutParams(cardParams);
        return card;
    }

    @NonNull
    private ImageButton buildDeleteBadge(@NonNull MaterialCardView card) {
        var btn = new ImageButton(this);
        btn.setImageResource(R.drawable.ic_close_24);
        btn.setBackgroundResource(R.color.white);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics())
        );
        params.gravity = Gravity.END | Gravity.TOP;
        params.setMargins(0, 8, 8, 0);
        btn.setLayoutParams(params);

        btn.setOnClickListener(v -> {
            ((ViewGroup) card.getParent()).removeView(card);
            Toast.makeText(this, "Đã xoá thẻ", Toast.LENGTH_SHORT).show();
        });

        return btn;
    }
    // </editor-fold>

    // <editor-fold> desc="import file module"
    private void handleImportEvent() {
        var handler = new CrossRefApiHandler();
        importBtn.setOnClickListener(v -> {
            if (entries == null || entries.isEmpty()) {
                Toast.makeText(this, "Chưa có file BibTex nào được chọn", Toast.LENGTH_SHORT).show();
                return;
            }
            var count = new AtomicInteger();
            Executors.newSingleThreadExecutor().execute(() -> {
//                for (BibEntry e : entries) {
//                    count.incrementAndGet();
//                    Log.e("DOI empty", e.getDoi() + " " + count.get());
//                    if (e.getDoi() == null || e.getDoi().isEmpty()) {
//                        runOnUiThread(() -> Toast.makeText(this, "DOI không hợp lệ", Toast.LENGTH_SHORT).show());
//                        return;
//                    }
//                    var paper = handler.getArticleUrlFromDOI(e.getDoi());
                var object = handler.getArticleUrlFromDOI("10.34190/iccws.20.1.3366");
                if (object != null) {
                    Log.d("Paper", object.toString());
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        mapper.readValue(object.toString(), PaperResearch.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "DOI không hợp lệ", Toast.LENGTH_SHORT).show());
                }
//                }
            });
        });
    }
    // </editor-fold>
}
