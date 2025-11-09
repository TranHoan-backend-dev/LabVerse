package com.se1853_jv.labverse.data.sync;

import android.content.Context;
import android.util.Log;

import com.se1853_jv.labverse.domain.db.DatabaseClient;
import com.se1853_jv.labverse.domain.infrastructure.annotation.model.Highlight;
import com.se1853_jv.labverse.domain.infrastructure.annotation.model.Note;
import com.se1853_jv.labverse.domain.infrastructure.annotation.repo.HighlightRepository;
import com.se1853_jv.labverse.domain.infrastructure.annotation.repo.NoteRepository;
import com.se1853_jv.labverse.domain.infrastructure.workflow.model.ReadingWorkflow;
import com.se1853_jv.labverse.domain.infrastructure.workflow.repo.ReadingWorkflowRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Helper class để tích hợp sync vào các repository operations
 * Sử dụng class này thay vì gọi trực tiếp repository khi muốn có offline support
 */
public class OfflineSyncHelper {
    private static final String TAG = "OfflineSyncHelper";
    private final Context context;
    private final SyncManager syncManager;
    private final NoteRepository noteRepository;
    private final HighlightRepository highlightRepository;
    private final ReadingWorkflowRepository workflowRepository;
    private final ExecutorService executorService;

    public OfflineSyncHelper(Context context) {
        this.context = context.getApplicationContext();
        this.syncManager = SyncManager.getInstance(context);
        var db = DatabaseClient.getInstance(context).getAppDatabase();
        this.noteRepository = db.noteRepository();
        this.highlightRepository = db.highlightRepository();
        this.workflowRepository = db.readingWorkflowRepository();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Lưu note và queue để sync
     */
    public void saveNote(Note note, String operation) {
        executorService.execute(() -> {
            try {
                // Lưu vào local database
                if ("CREATE".equals(operation)) {
                    noteRepository.create(note);
                } else if ("UPDATE".equals(operation)) {
                    noteRepository.update(note);
                } else if ("DELETE".equals(operation)) {
                    noteRepository.delete(note);
                }

                // Queue để sync
                syncManager.queueNote(note, operation);
                Log.d(TAG, "Note saved and queued for sync: " + note.getId());
            } catch (Exception e) {
                Log.e(TAG, "Error saving note", e);
            }
        });
    }

    /**
     * Lưu highlight và queue để sync
     */
    public void saveHighlight(Highlight highlight, String operation) {
        executorService.execute(() -> {
            try {
                // Lưu vào local database
                if ("CREATE".equals(operation)) {
                    highlightRepository.create(highlight);
                } else if ("UPDATE".equals(operation)) {
                    highlightRepository.update(highlight);
                } else if ("DELETE".equals(operation)) {
                    highlightRepository.delete(highlight);
                }

                // Queue để sync
                syncManager.queueHighlight(highlight, operation);
                Log.d(TAG, "Highlight saved and queued for sync: " + highlight.getId());
            } catch (Exception e) {
                Log.e(TAG, "Error saving highlight", e);
            }
        });
    }

    /**
     * Update reading progress và queue để sync
     */
    public void updateReadingProgress(ReadingWorkflow workflow) {
        executorService.execute(() -> {
            try {
                // Update trong local database
                workflowRepository.update(workflow);

                // Queue để sync
                syncManager.queueReadingProgress(workflow);
                Log.d(TAG, "Reading progress updated and queued for sync");
            } catch (Exception e) {
                Log.e(TAG, "Error updating reading progress", e);
            }
        });
    }

    /**
     * Update workflow status và queue để sync
     */
    public void updateWorkflowStatus(ReadingWorkflow workflow) {
        executorService.execute(() -> {
            try {
                // Update trong local database
                workflowRepository.update(workflow);

                // Queue để sync
                syncManager.queueWorkflowStatus(workflow);
                Log.d(TAG, "Workflow status updated and queued for sync");
            } catch (Exception e) {
                Log.e(TAG, "Error updating workflow status", e);
            }
        });
    }
}











