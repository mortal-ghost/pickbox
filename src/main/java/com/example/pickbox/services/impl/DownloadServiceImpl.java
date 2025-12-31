package com.example.pickbox.services.impl;

import org.springframework.stereotype.Service;

import com.example.pickbox.constants.StorageType;
import com.example.pickbox.models.StorageItem;
import com.example.pickbox.services.DownloadService;
import com.example.pickbox.services.StorageService;
import com.example.pickbox.services.StorageServiceFactory;
import com.example.pickbox.dao.FileRepository; // Assuming this exists or use FileService

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DownloadServiceImpl implements DownloadService {

    private final StorageServiceFactory storageServiceFactory;
    private final FileRepository fileRepository;

    @Override
    public String generateDownloadUrl(String userId, String fileId) {
        StorageItem item = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        if (!item.getOwnerUserId().equals(userId)) {
            // For now simple ownership check. Should ideally handle sharing.
            throw new IllegalArgumentException("Unauthorized access to file");
        }

        // Defaulting to LOCAL storage as it's the only one implemented effectively
        StorageService storageService = storageServiceFactory.getStorageService(StorageType.LOCAL);

        // Use storageKey if present (for finalized uploads), fallback to ID if it IS
        // the key
        String storageKey = item.getStorageKey() != null ? item.getStorageKey() : item.getId();

        return storageService.generateDownloadUrl(storageKey, item.getName());
    }
}
