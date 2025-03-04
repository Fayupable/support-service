package com.fayupable.test.service.verification;

import com.fayupable.test.dto.verificationcode.VerificationCodeDto;
import com.fayupable.test.entity.user.UserInfo;
import com.fayupable.test.entity.verification.VerificationCode;
import com.fayupable.test.exception.UserAlreadyVerifiedException;
import com.fayupable.test.exception.UserNotFoundException;
import com.fayupable.test.exception.VerificationCodeExpiredException;
import com.fayupable.test.exception.VerificationCodeNotFoundException;
import com.fayupable.test.mapper.verificationcode.VerificationCodeMapper;
import com.fayupable.test.repository.IUserRepository;
import com.fayupable.test.repository.IVerificationCodeRepository;
import com.fayupable.test.request.verification.AddVerificationCodeRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationCodeService implements IVerificationCodeService {

    private final IVerificationCodeRepository verificationCodeRepository;
    private final VerificationCodeMapper verificationCodeMapper;
    private final IUserRepository userRepository;

    @Transactional
    @Override
    public VerificationCode generateAndSaveVerificationCode(UUID userId) {
        UserInfo user = getUserById(userId);
        String verificationCode = generateVerificationCode();
        VerificationCode verificationEntity = createVerificationCode(user, verificationCode);
        verificationCodeRepository.save(verificationEntity);
        return verificationEntity;
    }

    @Transactional
    @Override
    public void deleteExpiredVerificationCodes() {
        verificationCodeRepository.deleteAllByExpirationTimeBefore(LocalDateTime.now());
    }

    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #addVerificationCodeRequest.userId")
    @Transactional
    @Override
    public VerificationCodeDto validateVerificationCode(AddVerificationCodeRequest addVerificationCodeRequest) {
        VerificationCode verificationCode = getVerificationCodeFromDb(addVerificationCodeRequest.getUserId(), addVerificationCodeRequest.getCode());
        handleExpireCode(verificationCode);
        markUserAsVerified(addVerificationCodeRequest.getUserId());
        deleteVerificationCode(verificationCode);
        return verificationCodeMapper.fromVerificationCode(verificationCode);
    }

    @PreAuthorize("authentication.principal.id == #userId")
    @Transactional
    @Override
    public VerificationCode resendVerificationCode(UUID userId) {
        UserInfo user = getUserById(userId);
        if (user.isVerified()) {
            throw new UserAlreadyVerifiedException("User already verified");
        }
        VerificationCode verificationCode = verificationCodeRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new VerificationCodeNotFoundException("Verification code not found"));
        verificationCode.setExpirationTime(LocalDateTime.now().plusMinutes(5));
        verificationCode.setCode(generateVerificationCode());
        verificationCodeRepository.save(verificationCode);
        return verificationCode;
    }


    private VerificationCode getVerificationCodeFromDb(UUID userId, String code) {
        return verificationCodeRepository.findByUserUserIdAndCode(userId, code)
                .orElseThrow(() -> new VerificationCodeNotFoundException("Verification code not found"));
    }

    private void handleExpireCode(VerificationCode verificationCode) {
        if (isCodeExpired(verificationCode)) {
            deleteVerificationCodeFromDB(verificationCode);
            throw new VerificationCodeExpiredException("Verification code expired");
        }
    }

    private void deleteVerificationCodeFromDB(VerificationCode verificationCode) {
        verificationCodeRepository.delete(verificationCode);
    }

    private void markUserAsVerified(UUID userId) {
        UserInfo user = getUserById(userId);
        user.setVerified(true);
        userRepository.save(user);
    }

    private UserInfo getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private String generateVerificationCode() {
        return String.format("%06d", new java.util.Random().nextInt(999999));
    }

    private VerificationCode createVerificationCode(UserInfo user, String code) {
        return VerificationCode.builder()
                .user(user)
                .code(code)
                .expirationTime(LocalDateTime.now().plusMinutes(5))
                .build();
    }

    private boolean isCodeExpired(VerificationCode verificationCode) {
        return verificationCode.getExpirationTime().isBefore(LocalDateTime.now());
    }

    private VerificationCode getVerificationCode(UUID userId, String code) {
        return verificationCodeRepository.findByUserUserIdAndCode(userId, code)
                .orElseThrow(() -> new VerificationCodeNotFoundException("Invalid verification code"));
    }

    private void deleteVerificationCode(VerificationCode verificationCode) {
        verificationCodeRepository.delete(verificationCode);
    }


}
