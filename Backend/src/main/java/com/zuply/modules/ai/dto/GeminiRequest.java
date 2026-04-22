package com.zuply.modules.ai.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeminiRequest {

    // Base64-encoded image string
    private String base64Image;

    // MIME type of the image (image/jpeg or image/png)
    private String mimeType;

    // The prompt instructing Gemini what to generate
    private String prompt;
}