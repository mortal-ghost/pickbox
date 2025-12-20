package com.example.pickbox.dtos;

import lombok.Data;

@Data
public class UploadRequest {
    private String name;
    private long size;
    private String userId;
    private String parentId;
}
