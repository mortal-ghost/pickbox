package com.example.pickbox.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.example.pickbox.dtos.ItemDto;
import com.example.pickbox.dtos.UploadRequest;

import jakarta.servlet.http.HttpServletResponse;

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
public class FileController {
    @PostMapping("/folder")
    public ResponseEntity<ItemDto> createFolder(@RequestBody UploadRequest uploadRequest, 
                                                @RequestAttribute(required = false) String userId) {
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> getFile(@PathVariable String id, 
                                            @RequestAttribute(required = false) String userId) {
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{id}/content")
    public ResponseEntity<ItemDto> getFileContent(@PathVariable String id, 
                                            @RequestAttribute(required = false) String userId,
                                            HttpServletResponse response) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("{id}/list")
    public ResponseEntity<List<ItemDto>> listFiles(@PathVariable String id, 
                                            @RequestAttribute(required = false) String userId) {
        return ResponseEntity.ok().build();
    }
}
