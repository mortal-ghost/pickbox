package com.example.pickbox.constants;

import lombok.Getter;

public enum StorageType {
    LOCAL("local"),
    S3("s3");

    @Getter
    private final String value;

    StorageType(String value) {
        this.value = value;
    }
}
