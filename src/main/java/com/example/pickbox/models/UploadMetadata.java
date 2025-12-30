package com.example.pickbox.models;

import java.util.List;
import java.time.Instant;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
@Document(collection = "uploads")
public class UploadMetadata {
    @Id
    @Field("_id")
    private String uploadId;

    private String fileName;
    private String parentId;
    private long fileSize;

    private List<ChunkMetadata> completedChunks;
    private long totalChunks;
    private long chunkSize;

    private String mimeType;
    private String storageId;
    private String userId;

    private FileStatus status;

    private Instant createdAt;
    private Instant updatedAt;
}
