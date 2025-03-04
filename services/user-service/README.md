# User Service

## Overview

The **User Service** is a core microservice responsible for user management, authentication, and communication with
other microservices in a distributed system. It handles user registration, authentication, profile management, and
verification processes while integrating with external services like Kafka for event-driven messaging and JWT for secure
authentication.

## Technologies Used

- **Spring Boot 3.4.2**: Framework for building Java-based microservices.
- **Spring Cloud Config**: Centralized configuration management.
- **Spring Cloud Netflix Eureka Client**: Enables service discovery and registration with Eureka Server.
- **Spring Security**: Provides authentication and authorization features with JWT support.
- **Spring Boot Validation**: Enables input validation for request payloads.
- **Spring Boot Web**: Used for building RESTful APIs.
- **Spring Data JPA**: ORM framework for interacting with the database.
- **PostgreSQL**: Database for storing user information and credentials.
- **Kafka**: Event-driven messaging for user events like registration and login.
- **Lombok**: Reduces boilerplate code for Java classes.
- **JWT (JSON Web Tokens)**: Secure token-based authentication system.
- **Docker**: Containerization for deployment.

## Features

- User registration with email and password.
- JWT-based authentication and authorization.
- User verification with email verification codes.
- User profile management with multiple contact details support.
- Role-based access control.
- Kafka-based event-driven messaging for user events.
- Token validation for inter-service communication.
- User status tracking (PENDING, VERIFIED, etc.).
- Resending verification codes for user convenience.

## Project Structure

```
user-service/
│── src/main/java/com/fayupable/test/
│   ├── config/             # Configuration classes
│   ├── controller/         # REST API controllers
│   │   ├── AuthController.java   # Authentication endpoints
│   │   ├── UserController.java   # User management endpoints
│   ├── dto/                # Data transfer objects
│   │   ├── user/           # User-related DTOs
│   ├── entity/             # JPA entities
│   │   ├── UserInfo.java   # Core user entity
│   │   ├── UserContact.java # User contact information
│   │   ├── UserProfile.java # User profile details
│   │   ├── VerificationCode.java # Email verification codes
│   ├── enums/              # Enumerations
│   │   ├── UserRole.java   # User role types
│   │   ├── UserStatus.java # User account statuses
│   ├── exception/          # Custom exceptions
│   ├── kafka/              # Kafka event producers
│   │   ├── UserProducer.java # Produces user events
│   ├── mapper/             # Object mappers
│   │   ├── UserInfoMapper.java # Maps between entities and DTOs
│   ├── repository/         # Database repositories
│   │   ├── UserRepository.java # User data access
│   │   ├── VerificationCodeRepository.java # Verification code storage
│   ├── request/            # Request models
│   │   ├── login/          # Login requests
│   │   ├── user/           # User data requests
│   ├── response/           # Response models
│   │   ├── UserResponse.java # Standard user responses
│   │   ├── LoginResponse.java # Authentication responses
│   ├── security/           # Security configuration
│   │   ├── jwt/            # JWT utilities and configuration
│   │   ├── WebSecurityConfig.java # Security setup
│   ├── service/            # Business logic
│   │   ├── user/           # User service implementations
│   │   ├── verification/   # Verification service implementations
│── src/main/resources/
│   ├── application.yml     # Application configuration
│── pom.xml                 # Dependencies and build configuration
```

## Installation & Setup

### Prerequisites

- Java 21 installed
- Maven installed
- PostgreSQL database set up
- Kafka broker running

### Configuration

Configure `application.yml` and `user-service.yml` to set up the **User Service**:

```yaml
spring:
  config:
    import: optional:configserver:http://localhost:8888
  application:
    name: user-service
```


```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/user
    username: fayupable
    password: fayupable
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    show-sql: true


server:
  port: 8100

auth:
  token:
    expirationInMils: 3600000
    jwtSecret: 36763979244226452948404D635166546A576D5A7134743777217A25432A4620
```

### Running the Server

