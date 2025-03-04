package com.fayupable.test.request;

import lombok.Data;

@Data
public class AddUserProfileRequest {
    private String bio;
    private String avatarUrl;
}
