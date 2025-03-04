package com.fayupable.test.repository;

import com.fayupable.test.entity.user.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IUserRepository extends JpaRepository<UserInfo, UUID> {
    Optional<UserInfo> findByEmail(String email);


    @Query("SELECT u.email FROM UserInfo u WHERE u.userId = :userId")
    Optional<String> findEmailByUserId(UUID userId);

    @Query("SELECT u.roles FROM UserInfo u WHERE u.userId = :userId")
    Optional<String> findRoleByUserId(UUID userId);

}
