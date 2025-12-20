package com.example.pickbox.dtos;

import lombok.Data;

@Data
public class ChunkUploadRequest {
    private String uploadId;
    private int chunkIndex;
    private String fileName;
    private String mimeType;
    private long fileSize;
}
