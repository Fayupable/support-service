# API Gateway Service

## Overview
The **API Gateway Service** serves as the central entry point for the Support Ticket System's microservices architecture. It handles all client requests, providing routing, security, and load balancing capabilities.

## Core Features
- **Centralized Authentication** - JWT token validation for all requests
- **Dynamic Routing** - Intelligent request forwarding to microservices
- **Load Balancing** - Request distribution across service instances
- **Circuit Breaking** - Fault tolerance and failover handling
- **Rate Limiting** - Request throttling for API protection
- **Request/Response Transformation** - Data modification and validation
- **Logging & Monitoring** - Request tracking and performance metrics

## Technologies
- **Spring Boot 3.4.2**
- **Spring Cloud Gateway 2024.0.0**
- **Spring Security**
- **JWT Authentication**
- **Spring Cloud Netflix Eureka Client**
- **Java 21**
- **Maven**

## Project Structure
```
gateway-service/
│── src/main/java/com/fayupable/gateway/
│   ├── GatewayApplication.java
│   ├── config/
│   │   ├── GatewayConfig.java
│   │   ├── SecurityConfig.java
│   │   └── RouteConfig.java
│   ├── filter/
│   │   ├── AuthenticationFilter.java
│   │   ├── LoggingFilter.java
│   │   └── RateLimitingFilter.java
│   └── security/
│       ├── JwtUtil.java
│       └── RouterValidator.java
│── src/main/resources/
│   ├── application.yml
│   └── gateway-service.yml
│── pom.xml
```

## Configuration

### Main Application Configuration (`application.yml`)
```yaml
spring:
  application:
    name: gateway-service
  config:
    import: optional:configserver:http://localhost:8888
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true

server:
  port: 8222

jwt:
  secret: your-secret-key
  expiration: 3600000
```

### Route Configuration (`gateway-service.yml`)
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/auth/**
          filters:
            - name: CircuitBreaker
              args:
                name: authService
                fallbackUri: forward:/auth-fallback

        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/users/**
          filters:
            - AuthenticationFilter

        - id: support-service
          uri: lb://support-service
          predicates:
            - Path=/support/**
          filters:
            - AuthenticationFilter
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20

        - id: image-service
          uri: lb://image-service
          predicates:
            - Path=/images/**
          filters:
            - AuthenticationFilter
            - name: RequestSize
              args:
                maxSize: 10MB
```

## Security Implementation

### Authentication Filter
```java
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    private final RouterValidator routerValidator;
    private final JwtUtil jwtUtil;

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (routerValidator.isSecured.test(request)) {
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new UnauthorizedException("Authorization header is missing");
                }

                String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }
                try {
                    jwtUtil.validateToken(authHeader);
                } catch (Exception e) {
                    throw new UnauthorizedException("Invalid token");
                }
            }
            return chain.filter(exchange);
        };
    }
}
```

## API Routes

### Public Endpoints (No Authentication Required)
- `POST /auth/login` - User authentication
- `POST /auth/register` - New user registration
- `GET /auth/validate` - Token validation

### Protected Endpoints (Authentication Required)
- User Service Routes
    - `GET /users/profile` - Get user profile
    - `PUT /users/update` - Update user details

- Support Service Routes
    - `POST /support/tickets` - Create ticket
    - `GET /support/tickets` - List tickets
    - `PUT /support/tickets/{id}` - Update ticket

- Image Service Routes
    - `POST /images/upload` - Upload image
    - `GET /images/{id}` - Get image

## Error Handling

### Global Error Responses
```java
@Configuration
public class GatewayExceptionHandler {
    @Bean
    public ErrorWebExceptionHandler errorWebExceptionHandler() {
        return (ServerWebExchange exchange, Throwable ex) -> {
            if (ex instanceof UnauthorizedException) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            } else if (ex instanceof RateLimitExceededException) {
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            }
            // Other error handling...
            return exchange.getResponse().setComplete();
        };
    }
}
```

## Testing

### Prerequisites
- Running Config Server (port 8888)
- Running Eureka Server (port 8761)
- Running dependent microservices

### Test Endpoints
```sh
# Auth Test
curl -X POST http://localhost:8222/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"password"}'

# Protected Endpoint Test
curl -X GET http://localhost:8222/support/tickets \
  -H "Authorization: Bearer your-jwt-token"
```

## Monitoring

### Actuator Endpoints
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,gateway
```

Access metrics: `http://localhost:8222/actuator/metrics`

## Troubleshooting

### Common Issues
1. **Authentication Failures**
    - Check JWT token validity
    - Verify authorization header format

2. **Service Discovery Issues**
    - Ensure services are registered with Eureka
    - Check service instance health

3. **Rate Limiting**
    - Monitor rate limiter metrics
    - Adjust rate limits if needed

## Conclusion
The Gateway Service is the cornerstone of the Support Ticket System's architecture, providing essential security, routing, and monitoring capabilities. Its proper configuration and maintenance are crucial for system stability and security.
reflects the gateway's critical role in your microservices architecture while maintaining clarity and practicality.