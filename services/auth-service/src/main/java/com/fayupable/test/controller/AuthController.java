package com.fayupable.test.controller;

import com.fayupable.test.client.UserClient;
import com.fayupable.test.request.AddUserInfoRequest;
import com.fayupable.test.request.LoginRequest;
import com.fayupable.test.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserClient userClient;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(userClient.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AddUserInfoRequest request) {
        return ResponseEntity.ok(userClient.register(request));
    }


}
