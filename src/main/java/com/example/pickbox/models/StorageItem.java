package com.example.pickbox.models;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;
import com.mongodb.lang.Nullable;

import lombok.Builder;
import lombok.Data;

@Document(collection = "files")
@Data
@Builder
public class StorageItem {
    @Id
    private String id;

    @NonNull
    private String name;

    @NonNull
    private String ownerUserId;

    @NonNull
    private long size;

    @Nullable
    private String mimeType;

    private String storageKey;

    @Nullable
    private String storageVersion;

    @Nullable
    private String bucketName;

    @Nullable
    private String parentId;

    // 'F' for file and 'D' for directory
    private char type;

    @NonNull
    private FileStatus status;

    private Instant createdAt;

    private Instant updatedAt;
}
