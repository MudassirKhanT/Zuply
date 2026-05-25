package com.zuply.modules.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zuply.modules.ai.dto.AIGeneratedContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class  GeminiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${upload.path}")
    private String uploadPath;

    // Base URL — model segment is replaced per attempt
    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    // ── Image analysis & product generation (multimodal, lite token efficiency) ──
    private static final List<String> MODEL_ROTATION = Arrays.asList(
            "gemini-2.0-flash-lite",
            "gemini-2.0-flash",
            "gemini-2.5-flash-lite"
    );

    private static final String GEMINI_PROMPT =
            "Analyze this product image and return a JSON object with these exact fields:\n" +
            "{\n" +
            "  \"title\": \"product title\",\n" +
            "  \"description\": \"detailed product description\",\n" +
            "  \"color\": \"primary color\",\n" +
            "  \"material\": \"material if visible\",\n" +
            "  \"productType\": \"type of product\",\n" +
            "  \"suggestedPriceMin\": \"minimum price in INR\",\n" +
            "  \"suggestedPriceMax\": \"maximum price in INR\",\n" +
            "  \"highlights\": [\"point 1\", \"point 2\", \"point 3\"],\n" +
            "  \"tags\": [\"tag1\", \"tag2\", \"tag3\", \"tag4\", \"tag5\"],\n" +
            "  \"suggestedCategory\": \"one of: Electronics, Clothing, Grocery, " +
            "Food & Beverage, Home & Kitchen, Beauty & Personal Care, " +
            "Health & Wellness, Agriculture, Fashion & Footwear\"\n" +
            "}\n" +
            "Return ONLY the JSON object. No explanation, no markdown, no extra text.";

    public AIGeneratedContent generateContent(String processedImagePath) throws IOException {
        byte[] imageBytes;
        String mimeType;

        if (processedImagePath.startsWith("http")) {
            // Remote URL (Supabase) — download it
            ResponseEntity<byte[]> response = restTemplate.getForEntity(processedImagePath, byte[].class);
            imageBytes = response.getBody();
            mimeType = "image/png"; // Supabase images are PNG
        } else {
            // Local path — resolve against upload directory
            String resolvedPath;
            if (processedImagePath.startsWith("/uploads/")) {
                resolvedPath = Paths.get(uploadPath).toAbsolutePath()
                        .resolve(processedImagePath.replace("/uploads/", ""))
                        .toString();
            } else {
                resolvedPath = Paths.get(uploadPath).toAbsolutePath()
                        .resolve(processedImagePath)
                        .toString();
            }
            imageBytes = Files.readAllBytes(Paths.get(resolvedPath));
            mimeType = resolvedPath.toLowerCase().endsWith(".jpg")
                    || resolvedPath.toLowerCase().endsWith(".jpeg")
                    ? "image/jpeg" : "image/png";
        }

        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String requestJson = buildRequestJson(base64Image, mimeType);

        Exception lastError = null;
        for (String model : MODEL_ROTATION) {
            String url = String.format(BASE_URL, model) + "?key=" + geminiApiKey;
            try {
                log.info("Trying Gemini model: {}", model);
                AIGeneratedContent result = callGemini(url, requestJson);
                log.info("Gemini model {} succeeded", model);
                return result;
            } catch (HttpClientErrorException e) {
                int status = e.getStatusCode().value();
                if (status == 429) {
                    // Wait for the exact retryDelay Gemini told us, then retry this model once
                    long waitMs = Math.min(extractRetryDelayMs(e.getResponseBodyAsString()), 3_000);
                    log.warn("Model {} → 429, waiting {}ms then retrying once", model, waitMs);
                    try { Thread.sleep(waitMs); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    try {
                        AIGeneratedContent result = callGemini(url, requestJson);
                        log.info("Model {} succeeded after retry", model);
                        return result;
                    } catch (Exception retryEx) {
                        log.warn("Retry on {} also failed: {}", model, retryEx.getMessage());
                        lastError = retryEx;
                    }
                } else if (status == 404 || status == 400) {
                    log.warn("Model {} → {} — skipping", model, status);
                    lastError = e;
                } else {
                    log.warn("Model {} returned {} — {}", model, status, e.getResponseBodyAsString());
                    throw e;
                }
            } catch (Exception e) {
                log.warn("Model {} failed with: {}", model, e.getMessage());
                lastError = e;
            }
        }

        // All models exhausted — return demo content so the UI flow still works
        log.warn("[GeminiService] All models exhausted — using demo fallback content");
        return buildDemoContent();
    }

    /** Demo-safe fallback: returns a realistic-looking product listing when Gemini is unavailable. */
    private AIGeneratedContent buildDemoContent() {
        return AIGeneratedContent.builder()
                .title("Premium Quality Product")
                .description("A high-quality product sourced from a trusted local seller. " +
                        "This item is carefully inspected before listing to ensure it meets " +
                        "Zuply's quality standards. Perfect for everyday use and gifting.")
                .color("Assorted")
                .material("Mixed")
                .productType("General Merchandise")
                .suggestedPriceMin("199")
                .suggestedPriceMax("499")
                .highlights(java.util.Arrays.asList(
                        "High quality construction",
                        "Sourced from verified local seller",
                        "Fast local delivery available"))
                .tags(java.util.Arrays.asList(
                        "local", "quality", "zuply", "fresh", "trusted"))
                .suggestedCategory("Home & Kitchen")
                .build();
    }

    private AIGeneratedContent callGemini(String url, String requestJson) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        JsonNode root = objectMapper.readTree(response.getBody());
        String textResponse = root.at("/candidates/0/content/parts/0/text").asText();
        String cleanedJson = stripMarkdownFences(textResponse);
        return objectMapper.readValue(cleanedJson, AIGeneratedContent.class);
    }

    private String buildRequestJson(String base64Image, String mimeType) throws IOException {
        Map<String, String> inlineData = new HashMap<>();
        inlineData.put("mimeType", mimeType);
        inlineData.put("data", base64Image);

        Map<String, Object> imagePart = new HashMap<>();
        imagePart.put("inlineData", inlineData);

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", GEMINI_PROMPT);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", Arrays.asList(imagePart, textPart));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", Collections.singletonList(content));
        // 400 tokens is enough for the structured JSON product listing — saves quota
        requestBody.put("generationConfig", Map.of("maxOutputTokens", 1024, "temperature", 0.1));

        return objectMapper.writeValueAsString(requestBody);
    }

    /** Extracts retryDelay seconds from the Gemini 429 response body, returns millis. */
    private long extractRetryDelayMs(String body) {
        try {
            if (body == null) return 30_000;
            int idx = body.indexOf("\"retryDelay\"");
            if (idx < 0) return 30_000;
            int q1 = body.indexOf('"', idx + 13) + 1;
            int q2 = body.indexOf('"', q1);
            String val = body.substring(q1, q2).replace("s", "").trim(); // e.g. "58.969"
            return (long)(Double.parseDouble(val) * 1000);
        } catch (Exception ex) {
            return 30_000;
        }
    }

    private String stripMarkdownFences(String raw) {
        if (raw == null) return "{}";
        String trimmed = raw.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline != -1) trimmed = trimmed.substring(firstNewline + 1);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.lastIndexOf("```")).trim();
        }
        return trimmed;
    }
}
