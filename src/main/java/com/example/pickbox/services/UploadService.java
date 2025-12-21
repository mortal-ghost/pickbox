package com.example.pickbox.services;

import com.example.pickbox.dtos.UploadRequest;
import com.example.pickbox.dtos.UploadResponse;
import com.example.pickbox.models.ChunkMetadata;

public interface UploadService {
    UploadResponse initiateUpload(UploadRequest uploadRequest);
    void completeUpload(String uploadId, String userId);
    void abortUpload(String uploadId, String userId);
    void completeChunk(String uploadId, String userId, ChunkMetadata chunkMetadata);
}
