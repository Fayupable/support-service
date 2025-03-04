package com.fayupable.test.request.user;

import lombok.Data;

import java.util.UUID;

@Data
public class UpdateUserProfileRequest {
    private UUID profileId;
    private String bio;
    private String avatarUrl;
}