package com.fayupable.test.mapper;


import com.fayupable.test.dto.ImageDto;
import com.fayupable.test.entity.Image;
import org.springframework.stereotype.Service;

@Service
public class ImageMapper {

    public ImageDto fromImage(Image image) {
        if (image == null) {
            return null;
        }
        ImageDto imageDto = new ImageDto();
        imageDto.setImageId(image.getImageId());
        imageDto.setUserId(image.getUserId());
        imageDto.setFileName(image.getFileName());
        imageDto.setFileType(image.getFileType());
        imageDto.setDownloadUrl(image.getDownloadUrl());
        imageDto.setCreatedAt(image.getCreatedAt());
        imageDto.setUpdatedAt(image.getUpdatedAt());
        return imageDto;
    }
}
