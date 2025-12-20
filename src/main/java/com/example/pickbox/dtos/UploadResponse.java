package com.example.pickbox.dtos;

import com.example.pickbox.models.UploadMetadata;

import lombok.Data;

@Data
public class UploadResponse {
    private String uploadId;
    private String fileId;
    private long chunkSize;
    private long totalChunks;
    private long totalSize;
    private String fileName;
    private String error;
    private boolean success;

    public UploadResponse(UploadMetadata uploadMetadata) {
        uploadId = uploadMetadata.getUploadId();
        chunkSize = uploadMetadata.getChunkSize();
        totalChunks = uploadMetadata.getTotalChunks();
        totalSize = uploadMetadata.getFileSize();
        fileName = uploadMetadata.getFileName();
        success = true;
    }

    public UploadResponse(String error) {
        this.error = error;
        success = false;
    }

    public UploadResponse() {
        success = true;
    }
}