1. Clone the repository:
   ```sh
   git clone https://github.com/fayupable/user-service.git
   cd user-service
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

- **Register a user:**
  ```sh
  POST http://localhost:8070/user/auth/register
  Content-Type: application/json
  
  {
    "email": "johndoe@example.com",
    "password": "securepassword123",
    "contacts": [
      {
        "contactType": "PHONE",
        "contactValue": "+1-555-123-4567"
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

- **Login:**
  ```sh
  POST http://localhost:8070/user/auth/login
  Content-Type: application/json
  
  {
    "email": "johndoe@example.com",
    "password": "securepassword123"
  }
  ```

- **Verify a user account:**
  ```sh
  POST http://localhost:8070/user/auth/verify
  Content-Type: application/json
  
  {
    "email": "johndoe@example.com",
    "verificationCode": "abcd1234"
  }
  ```

- **Resend verification code:**
  ```sh
  POST http://localhost:8070/user/auth/resend-verification-code
  Authorization: Bearer YOUR_ACCESS_TOKEN
  ```

- **Validate token:**
  ```sh
  POST http://localhost:8070/user/auth/validate
  Authorization: Bearer YOUR_ACCESS_TOKEN
  ```

### User Management Endpoints

- **Get user email by ID:**
  ```sh
  GET http://localhost:8070/user/{userId}/email
  ```

- **Get user role by ID:**
  ```sh
  GET http://localhost:8070/user/{userId}/role
  ```

## Data Models

### Core Entities

- **UserInfo**: The primary entity representing a registered user
  ```java
  public class UserInfo {
      @Id
      @GeneratedValue(strategy = GenerationType.UUID)
      private UUID userId;
      private String email;
      private String password;
      private UserRole role;
      private UserStatus status;
      private boolean verified;
      
      @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
      private List<UserContact> contacts;
      
      @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
      private List<UserProfile> profiles;
  }
  ```

- **VerificationCode**: Stores verification codes sent to users
  ```java
  public class VerificationCode {
      @Id
      @GeneratedValue(strategy = GenerationType.UUID)
      private UUID id;
      private UUID userId;
      private String code;
      private LocalDateTime expirationTime;
      private boolean used;
  }
  ```

## Authentication Flow

1. **Registration Process**:
    - User submits registration details
    - Service creates new user with PENDING status
    - Verification code is generated and sent via Kafka
    - Email service sends verification email to user

2. **Verification Process**:
    - User submits verification code
    - Service validates code and expiration
    - User status is updated to VERIFIED
    - User can now access protected resources

3. **Login Process**:
    - User submits credentials
    - Service authenticates and generates JWT
    - Token contains user ID and permissions
    - Login event is sent to Kafka for auditing

## Kafka Integration

The **User Service** publishes events to Kafka when users register or log in:

### Registration Event

```markdown
2025-02-11T15:02:13.083+03:00 INFO 15913 --- [user-service] [nio-8070-exec-4] c.f.mailsender.kafka.UserProducer        :
Sending confirmation: UserConfirmation(userId=65045ce9-4b54-4389-b574-d935b9b02fdb, email=johndoe@example.com,
verificationCode=cd943fe27c, verificationCodeExpiration=2025-02-11T15:07:13.059396, userLoginTime=null)
```

```json
{
  "userId": "65045ce9-4b54-4389-b574-d935b9b02fdb",
  "email": "johndoe@example.com",
  "verificationCode": "cd943fe27c",
  "verificationCodeExpiration": "2025-02-11T15:07:13.059396",
  "userLoginTime": null
}
```

### Login Event

```markdown
2025-02-11T15:01:40.788+03:00 INFO 15913 --- [user-service] [nio-8070-exec-3] c.f.mailsender.kafka.UserProducer        :
Sending login: UserConfirmation(userId=d1c4d737-3a05-41be-940f-f859de08da73, email=johndoe@example.com,
verificationCode=null, verificationCodeExpiration=null, userLoginTime=2025-02-11T15:01:40.788362)
```

```json
{
  "userId": "d1c4d737-3a05-41be-940f-f859de08da73",
  "email": "johndoe@example.com",
  "verificationCode": null,
  "verificationCodeExpiration": null,
  "userLoginTime": "2025-02-11T15:01:40.788362"
}
```

## Security Implementation

The service uses Spring Security with JWT for authentication:

- **JwtUtils**: Generates and validates JWT tokens
- **AuthDetails**: Custom UserDetails implementation for authentication
- **SecurityContextHolder**: Stores currently authenticated user
- **@PreAuthorize**: Annotation-based method security

## Docker Support

To run the **User Service** inside a Docker container:

1. Create a `Dockerfile`:
   ```Dockerfile
   FROM openjdk:21-jdk
   WORKDIR /app
   COPY target/user-service-0.0.1-SNAPSHOT.jar app.jar
   EXPOSE 8070
   ENTRYPOINT ["java", "-jar", "app.jar"]
   ```

2. Build and run the container:
   ```sh
   docker build -t user-service .
   docker run -p 8070:8070 --name user-service --network microservices-network user-service
   ```

## Common Issues & Troubleshooting

### 1. Database Connection Error

**Error:** `org.postgresql.util.PSQLException: Connection refused`
**Solution:** Ensure PostgreSQL is running and configured correctly.

```sh
# Check PostgreSQL service status
sudo service postgresql status

# Start PostgreSQL if not running
sudo service postgresql start
```

### 2. Kafka Connection Issue

**Error:** `org.apache.kafka.common.errors.TimeoutException: Timeout expired`
**Solution:** Ensure Kafka broker is running and accessible.

```sh
# Check Kafka service status
sudo systemctl status kafka

# Start Kafka if not running
sudo systemctl start zookeeper
sudo systemctl start kafka
```

### 3. Authentication Failure

**Error:** `Invalid username or password`
**Solution:** Ensure the user is registered and credentials are correct.

### 4. Verification Code Expiration

**Error:** `Verification code has expired`
**Solution:** Request a new verification code through the resend endpoint.

### 5. JWT Token Issues

**Error:** `JWT signature does not match locally computed signature`
**Solution:** Ensure the same secret key is used for generation and validation.

## Testing

The service includes comprehensive tests:

```sh
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceImplTest

# Generate test coverage report
mvn jacoco:report
```

## Performance Considerations

- Database connection pooling is configured for optimal performance
- Caching is implemented for frequently accessed user data
- JWT tokens are designed with a reasonable expiration time
- Kafka producers use asynchronous sending for better throughput

## Conclusion

The **User Service** forms the foundation of the microservices architecture, providing robust user management,
authentication, and authorization capabilities. With its clean separation of concerns and event-driven communication
model, it enables seamless integration with other microservices while maintaining high security standards for user data.