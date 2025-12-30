package com.example.pickbox.controllers;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import com.example.pickbox.services.impl.LocalStorageService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class LocalFileUploadController {
    private final LocalStorageService localStorageService;

    public LocalFileUploadController(LocalStorageService localStorageService) {
        this.localStorageService = localStorageService;
    }

    @PostMapping("/upload/chunk/{uploadId}/{chunkIndex}")
    public ResponseEntity<Map<String, String>> uploadChunk(
            @RequestAttribute(required = false) String userId,
            @PathVariable String uploadId,
            @PathVariable int chunkIndex,
            HttpServletRequest request) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            String storageId = localStorageService.uploadChunk(uploadId, chunkIndex, request.getInputStream());
            return ResponseEntity.ok(java.util.Collections.singletonMap("storageId", storageId));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
