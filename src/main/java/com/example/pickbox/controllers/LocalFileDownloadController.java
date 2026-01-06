package com.example.pickbox.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.pickbox.services.impl.LocalStorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class LocalFileDownloadController {

    private final LocalStorageService localStorageService;

    @GetMapping("/local-download/{uploadId}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String uploadId,
            @RequestParam long expires,
            @RequestParam String signature,
            @RequestParam(required = false) String filename) {
        log.info("Download request for uploadId: {}", uploadId);
        if (!localStorageService.verifySignature(uploadId, expires, signature)) {
            log.warn("Invalid signature or expired link for uploadId: {}", uploadId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Path filePath = localStorageService.getFilePath(uploadId);
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            log.info("File exists at path: {}", filePath);

            Resource resource = new InputStreamResource(Files.newInputStream(filePath));

            String contentDisposition = "attachment";
            if (filename != null && !filename.isEmpty()) {
                // Ensure filename is safe or decoded properly (Spring might decode param
                // automatically)
                contentDisposition += "; filename=\"" + filename + "\"";
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(Files.size(filePath))
                    .body(resource);

        } catch (IOException e) {
            log.error("Failed to read file for download: {}", uploadId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
