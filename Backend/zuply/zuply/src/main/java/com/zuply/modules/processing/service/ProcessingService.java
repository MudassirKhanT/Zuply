package com.zuply.modules.processing.service;

import com.zuply.modules.upload.dto.ImageStatus;
import com.zuply.modules.upload.model.Image;
import com.zuply.modules.upload.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProcessingService {

    private final ImageRepository imageRepository;

    public void processImage(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));

        image.setStatus(ImageStatus.PROCESSING);
        imageRepository.save(image);

        // Use original URL as processed URL when no external processing is configured
        if (image.getProcessedUrl() == null) {
            image.setProcessedUrl(image.getOriginalUrl());
        }

        image.setStatus(ImageStatus.PROCESSED);
        imageRepository.save(image);
    }
}
