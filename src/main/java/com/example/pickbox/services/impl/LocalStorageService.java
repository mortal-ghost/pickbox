package com.example.pickbox.services.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Base64;
import java.util.Comparator;
import java.util.stream.Stream;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.pickbox.services.StorageService;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LocalStorageService implements StorageService {
    private final String uploadDirectory;

    private final String baseUploadUrl;

    private final String secretKey;

    private final String baseUrl;

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
            @Value("${local.upload.url}") String baseUploadUrl,
            @Value("${local.storage.secret-key}") String secretKey,
            @Value("${local.base.url}") String baseUrl) {
        this.uploadDirectory = uploadDirectory;
        this.baseUploadUrl = baseUploadUrl;
        this.secretKey = secretKey;
        this.baseUrl = baseUrl;
    }

    @PostConstruct
    private void init() {
        Path uploadDir = Path.of(uploadDirectory);
        if (!uploadDir.toFile().exists()) {
            boolean created = uploadDir.toFile().mkdirs();
            log.info("Initialized upload directory: {}, created: {}", uploadDirectory, created);
        }
    }

    @Override
    public String initiateUpload(String userId, String uploadId) {
        log.info("Initiating local upload. UserId: {}, UploadId: {}", userId, uploadId);
        // Correctly construct the temp chunk path relative to uploadDirectory
        Path tempChunkPath = Path.of(uploadDirectory, TEMP_CHUNK_DIR + uploadId);
        try {
            boolean created = tempChunkPath.toFile().mkdirs();
            log.info("Created temp chunk directory: {}, success: {}", tempChunkPath, created);
        } catch (Exception e) {
            log.error("Failed to create temp chunk directory: {}", tempChunkPath, e);
            throw new RuntimeException(e);
        }
        return uploadId;
    }

    @Override
    public String generateSignedUrl(String uploadId, int chunkIndex) {
        return baseUploadUrl + "/" + uploadId + "/" + chunkIndex;
    }

    @Override
    public void completeUpload(String uploadId) {
        log.info("Completing local upload: {}", uploadId);
        Path finalFilePath = Path.of(uploadDirectory, uploadId);
        Path tempChunkPath = Path.of(uploadDirectory, TEMP_CHUNK_DIR + uploadId);
        Path tempMergeFile = Path.of(uploadDirectory, uploadId + TEMP_CHUNK_DIR);

        try (OutputStream outputStream = Files.newOutputStream(tempMergeFile);
                Stream<Path> chunkPaths = Files.list(tempChunkPath)) {

            // Log the number of chunks found
            // Since stream can be consumed once, we might want to collect to verify count
            // or just trust the process and log exceptions

            chunkPaths.filter(path -> path.getFileName().toString().matches("\\d+"))
                    .sorted(Comparator.comparingInt(path -> Integer.parseInt(path.getFileName().toString())))
                    .forEachOrdered(path -> {
                        try {
                            log.debug("Merging chunk: {}", path);
                            Files.copy(path, outputStream);
                        } catch (Exception e) {
                            log.error("Failed to merge chunk: {}", path, e);
                            throw new RuntimeException(e);
                        }
                    });
            log.info("Chunks merged successfully to temporary file: {}", tempMergeFile);

        } catch (Exception e) {
            log.error("Failed to merge chunks for upload: {}", uploadId, e);
            try {
                Files.deleteIfExists(tempMergeFile);
            } catch (Exception ignored) {
                log.warn("Failed to delete temp merge file after merge failure: {}", tempMergeFile);
            }
            throw new RuntimeException("Failed to merge chunks", e);
        }

        // Atomic move to final destination
        try {
            Files.move(tempMergeFile, finalFilePath, ATOMIC_MOVE);
            log.info("Moved merged file to final destination: {}", finalFilePath);
        } catch (Exception e) {
            log.error("Failed to move merged file to final destination", e);
            try {
                Files.deleteIfExists(tempMergeFile);
            } catch (Exception ignored) {
                log.warn("Failed to delete temp merge file after move failure: {}", tempMergeFile);
            }
            throw new RuntimeException("Failed to finalize upload file", e);
        }

        // Cleanup chunks and temp directory
        try (Stream<Path> paths = Files.walk(tempChunkPath)) {
            paths.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(file -> {
                        boolean deleted = file.delete();
                        if (!deleted) {
                            log.warn("Failed to delete temp file during cleanup: {}", file);
                        }
                    });
            log.info("Cleaned up temp chunks for upload: {}", uploadId);
        } catch (Exception e) {
            log.error("Failed to clean up temp chunks for upload: {}", uploadId, e);
        }
    }

    @Override
    public void abortUpload(String uploadId) {
        log.info("Aborting local upload: {}", uploadId);
        Path filePath = Path.of(uploadDirectory, uploadId);
        Path tempMergeFile = Path.of(uploadDirectory, uploadId + TEMP_CHUNK_DIR);
        Path tempChunkPath = Path.of(uploadDirectory, TEMP_CHUNK_DIR + uploadId);
        try {
            Files.deleteIfExists(filePath);
            Files.deleteIfExists(tempMergeFile);
            if (Files.exists(tempChunkPath)) {
                try (Stream<Path> paths = Files.walk(tempChunkPath)) {
                    paths.sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(java.io.File::delete);
                }
            }
            log.info("Aborted upload and cleaned up files: {}", uploadId);
        } catch (Exception e) {
            log.error("Failed to abort upload: {}", uploadId, e);
        }
    }

    public String uploadChunk(String uploadId, int chunkIndex, InputStream inputStream) {
        log.info("Uploading chunk. UploadId: {}, Index: {}", uploadId, chunkIndex);
        Path chunkPath = Path.of(uploadDirectory, TEMP_CHUNK_DIR + uploadId, String.valueOf(chunkIndex));
        try (inputStream) {
            Files.copy(inputStream, chunkPath, StandardCopyOption.REPLACE_EXISTING);
            Files.setPosixFilePermissions(chunkPath, PosixFilePermissions.fromString(FILE_PERMISSIONS_STRING));
            log.info("Chunk uploaded successfully: {}", chunkPath);
        } catch (Exception e) {
            log.error("Failed to upload chunk: {}", chunkPath, e);
            throw new RuntimeException(e);
        }
        return chunkPath.getFileName().toString();
    }

    @Override
    public String generateDownloadUrl(String uploadId, String originalFileName) {
        long expiration = System.currentTimeMillis() + 3600 * 1000; // 1 hour
        String signature = generateSignature(uploadId, expiration);

        // encode filename
        String encodedFilename = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8);

        return String.format("%s/local-download/%s?expires=%d&signature=%s&filename=%s",
                baseUrl, uploadId, expiration, signature, encodedFilename);
    }

    public boolean verifySignature(String uploadId, long expiration, String signature) {
        if (System.currentTimeMillis() > expiration) {
            return false;
        }
        String expectedSignature = generateSignature(uploadId, expiration);
        return expectedSignature.equals(signature);
    }

    private String generateSignature(String uploadId, long expiration) {
        try {
            String data = uploadId + ":" + expiration;
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(this.secretKey.getBytes(),
                    "HmacSHA256");
            hmac.init(secretKeySpec);
            byte[] hash = hmac.doFinal(data.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    public Path getFilePath(String uploadId) {
        return Path.of(uploadDirectory, uploadId);
    }
}
