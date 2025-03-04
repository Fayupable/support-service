package com.fayupable.test.service;

import com.fayupable.test.dto.ImageDto;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface IImageService {
    ImageDto getImageById(UUID id);

    Resource downloadImage(UUID imageId);

    String getImageContentType(UUID imageId);

    List<ImageDto> saveImages(List<MultipartFile> files, UUID userId);
}