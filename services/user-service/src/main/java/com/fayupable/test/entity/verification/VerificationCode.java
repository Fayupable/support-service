package com.fayupable.test.entity.verification;

import com.fayupable.test.entity.user.UserInfo;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "verification_code")
public class VerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "verification_code_id")
    private UUID verificationCodeId;

    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserInfo user;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expirationTime;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        expirationTime = LocalDateTime.now().plusMinutes(5);
    }


    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationTime);
    }
}