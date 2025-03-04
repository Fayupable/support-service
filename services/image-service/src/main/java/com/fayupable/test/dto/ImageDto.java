package com.fayupable.test.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ImageDto {
    private UUID imageId;
    private UUID userId;
    private String fileName;
    private String fileType;
    private String downloadUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}