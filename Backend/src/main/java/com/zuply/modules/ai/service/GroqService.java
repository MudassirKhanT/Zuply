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
public class GroqService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.api.url}")
    private String groqApiUrl;

    // Vision-capable models tried in order — first success wins
    private static final List<String> MODELS = Arrays.asList(
            "meta-llama/llama-4-scout-17b-16e-instruct",
            "llama-3.2-11b-vision-preview",
            "llama-3.2-90b-vision-preview"
    );

    private static final String PROMPT =
            "Analyze this product image and return a JSON object with these exact fields:\n" +
            "{\n" +
            "  \"title\": \"product title\",\n" +
            "  \"description\": \"detailed product description\",\n" +
            "  \"color\": \"primary color\",\n" +
            "  \"material\": \"material if visible\",\n" +
            "  \"productType\": \"type of product\",\n" +
            "  \"suggestedPriceMin\": \"minimum price in INR as plain number\",\n" +
            "  \"suggestedPriceMax\": \"maximum price in INR as plain number\",\n" +
            "  \"highlights\": [\"point 1\", \"point 2\", \"point 3\"],\n" +
            "  \"tags\": [\"tag1\", \"tag2\", \"tag3\", \"tag4\", \"tag5\"],\n" +
            "  \"suggestedCategory\": \"one of: Electronics, Clothing, Grocery, " +
            "Food & Beverage, Home & Kitchen, Beauty & Personal Care, " +
            "Health & Wellness, Agriculture, Fashion & Footwear\"\n" +
            "}\n" +
            "Return ONLY the JSON object. No explanation, no markdown, no extra text.";

    public AIGeneratedContent generateContent(String imagePath) throws IOException {
        byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String mimeType = imagePath.toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
        String dataUrl = "data:" + mimeType + ";base64," + base64Image;

        Exception lastError = null;
        for (String model : MODELS) {
            try {
                log.info("Trying Groq model: {}", model);
                AIGeneratedContent result = callGroq(model, dataUrl);
                log.info("Groq model {} succeeded", model);
                return result;
            } catch (HttpClientErrorException e) {
                int status = e.getStatusCode().value();
                if (status == 429) {
                    log.warn("Groq model {} hit rate limit, trying next…", model);
                    lastError = e;
                    try { Thread.sleep(2000); } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else if (status == 400 || status == 404) {
                    log.warn("Groq model {} not available ({}), trying next…", model, status);
                    lastError = e;
                } else {
                    log.warn("Groq model {} failed {}: {}", model, status, e.getResponseBodyAsString());
                    throw e;
                }
            } catch (Exception e) {
                log.warn("Groq model {} error: {}", model, e.getMessage());
                lastError = e;
            }
        }

        throw new RuntimeException("All Groq models exhausted. Last error: " +
                (lastError != null ? lastError.getMessage() : "unknown"), lastError);
    }

    private AIGeneratedContent callGroq(String model, String dataUrl) throws IOException {
        // Build OpenAI-compatible multimodal message
        Map<String, Object> imageContent = new HashMap<>();
        imageContent.put("type", "image_url");
        imageContent.put("image_url", Map.of("url", dataUrl));

        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");
        textContent.put("text", PROMPT);

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", Arrays.asList(imageContent, textContent));

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(message));
        body.put("max_tokens", 1024);
        body.put("temperature", 0.2);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + groqApiKey);

        HttpEntity<String> request = new HttpEntity<>(
                objectMapper.writeValueAsString(body), headers);

        ResponseEntity<String> response = restTemplate.exchange(
                groqApiUrl, HttpMethod.POST, request, String.class);

        JsonNode root = objectMapper.readTree(response.getBody());
        String text = root.at("/choices/0/message/content").asText();
        String cleanedJson = stripMarkdownFences(text);
        return objectMapper.readValue(cleanedJson, AIGeneratedContent.class);
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
