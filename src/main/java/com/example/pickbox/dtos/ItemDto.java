package com.example.pickbox.dtos;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private String id;
    private String name;
    private String type;
    private String parentId;
    private long size;
    private String mimeType;
    private Instant createdAt;
    private Instant updatedAt;
}
