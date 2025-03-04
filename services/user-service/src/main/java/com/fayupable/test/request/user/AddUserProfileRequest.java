package com.fayupable.test.request.user;

import lombok.Data;

@Data
public class AddUserProfileRequest {
    private String bio;
    private String avatarUrl;
}
