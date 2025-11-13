package com.se1853_jv.labverse.presentation.paper.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.se1853_jv.labverse.domain.infrastructure.annotation.model.Highlight;
import com.se1853_jv.labverse.domain.infrastructure.annotation.model.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * View đơn giản để vẽ note và highlight chồng lên PDFView hiện tại.
 * Toạ độ đang được lưu dạng normalized (0 - 10000) để chuyển đổi lại theo kích thước màn hình.
 */
public class AnnotationOverlayView extends View {

    private static final float NOTE_RADIUS_DP = 8f;
    private static final float HIGHLIGHT_RADIUS_DP = 20f; // Increased for better visibility
    private static final float STROKE_WIDTH_DP = 2f;
    private static final float NOTE_TEXT_SIZE_DP = 12f;
    private static final float NOTE_TEXT_PADDING_DP = 4f;

    private final Paint notePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint noteStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint noteTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint noteBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final List<Note> notes = new ArrayList<>();
    private final List<Highlight> highlights = new ArrayList<>();

    private int currentPage = 0;
    
    // PDF page dimensions and position for coordinate conversion
    private float pdfPageWidth = 0f;
    private float pdfPageHeight = 0f;
    private float pdfPageOffsetX = 0f;
    private float pdfPageOffsetY = 0f;

    private float density;
    
    private OnNoteClickListener onNoteClickListener;

    public AnnotationOverlayView(Context context) {
        super(context);
        init(context);
    }

    public AnnotationOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AnnotationOverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        density = context.getResources().getDisplayMetrics().density;

        notePaint.setStyle(Paint.Style.FILL);
        notePaint.setColor(Color.parseColor("#3F51B5"));

        noteStrokePaint.setStyle(Paint.Style.STROKE);
        noteStrokePaint.setStrokeWidth(STROKE_WIDTH_DP * density);
        noteStrokePaint.setColor(Color.WHITE);

        // Highlight paint - 30% transparency to see PDF content underneath
        highlightPaint.setStyle(Paint.Style.FILL);
        highlightPaint.setAlpha(1); // 30% opacity (255 * 0.3 ≈ 77)
        
        // Note text paint - red color, transparent background
        noteTextPaint.setColor(Color.RED);
        noteTextPaint.setTextSize(NOTE_TEXT_SIZE_DP * density);
        noteTextPaint.setTextAlign(Paint.Align.LEFT);
        noteTextPaint.setFakeBoldText(true);
        
        // Note background paint - fully transparent (not used, but kept for potential future use)
        noteBackgroundPaint.setColor(Color.TRANSPARENT);
        noteBackgroundPaint.setAlpha(0);
        noteBackgroundPaint.setStyle(Paint.Style.FILL);
        
