package com.project1.shriganeshaynamah.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif");

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private final Path uploadRoot;

    public FileStorageService(@Value("${app.file.upload-dir:uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not create upload directory: " + uploadRoot, ex);
        }
    }

    public String storeContactImage(MultipartFile file, int userId) {
        return storeImage(file, "contacts", userId);
    }

    public String storeUserImage(MultipartFile file, int userId) {
        return storeImage(file, "users", userId);
    }

    private String storeImage(MultipartFile file, String category, int userId) {
        validateFile(file);

        String extension = resolveExtension(file);
        String filename = UUID.randomUUID() + extension;
        Path targetDir = uploadRoot.resolve(category).resolve(String.valueOf(userId));

        try {
            Files.createDirectories(targetDir);
            Path targetFile = targetDir.resolve(filename);
            file.transferTo(targetFile);
            return "/uploads/" + category + "/" + userId + "/" + filename;
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to store image. Please try again.");
        }
    }

    public void deleteIfExists(String publicPath) {
        if (publicPath == null || publicPath.isBlank() || !publicPath.startsWith("/uploads/")) {
            return;
        }

        Path filePath = uploadRoot.resolve(publicPath.substring("/uploads/".length())).normalize();
        if (!filePath.startsWith(uploadRoot)) {
            return;
        }

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
            // Best-effort cleanup; DB record is still updated.
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select an image file.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Image must be 5 MB or smaller.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Only JPG, PNG, WEBP, and GIF images are allowed.");
        }
    }

    private String resolveExtension(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return ".jpg";
        }
        return switch (contentType.toLowerCase()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
    }

    public Path getUploadRoot() {
        return uploadRoot;
    }
}
