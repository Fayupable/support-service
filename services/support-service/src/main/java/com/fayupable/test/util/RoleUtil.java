package com.fayupable.test.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fayupable.test.client.user.UserClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoleUtil {
    private final UserClient userClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserRoleContext userRoleContext;


    private static final Map<String, Integer> roleHierarchy;

    static {
        roleHierarchy = new HashMap<>();
        roleHierarchy.put("ROLE_ADMIN", 4);
        roleHierarchy.put("ROLE_MODERATOR", 3);
        roleHierarchy.put("ROLE_SUPPORT_STAFF", 2);
        roleHierarchy.put("ROLE_USER", 1);
    }

    /**
     * RoleUtil is a utility class that helps in determining whether a user has the required role
     * or a higher role based on a predefined role hierarchy.
     * This class provides role-based access control functionality without using Spring Security.
     * It leverages the role hierarchy defined in the static block and communicates with the
     * UserClient to fetch the user's role based on their user ID.
     * <p>
     * Usage:
     * - You can use the method `hasRoleOrHigher(UUID userId, String requiredRole)` to check
     * if a user has the required role or a higher role.
     * <p>
     * Role Hierarchy:
     * - ROLE_ADMIN has the highest priority (level 4).
     * - ROLE_MODERATOR has a priority level of 3.
     * - ROLE_SUPPORT_STAFF has a priority level of 2.
     * - ROLE_USER has the lowest priority (level 1).
     * <p>
     * Example:
     * - If a user has the role "ROLE_ADMIN", they will automatically be considered to have
     * the roles "ROLE_MODERATOR", "ROLE_SUPPORT_STAFF", and "ROLE_USER" as well.
     * - If a user has the role "ROLE_SUPPORT_STAFF", they will also be considered to have
     * the role "ROLE_USER", but not "ROLE_MODERATOR" or "ROLE_ADMIN".
     * <p>
     * Methods:
     * - boolean hasRoleOrHigher(UUID userId, String requiredRole):
     * - Fetches the user's role from the UserClient using the provided userId.
     * - Compares the user's role level with the required role level using the roleHierarchy map.
     * - Returns true if the user has the required role or a higher role, otherwise false.
     * - Handles any exceptions during role retrieval and logs an error message.
     *
     * @param userId       The unique identifier of the user whose role needs to be checked.
     * @param requiredRole The role against which the user's role will be checked.
     * @return true if the user has the required role or a higher role, false otherwise.
     */
    public boolean hasRoleOrHigher(UUID userId, String requiredRole) {
        log.info("Checking role for userId: {}, required role: {}", userId, requiredRole);
        String userRole = userClient.getRoleByUserId(userId);

        if (userRole == null || !roleHierarchy.containsKey(userRole)) {
            log.warn("Invalid role detected - userRole: {}, exists in hierarchy: {}",
                    userRole, roleHierarchy.containsKey(userRole));
            return false;
        }

        boolean hasRole = roleHierarchy.get(userRole) >= roleHierarchy.get(requiredRole);
        log.info("Role check result - userRole level: {}, required role level: {}, hasRole: {}",
                roleHierarchy.get(userRole), roleHierarchy.get(requiredRole), hasRole);

        return hasRole;
    }

    public boolean hasRoleOrHigherTest(UUID userId, String requiredRole) {
        try {
            String userRole = userRoleContext.getUserRole(userId);
            if (userRole == null || !roleHierarchy.containsKey(userRole)) {
                return false;
            }
            return roleHierarchy.get(userRole) >= roleHierarchy.get(requiredRole);
        } catch (Exception e) {
            log.error("Error checking user role for userId: {}", userId, e);
            return false;
        }
    }
}