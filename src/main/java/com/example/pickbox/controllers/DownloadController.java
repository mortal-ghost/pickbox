package com.example.pickbox.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.example.pickbox.dtos.ItemDto;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/download")
public class DownloadController {
    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> downloadFile(@PathVariable String id, 
                                            @RequestAttribute(required = false) String userId,
                                            HttpServletResponse response) {
        // To be implemented using presigned url
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/metadata")
    public ResponseEntity<ItemDto> downloadFileMetadata(@PathVariable String id, 
                                            @RequestAttribute(required = false) String userId) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/chunk/{chunkIndex}")
    public ResponseEntity<ItemDto> downloadFileChunk(@PathVariable String id, 
                                            @PathVariable int chunkIndex,
                                            @RequestAttribute(required = false) String userId,
                                            HttpServletResponse response) {
        return ResponseEntity.ok().build();
    }
}
