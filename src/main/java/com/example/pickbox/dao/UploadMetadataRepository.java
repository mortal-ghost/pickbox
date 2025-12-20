package com.example.pickbox.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.pickbox.models.UploadMetadata;

public interface UploadMetadataRepository extends MongoRepository<UploadMetadata, String> {
    
}
