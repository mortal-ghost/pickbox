package com.example.pickbox.dao;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.pickbox.models.StorageItem;

public interface FileRepository extends MongoRepository<StorageItem, String> {

    List<StorageItem> findAllByOwnerUserIdAndParentId(String ownerUserId, String parentId);

    List<StorageItem> findAllByOwnerUserIdAndParentIdIsNull(String ownerUserId);

    List<StorageItem> findAllByOwnerUserId(String ownerUserId);

    public boolean existsByParentIdAndName(String parentId, String name);
}
