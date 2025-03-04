package com.fayupable.test.mapper.user;

import com.fayupable.test.dto.user.UserInfoDto;
import com.fayupable.test.entity.user.UserInfo;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class UserInfoMapper {

    public UserInfoDto fromUserInfo(UserInfo userInfo) {
        if (userInfo == null) {
            return null;
        }

        UserInfoDto dto = new UserInfoDto();
        dto.setUserId(userInfo.getUserId());
        dto.setUsername(userInfo.getUsername());
        dto.setEmail(userInfo.getEmail());
        dto.setFirstName(userInfo.getFirstName());
        dto.setLastName(userInfo.getLastName());
        dto.setStatus(userInfo.getStatus());
        dto.setRoles(userInfo.getRoles());
        dto.setVerified(userInfo.isVerified());
        dto.setCreatedAt(userInfo.getCreatedAt());
        dto.setUpdatedAt(userInfo.getUpdatedAt());

        if (userInfo.getProfiles() != null) {
            dto.setProfiles(userInfo.getProfiles().stream()
                    .map(UserProfileMapper::fromUserProfile)
                    .collect(Collectors.toList()));
        }

        if (userInfo.getContacts() != null) {
            dto.setContacts(userInfo.getContacts().stream()
                    .map(UserContactMapper::fromUserContact)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
