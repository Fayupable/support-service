package com.fayupable.test.repository;

import com.fayupable.test.entity.verification.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IVerificationCodeRepository extends JpaRepository<VerificationCode, UUID> {
    Optional<VerificationCode> findByUserUserIdAndCode(UUID userUserId, String code);

    void deleteAllByExpirationTimeBefore(LocalDateTime now);

    void deleteAllByUserUserId(UUID userUserId);

    Optional<VerificationCode> findByUserUserId(UUID userUserId);

}
