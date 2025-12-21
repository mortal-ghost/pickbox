package com.example.pickbox.dao;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.example.pickbox.models.StorageItem;
import org.springframework.lang.NonNull;

public interface FileRepository extends MongoRepository<StorageItem, String> {
    @Query("{ '_id' : ?0 }")
    public boolean existsById(@NonNull String id);

    List<StorageItem> findAllByOwnerUserIdAndParentId(String ownerUserId, String parentId);

    List<StorageItem> findAllByOwnerUserIdAndParentIdIsNull(String ownerUserId);

    List<StorageItem> findAllByOwnerUserId(String ownerUserId);

    public boolean existsByParentIdAndName(String parentId, String name);
}
