package com.example.pickbox.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.example.pickbox.dtos.ItemDto;
import com.example.pickbox.dtos.UploadRequest;
import com.example.pickbox.services.FileService;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    private void validateUser(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }
    }

    @GetMapping("/")
    public ResponseEntity<List<ItemDto>> getAllFilesAndFolders(@RequestAttribute(required = false) String userId) {
        validateUser(userId);
        return ResponseEntity.ok(fileService.listAllFiles(userId));
    }

    @PostMapping("/folder")
    public ResponseEntity<ItemDto> createFolder(@RequestBody UploadRequest uploadRequest,
            @RequestAttribute(required = false) String userId) {
        validateUser(userId);
        return ResponseEntity
                .ok(fileService.createFolder(uploadRequest.getName(), uploadRequest.getParentId(), userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> getFile(@PathVariable String id,
            @RequestAttribute(required = false) String userId) {
        validateUser(userId);
        return ResponseEntity.ok(fileService.getFile(id, userId));
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<Void> getFileContent(@PathVariable String id,
            @RequestAttribute(required = false) String userId,
            HttpServletResponse response) {
        validateUser(userId);
        try (ServletOutputStream os = response.getOutputStream()) {
            fileService.getFileContent(id, userId, os);
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + id);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to download file", e);
        }
    }

    @GetMapping({ "/list", "/{id}/list" })
    public ResponseEntity<List<ItemDto>> listFiles(@PathVariable(required = false) String id,
            @RequestAttribute(required = false) String userId) {
        validateUser(userId);
        return ResponseEntity.ok(fileService.listFiles(id, userId));
    }
}
