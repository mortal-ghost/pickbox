package com.example.pickbox.dao;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.example.pickbox.models.StorageItem;
import org.springframework.lang.NonNull;

public interface FileRepository extends MongoRepository<StorageItem, String> {
    @Query("{ '_id' : ?0 }")
    public boolean existsById(@NonNull String id);
}
