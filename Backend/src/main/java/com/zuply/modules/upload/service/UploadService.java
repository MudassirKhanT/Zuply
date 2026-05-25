package com.zuply.modules.upload.service;

import com.zuply.modules.upload.dto.ImageStatus;
import com.zuply.modules.upload.dto.UploadResponse;
import com.zuply.modules.upload.model.Image;
import com.zuply.modules.upload.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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
    private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024;

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key:}")
    private String supabaseServiceRoleKey;

    @Value("${supabase.bucket:zuply-images}")
    private String supabaseBucket;

    @Autowired
    private ImageRepository imageRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public UploadResponse uploadImage(MultipartFile file, Long userId) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Invalid file type. Only JPEG and PNG images are allowed.");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("File size exceeds the 10MB limit.");
        }

        String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String imageUrl;

        if (supabaseUrl != null && !supabaseUrl.isBlank()) {
            // Production: upload to Supabase Storage
            imageUrl = uploadToSupabase(file, uniqueFileName, contentType);
        } else {
            // Development: save to local disk
            Path destination = Paths.get(uploadPath).resolve(uniqueFileName);
            Files.createDirectories(destination.getParent());
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            imageUrl = "/uploads/" + uniqueFileName;
        }

        Image image = Image.builder()
                .userId(userId)
                .originalUrl(imageUrl)
                .fileName(uniqueFileName)
                .fileType(file.getContentType())
                .status(ImageStatus.PENDING)
                .build();
        Image saved = imageRepository.save(image);

        return UploadResponse.builder()
                .imageId(saved.getId())
                .originalUrl(imageUrl)
                .imageUrl(imageUrl)
                .status(saved.getStatus())
                .message("Image uploaded successfully.")
                .build();
    }

    private String uploadToSupabase(MultipartFile file, String fileName, String contentType) throws IOException {
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + supabaseBucket + "/" + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseServiceRoleKey);
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.set("x-upsert", "true");

        HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);
        restTemplate.exchange(uploadUrl, HttpMethod.POST, entity, String.class);

        // Return the public URL for the uploaded file
        return supabaseUrl + "/storage/v1/object/public/" + supabaseBucket + "/" + fileName;
    }
}
