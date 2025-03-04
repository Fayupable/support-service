package com.fayupable.test.service;

import com.fayupable.test.dto.ImageDto;
import com.fayupable.test.entity.Image;
import com.fayupable.test.exception.ImageNotFoundException;
import com.fayupable.test.exception.UnauthorizedAccessException;
import com.fayupable.test.mapper.ImageMapper;
import com.fayupable.test.repository.IImageRepository;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService implements IImageService {
    private final IImageRepository imageRepository;
    private final ImageMapper imageMapper;

    @Override
    public ImageDto getImageById(UUID id) {
        log.info("Fetching image by ID: {}", id);
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Image not found with ID: {}", id);
                    return new NotFoundException("Image not found");
                });
        log.info("Image found: {}", image.getFileName());
        return imageMapper.fromImage(image);
    }


    @Override
    @Transactional
    public Resource downloadImage(UUID imageId) {
        log.info("Downloading image with ID: {}", imageId);

        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException("Image not found with ID: " + imageId));

        try {
            byte[] imageBytes = image.getImage().getBytes(1, (int) image.getImage().length());
            String fileType = Optional.ofNullable(image.getFileType()).orElse("application/octet-stream");

            log.info("Image ID: {} - File Type: {}", imageId, fileType);

            return new ByteArrayResource(imageBytes);
        } catch (SQLException e) {
            log.error("Error while retrieving image bytes for ID: {}", imageId, e);
            throw new RuntimeException("Failed to retrieve image", e);
        }
    }

    @Override
    public String getImageContentType(UUID imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException("Image not found with ID: " + imageId));

        return Optional.ofNullable(image.getFileType()).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);
    }


    @Override
    @Transactional
    public List<ImageDto> saveImages(List<MultipartFile> files, UUID userId) {
        log.info("Saving {} images for user ID: {}", files.size(), userId);
        validateUserId(userId);
        List<ImageDto> savedImages = new ArrayList<>();

        for (MultipartFile file : files) {
            Image savedImage = processAndSaveImage(file, userId);
            updateDownloadUrl(savedImage);
            savedImages.add(imageMapper.fromImage(savedImage));
            log.info("Image saved successfully: {}", savedImage.getFileName());
        }

        log.info("Successfully saved {} images for user ID: {}", savedImages.size(), userId);
        return savedImages;
    }

    private void validateUserId(UUID userId) {
        if (userId == null) {
            log.error("User ID is missing in headers");
            throw new UnauthorizedAccessException("User ID is missing in headers");
        }
    }

    private Image processAndSaveImage(MultipartFile file, UUID userId) {
        try {
            log.info("Processing image upload: {}", file.getOriginalFilename());
            Image image = new Image();
            image.setUserId(userId);
            image.setFileName(file.getOriginalFilename());
            image.setFileType(file.getContentType());
            image.setImage(new SerialBlob(file.getBytes()));

            Image savedImage = imageRepository.save(image);
            log.info("Image processing complete. Image ID: {}", savedImage.getImageId());
            return savedImage;
        } catch (IOException | SQLException e) {
            log.error("Error while processing image: {}", e.getMessage());
            throw new RuntimeException("Error while processing image", e);
        }
    }

    private void updateDownloadUrl(Image image) {
        image.setDownloadUrl("/images/image/download/" + image.getImageId());
        imageRepository.save(image);
        log.info("Download URL updated for image ID: {}", image.getImageId());
    }
}