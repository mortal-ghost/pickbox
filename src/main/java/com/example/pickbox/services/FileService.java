package com.example.pickbox.services;

import java.io.OutputStream;
import java.util.List;

import com.example.pickbox.dtos.ItemDto;

public interface FileService {
    ItemDto createFolder(String name, String parentId, String userId);

    List<ItemDto> listFiles(String parentId, String userId);

    ItemDto getFile(String id, String userId);

    List<ItemDto> listAllFiles(String userId);

    void getFileContent(String id, String userId, OutputStream os);
}
