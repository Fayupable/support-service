package com.fayupable.test.dto.verificationcode;

import lombok.Data;

import java.util.UUID;

@Data
public class VerificationCodeDto {
    private UUID userId;
    private String verificationCode;
}
