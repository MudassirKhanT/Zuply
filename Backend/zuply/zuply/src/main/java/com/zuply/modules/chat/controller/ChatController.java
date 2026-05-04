package com.zuply.modules.chat.controller;

import com.zuply.common.ApiResponse;
import com.zuply.modules.chat.dto.ChatRequest;
import com.zuply.modules.chat.dto.ChatResponse;
import com.zuply.modules.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@RequestBody ChatRequest request) {
        try {
            if (request.getMessage() == null || request.getMessage().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Message must not be empty"));
            }
            ChatResponse response = chatService.chat(request);
            return ResponseEntity.ok(ApiResponse.success(response, "OK"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Chat service error: " + e.getMessage()));
        }
    }
}
