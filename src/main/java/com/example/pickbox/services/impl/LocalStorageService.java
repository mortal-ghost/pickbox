package com.example.pickbox.services.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Comparator;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;

import com.example.pickbox.services.StorageService;

import jakarta.annotation.PostConstruct;

@Service
public class LocalStorageService implements StorageService {
    private final String uploadDirectory;

    private final String baseUploadUrl;

    private static final String TEMP_CHUNK_DIR = ".tmp";

    /**
     * File Permissions: rw------- (600)
     * Why:
     * 1. Security: Only the owner (the application process) should be able to
     * read/write the files.
     * 2. Privacy: Prevents other users on the system from accessing sensitive user
     * data.
     * 3. Safety: No Execute (x) permission prevents execution of malicious scripts
     * if uploaded.
     */
    private static final String FILE_PERMISSIONS_STRING = "rw-------";

    public LocalStorageService(@Value("${local.storage.path}") String uploadDirectory,
            @Value("${local.upload.url}") String baseUploadUrl) {
        this.uploadDirectory = uploadDirectory;
        this.baseUploadUrl = baseUploadUrl;
    }

    @PostConstruct
    private void init() {
        Path uploadDir = Path.of(uploadDirectory);
        if (!uploadDir.toFile().exists()) {
            uploadDir.toFile().mkdirs();
        }
    }

    @Override
    public String initiateUpload(String userId, String uploadId) {
        Path filePath = Path.of(uploadDirectory, uploadId);
        Path tempChunkPath = Path.of(uploadDirectory, TEMP_CHUNK_DIR + uploadId);
        try {
            tempChunkPath.toFile().mkdirs();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return filePath.toString();
    }

    @Override
    public String generateSignedUrl(String uploadId, int chunkIndex) {
        return baseUploadUrl + "/" + uploadId + "/" + chunkIndex;
    }

    @Override
    public void completeUpload(String uploadId) {
        Path finalFilePath = Path.of(uploadDirectory, uploadId);
        Path tempChunkPath = Path.of(uploadDirectory, TEMP_CHUNK_DIR + uploadId);
        // Write to a temporary merge file first
        Path tempMergeFile = Path.of(uploadDirectory, uploadId + TEMP_CHUNK_DIR);

        try (OutputStream outputStream = Files.newOutputStream(tempMergeFile);
                Stream<Path> chunkPaths = Files.list(tempChunkPath)) {

            chunkPaths.sorted(Comparator.comparingInt(path -> Integer.parseInt(path.getFileName().toString())))
                    .forEachOrdered(path -> {
                        try {
                            Files.copy(path, outputStream);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });

        } catch (Exception e) {
            try {
                Files.deleteIfExists(tempMergeFile);
            } catch (Exception ignored) {
            }
            throw new RuntimeException("Failed to merge chunks", e);
        }

        // Atomic move to final destination
        try {
            Files.move(tempMergeFile, finalFilePath, ATOMIC_MOVE);
        } catch (Exception e) {
            try {
                Files.deleteIfExists(tempMergeFile);
            } catch (Exception ignored) {
            }
            throw new RuntimeException("Failed to finalize upload file", e);
        }

        // Cleanup chunks and temp directory
        try (Stream<Path> paths = Files.walk(tempChunkPath)) {
            paths.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(java.io.File::delete);
        } catch (Exception e) {
            System.out.println("Failed to clean up temp chunks: " + e.getMessage());
        }
    }

    @Override
    public void abortUpload(String uploadId) {
        Path filePath = Path.of(uploadDirectory, uploadId);
        Path tempMergeFile = Path.of(uploadDirectory, uploadId + TEMP_CHUNK_DIR);
        Path tempChunkPath = Path.of(uploadDirectory, TEMP_CHUNK_DIR + uploadId);
        try {
            Files.deleteIfExists(filePath);
            Files.deleteIfExists(tempMergeFile);
            Files.list(tempChunkPath).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            });
            Files.deleteIfExists(tempChunkPath);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void uploadChunk(String uploadId, int chunkIndex, InputStream inputStream) {
        Path chunkPath = Path.of(uploadDirectory, TEMP_CHUNK_DIR + uploadId, String.valueOf(chunkIndex));
        try (inputStream) {
            Files.copy(inputStream, chunkPath, StandardCopyOption.REPLACE_EXISTING);
            Files.setPosixFilePermissions(chunkPath, PosixFilePermissions.fromString(FILE_PERMISSIONS_STRING));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
