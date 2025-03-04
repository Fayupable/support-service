# Authentication Service

## Overview

The **Authentication Service** is a specialized microservice that acts as a gateway for authentication operations in a
distributed system. It primarily delegates authentication requests to the User Service while providing a unified access
point for client applications. This service simplifies the authentication flow by abstracting the User Service's
authentication mechanisms and offering a clean API for login and registration operations.

## Technologies Used

- **Spring Boot 3.4.2**: Framework for building Java-based microservices
- **Spring Cloud Config**: Centralized configuration management
- **Spring Cloud Netflix Eureka Client**: Service discovery and registration
- **Spring Cloud OpenFeign**: Declarative REST client for service communication
- **Spring Web**: Used for building RESTful APIs
- **Lombok**: Reduces boilerplate code for Java classes
- **Docker**: Containerization for deployment

## Features

- User login through User Service integration
- User registration through User Service integration
- Clean API facade pattern for authentication operations
- Centralized authentication entry point for client applications

## Project Structure

```
auth-service/
│── src/main/java/com/fayupable/test/
│   ├── client/           # Feign clients
│   │   ├── UserClient.java   # User service client
│   ├── controller/       # REST API controllers
│   │   ├── AuthController.java  # Authentication endpoints
│   ├── request/          # Request models
│   │   ├── LoginRequest.java    # Login request
│   │   ├── AddUserInfoRequest.java  # Registration request
│   │   ├── AddUserContactRequest.java  # Contact info
│   │   ├── AddUserProfileRequest.java  # Profile info
│   ├── response/         # Response models
│   │   ├── AuthResponse.java    # Authentication response
│── src/main/resources/
│   ├── application.yml   # Configuration file
│── pom.xml               # Dependencies and build configuration
```

## Installation & Setup

### Prerequisites

- Java 21 installed
- Maven installed
- User Service running and accessible

### Configuration

Configure `application.yml` and `auth-service.yml` for the **Authentication Service**:

```yaml
spring:
  config:
    import: optional:configserver:http://localhost:8888
  application:
    name: auth-service
```

```yaml
server:
  port: 8070

application:
  config:
    user-url: localhost:8222/user/auth

```

### Running the Server

1. Clone the repository:
   ```sh
   git clone https://github.com/fayupable/auth-service.git
   cd auth-service
   ```
2. Build the project:
   ```sh
   mvn clean package
   ```
3. Run the application:
   ```sh
   mvn spring-boot:run
   ```

## API Documentation

### Authentication Endpoints

- **Login:**
  ```sh
  POST http://localhost:8090/api/auth/login
  Content-Type: application/json
  
  {
    "email": "johndoe@example.com",
    "password": "securepassword123"
  }
  ```

- **Register a user:**
  ```sh
  POST http://localhost:8090/api/auth/register
  Content-Type: application/json
  
  {
    "username": "johndoe",
    "email": "johndoe@example.com",
    "password": "securepassword123",
    "firstName": "John",
    "lastName": "Doe",
    "contacts": [
      {
        "phoneNumber": "+1-555-123-4567"
      }
    ],
    "profiles": [
      {
        "bio": "Software Developer with 5 years experience",
        "avatarUrl": "https://example.com/avatar.png"
      }
    ]
  }
  ```

## Integration with User Service

The Authentication Service uses OpenFeign to communicate with the User Service:

```java

@FeignClient(name = "user-service", url = "${application.config.user-url}")
public interface UserClient {

    @PostMapping("/login")
    AuthResponse login(@RequestBody LoginRequest loginRequest);

    @PostMapping("/register")
    AuthResponse register(@RequestBody AddUserInfoRequest loginRequest);
}
```

This design allows the Authentication Service to:

1. Act as a proxy for authentication requests
2. Forward user credentials to the User Service
3. Return authentication responses to clients

## Authentication Flow

1. **Login Flow**:
    - Client submits credentials to Auth Service
    - Auth Service forwards credentials to User Service
    - User Service authenticates and returns JWT token
    - Auth Service returns token to client

2. **Registration Flow**:
    - Client submits registration details to Auth Service
    - Auth Service forwards registration data to User Service
    - User Service creates user and returns confirmation
    - Auth Service returns confirmation to client

## Gateway Integration

The Authentication Service is designed to work with API Gateway for centralized authentication:

```yaml
spring:
   cloud:
      gateway:
         discovery:
            locator:
               enabled: true
         routes:
            - id: auth-service
              uri: lb://auth-service
              predicates:
                 - Path=/api/auth/**
            - id: user-service
              uri: lb://user-service
              predicates:
                 - Path=/user/**
              filters:
                 - AuthenticationFilter
            - id: support-service
              uri: lb://support-service
              predicates:
                 - Path=/support-tickets/**
              filters:
                 - AuthenticationFilter
            - id: image-service
              uri: lb://image-service
              predicates:
                 - Path=/images/**
              filters:
                 - AuthenticationFilter
server:
   port: 8222

auth:
   token:
      expirationInMils: 3600000
      jwtSecret: 36763979244226452948404D635166546A576D5A7134743777217A25432A4620
```

## Docker Support

Run the **Authentication Service** in a Docker container:

1. Create a `Dockerfile`:
   ```Dockerfile
   FROM openjdk:21-jdk
   WORKDIR /app
   COPY target/auth-service-0.0.1-SNAPSHOT.jar app.jar
   EXPOSE 8090
   ENTRYPOINT ["java", "-jar", "app.jar"]
   ```

2. Build and run the container:
   ```sh
   docker build -t auth-service .
   docker run -p 8090:8090 --name auth-service --network microservices-network auth-service
   ```

## Common Issues & Troubleshooting

### 1. User Service Connection Issues

**Error:** `Connection refused to User Service`
**Solution:** Ensure User Service is running and network connectivity is established.

```sh
# Check if User Service is accessible
curl http://localhost:8070/actuator/health

# Check Eureka registry for User Service
curl http://localhost:8761/eureka/apps/user-service
```

### 2. Authentication Failure

**Error:** `Invalid username or password`
**Solution:** Ensure the user is registered and credentials are correct.

### 3. Registration Failure

**Error:** `Email already exists` or `User registration failed`
**Solution:** Verify registration data or check if user already exists.

## Future Enhancements

1. **Enhanced Security**:
    - Add rate limiting for login attempts
    - Implement IP-based blocking for suspicious activities
    - Add CAPTCHA for registration

2. **Additional Authentication Methods**:
    - OAuth2 integration
    - Social login (Google, Facebook, etc.)
    - Two-factor authentication

3. **Monitoring and Metrics**:
    - Track authentication attempts
    - Monitor success/failure rates
    - Alert on unusual authentication patterns

## Conclusion

The **Authentication Service** provides a simple but effective facade for authentication operations in your
microservices architecture. By centralizing the authentication entry points while delegating the actual authentication
logic to the User Service, it creates a clean separation of concerns that makes your system more maintainable and easier
to secure.

The service's current implementation focuses on basic authentication flows through the User Service integration,
providing a foundation that can be extended with additional security features and authentication methods as your
application evolves.