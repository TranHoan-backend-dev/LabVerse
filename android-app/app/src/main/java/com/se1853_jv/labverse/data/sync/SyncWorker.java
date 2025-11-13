package com.se1853_jv.labverse.data.sync;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.se1853_jv.labverse.domain.db.DatabaseClient;
import com.se1853_jv.labverse.data.api.annotation.AnnotationApiHandler;
import com.se1853_jv.labverse.domain.infrastructure.annotation.model.Highlight;
import com.se1853_jv.labverse.domain.infrastructure.annotation.model.Note;
import com.se1853_jv.labverse.domain.infrastructure.sync.model.SyncQueue;
import com.se1853_jv.labverse.domain.infrastructure.sync.repo.SyncQueueRepository;
import com.se1853_jv.labverse.domain.infrastructure.workflow.model.ReadingWorkflow;

import android.content.SharedPreferences;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * WorkManager Worker để sync offline changes lên server
 */
public class SyncWorker extends Worker {
    private static final String TAG = "SyncWorker";
    private final SyncQueueRepository syncQueueRepository;
    private final ExecutorService executorService;
    private final Gson gson;

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.syncQueueRepository = DatabaseClient.getInstance(context).getAppDatabase().syncQueueRepository();
        this.executorService = Executors.newSingleThreadExecutor();
        this.gson = new Gson();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "SyncWorker started");

        try {
            // Lấy tất cả pending syncs
            List<SyncQueue> pendingSyncs = syncQueueRepository.getAllPendingSyncs();
            
            if (pendingSyncs.isEmpty()) {
                Log.d(TAG, "No pending syncs");
                return Result.success();
            }

            Log.d(TAG, "Found " + pendingSyncs.size() + " pending syncs");

            // Sync từng item
            for (SyncQueue syncItem : pendingSyncs) {
                boolean success = syncItem(syncItem);
                
                if (success) {
                    syncItem.setIsSynced(true);
                    syncQueueRepository.update(syncItem);
                    Log.d(TAG, "Synced: " + syncItem.getSyncType() + " - " + syncItem.getEntityId());
                } else {
                    // Tăng retry count
                    int retryCount = syncItem.getRetryCount() != null ? syncItem.getRetryCount() : 0;
                    syncItem.setRetryCount(retryCount + 1);
                    syncQueueRepository.update(syncItem);

                    // Nếu retry quá nhiều lần, bỏ qua
                    if (retryCount >= 3) {
                        Log.w(TAG, "Max retries reached for: " + syncItem.getId());
                        syncItem.setIsSynced(true); // Mark as synced để không retry nữa
                        syncQueueRepository.update(syncItem);
                    }
                }
            }

            // Xóa các items đã sync thành công
            syncQueueRepository.deleteSyncedItems();

            Log.d(TAG, "SyncWorker completed");
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Error in SyncWorker", e);
            return Result.retry(); // Retry nếu có lỗi
        }
    }

    private boolean syncItem(SyncQueue syncItem) {
        try {
            switch (syncItem.getSyncType()) {
                case "NOTE":
                    return syncNote(syncItem);
                case "HIGHLIGHT":
                    return syncHighlight(syncItem);
                case "READING_PROGRESS":
                    return syncReadingProgress(syncItem);
                case "WORKFLOW_STATUS":
                    return syncWorkflowStatus(syncItem);
                default:
                    Log.w(TAG, "Unknown sync type: " + syncItem.getSyncType());
                    return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error syncing item: " + syncItem.getId(), e);
            return false;
        }
    }

    private boolean syncNote(SyncQueue syncItem) {
        try {
            Note note = gson.fromJson(syncItem.getJsonData(), Note.class);
            
            // Create API request
            com.se1853_jv.labverse.data.api.annotation.AnnotationApi.CreateNoteRequest request = 
                new com.se1853_jv.labverse.data.api.annotation.AnnotationApi.CreateNoteRequest();
            
            // TODO: Get paperId, collectionId, userId from context hoặc database
            // For now, using syncItem metadata
            request.paperId = extractPaperIdFromSyncItem(syncItem);
            request.collectionId = extractCollectionIdFromSyncItem(syncItem);
            request.content = note.getContent();
            request.coordinationX = note.getCoordinationX().intValue();
            request.coordinationY = note.getCoordinationY().intValue();
            request.pageNumber = note.getPageNumber();
            
            // Call API
            AnnotationApiHandler apiHandler = new AnnotationApiHandler();
            String token = getJwtToken();
            
            if (token == null) {
                Log.w(TAG, "No JWT token available for sync");
                return false;
            }
            
            // Synchronous call (since we're already in background thread)
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            final boolean[] success = {false};
            
            apiHandler.createNote(token, request, new com.se1853_jv.labverse.data.api.ApiCallback<com.se1853_jv.labverse.data.api.annotation.AnnotationApi.NoteResponse>() {
                @Override
                public void onSuccess(com.se1853_jv.labverse.data.api.annotation.AnnotationApi.NoteResponse data) {
                    success[0] = true;
                    latch.countDown();
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error syncing note: " + error);
                    success[0] = false;
                    latch.countDown();
                }
            });
            
            // Wait for response (with timeout)
            latch.await(10, java.util.concurrent.TimeUnit.SECONDS);
            
            Log.d(TAG, "Synced note: " + syncItem.getEntityId() + " - Success: " + success[0]);
            return success[0];
            
        } catch (Exception e) {
            Log.e(TAG, "Error syncing note", e);
            return false;
        }
    }

    private boolean syncHighlight(SyncQueue syncItem) {
        try {
            Highlight highlight = gson.fromJson(syncItem.getJsonData(), Highlight.class);
            
            // Create API request
            com.se1853_jv.labverse.data.api.annotation.AnnotationApi.CreateHighlightRequest request = 
                new com.se1853_jv.labverse.data.api.annotation.AnnotationApi.CreateHighlightRequest();
            
            request.paperId = extractPaperIdFromSyncItem(syncItem);
            request.collectionId = extractCollectionIdFromSyncItem(syncItem);
            request.color = highlight.getColorCode();
            request.coordinationX = highlight.getCoordinationX().intValue();
            request.coordinationY = highlight.getCoordinationY().intValue();
            request.pageNumber = highlight.getPageNumber();
            
            AnnotationApiHandler apiHandler = new AnnotationApiHandler();
            String token = getJwtToken();
            
            if (token == null) {
                Log.w(TAG, "No JWT token available for sync");
                return false;
            }
            
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            final boolean[] success = {false};
            
            apiHandler.createHighlight(token, request, new com.se1853_jv.labverse.data.api.ApiCallback<com.se1853_jv.labverse.data.api.annotation.AnnotationApi.HighlightResponse>() {
                @Override
                public void onSuccess(com.se1853_jv.labverse.data.api.annotation.AnnotationApi.HighlightResponse data) {
                    success[0] = true;
                    latch.countDown();
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error syncing highlight: " + error);
                    success[0] = false;
                    latch.countDown();
                }
            });
            
            latch.await(10, java.util.concurrent.TimeUnit.SECONDS);
            
            Log.d(TAG, "Synced highlight: " + syncItem.getEntityId() + " - Success: " + success[0]);
            return success[0];
            
        } catch (Exception e) {
            Log.e(TAG, "Error syncing highlight", e);
            return false;
        }
    }

    private boolean syncReadingProgress(SyncQueue syncItem) {
        try {
            ReadingWorkflow workflow = gson.fromJson(syncItem.getJsonData(), ReadingWorkflow.class);
            if (workflow == null) {
                Log.e(TAG, "Failed to deserialize ReadingWorkflow from JSON");
                return false;
            }
            
            // Check if JWT token is available via SessionManager
            com.se1853_jv.labverse.data.utils.SessionManager sessionManager = 
                new com.se1853_jv.labverse.data.utils.SessionManager(getApplicationContext());
            String jwtToken = sessionManager.getToken();
            
            if (jwtToken == null || jwtToken.isEmpty()) {
                Log.w(TAG, "No JWT token available for syncing reading progress");
                return false;
            }
            
            // Create API request
            com.se1853_jv.labverse.data.dto.request.ReadingWorkflowProgressRequest request = 
                new com.se1853_jv.labverse.data.dto.request.ReadingWorkflowProgressRequest();
            request.setCollectionId(workflow.getCollectionId());
            request.setPaperId(workflow.getPaperId());
            request.setUsersid(workflow.getUserId());
            request.setLastPage(workflow.getLastPage());
            request.setProgress(workflow.getProgress());
            
            // Call API synchronously
            // ReadingWorkflowApiHandler will automatically add JWT token via interceptor
            final boolean[] success = {false};
            final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            
            com.se1853_jv.labverse.data.api.workflow.ReadingWorkflowApiHandler apiHandler = 
                new com.se1853_jv.labverse.data.api.workflow.ReadingWorkflowApiHandler(getApplicationContext());
            
            apiHandler.updateProgress(request, new com.se1853_jv.labverse.data.api.ApiCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    success[0] = true;
                    latch.countDown();
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error syncing reading progress: " + error);
                    success[0] = false;
                    latch.countDown();
                }
            });
            
            // Wait for API call to complete (max 10 seconds)
            latch.await(10, java.util.concurrent.TimeUnit.SECONDS);
            
            Log.d(TAG, "Synced reading progress: " + syncItem.getEntityId() + " - Success: " + success[0]);
            return success[0];
            
        } catch (Exception e) {
            Log.e(TAG, "Error syncing reading progress", e);
            return false;
        }
    }

    private boolean syncWorkflowStatus(SyncQueue syncItem) {
        // TODO: Implement API call để sync workflow status
        // Có thể cần tạo Workflow API endpoint trong paper-service hoặc group-service
        Log.d(TAG, "Syncing workflow status: " + syncItem.getEntityId());
        // Tạm thời return true - cần implement API endpoint cho workflow
        return true;
    }
    
    /**
     * Helper methods
     */
    private String extractPaperIdFromSyncItem(SyncQueue syncItem) {
        try {
            if ("NOTE".equals(syncItem.getSyncType())) {
                Note note = gson.fromJson(syncItem.getJsonData(), Note.class);
                return note.getPaperId() != null ? note.getPaperId() : "";
            } else if ("HIGHLIGHT".equals(syncItem.getSyncType())) {
                Highlight highlight = gson.fromJson(syncItem.getJsonData(), Highlight.class);
                return highlight.getPaperId() != null ? highlight.getPaperId() : "";
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting paperId from syncItem", e);
        }
        return "";
    }
    
    private String extractCollectionIdFromSyncItem(SyncQueue syncItem) {
        try {
            if ("NOTE".equals(syncItem.getSyncType())) {
                Note note = gson.fromJson(syncItem.getJsonData(), Note.class);
                return note.getCollectionId() != null ? note.getCollectionId() : "";
            } else if ("HIGHLIGHT".equals(syncItem.getSyncType())) {
                Highlight highlight = gson.fromJson(syncItem.getJsonData(), Highlight.class);
                return highlight.getCollectionId() != null ? highlight.getCollectionId() : "";
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting collectionId from syncItem", e);
        }
        return "";
    }
    
    private String getJwtToken() {
        com.se1853_jv.labverse.data.utils.SessionManager sessionManager = 
            new com.se1853_jv.labverse.data.utils.SessionManager(getApplicationContext());
        return sessionManager.getToken();
    }

    /**
     * Tạo OneTimeWorkRequest để trigger sync
     */
    public static OneTimeWorkRequest createSyncRequest() {
        return new OneTimeWorkRequest.Builder(SyncWorker.class)
                .addTag("SYNC_WORK")
                .build();
    }
}



