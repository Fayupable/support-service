package com.fayupable.test.client.image;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ImageResponse {
    private String message;
    private List<ImageDto> data;
}