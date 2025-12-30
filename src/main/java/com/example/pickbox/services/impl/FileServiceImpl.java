package com.example.pickbox.services.impl;

import java.io.OutputStream;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.pickbox.constants.StorageType;
import com.example.pickbox.dao.FileRepository;
import com.example.pickbox.dtos.ItemDto;
import com.example.pickbox.models.FileStatus;
import com.example.pickbox.models.StorageItem;
import com.example.pickbox.services.FileService;
import com.example.pickbox.services.StorageService;
import com.example.pickbox.services.StorageServiceFactory;

@Service
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;

    private final StorageService storageService;

    public FileServiceImpl(FileRepository fileRepository, 
        StorageServiceFactory storageServiceFactory,
        @Value("${storage.type}") String storageType) {
        this.fileRepository = fileRepository;
        this.storageService = storageServiceFactory.getStorageService(StorageType.valueOf(storageType.toUpperCase()));
    }

    @Override
    public ItemDto createFolder(String name, String parentId, String userId) {
        // Should validate if the parent exists
        if (parentId != null && !fileRepository.existsById(parentId)) {
            throw new RuntimeException("Parent not found");
        }
        // parentId - name should be unique
        if (parentId != null && fileRepository.existsByParentIdAndName(parentId, name)) {
            throw new RuntimeException("Folder already exists");
        }
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

    @Override
    public ItemDto getFile(String id, String userId) {
        StorageItem item = fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));
        return mapToDto(item);
    }

    @Override
    public List<ItemDto> listAllFiles(String userId) {
        List<StorageItem> items = fileRepository.findAllByOwnerUserId(userId);
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

    @Override
    public void getFileContent(String id, String userId, OutputStream os) {
        StorageItem item = fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));
        if (item.getType() == 'D') {
            throw new RuntimeException("Cannot preview a directory");
        }
        if (item.getStatus() != FileStatus.ACTIVE) {
            throw new RuntimeException("File is not active");
        }
        if (item.getSize() > 1024 * 1024) {
            throw new RuntimeException("File is too large to preview");
        }
    }
}
