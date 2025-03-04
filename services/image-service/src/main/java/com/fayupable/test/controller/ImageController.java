package com.fayupable.test.controller;

import com.fayupable.test.response.ImageResponse;
import com.fayupable.test.service.IImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {
    private final IImageService imageService;

    @GetMapping("/image/getImage/{id}")
    public ResponseEntity<ImageResponse> getImageById(@PathVariable UUID id) {
        return ResponseEntity.ok(new ImageResponse("Image fetched successfully", imageService.getImageById(id)));
    }

    @PostMapping("/add")
    public ResponseEntity<ImageResponse> saveImage(@RequestParam List<MultipartFile> file, @RequestHeader("userId") UUID userId) {
        return ResponseEntity.ok(new ImageResponse("Image saved successfully", imageService.saveImages(file, userId)));
    }

    @GetMapping("/image/download/{imageId}")
    public ResponseEntity<Resource> downloadImage(@PathVariable UUID imageId) {
        Resource resource = imageService.downloadImage(imageId);
        String contentType = imageService.getImageContentType(imageId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + imageId + "\"")
                .body(resource);
    }


}