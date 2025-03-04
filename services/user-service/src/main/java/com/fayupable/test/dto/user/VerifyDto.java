package com.fayupable.test.dto.user;

import lombok.Data;

@Data
public class VerifyDto {
    private String email;
    private String verificationCode;
}
