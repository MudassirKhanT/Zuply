package com.zuply.modules.chat.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zuply.common.ApiResponse;
import com.zuply.modules.chat.dto.ChatRequest;
import com.zuply.modules.chat.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    // Try models in order — skip to next on 429 or 404
    private static final List<String> CHAT_MODELS = Arrays.asList(
            "gemini-2.0-flash-lite",
            "gemini-2.0-flash",
            "gemini-2.5-flash-lite"
    );

    private static final String GEMINI_BASE =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    private static final String SYSTEM_PROMPT =
            "You are Zuply AI, a helpful shopping assistant for Zuply — a hyperlocal e-commerce platform " +
            "in India that connects customers with local sellers nearby.\n\n" +
            "Key facts about Zuply:\n" +
            "- Customers browse and buy products from local sellers near them\n" +
            "- Sellers upload product photos and AI automatically generates listing details\n" +
            "- Payment methods: UPI, Card, and Cash on Delivery (COD)\n" +
            "- No account needed to browse; login required only for checkout\n" +
            "- Categories: Electronics, Clothing, Grocery, Food & Beverage, Home & Kitchen, " +
            "Beauty & Personal Care, Health & Wellness, Agriculture, Fashion & Footwear\n" +
            "- Sellers register and await admin approval before listing products\n" +
            "- Orders flow: PLACED → PROCESSING → DELIVERED\n\n" +
            "Keep answers short, friendly and helpful. Respond in the same language the user writes in.";

    @PostMapping
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@RequestBody ChatRequest request) {
        Exception lastError = null;

        for (String model : CHAT_MODELS) {
            try {
                String reply = callGemini(model, request.getMessage(), request.getHistory());
                return ResponseEntity.ok(ApiResponse.success("Reply generated", new ChatResponse(reply)));
            } catch (HttpClientErrorException e) {
                int status = e.getStatusCode().value();
                if (status == 429) {
                    log.warn("[Chat] Quota exceeded for model {} — trying next model.", model);
                    lastError = e;
                } else {
                    log.error("[Chat] Gemini HTTP {} for model {}: {}", status, model, e.getResponseBodyAsString());
                    lastError = e;
                }
            } catch (Exception e) {
                log.error("[Chat] Error with model {}: {}", model, e.getMessage());
                lastError = e;
            }
        }

        log.warn("[Chat] All models exhausted — using demo fallback response");
        String fallback = getDemoReply(request.getMessage());
        return ResponseEntity.ok(ApiResponse.success("Reply generated", new ChatResponse(fallback)));
    }

    /** Returns a helpful pre-programmed reply when Gemini is unavailable. */
    private String getDemoReply(String message) {
        if (message == null) return getDefaultReply();
        String m = message.toLowerCase();

        if (m.contains("hello") || m.contains("hi") || m.contains("hey") || m.contains("namaste"))
            return "Hi there! 👋 I'm Zuply AI, your local shopping assistant. How can I help you today?";

        if (m.contains("order") && (m.contains("track") || m.contains("status") || m.contains("where")))
            return "To track your order, go to **My Orders** in your account. Orders follow the flow: " +
                   "Placed → Processing → Delivered. If you need help, contact your seller directly from the order page.";

        if (m.contains("order") && m.contains("cancel"))
            return "To cancel an order, visit **My Orders**, select the order, and tap **Cancel** if it's still in Placed status. " +
                   "Once Processing or Delivered, cancellation may not be available.";

        if (m.contains("pay") || m.contains("payment") || m.contains("upi") || m.contains("cod"))
            return "Zuply supports **UPI**, **Card payments**, and **Cash on Delivery (COD)**. " +
                   "Choose your preferred method at checkout. COD is available for all local sellers nearby.";

        if (m.contains("deliver") || m.contains("shipping") || m.contains("how long"))
            return "Delivery times depend on the local seller. Since Zuply connects you with sellers " +
                   "in your area, most orders arrive within **1-3 days**. Check the seller's delivery info on the product page.";

        if (m.contains("return") || m.contains("refund") || m.contains("exchange"))
            return "Return and refund policies vary by seller. Check the **Return Policy** on each product listing. " +
                   "If you have an issue, raise a return request from **My Orders** within the return window.";

        if (m.contains("sell") || m.contains("seller") || m.contains("register") || m.contains("list"))
            return "To become a seller on Zuply: \n1. Register as a Seller\n2. Wait for admin approval\n" +
                   "3. Upload product photos — our AI auto-generates the listing for you!\n" +
                   "Categories: Electronics, Clothing, Grocery, Food & Beverage, Home & Kitchen, and more.";

        if (m.contains("categor") || m.contains("product") || m.contains("what") && m.contains("sell"))
            return "Zuply has these categories: **Electronics, Clothing, Grocery, Food & Beverage, " +
                   "Home & Kitchen, Beauty & Personal Care, Health & Wellness, Agriculture, Fashion & Footwear**. " +
                   "Browse products from sellers near you!";

        if (m.contains("account") || m.contains("login") || m.contains("register") || m.contains("sign"))
            return "You can **browse products without an account**. A login is only required at checkout. " +
                   "Register using your email — it takes less than a minute!";

        if (m.contains("contact") || m.contains("support") || m.contains("help"))
            return "For support, you can reach out to the seller directly from your order page. " +
                   "For platform issues, contact Zuply support through the Help section in your account.";

        if (m.contains("thank"))
            return "You're welcome! 😊 Happy shopping on Zuply. Let me know if you need anything else.";

        return getDefaultReply();
    }

    private String getDefaultReply() {
        return "I'm Zuply AI, your local shopping assistant! 🛍️ I can help you with:\n" +
               "• Tracking orders\n• Payment options (UPI, Card, COD)\n" +
               "• Delivery & returns\n• Becoming a seller\n• Finding products nearby\n\n" +
               "What would you like to know?";
    }

    private String callGemini(String model, String userMessage,
                               List<ChatRequest.ChatTurn> history) throws Exception {

        List<Map<String, Object>> contents = new ArrayList<>();

        if (history != null) {
            for (ChatRequest.ChatTurn turn : history) {
                contents.add(Map.of(
                        "role",  turn.getRole(),
                        "parts", List.of(Map.of("text", turn.getText()))
                ));
            }
        }
        contents.add(Map.of(
                "role",  "user",
                "parts", List.of(Map.of("text", userMessage))
        ));

        Map<String, Object> body = new HashMap<>();
        body.put("contents", contents);
        body.put("systemInstruction", Map.of("parts", List.of(Map.of("text", SYSTEM_PROMPT))));
        body.put("generationConfig",  Map.of("maxOutputTokens", 200, "temperature", 0.5));

        String url = String.format(GEMINI_BASE, model) + "?key=" + geminiApiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST,
                new HttpEntity<>(objectMapper.writeValueAsString(body), headers),
                String.class);

        JsonNode root = objectMapper.readTree(response.getBody());
        return root.at("/candidates/0/content/parts/0/text")
                   .asText("Sorry, I couldn't generate a response.");
    }
}
