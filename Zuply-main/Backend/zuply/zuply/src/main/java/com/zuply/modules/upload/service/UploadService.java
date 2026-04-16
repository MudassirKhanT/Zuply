package com.zuply.modules.upload.service;

import com.zuply.modules.upload.dto.ImageStatus;
import com.zuply.modules.upload.dto.UploadResponse;
import com.zuply.modules.upload.model.Image;
import com.zuply.modules.upload.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class UploadService {

    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png");
    private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024; // 10MB

    @Value("${upload.path}")
    private String uploadPath;

    @Autowired
    private ImageRepository imageRepository;

    public UploadResponse uploadImage(MultipartFile file, Long userId) throws IOException {

        // TODO 1 — Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Invalid file type. Only JPEG and PNG images are allowed.");
        }

        // TODO 2 — Validate file size
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException(
                    "File size exceeds the 10MB limit.");
        }

        // TODO 3 — Generate unique filename
        String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        // TODO 4 — Save file to disk
        Path destination = Paths.get(uploadPath).resolve(uniqueFileName);
        Files.createDirectories(destination.getParent());
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        // TODO 5 — Build and save Image entity with PENDING status
        Image image = Image.builder()
                .userId(userId)
                .originalUrl(destination.toString())
                .fileName(uniqueFileName)
                .fileType(file.getContentType())
                .status(ImageStatus.PENDING)
                .build();
        Image saved = imageRepository.save(image);

        // TODO 6 — Return UploadResponse
        return UploadResponse.builder()
                .imageId(saved.getId())
                .originalUrl(saved.getOriginalUrl())
                .status(saved.getStatus())
                .message("Image uploaded successfully.")
                .build();
    }
}
