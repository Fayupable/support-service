package com.fayupable.test.controller;

import com.fayupable.test.dto.user.VerifyDto;
import com.fayupable.test.request.login.LoginRequest;
import com.fayupable.test.request.user.AddUserInfoRequest;
import com.fayupable.test.response.LoginResponse;
import com.fayupable.test.response.UserResponse;
import com.fayupable.test.security.jwt.JwtUtils;
import com.fayupable.test.service.user.IAuthService;
import com.fayupable.test.service.user.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody AddUserInfoRequest request) {
        return ResponseEntity.ok(new UserResponse("User registered", userService.addUser(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        return ResponseEntity.ok(userService.validateToken(token));
    }

}