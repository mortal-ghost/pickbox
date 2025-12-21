package com.example.pickbox.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.example.pickbox.dtos.ItemDto;
import com.example.pickbox.dtos.UploadRequest;
import com.example.pickbox.services.FileService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestMapping;

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

    @GetMapping("/")
    public ResponseEntity<List<ItemDto>> getAllFilesAndFolders(@RequestAttribute(required = false) String userId) {
        return ResponseEntity.ok(fileService.listFiles(null, userId));
    }

    private void validateUser(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }
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
        // TODO: Implement get single file
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<ItemDto> getFileContent(@PathVariable String id,
            @RequestAttribute(required = false) String userId,
            HttpServletResponse response) {
        // TODO: Implement download
        return ResponseEntity.ok().build();
    }

    @GetMapping({ "/list", "/{id}/list" })
    public ResponseEntity<List<ItemDto>> listFiles(@PathVariable(required = false) String id,
            @RequestAttribute(required = false) String userId) {
        return ResponseEntity.ok(fileService.listFiles(id, userId));
    }
}
