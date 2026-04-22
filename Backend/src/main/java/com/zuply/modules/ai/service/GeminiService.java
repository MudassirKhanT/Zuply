package com.zuply.modules.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zuply.modules.ai.dto.AIGeneratedContent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


@Service
@RequiredArgsConstructor
public class GeminiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    // Prompt instructs Gemini to return ONLY a JSON object — no markdown, no extra text.
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

        // ── Step 1: Read image file from disk and encode it to Base64 ────────
        byte[] imageBytes = Files.readAllBytes(Paths.get(processedImagePath));
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        // Determine MIME type from file extension (default to image/png).
        String mimeType = processedImagePath.toLowerCase().endsWith(".jpg")
                || processedImagePath.toLowerCase().endsWith(".jpeg")
                ? "image/jpeg"
                : "image/png";

        // ── Step 2: Build Gemini API request body as a nested Map ────────────
        //
        // Required Gemini Vision JSON structure:
        // {
        //   "contents": [{
        //     "parts": [
        //       { "inlineData": { "mimeType": "image/png", "data": "<base64>" } },
        //       { "text": "<prompt>" }
        //     ]
        //   }]
        // }

        // Inner inlineData map
        Map<String, String> inlineData = new HashMap<>();
        inlineData.put("mimeType", mimeType);
        inlineData.put("data", base64Image);

        // Image part
        Map<String, Object> imagePart = new HashMap<>();
        imagePart.put("inlineData", inlineData);

        // Text prompt part
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", GEMINI_PROMPT);

        // Content object holding both parts
        Map<String, Object> content = new HashMap<>();
        content.put("parts", Arrays.asList(imagePart, textPart));

        // Top-level request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", Collections.singletonList(content));

        // Serialize the full request body to a JSON string
        String requestJson = objectMapper.writeValueAsString(requestBody);

        // ── Step 3: POST to Gemini API ───────────────────────────────────────
        // API key is passed as a query parameter — not in the Authorization header.
        String url = geminiApiUrl + "?key=" + geminiApiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );

        // ── Step 4: Extract text block from Gemini response ──────────────────
        //
        // Gemini returns a response shaped like:
        // {
        //   "candidates": [{
        //     "content": {
        //       "parts": [{ "text": "{ ...our JSON... }" }]
        //     }
        //   }]
        // }
        //
        // Navigate the JSON tree to reach the text string containing our JSON.
        JsonNode root = objectMapper.readTree(response.getBody());
        String textResponse = root
                .at("/candidates/0/content/parts/0/text")
                .asText();

        // ── Step 5: Clean and parse the text into AIGeneratedContent ─────────
        // Gemini occasionally wraps the JSON in ```json ... ``` even when told
        // not to. Strip any markdown code fences if present so Jackson can parse.
        String cleanedJson = stripMarkdownFences(textResponse);

        return objectMapper.readValue(cleanedJson, AIGeneratedContent.class);
    }


    private String stripMarkdownFences(String raw) {
        if (raw == null) return "{}";
        String trimmed = raw.trim();
        // Remove ```json or ``` at the start
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline != -1) {
                trimmed = trimmed.substring(firstNewline + 1);
            }
        }
        // Remove ``` at the end
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.lastIndexOf("```")).trim();
        }
        return trimmed;
    }
}