package com.example.pickbox.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.pickbox.dtos.UploadRequest;
import com.example.pickbox.dtos.UploadResponse;

@RestController
public class UploadController {
    public UploadController() {
    }

    private boolean validateAuth(String userId) {
        return userId != null && !userId.isEmpty();
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> initializeUpload(@RequestAttribute("userId") String userId,
            @RequestBody UploadRequest uploadRequest) {
        if (!validateAuth(userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new UploadResponse());
    }

    @PostMapping("/upload/chunk/{uploadId}/{chunkIndex}")
    public ResponseEntity<UploadResponse> uploadChunk(@RequestAttribute("userId") String userId,
            @PathVariable String uploadId,
            @PathVariable int chunkIndex, @RequestBody UploadRequest uploadRequest) {
        if (!validateAuth(userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new UploadResponse());
    }

    @PostMapping("/upload/complete/{uploadId}")
    public ResponseEntity<UploadResponse> completeUpload(@RequestAttribute("userId") String userId,
            @PathVariable String uploadId) {
        if (!validateAuth(userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new UploadResponse());
    }

    @PostMapping("/upload/abort/{uploadId}")
    public ResponseEntity<UploadResponse> abortUpload(@RequestAttribute("userId") String userId,
            @PathVariable String uploadId) {
        if (!validateAuth(userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new UploadResponse());
    }
}
