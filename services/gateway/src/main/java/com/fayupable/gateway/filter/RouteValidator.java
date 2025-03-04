package com.fayupable.gateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Predicate;

@Component
@Slf4j
public class RouteValidator {

    public static final List<String> openApiEndpoints = List.of(
            "/user/auth/login",
            "/user/auth/register",
            "/auth/register",
            "/auth/token",
            "/eureka"
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> {
                String path = request.getURI().getPath();
                boolean isSecured = !openApiEndpoints
                        .stream()
                        .anyMatch(uri -> {
                            String regexPattern = uri.replace("**", ".*");
                            return path.matches(regexPattern);
                        });

                log.debug("Path: {}, Is Secured: {}", path, isSecured);
                return isSecured;
            };
}