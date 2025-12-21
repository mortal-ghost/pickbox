package com.example.pickbox.services;

public interface StorageService {
    String initiateUpload(String userId, String uploadId);

    String generateSignedUrl(String uploadId, int chunkIndex);

    void completeUpload(String uploadId);

    void abortUpload(String uploadId);
}
