package com.fayupable.test.service.user;

import com.fayupable.test.dto.user.UserInfoDto;
import com.fayupable.test.dto.user.VerifyDto;
import com.fayupable.test.request.login.LoginRequest;
import com.fayupable.test.request.user.AddUserInfoRequest;
import com.fayupable.test.request.user.UpdateUserInfoRequest;
import com.fayupable.test.response.LoginResponse;

import java.util.Map;
import java.util.UUID;

public interface IUserService {
    String getEmailByUserId(UUID userId);

    String getRoleByUserId(UUID userId);

//    UserInfoDto addUser(AddUserInfoRequest request);
//
//    LoginResponse login(LoginRequest request);
//
//    Map<String, Object> validateToken(String token);

    UserInfoDto verifyUser(VerifyDto code);

    String resendVerificationCode();

    UserInfoDto updateUserInfo(UpdateUserInfoRequest updateUserInfoRequest, UUID userId);
}
