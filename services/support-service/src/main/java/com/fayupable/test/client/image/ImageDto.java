package com.fayupable.test.client.image;

import lombok.Data;

import java.util.UUID;

@Data
public class ImageDto {
    private UUID imageId;
    private UUID userId;
    private String fileName;
    private String fileType;
    private String downloadUrl;
}
