package com.example.pickbox.services.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.pickbox.constants.StorageType;
import com.example.pickbox.dao.FileRepository;
import com.example.pickbox.dao.UploadMetadataRepository;
import com.example.pickbox.dtos.UploadRequest;
import com.example.pickbox.dtos.UploadResponse;
import com.example.pickbox.models.ChunkMetadata;
import com.example.pickbox.models.FileStatus;
import com.example.pickbox.models.StorageItem;
import com.example.pickbox.models.UploadMetadata;
import com.example.pickbox.services.StorageService;
import com.example.pickbox.services.StorageServiceFactory;
import com.example.pickbox.services.UploadService;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import com.mongodb.client.result.UpdateResult;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UploadServiceImpl implements UploadService {
    private final FileRepository fileRepository;

    private final StorageService storageService;

    private final UploadMetadataRepository uploadMetadataRepository;

    private final long chunkSize;

    private final MongoTemplate mongoTemplate;

    public UploadServiceImpl(FileRepository fileRepository,
            StorageServiceFactory storageServiceFactory,
            UploadMetadataRepository uploadMetadataRepository,
            MongoTemplate mongoTemplate,
            @Value("${chunk.size}") long chunkSize) {
        this.fileRepository = fileRepository;
        this.storageService = storageServiceFactory.getStorageService(StorageType.LOCAL);
        this.uploadMetadataRepository = uploadMetadataRepository;
        this.mongoTemplate = mongoTemplate;
        this.chunkSize = chunkSize;
    }

    // ... (rest of the file until completeChunk)

    @Override
    public void completeChunk(String uploadId, String userId, ChunkMetadata chunkMetadata) {
        log.info("Completing chunk {} for upload {}", chunkMetadata.getChunkIndex(), uploadId);
        if (uploadId == null || uploadId.isEmpty()) {
            log.error("Upload ID is required for chunk completion");
            throw new IllegalArgumentException("Upload ID is required");
        }

        // Use atomic update to avoid race conditions
        Query query = new Query(
                Criteria.where("_id").is(uploadId)
                        .and("status").is(FileStatus.UPLOADING)
                        .and("userId").is(userId)
                        .and("completedChunks.chunkIndex").ne(chunkMetadata.getChunkIndex()));

        Update update = new Update();
        update.push("completedChunks", chunkMetadata);

        UpdateResult result = mongoTemplate.updateFirst(query, update, UploadMetadata.class);

        if (result.getMatchedCount() == 0) {
            // Check if it failed because chunk already exists (Idempotency)
            Query duplicateCheckQuery = new Query(
                    Criteria.where("_id").is(uploadId)
                            .and("completedChunks.chunkIndex").is(chunkMetadata.getChunkIndex()));

            if (mongoTemplate.exists(duplicateCheckQuery, UploadMetadata.class)) {
                log.info("Chunk {} already exists for upload {}. Ignoring duplicate.", chunkMetadata.getChunkIndex(),
                        uploadId);
                return;
            }

            // If no document matched and not a duplicate, it means either:
            // 1. Upload ID doesn't exist
            // 2. Status is not UPLOADING
            // 3. UserId doesn't match
            log.error(
                    "Failed to complete chunk. Upload not found, invalid state/user, or concurrent modification. UploadId: {}",
                    uploadId);
            throw new IllegalArgumentException("Upload already deleted or completed or aborted");
        }
    }

    private boolean validateUser(String requestUserId, String metadataUserId) {
        return requestUserId != null && !requestUserId.isEmpty() && requestUserId.equals(metadataUserId);
    }

    @Override
    public UploadResponse initiateUpload(UploadRequest uploadRequest) {
        log.info("Initiating upload for file: {}, user: {}", uploadRequest.getName(), uploadRequest.getUserId());
        if (uploadRequest.getName() == null || uploadRequest.getName().isEmpty()) {
            log.error("File name is required");
            throw new IllegalArgumentException("File name is required");
        }
        if (uploadRequest.getSize() <= 0) {
            log.error("File size should be greater than 0");
            throw new IllegalArgumentException("File size should be greater than 0");
        }

        if (uploadRequest.getParentId() != null && !fileRepository.existsById(uploadRequest.getParentId())) {
            log.error("Parent file does not exist: {}", uploadRequest.getParentId());
            throw new IllegalArgumentException("Parent file does not exist");
        }
        UploadMetadata uploadMetadata;
        try {
            String uploadId = UUID.randomUUID().toString().replace("-", "");
            uploadMetadata = UploadMetadata.builder()
                    .uploadId(uploadId)
                    .fileName(uploadRequest.getName())
                    .fileSize(uploadRequest.getSize())
                    .totalChunks((int) Math.ceil((double) uploadRequest.getSize() / chunkSize))
                    .userId(uploadRequest.getUserId())
                    .parentId(uploadRequest.getParentId())
                    .completedChunks(new ArrayList<>())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .chunkSize(chunkSize)
                    .mimeType(uploadRequest.getMimeType())
                    .status(FileStatus.UPLOADING)
                    .build();
            String storageId = storageService.initiateUpload(uploadMetadata.getUserId(), uploadId);
            uploadMetadata.setStorageId(storageId);
            uploadMetadataRepository.save(uploadMetadata);
            log.info("Upload metadata saved: {}, file name: {}, file size: {}", uploadId, uploadMetadata.getFileName(),
                    uploadMetadata.getFileSize());
        } catch (Exception e) {
            log.error("Failed to initiate upload", e);
            throw new RuntimeException("Failed to initiate upload", e);
        }
        return new UploadResponse(uploadMetadata);
    }

    @Override
    public void completeUpload(String uploadId, String userId) {
        log.info("Completing upload: {}", uploadId);
        if (uploadId == null || uploadId.isEmpty()) {
            log.error("Upload ID is required for completion");
            throw new IllegalArgumentException("Upload ID is required");
        }
        UploadMetadata uploadMetadata = uploadMetadataRepository.findById(uploadId).orElse(null);
        if (uploadMetadata == null || uploadMetadata.getStatus() != FileStatus.UPLOADING) {
            // Upload already deleted or completed or aborted
            log.error("Upload already deleted or completed or aborted for upload: {}", uploadId);
            throw new IllegalArgumentException("Upload already deleted or completed or aborted");
        }
        if (!validateUser(userId, uploadMetadata.getUserId())) {
            log.error("User ID does not match for upload: {}, expected: {}, actual: {}", uploadId,
                    uploadMetadata.getUserId(), userId);
            throw new IllegalArgumentException("User ID does not match");
        }

        if (uploadMetadata.getTotalChunks() != uploadMetadata.getCompletedChunks().size()) {
            log.error("All chunks are not uploaded for upload: {}. Expected: {}, Actual: {}", uploadId,
                    uploadMetadata.getTotalChunks(), uploadMetadata.getCompletedChunks().size());
            throw new IllegalArgumentException("All chunks are not uploaded");
        }

        // Storage First: Execute S3/Storage completion with Retry Logic
        int maxRetries = 3;
        int attempt = 0;
        boolean success = false;

        while (attempt < maxRetries) {
            try {
                storageService.completeUpload(uploadMetadata.getStorageId());
                success = true;
                break;
            } catch (Exception e) {
                attempt++;
                log.error("Storage completion failed (attempt {}/{}): {}", attempt, maxRetries, e.getMessage());
                if (attempt >= maxRetries) {
                    throw new RuntimeException("Failed to complete upload after " + maxRetries + " attempts", e);
                }
                try {
                    Thread.sleep(2000); // Wait 2 seconds before retry
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Upload completion interrupted", ie);
                }
            }
        }

        if (!success) {
            log.error("Failed to complete upload after retries: {}", uploadId);
            throw new RuntimeException("Failed to complete upload");
        }

        uploadMetadata.setStatus(FileStatus.DELETED);
        uploadMetadata.setUpdatedAt(Instant.now());
        uploadMetadataRepository.save(uploadMetadata);

        StorageItem storageItem = StorageItem.builder()
                .id(uploadMetadata.getUploadId())
                .storageKey(uploadMetadata.getStorageId())
                .storageVersion(uploadMetadata.getUploadId())
                .bucketName("pickbox")
                .name(uploadMetadata.getFileName())
                .mimeType(uploadMetadata.getMimeType())
                .size(uploadMetadata.getFileSize())
                .status(FileStatus.ACTIVE)
                .ownerUserId(uploadMetadata.getUserId())
                .createdAt(uploadMetadata.getCreatedAt())
                .updatedAt(uploadMetadata.getUpdatedAt())
                .build();
        fileRepository.save(storageItem);
        log.info("Upload completed successfully: {}", uploadId);
    }

    @Override
    public void abortUpload(String uploadId, String userId) {
        log.info("Aborting upload: {}", uploadId);
        if (uploadId == null || uploadId.isEmpty()) {
            log.error("Upload ID is required for abort");
            throw new IllegalArgumentException("Upload ID is required");
        }
        UploadMetadata uploadMetadata = uploadMetadataRepository.findById(uploadId).orElse(null);
        if (uploadMetadata == null || uploadMetadata.getStatus() != FileStatus.UPLOADING) {
            // Upload already deleted or completed or aborted
            return;
        }
        if (!validateUser(userId, uploadMetadata.getUserId())) {
            log.error("User ID does not match for abort: {}, expected: {}, actual: {}", uploadId,
                    uploadMetadata.getUserId(), userId);
            throw new IllegalArgumentException("User ID does not match");
        }
        uploadMetadata.setStatus(FileStatus.DELETED);
        uploadMetadataRepository.save(uploadMetadata);
    }
}
