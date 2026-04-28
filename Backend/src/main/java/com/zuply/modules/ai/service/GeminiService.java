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
public class GeminiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    // Base URL — model segment is replaced per attempt
    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    // Models tried in order — first success wins.
    // *-latest aliases were removed from the v1beta API; use versioned names instead.
    private static final List<String> MODEL_ROTATION = Arrays.asList(
            "gemini-2.0-flash-lite",
            "gemini-2.0-flash",
            "gemini-1.5-flash",
            "gemini-1.5-flash-8b"
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
        byte[] imageBytes = Files.readAllBytes(Paths.get(processedImagePath));
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String mimeType = processedImagePath.toLowerCase().endsWith(".jpg")
                || processedImagePath.toLowerCase().endsWith(".jpeg")
                ? "image/jpeg" : "image/png";

        String requestJson = buildRequestJson(base64Image, mimeType);

        Exception lastError = null;
        for (int i = 0; i < MODEL_ROTATION.size(); i++) {
            String model = MODEL_ROTATION.get(i);
            try {
                log.info("Trying Gemini model: {}", model);
                String url = String.format(BASE_URL, model) + "?key=" + geminiApiKey;
                AIGeneratedContent result = callGemini(url, requestJson);
                log.info("Gemini model {} succeeded", model);
                return result;
            } catch (HttpClientErrorException e) {
                int status = e.getStatusCode().value();
                if (status == 429) {
                    log.warn("Model {} hit rate limit (429), waiting 3 s before next model…", model);
                    lastError = e;
                    try { Thread.sleep(3000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                } else if (status == 404) {
                    log.warn("Model {} not found (404), trying next model", model);
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

        throw new RuntimeException("All Gemini models exhausted. Last error: " +
                (lastError != null ? lastError.getMessage() : "unknown"), lastError);
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

        return objectMapper.writeValueAsString(requestBody);
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
