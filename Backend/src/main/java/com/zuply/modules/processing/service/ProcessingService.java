package com.zuply.modules.processing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zuply.modules.upload.dto.ImageStatus;
import com.zuply.modules.upload.model.Image;
import com.zuply.modules.upload.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessingService {

    private final ImageRepository imageRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${huggingface.api.key:}")
    private String hfApiKey;

    private static final String HF_RMBG_URL =
            "https://api-inference.huggingface.co/models/briaai/RMBG-1.4";

    // ── Entry point ───────────────────────────────────────────────────
    public void processImage(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found: " + imageId));

        image.setStatus(ImageStatus.PROCESSING);
        imageRepository.save(image);

        try {
            Path originalPath = Paths.get(uploadPath).resolve(
                    // Always read the originally uploaded file, not a previously processed one
                    image.getOriginalUrl().replace("/uploads/", ""));
            BufferedImage original = ImageIO.read(originalPath.toFile());

            if (original == null) throw new IOException("Cannot read image: " + originalPath);

            BufferedImage withWhiteBg;

            // ── Attempt HF background removal ─────────────────────────
            if (hfApiKey != null && !hfApiKey.isBlank()) {
                try {
                    log.info("Calling Hugging Face RMBG-1.4 for image {}", imageId);
                    byte[] originalBytes = Files.readAllBytes(originalPath);
                    byte[] noBgBytes    = callHuggingFaceRmbg(originalBytes,
                            image.getFileType() != null ? image.getFileType() : "image/jpeg");

                    BufferedImage transparent = ImageIO.read(new ByteArrayInputStream(noBgBytes));
                    withWhiteBg = addWhiteBackground(transparent);
                    log.info("HF background removal succeeded for image {}", imageId);

                } catch (Exception e) {
                    log.warn("HF bg removal failed ({}), using enhancement only", e.getMessage());
                    withWhiteBg = addWhiteBackground(original);
                }
            } else {
                log.info("No HF API key — skipping bg removal, applying enhancement only");
                withWhiteBg = addWhiteBackground(original);
            }

            // ── Enhance brightness / contrast / sharpness ─────────────
            BufferedImage enhanced = enhance(withWhiteBg);

            // ── Save as PNG ────────────────────────────────────────────
            String baseName         = image.getOriginalUrl()
                    .replace("/uploads/", "")
                    .replaceAll("(?i)\\.(jpg|jpeg|png)$", "");
            String processedFileName = "processed_" + baseName + ".png";
            Path   processedPath     = Paths.get(uploadPath).resolve(processedFileName);
            ImageIO.write(enhanced, "PNG", processedPath.toFile());

            image.setProcessedUrl("/uploads/" + processedFileName);
            image.setFileName(processedFileName);   // Gemini will analyse the cleaned image
            image.setStatus(ImageStatus.PROCESSED);
            log.info("Image {} processing complete → {}", imageId, processedFileName);

        } catch (Exception e) {
            log.error("Processing failed for image {}: {}", imageId, e.getMessage());
            if (image.getProcessedUrl() == null) image.setProcessedUrl(image.getOriginalUrl());
            image.setStatus(ImageStatus.PROCESSED);
        }

        imageRepository.save(image);
    }

    // ── Hugging Face RMBG-1.4 API call ───────────────────────────────
    // Sends image as JSON { "inputs": "<base64>" } which is the format RMBG-1.4 expects.
    // Falls back to raw-bytes format if JSON returns a non-200.
    private byte[] callHuggingFaceRmbg(byte[] imageBytes, String mimeType) throws Exception {
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        String jsonBody = objectMapper.writeValueAsString(Map.of("inputs", base64));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + hfApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-wait-for-model", "true");

        HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    HF_RMBG_URL, HttpMethod.POST, request, byte[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            throw new RuntimeException("HF API returned: " + response.getStatusCode());

        } catch (HttpClientErrorException e) {
            log.warn("HF JSON request failed ({} {}), trying raw-bytes fallback",
                    e.getStatusCode().value(), e.getResponseBodyAsString());
            return callHuggingFaceRmbgRawBytes(imageBytes, mimeType);
        }
    }

    // Raw-bytes fallback (Content-Type: image/jpeg|png)
    private byte[] callHuggingFaceRmbgRawBytes(byte[] imageBytes, String mimeType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + hfApiKey);
        headers.setContentType(MediaType.parseMediaType(mimeType));
        headers.set("x-wait-for-model", "true");

        HttpEntity<byte[]> request = new HttpEntity<>(imageBytes, headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(
                HF_RMBG_URL, HttpMethod.POST, request, byte[].class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("HF API (raw) returned: " + response.getStatusCode());
        }
        return response.getBody();
    }

    // ── White canvas with padding ─────────────────────────────────────
    private BufferedImage addWhiteBackground(BufferedImage input) {
        int w   = input.getWidth();
        int h   = input.getHeight();
        int padX = (int)(w * 0.04);
        int padY = (int)(h * 0.04);

        BufferedImage canvas = new BufferedImage(w + padX * 2, h + padY * 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.drawImage(input, padX, padY, null);
        g.dispose();
        return canvas;
    }

    // ── Brightness + contrast + sharpness boost ───────────────────────
    private BufferedImage enhance(BufferedImage input) {
        RescaleOp bright = new RescaleOp(1.08f, 8f, null);
        BufferedImage brightened = bright.filter(input, null);

        float[] k = {
                 0f, -0.3f,  0f,
                -0.3f, 2.2f, -0.3f,
                 0f, -0.3f,  0f
        };
        return new ConvolveOp(new Kernel(3, 3, k), ConvolveOp.EDGE_NO_OP, null)
                .filter(brightened, null);
    }
}
