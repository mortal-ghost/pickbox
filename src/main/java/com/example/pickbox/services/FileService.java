package com.example.pickbox.services;

import java.util.List;

import com.example.pickbox.dtos.ItemDto;

public interface FileService {
    ItemDto createFolder(String name, String parentId, String userId);

    List<ItemDto> listFiles(String parentId, String userId);
}
