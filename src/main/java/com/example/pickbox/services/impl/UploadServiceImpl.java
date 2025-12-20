package com.example.pickbox.services.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.pickbox.dao.FileRepository;
import com.example.pickbox.dao.UploadMetadataRepository;
import com.example.pickbox.dtos.ChunkUploadRequest;
import com.example.pickbox.dtos.UploadRequest;
import com.example.pickbox.dtos.UploadResponse;
import com.example.pickbox.models.FileStatus;
import com.example.pickbox.models.UploadMetadata;
import com.example.pickbox.services.StorageService;
import com.example.pickbox.services.UploadService;

@Service
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
                    .fileName(uploadRequest.getName())
                    .fileSize(uploadRequest.getSize())
                    .totalChunks((int) Math.ceil((double) uploadRequest.getSize() / chunkSize))
                    .userId(uploadRequest.getUserId())
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
    public void completeUpload(String uploadId) {
    }

    @Override
    public void abortUpload(String uploadId) {
        if (uploadId == null || uploadId.isEmpty()) {
            throw new IllegalArgumentException("Upload ID is required");
        }
        UploadMetadata uploadMetadata = uploadMetadataRepository.findById(uploadId).orElse(null);
        if (uploadMetadata == null || uploadMetadata.getStatus() != FileStatus.UPLOADING) {
            // Upload already deleted or completed or aborted
            return;
        }
        uploadMetadata.setStatus(FileStatus.DELETED);
        uploadMetadataRepository.save(uploadMetadata);
        storageService.abortUpload(uploadMetadata.getUploadId());
    }

    @Override
    public boolean uploadChunk(ChunkUploadRequest chunkUploadRequest) {
        return false;
    }
}
