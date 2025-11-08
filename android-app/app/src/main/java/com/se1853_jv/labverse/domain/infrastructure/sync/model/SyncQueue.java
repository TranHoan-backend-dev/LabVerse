package com.se1853_jv.labverse.domain.infrastructure.sync.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Entity để track các thay đổi cần sync lên server khi có internet
 */
@Getter
@Setter
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SyncQueue {
    @PrimaryKey(autoGenerate = true)
    Long id;

    @NonNull
    String syncType; // "NOTE", "HIGHLIGHT", "READING_PROGRESS", "WORKFLOW_STATUS"

    @NonNull
    String entityId; // ID của Note, Highlight, ReadingWorkflow, etc.

    @NonNull
    String operation; // "CREATE", "UPDATE", "DELETE"

    @NonNull
    String jsonData; // JSON data của entity cần sync

    Long createdAt; // Timestamp khi tạo

    Boolean isSynced; // Đã sync chưa

    Integer retryCount; // Số lần retry
}


