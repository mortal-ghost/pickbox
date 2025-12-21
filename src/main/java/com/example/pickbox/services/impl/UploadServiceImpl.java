package com.example.pickbox.services.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.pickbox.dao.FileRepository;
import com.example.pickbox.dao.UploadMetadataRepository;
import com.example.pickbox.dtos.UploadRequest;
import com.example.pickbox.dtos.UploadResponse;
import com.example.pickbox.models.ChunkMetadata;
import com.example.pickbox.models.FileStatus;
import com.example.pickbox.models.StorageItem;
import com.example.pickbox.models.UploadMetadata;
import com.example.pickbox.services.StorageService;
import com.example.pickbox.services.UploadService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UploadServiceImpl implements UploadService {
    private final FileRepository fileRepository;

    private final StorageService storageService;

    private final UploadMetadataRepository uploadMetadataRepository;

    private final long chunkSize;

    public UploadServiceImpl(FileRepository fileRepository,
            StorageService storageService,
            UploadMetadataRepository uploadMetadataRepository,
            @Value("${chunk.size}") long chunkSize) {
        this.fileRepository = fileRepository;
        this.storageService = storageService;
        this.uploadMetadataRepository = uploadMetadataRepository;
        this.chunkSize = chunkSize;
    }

    private boolean validateUser(String requestUserId, String metadataUserId) {
        return requestUserId != null && !requestUserId.isEmpty() && requestUserId.equals(metadataUserId);
    }

    @Override
    public UploadResponse initiateUpload(UploadRequest uploadRequest) {
        if (uploadRequest.getName() == null || uploadRequest.getName().isEmpty()) {
            return new UploadResponse("File name is required");
        }
        if (uploadRequest.getSize() <= 0) {
            return new UploadResponse("File size should be greater than 0");
        }

        if (uploadRequest.getParentId() != null && !fileRepository.existsById(uploadRequest.getParentId())) {
            return new UploadResponse("Parent file does not exist");
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
                    .status(FileStatus.UPLOADING)
                    .build();
            String storageId = storageService.initiateUpload(uploadMetadata.getUserId(), uploadId);
            uploadMetadata.setStorageId(storageId);
            uploadMetadataRepository.save(uploadMetadata);
        } catch (Exception e) {
            return new UploadResponse(e.getMessage());
        }
        return new UploadResponse(uploadMetadata);
    }

    @Override
    public void completeUpload(String uploadId, String userId) {
        if (uploadId == null || uploadId.isEmpty()) {
            throw new IllegalArgumentException("Upload ID is required");
        }
        UploadMetadata uploadMetadata = uploadMetadataRepository.findById(uploadId).orElse(null);
        if (uploadMetadata == null || uploadMetadata.getStatus() != FileStatus.UPLOADING) {
            // Upload already deleted or completed or aborted
            return;
        }
        if (!validateUser(userId, uploadMetadata.getUserId())) {
            throw new IllegalArgumentException("User ID does not match");
        }

        if (uploadMetadata.getTotalChunks() != uploadMetadata.getCompletedChunks().size()) {
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
    }

    @Override
    public void abortUpload(String uploadId, String userId) {
        if (uploadId == null || uploadId.isEmpty()) {
            throw new IllegalArgumentException("Upload ID is required");
        }
        UploadMetadata uploadMetadata = uploadMetadataRepository.findById(uploadId).orElse(null);
        if (uploadMetadata == null || uploadMetadata.getStatus() != FileStatus.UPLOADING) {
            // Upload already deleted or completed or aborted
            return;
        }
        if (!validateUser(userId, uploadMetadata.getUserId())) {
            throw new IllegalArgumentException("User ID does not match");
        }
        uploadMetadata.setStatus(FileStatus.DELETED);
        uploadMetadataRepository.save(uploadMetadata);
        storageService.abortUpload(uploadMetadata.getUploadId());
    }

    @Override
    public void completeChunk(String uploadId, String userId, ChunkMetadata chunkMetadata) {
        if (uploadId == null || uploadId.isEmpty()) {
            throw new IllegalArgumentException("Upload ID is required");
        }
        UploadMetadata uploadMetadata = uploadMetadataRepository.findById(uploadId).orElse(null);
        if (uploadMetadata == null || uploadMetadata.getStatus() != FileStatus.UPLOADING) {
            // Upload already deleted or completed or aborted
            return;
        }
        if (!validateUser(userId, uploadMetadata.getUserId())) {
            throw new IllegalArgumentException("User ID does not match");
        }
        uploadMetadata.getCompletedChunks().add(chunkMetadata);
        uploadMetadataRepository.save(uploadMetadata);
    }
}
