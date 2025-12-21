package com.example.pickbox.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.pickbox.dtos.UploadRequest;
import com.example.pickbox.dtos.UploadResponse;
import com.example.pickbox.models.ChunkMetadata;
import com.example.pickbox.services.UploadService;

@RestController
@RequestMapping("/upload")
public class UploadController {
    private final UploadService uploadService;
    
    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    private boolean validateAuth(String userId) {
        return userId != null && !userId.isEmpty();
    }

    @PostMapping("/init")
    public ResponseEntity<UploadResponse> initializeUpload(
            @RequestAttribute(required = false) String userId,
            @RequestBody UploadRequest uploadRequest) {
        if (!validateAuth(userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Validations pending
        return ResponseEntity.ok(uploadService.initiateUpload(uploadRequest));
    }

    @PostMapping("/chunk/{uploadId}/{chunkIndex}/complete")
    public ResponseEntity<Boolean> completeChunk(
            @RequestAttribute(required = false) String userId,
            @PathVariable String uploadId,
            @PathVariable int chunkIndex, @RequestBody ChunkMetadata chunkMetadata) {
        if (!validateAuth(userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        uploadService.completeChunk(uploadId, userId, chunkMetadata);
        return ResponseEntity.ok(true);
    }

    @GetMapping("/complete/{uploadId}")
    public ResponseEntity<Boolean> completeUpload(
            @RequestAttribute(required = false) String userId,
            @PathVariable String uploadId) {
        if (!validateAuth(userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        uploadService.completeUpload(uploadId, userId);
        return ResponseEntity.ok(true);
    }

    @GetMapping("/abort/{uploadId}")
    public ResponseEntity<Boolean> abortUpload(
            @RequestAttribute(required = false) String userId,
            @PathVariable String uploadId) {
        if (!validateAuth(userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        uploadService.abortUpload(uploadId, userId);
        return ResponseEntity.ok(true);
    }
}
