package com.example.pickbox.services.impl;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.pickbox.dao.FileRepository;
import com.example.pickbox.dtos.ItemDto;
import com.example.pickbox.models.StorageItem;
import com.example.pickbox.services.FileService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;

    @Override
    public ItemDto createFolder(String name, String parentId, String userId) {
        StorageItem folder = StorageItem.builder()
                .name(name)
                .ownerUserId(userId)
                .parentId(parentId)
                .type('D') // Directory
                .size(0)
                .status(com.example.pickbox.models.FileStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        StorageItem savedFolder = fileRepository.save(folder);
        return mapToDto(savedFolder);
    }

    @Override
    public List<ItemDto> listFiles(String parentId, String userId) {
        List<StorageItem> items;
        if (parentId == null || parentId.isEmpty()) {
            items = fileRepository.findAllByOwnerUserIdAndParentIdIsNull(userId);
        } else {
            items = fileRepository.findAllByOwnerUserIdAndParentId(userId, parentId);
        }

        return items.stream()
                .map(this::mapToDto)
                .toList();
    }

    private ItemDto mapToDto(StorageItem item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .type(item.getType() == 'D' ? "FOLDER" : "FILE")
                .parentId(item.getParentId())
                .mimeType(item.getType() == 'D' ? "application/folder" : item.getMimeType()) // Handle MIME for folder
                .size(item.getSize())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
