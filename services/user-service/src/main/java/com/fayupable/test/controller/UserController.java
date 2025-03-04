package com.fayupable.test.controller;

import com.fayupable.test.dto.user.VerifyDto;
import com.fayupable.test.request.user.AddUserInfoRequest;
import com.fayupable.test.request.user.UpdateUserInfoRequest;
import com.fayupable.test.response.UserResponse;
import com.fayupable.test.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @GetMapping("/{userId}/email")
    public String getEmailByUserId(@PathVariable UUID userId) {
        return userService.getEmailByUserId(userId);
    }

    @GetMapping("/{userId}/role")
    public String getRoleByUserId(@PathVariable UUID userId) {
        return userService.getRoleByUserId(userId);
    }

    @PostMapping("/verify")
    public ResponseEntity<UserResponse> verifyUser(@RequestBody VerifyDto code) {
        return ResponseEntity.ok(new UserResponse("User verified", userService.verifyUser(code)));
    }

    @PostMapping("/resend-verification-code")
    public ResponseEntity<UserResponse> resendVerificationCode() {
        return ResponseEntity.ok(new UserResponse("Verification code resent", userService.resendVerificationCode()));
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<UserResponse> update(@RequestBody UpdateUserInfoRequest request, @PathVariable UUID userId) {
        return ResponseEntity.ok(new UserResponse("User updated", userService.updateUserInfo(request, userId)));
    }


}
