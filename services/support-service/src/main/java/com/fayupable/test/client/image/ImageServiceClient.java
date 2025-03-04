package com.fayupable.test.client.image;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "image-service", url = "${application.config.image-url}")
public interface ImageServiceClient {
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ImageResponse addImages(@RequestPart("file") List<MultipartFile> file, @RequestHeader("userId") UUID userId);
}
