package com.se1853_jv.labverse.data.sync;

import android.content.Context;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.gson.Gson;
import com.se1853_jv.labverse.domain.db.DatabaseClient;
import com.se1853_jv.labverse.domain.infrastructure.annotation.model.Highlight;
import com.se1853_jv.labverse.domain.infrastructure.annotation.model.Note;
import com.se1853_jv.labverse.domain.infrastructure.sync.model.SyncQueue;
import com.se1853_jv.labverse.domain.infrastructure.sync.repo.SyncQueueRepository;
import com.se1853_jv.labverse.domain.infrastructure.workflow.model.ReadingWorkflow;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manager để quản lý sync operations
 */
public class SyncManager {
    private static final String TAG = "SyncManager";
    private static SyncManager instance;
    private final Context context;
    private final SyncQueueRepository syncQueueRepository;
    private final NetworkMonitor networkMonitor;
    private final ExecutorService executorService;
    private final Gson gson;

    private SyncManager(Context context) {
        this.context = context.getApplicationContext();
        this.syncQueueRepository = DatabaseClient.getInstance(context).getAppDatabase().syncQueueRepository();
        this.networkMonitor = new NetworkMonitor(context);
        this.executorService = Executors.newSingleThreadExecutor();
        this.gson = new Gson();

        // Setup network monitoring
        setupNetworkMonitoring();
    }

    public static synchronized SyncManager getInstance(Context context) {
        if (instance == null) {
            instance = new SyncManager(context);
        }
        return instance;
    }

    private void setupNetworkMonitoring() {
        networkMonitor.addListener(new NetworkMonitor.NetworkStateListener() {
            @Override
            public void onNetworkAvailable() {
                Log.d(TAG, "Network available - triggering sync");
                triggerSync();
            }

            @Override
            public void onNetworkUnavailable() {
                Log.d(TAG, "Network unavailable");
            }
        });
        networkMonitor.startMonitoring();
    }

    /**
     * Queue một note để sync
     */
    public void queueNote(Note note, String operation) {
        executorService.execute(() -> {
            try {
                SyncQueue syncQueue = SyncQueue.builder()
                        .syncType("NOTE")
                        .entityId(note.getId())
                        .operation(operation)
                        .jsonData(gson.toJson(note))
                        .createdAt(System.currentTimeMillis())
                        .isSynced(false)
                        .retryCount(0)
                        .build();
                syncQueueRepository.insert(syncQueue);
                Log.d(TAG, "Queued note: " + note.getId());
                
                // Nếu có internet, trigger sync ngay
                if (networkMonitor.isNetworkAvailable()) {
                    triggerSync();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error queueing note", e);
            }
        });
    }

    /**
     * Queue một highlight để sync
     */
    public void queueHighlight(Highlight highlight, String operation) {
        executorService.execute(() -> {
            try {
                SyncQueue syncQueue = SyncQueue.builder()
                        .syncType("HIGHLIGHT")
                        .entityId(highlight.getId())
                        .operation(operation)
                        .jsonData(gson.toJson(highlight))
                        .createdAt(System.currentTimeMillis())
                        .isSynced(false)
                        .retryCount(0)
                        .build();
                syncQueueRepository.insert(syncQueue);
                Log.d(TAG, "Queued highlight: " + highlight.getId());
                
                if (networkMonitor.isNetworkAvailable()) {
                    triggerSync();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error queueing highlight", e);
            }
        });
    }

    /**
     * Queue reading progress để sync
     */
    public void queueReadingProgress(ReadingWorkflow workflow) {
        executorService.execute(() -> {
            try {
                SyncQueue syncQueue = SyncQueue.builder()
                        .syncType("READING_PROGRESS")
                        .entityId(workflow.getPaperId() + "_" + workflow.getUserId())
                        .operation("UPDATE")
                        .jsonData(gson.toJson(workflow))
                        .createdAt(System.currentTimeMillis())
                        .isSynced(false)
                        .retryCount(0)
                        .build();
                syncQueueRepository.insert(syncQueue);
                Log.d(TAG, "Queued reading progress for paper: " + workflow.getPaperId());
                
                if (networkMonitor.isNetworkAvailable()) {
                    triggerSync();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error queueing reading progress", e);
            }
        });
    }

    /**
     * Queue workflow status để sync
     */
    public void queueWorkflowStatus(ReadingWorkflow workflow) {
        executorService.execute(() -> {
            try {
                SyncQueue syncQueue = SyncQueue.builder()
                        .syncType("WORKFLOW_STATUS")
                        .entityId(workflow.getPaperId() + "_" + workflow.getUserId())
                        .operation("UPDATE")
                        .jsonData(gson.toJson(workflow))
                        .createdAt(System.currentTimeMillis())
                        .isSynced(false)
                        .retryCount(0)
                        .build();
                syncQueueRepository.insert(syncQueue);
                Log.d(TAG, "Queued workflow status for paper: " + workflow.getPaperId());
                
                if (networkMonitor.isNetworkAvailable()) {
                    triggerSync();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error queueing workflow status", e);
            }
        });
    }

    /**
     * Trigger sync với WorkManager
     */
    public void triggerSync() {
        executorService.execute(() -> {
            int pendingCount = syncQueueRepository.getPendingSyncCount();
            if (pendingCount == 0) {
                Log.d(TAG, "No pending syncs");
                return;
            }

            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                    .setConstraints(constraints)
                    .addTag("SYNC_WORK")
                    .build();

            WorkManager.getInstance(context)
                    .enqueueUniqueWork("SYNC_WORK", ExistingWorkPolicy.REPLACE, syncRequest);
            
            Log.d(TAG, "Triggered sync with " + pendingCount + " pending items");
        });
    }

    /**
     * Lấy số lượng pending syncs
     */
    public int getPendingSyncCount() {
        return syncQueueRepository.getPendingSyncCount();
    }

    /**
     * Kiểm tra có network không
     */
    public boolean isNetworkAvailable() {
        return networkMonitor.isNetworkAvailable();
    }

    /**
     * Cleanup
     */
    public void cleanup() {
        networkMonitor.stopMonitoring();
        executorService.shutdown();
    }
}



