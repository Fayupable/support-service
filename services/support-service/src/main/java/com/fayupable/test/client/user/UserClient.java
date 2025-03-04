package com.fayupable.test.client.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service", url = "${application.config.user-url}")
public interface UserClient {
    @GetMapping("/{userId}/email")
    String getEmailByUserId(@PathVariable UUID userId);

    @GetMapping("/{userId}/role")
    String getRoleByUserId(@PathVariable UUID userId);

}
