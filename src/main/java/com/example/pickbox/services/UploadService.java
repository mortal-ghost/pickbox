package com.example.pickbox.services;

import com.example.pickbox.dtos.UploadRequest;
import com.example.pickbox.dtos.UploadResponse;
import com.example.pickbox.dtos.ChunkUploadRequest;

public interface UploadService {
    UploadResponse initiateUpload(UploadRequest uploadRequest);
    void completeUpload(String uploadId);
    void abortUpload(String uploadId);
    boolean uploadChunk(ChunkUploadRequest chunkUploadRequest);
}
