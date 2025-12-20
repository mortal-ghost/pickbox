package com.example.pickbox.services;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.example.pickbox.constants.StorageType;
import com.example.pickbox.services.impl.LocalStorageService;

@Component
public class StorageServiceFactory implements ApplicationContextAware {
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static StorageService getStorageService(StorageType storageType) {
        if (storageType == null) {
            throw new IllegalArgumentException("StorageType cannot be null");
        }
        switch (storageType) {
            case LOCAL:
                return context.getBean(LocalStorageService.class);
            default:
                throw new IllegalArgumentException("Invalid storage type: " + storageType);
        }
    }
}