        setWillNotDraw(false);
    }
    
    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }
    
    public void setOnNoteClickListener(OnNoteClickListener listener) {
        this.onNoteClickListener = listener;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
        invalidate();
    }
    
    /**
     * Update PDF page position and size for coordinate conversion
     * This should be called when PDF scrolls or page changes
     */
    public void updatePdfPageInfo(float pageWidth, float pageHeight, float offsetX, float offsetY) {
        this.pdfPageWidth = pageWidth;
        this.pdfPageHeight = pageHeight;
        this.pdfPageOffsetX = offsetX;
        this.pdfPageOffsetY = offsetY;
        invalidate();
    }

    public void setNotes(List<Note> newNotes) {
        notes.clear();
        if (newNotes != null) {
            notes.addAll(newNotes);
        }
        invalidate();
    }

    public void setHighlights(List<Highlight> newHighlights) {
        highlights.clear();
        if (newHighlights != null) {
            highlights.addAll(newHighlights);
        }
        invalidate();
    }

    public void addNote(Note note) {
        notes.add(note);
        invalidate();
    }

    public void addHighlight(Highlight highlight) {
        highlights.add(highlight);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float noteRadius = NOTE_RADIUS_DP * density;
        float highlightRadius = HIGHLIGHT_RADIUS_DP * density;

        for (Highlight highlight : highlights) {
            if (highlight.getPageNumber() != currentPage) continue;
            float x = toAbsoluteOnScreen(highlight.getCoordinationX(), pdfPageWidth, pdfPageOffsetX);
            float y = toAbsoluteOnScreen(highlight.getCoordinationY(), pdfPageHeight, pdfPageOffsetY);
            try {
                highlightPaint.setColor(Color.parseColor(highlight.getColorCode()));
            } catch (IllegalArgumentException ignored) {
                highlightPaint.setColor(Color.YELLOW);
            }
            // Set alpha after setting color (setColor resets alpha to 255)
            highlightPaint.setAlpha(77); // 30% opacity (255 * 0.3 ≈ 77)
            canvas.drawCircle(x, y, highlightRadius, highlightPaint);
        }

        for (Note note : notes) {
            if (note.getPageNumber() != currentPage) continue;
            float x = toAbsoluteOnScreen(note.getCoordinationX(), pdfPageWidth, pdfPageOffsetX);
            float y = toAbsoluteOnScreen(note.getCoordinationY(), pdfPageHeight, pdfPageOffsetY);
            
            String noteContent = note.getContent();
            if (noteContent == null || noteContent.isEmpty()) {
                noteContent = "Note";
            }
            
            // Measure text bounds
            Rect textBounds = new Rect();
            noteTextPaint.getTextBounds(noteContent, 0, noteContent.length(), textBounds);
            
            float textHeight = textBounds.height();
            
            // Draw note text in red with transparent background
            canvas.drawText(noteContent, x, y, noteTextPaint);
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // If view is clickable (placement mode), let the touch listener in Activity handle it
        // by returning false to pass through to setOnTouchListener
        // Note: setOnTouchListener is called before onTouchEvent, but if onTouchEvent returns
        // false, the event will still pass through to underlying views
        if (isClickable()) {
            // In placement mode, pass through to Activity's touch listener
            // The listener will handle placement touch and return true/false accordingly
            return false;
        }
        
        // Only handle touch events if we have a listener and it's a tap on a note
        // When not in placement mode, check if tap is on a note
        if (event.getAction() == MotionEvent.ACTION_UP && onNoteClickListener != null) {
            float x = event.getX();
            float y = event.getY();
            
            // Check if tap is on any note (now text with background rectangle)
            for (Note note : notes) {
                if (note.getPageNumber() != currentPage) continue;
                float noteX = toAbsoluteOnScreen(note.getCoordinationX(), pdfPageWidth, pdfPageOffsetX);
                float noteY = toAbsoluteOnScreen(note.getCoordinationY(), pdfPageHeight, pdfPageOffsetY);
                
                String noteContent = note.getContent();
                if (noteContent == null || noteContent.isEmpty()) {
                    noteContent = "Note";
                }
                
                // Calculate text bounds for tap detection
                float textWidth = noteTextPaint.measureText(noteContent);
                Rect textBounds = new Rect();
                noteTextPaint.getTextBounds(noteContent, 0, noteContent.length(), textBounds);
                float textHeight = textBounds.height();
                
                // Check if tap is within note text area (with some padding for easier tapping)
                float padding = NOTE_TEXT_PADDING_DP * density * 2; // Extra padding for easier tap
                float textLeft = noteX;
                float textTop = noteY - textHeight;
                float textRight = noteX + textWidth;
                float textBottom = noteY;
                
                if (x >= textLeft - padding && x <= textRight + padding && 
                    y >= textTop - padding && y <= textBottom + padding) {
                    onNoteClickListener.onNoteClick(note);
                    return true; // Consume event only when tapping on a note
                }
            }
        }
        // Return false to allow touch events to pass through to PDFView for scrolling
        return false;
    }

    /**
     * Convert normalized coordinate (0-10000) to absolute screen coordinate
     * Based on PDF page position and size
     * normalized: coordinate relative to PDF page (0-10000)
     * pageDimension: size of PDF page in screen coordinates
     * pageOffset: offset of PDF page in screen coordinates
     */
    private float toAbsoluteOnScreen(Long normalized, float pageDimension, float pageOffset) {
        if (normalized == null || pageDimension == 0f) {
            // Fallback: if page info not available, use simple conversion
            return (normalized != null ? (normalized / 10000f) * getWidth() : 0f);
        }
        // Convert normalized (0-10000) to PDF page coordinate (0-pageDimension)
        float pageCoord = (normalized / 10000f) * pageDimension;
        // Add page offset to get screen coordinate
        return pageCoord + pageOffset;
    }
    
    /**
     * Convert screen coordinate to normalized coordinate (0-10000) based on PDF page
     */
    public float toNormalizedFromScreen(float screenCoord, float pageDimension, float pageOffset) {
        if (pageDimension == 0f) return 0f;
        // Subtract page offset to get PDF page coordinate
        float pageCoord = screenCoord - pageOffset;
        // Convert to normalized (0-10000)
        return (pageCoord / pageDimension) * 10000f;
    }
}


