package com.fayupable.test.service.verification;

import com.fayupable.test.dto.verificationcode.VerificationCodeDto;
import com.fayupable.test.entity.verification.VerificationCode;
import com.fayupable.test.request.verification.AddVerificationCodeRequest;
import jakarta.transaction.Transactional;

import java.util.UUID;

public interface IVerificationCodeService {

    VerificationCode generateAndSaveVerificationCode(UUID userId);

    void deleteExpiredVerificationCodes();

    VerificationCodeDto validateVerificationCode(AddVerificationCodeRequest addVerificationCodeRequest);

    VerificationCode resendVerificationCode(UUID userId);
}
