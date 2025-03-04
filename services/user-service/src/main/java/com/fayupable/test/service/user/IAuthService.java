package com.fayupable.test.service.user;

import com.fayupable.test.dto.user.UserInfoDto;
import com.fayupable.test.request.login.LoginRequest;
import com.fayupable.test.request.user.AddUserInfoRequest;
import com.fayupable.test.response.LoginResponse;

import java.util.Map;

public interface IAuthService {
    LoginResponse login(LoginRequest request);
    UserInfoDto addUser(AddUserInfoRequest request);
    Map<String, Object> validateToken(String token);
}
