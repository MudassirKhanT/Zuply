package com.zuply.modules.processing.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zuply.modules.upload.dto.ImageStatus;
import com.zuply.modules.upload.model.Image;
import com.zuply.modules.upload.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

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

    private static final String HF_SPACE_BASE = "https://not-lain-background-removal.hf.space";
    private static final String QUEUE_JOIN_URL = HF_SPACE_BASE + "/queue/join";
    private static final String QUEUE_DATA_URL = HF_SPACE_BASE + "/queue/data";

    // ── Entry point ───────────────────────────────────────────────────
    public void processImage(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found: " + imageId));

        image.setStatus(ImageStatus.PROCESSING);
        imageRepository.save(image);

        try {
            Path originalPath = Paths.get(uploadPath).resolve(
                    image.getOriginalUrl().replace("/uploads/", ""));
            BufferedImage original = ImageIO.read(originalPath.toFile());

            if (original == null) throw new IOException("Cannot read image: " + originalPath);

            BufferedImage withWhiteBg;

            // ── Attempt AI background removal via HF Space ────────────
            try {
                log.info("Calling HF Space (not-lain/background-removal) for image {}", imageId);
                byte[] originalBytes = Files.readAllBytes(originalPath);
                byte[] noBgBytes = callHfSpaceQueue(originalBytes,
                        image.getFileType() != null ? image.getFileType() : "image/jpeg");

                BufferedImage transparent = ImageIO.read(new ByteArrayInputStream(noBgBytes));
                withWhiteBg = addWhiteBackground(transparent);
                log.info("HF Space background removal succeeded for image {}", imageId);

            } catch (Exception e) {
                log.warn("HF Space bg removal failed ({}), using enhancement only", e.getMessage());
                withWhiteBg = addWhiteBackground(original);
            }

            // ── Enhance brightness / contrast / sharpness ─────────────
            BufferedImage enhanced = enhance(withWhiteBg);

            // ── Save as PNG ────────────────────────────────────────────
            String baseName          = image.getOriginalUrl()
                    .replace("/uploads/", "")
                    .replaceAll("(?i)\\.(jpg|jpeg|png)$", "");
            String processedFileName = "processed_" + baseName + ".png";
            Path   processedPath     = Paths.get(uploadPath).resolve(processedFileName);
            ImageIO.write(enhanced, "PNG", processedPath.toFile());

            image.setProcessedUrl("/uploads/" + processedFileName);
            image.setFileName(processedFileName);
            image.setStatus(ImageStatus.PROCESSED);
            log.info("Image {} processing complete → {}", imageId, processedFileName);

        } catch (Exception e) {
            log.error("Processing failed for image {}: {}", imageId, e.getMessage());
            if (image.getProcessedUrl() == null) image.setProcessedUrl(image.getOriginalUrl());
            image.setStatus(ImageStatus.PROCESSED);
        }

        imageRepository.save(image);
    }

    // ── Gradio Queue API ──────────────────────────────────────────────
    // ZeroGPU spaces reject direct /run/predict — must go through the queue:
    //   1. POST /queue/join  → {event_id}
    //   2. GET  /queue/data?session_hash=<hash>  (SSE stream)
    //      listen until msg == "process_completed"
    //   3. Extract output image from the completed event
    private byte[] callHfSpaceQueue(byte[] imageBytes, String mimeType) throws Exception {
        String sessionHash = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String dataUrl = "data:" + mimeType + ";base64,"
                + Base64.getEncoder().encodeToString(imageBytes);

        // Step 1 — join queue
        Map<String, Object> joinBody = new HashMap<>();
        joinBody.put("data", List.of(dataUrl));
        joinBody.put("fn_index", 0);
        joinBody.put("session_hash", sessionHash);

        HttpHeaders joinHeaders = new HttpHeaders();
        joinHeaders.setContentType(MediaType.APPLICATION_JSON);
        if (hfApiKey != null && !hfApiKey.isBlank()) {
            joinHeaders.set("Authorization", "Bearer " + hfApiKey);
        }

        HttpEntity<String> joinReq = new HttpEntity<>(
                objectMapper.writeValueAsString(joinBody), joinHeaders);
        restTemplate.exchange(QUEUE_JOIN_URL, HttpMethod.POST, joinReq, String.class);
        log.debug("Joined HF Space queue, session={}", sessionHash);

        // Step 2 — stream SSE until process_completed
        String sseUrl = QUEUE_DATA_URL + "?session_hash=" + sessionHash;
        String completedJson = restTemplate.execute(sseUrl, HttpMethod.GET,
                req -> {
                    req.getHeaders().set("Accept", "text/event-stream");
                    req.getHeaders().set("Cache-Control", "no-cache");
                    if (hfApiKey != null && !hfApiKey.isBlank()) {
                        req.getHeaders().set("Authorization", "Bearer " + hfApiKey);
                    }
                },
                response -> {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.getBody()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (!line.startsWith("data: ")) continue;
                            String json = line.substring(6).trim();
                            if (json.isEmpty()) continue;
                            JsonNode event = objectMapper.readTree(json);
                            String msg = event.path("msg").asText();
                            log.debug("SSE event: {}", msg);
                            if ("process_completed".equals(msg)) return json;
                        }
                    }
                    return null;
                });

        if (completedJson == null) {
            throw new RuntimeException("SSE stream ended without process_completed");
        }

        // Step 3 — extract result image
        JsonNode outputData = objectMapper.readTree(completedJson)
                .path("output").path("data");

        if (!outputData.isArray() || outputData.isEmpty()) {
            throw new RuntimeException("process_completed had no output: " + completedJson);
        }

        JsonNode output = outputData.get(0);

        // Gradio 3 — base64 data URL
        if (output.isTextual()) {
            String text = output.asText();
            if (text.startsWith("data:")) {
                return Base64.getDecoder().decode(text.split(",", 2)[1]);
            }
        }

        // Gradio 4 — {"url":"..."} or {"path":"..."}
        if (output.isObject()) {
            String fileUrl = output.path("url").asText(null);
            if (fileUrl == null || fileUrl.isBlank()) {
                fileUrl = HF_SPACE_BASE + "/file=" + output.path("path").asText();
            }
            log.info("Downloading result from: {}", fileUrl);
            ResponseEntity<byte[]> fileResp = restTemplate.getForEntity(fileUrl, byte[].class);
            if (fileResp.getBody() == null) throw new RuntimeException("Empty file download");
            return fileResp.getBody();
        }

        throw new RuntimeException("Unrecognised output format: " + output);
    }

    // ── White canvas with padding ─────────────────────────────────────
    private BufferedImage addWhiteBackground(BufferedImage input) {
        int w    = input.getWidth();
        int h    = input.getHeight();
        int padX = (int) (w * 0.04);
        int padY = (int) (h * 0.04);

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
