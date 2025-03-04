package com.fayupable.test.request.verification;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AddVerificationCodeRequest {
    private UUID userId;
    private String code;
}
