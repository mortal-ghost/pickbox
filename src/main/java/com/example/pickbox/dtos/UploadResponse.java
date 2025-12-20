package com.example.pickbox.dtos;

import lombok.Data;

@Data
public class UploadResponse {
    private String uploadId;
    private String uploadUrl;
    private String fileId;
    private long chunkSize;
    private long totalChunks;
    private long totalSize;
    private String fileName;
    private String error;
    private boolean success;
}
