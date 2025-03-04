package com.fayupable.test.util;

import com.fayupable.test.client.user.UserClient;
import com.fayupable.test.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.UUID;

@Component
@RequestScope
@RequiredArgsConstructor
@Slf4j
public class UserRoleContext {

    private final UserClient userClient;
    private UUID cachedUserId;
    private String cachedRole;

    public String getUserRole(UUID userId) {
        if (cachedUserId != null && cachedUserId.equals(userId)) {
            return cachedRole;
        }
        try {
            log.info("Fetching role for userId: {}", userId);
            this.cachedUserId = userId;
            this.cachedRole = userClient.getRoleByUserId(userId);
            return cachedRole;
        } catch (Exception e) {
            log.error("Failed to fetch role for userId: {}", userId, e);
            throw new UnauthorizedException("Failed to retrieve user role");
        }
    }
}