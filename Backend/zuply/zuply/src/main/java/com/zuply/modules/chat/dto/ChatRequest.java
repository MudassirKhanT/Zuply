package com.zuply.modules.chat.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    private String message;

    // Previous turns so Gemini understands context
    private List<Turn> history;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Turn {
        private String role; // "user" | "model"
        private String text;
    }
}
