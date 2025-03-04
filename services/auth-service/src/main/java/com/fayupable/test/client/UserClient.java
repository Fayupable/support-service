package com.fayupable.test.client;

import com.fayupable.test.request.AddUserInfoRequest;
import com.fayupable.test.request.LoginRequest;
import com.fayupable.test.response.AuthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", url = "${application.config.user-url}")
public interface UserClient {

    @PostMapping("/login")
    AuthResponse login(@RequestBody LoginRequest loginRequest);

    @PostMapping("/register")
    AuthResponse register(@RequestBody AddUserInfoRequest loginRequest);
}
