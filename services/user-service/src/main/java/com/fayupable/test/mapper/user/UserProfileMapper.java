package com.fayupable.test.mapper.user;

import com.fayupable.test.dto.user.UserProfileDto;
import com.fayupable.test.entity.user.UserProfile;
import org.springframework.stereotype.Service;

@Service
public class UserProfileMapper {

    public static UserProfileDto fromUserProfile(UserProfile userProfile) {
        if (userProfile == null) {
            return null;
        }
        UserProfileDto userProfileDto = new UserProfileDto();
        userProfileDto.setBio(userProfile.getBio());
        userProfileDto.setAvatarUrl(userProfile.getAvatarUrl());
        return userProfileDto;
    }
}
