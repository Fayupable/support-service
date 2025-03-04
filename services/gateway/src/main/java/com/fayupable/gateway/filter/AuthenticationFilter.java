package com.fayupable.gateway.filter;


import com.fayupable.gateway.dto.UserHeaderDto;
import com.fayupable.gateway.util.JwtUtil;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            log.info("AuthenticationFilter triggered for request: {}", exchange.getRequest().getURI());

            if (validator.isSecured.test(exchange.getRequest())) {
                log.info("Secured request detected: {}", exchange.getRequest().getURI());

                try {
                    String authHeader = extractAuthHeader(exchange);
                    validateToken(authHeader);
                    UserHeaderDto userHeaderDto = UserHeaderDto.builder()
                            .userId(jwtUtil.extractUserId(authHeader))
                            .username(jwtUtil.extractUsername(authHeader))
                            .role(jwtUtil.extractRoles(authHeader).get(0))
                            .build();
//
//                    String username = jwtUtil.extractUsername(authHeader);
//                    String userId = jwtUtil.extractUserId(authHeader);
//                    String role = jwtUtil.extractRoles(authHeader).get(0);

                    log.info("✅ Token doğrulandı. userId: {}, username: {}", userHeaderDto.getUserId(), userHeaderDto.getUsername());

//                    exchange.getResponse().addCookie(createUserIdCookie(userId));

                    ServerHttpRequest modifiedRequest = addHeadersToRequest(exchange, userHeaderDto);

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());

                } catch (Exception e) {
                    return handleUnauthorizedAccess(exchange, e);
                }
            }

            return chain.filter(exchange);
        };
    }

    private String extractAuthHeader(ServerWebExchange exchange) {
        if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            log.error("❌ Missing authorization header. Request URI: {}, Method: {}",
                    exchange.getRequest().getURI(),
                    exchange.getRequest().getMethod());
            throw new RuntimeException("Missing authorization header");
        }

        String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return authHeader;
    }

    private void validateToken(String authHeader) {
        try {
            jwtUtil.validateToken(authHeader);
        } catch (Exception e) {
            log.error("❌ Invalid token detected: {}", e.getMessage());
            throw new RuntimeException("Invalid token");
        }
    }

//    private ResponseCookie createUserIdCookie(String userId) {
//        return ResponseCookie.from("userId", userId)
//                .path("/")
//                .httpOnly(true)
//                .secure(false)
//                .build();
//    }

    private ServerHttpRequest addHeadersToRequest(ServerWebExchange exchange, UserHeaderDto userHeaderDto) {
        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header("userId", userHeaderDto.getUserId())
                .header("username", userHeaderDto.getUsername())
                .header("role", userHeaderDto.getRole())
                .build();

        log.info("✅ `userId` ve `username` header'ları eklendi: {}", modifiedRequest.getHeaders());
        return modifiedRequest;
    }

    private Mono<Void> handleUnauthorizedAccess(ServerWebExchange exchange, Exception e) {
        log.error("❌ Unauthorized access attempt detected. Request URI: {}, Method: {}, Error: {}",
                exchange.getRequest().getURI(),
                exchange.getRequest().getMethod(),
                e.getMessage());

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    public static class Config {

    }
}