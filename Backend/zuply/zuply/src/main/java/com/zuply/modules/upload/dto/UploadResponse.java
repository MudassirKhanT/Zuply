package com.zuply.modules.upload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {

    private Long imageId;
    private String originalUrl;
    private ImageStatus status;
    private String message;
}
