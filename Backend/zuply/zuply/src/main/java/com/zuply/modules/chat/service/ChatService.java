package com.zuply.modules.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zuply.modules.chat.dto.ChatRequest;
import com.zuply.modules.chat.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    // Zuply-specific system persona baked into every request
    private static final String SYSTEM_PROMPT =
            "You are Zuply AI — a smart, friendly shopping assistant for Zuply, India's local-first marketplace.\n\n" +
            "ABOUT ZUPLY:\n" +
            "- Zuply connects buyers with verified local sellers in their community.\n" +
            "- Customers can browse all products without creating an account.\n" +
            "- Login is required to add items to cart, place an order, or track orders.\n" +
            "- Sellers register on Zuply, get admin-approved, then list products.\n" +
            "- Sellers can use our AI listing tool — upload a product photo and AI generates the title, description, category, price range, and tags automatically.\n" +
            "- Products must be approved by a Zuply admin before appearing in the marketplace.\n" +
            "- Payment methods available: UPI, Credit/Debit Card, Cash on Delivery (COD).\n" +
            "- All deliveries are local — sellers ship to buyers in their area.\n\n" +
            "YOUR ROLE:\n" +
            "- Help users discover products and navigate the platform.\n" +
            "- Guide new users through registration and login.\n" +
            "- Help sellers understand how to start selling on Zuply.\n" +
            "- Explain checkout (login required), cart, wishlist, and orders.\n" +
            "- Answer questions about the AI listing feature.\n" +
            "- Be warm, concise, and use emojis sparingly but naturally.\n" +
            "- If asked about specific product prices or stock, guide them to browse the Products page.\n" +
            "- Do NOT invent product listings, prices, or order details.\n" +
            "- Keep answers short (2-4 sentences) unless a detailed explanation is needed.\n" +
            "- Never break character. You are always Zuply AI.";

    public ChatResponse chat(ChatRequest request) throws Exception {

        // Build the contents array: [system-as-first-user-turn, history turns, new message]
        List<Map<String, Object>> contents = new ArrayList<>();

        // Inject system persona as a user→model exchange at the very start
        // (Gemini 1.5 Flash supports systemInstruction but this approach works universally)
        Map<String, Object> sysUser = new HashMap<>();
        sysUser.put("role", "user");
        sysUser.put("parts", List.of(Map.of("text", SYSTEM_PROMPT + "\n\nUnderstand? Reply with: Ready!")));
        contents.add(sysUser);

        Map<String, Object> sysModel = new HashMap<>();
        sysModel.put("role", "model");
        sysModel.put("parts", List.of(Map.of("text", "Ready! I'm Zuply AI, your local marketplace assistant. How can I help you today? 🛍️")));
        contents.add(sysModel);

        // Append conversation history
        if (request.getHistory() != null) {
            for (ChatRequest.Turn turn : request.getHistory()) {
                Map<String, Object> content = new HashMap<>();
                content.put("role", turn.getRole());
                content.put("parts", List.of(Map.of("text", turn.getText())));
                contents.add(content);
            }
        }

        // Append the new user message
        Map<String, Object> userTurn = new HashMap<>();
        userTurn.put("role", "user");
        userTurn.put("parts", List.of(Map.of("text", request.getMessage())));
        contents.add(userTurn);

        // Generation config — keep responses focused
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 512);
        generationConfig.put("topP", 0.9);

        Map<String, Object> body = new HashMap<>();
        body.put("contents", contents);
        body.put("generationConfig", generationConfig);

        String url = apiUrl + "?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        } catch (HttpClientErrorException e) {
            log.error("[ChatService] Gemini API error {} — {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Gemini API error: " + e.getStatusCode() + " — " + e.getResponseBodyAsString(), e);
        }

        log.debug("[ChatService] Gemini response status: {}", response.getStatusCode());
        JsonNode root = objectMapper.readTree(response.getBody());
        String replyText = root.at("/candidates/0/content/parts/0/text").asText("");

        if (replyText.isBlank()) {
            log.warn("[ChatService] Empty reply from Gemini. Full response: {}", response.getBody());
            replyText = "I'm sorry, I couldn't generate a response right now. Please try again.";
        }

        return new ChatResponse(replyText.trim());
    }
}
