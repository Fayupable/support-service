# Support Ticket Service

## Overview

The **Support Ticket Service** is a specialized microservice that provides comprehensive functionality for managing customer support tickets within a distributed system. It enables users to create, update, and track support tickets while allowing support staff to manage, prioritize, and resolve these tickets efficiently. The service integrates with User Service for authentication and role verification, Image Service for attachment handling, and uses Kafka for event-driven notifications.

## Technologies Used

- **Spring Boot 3.4.2**: Framework for building Java-based microservices
- **Spring Data JPA**: ORM framework for database operations
- **Spring Cloud OpenFeign**: Declarative REST client for inter-service communication
- **Spring Cloud Config**: Centralized configuration management
- **Spring Cloud Netflix Eureka Client**: Service discovery and registration
- **Kafka**: Event streaming for notifications
- **Redis**: Caching for improved performance
- **PostgreSQL**: Primary database for ticket storage
- **Lombok**: Reduces boilerplate code for Java classes
- **Docker**: Containerization for deployment

## Features

- Create support tickets with optional attachments
- Update ticket details and status
- Role-based access control for actions
- Staff-specific ticket management operations
- Resolution tracking and management
- Event-driven notifications via Kafka
- Redis caching for frequently accessed data
- Image attachment handling through Image Service

## Project Structure

```
support-service/
│── src/main/java/com/fayupable/test/
│   ├── client/           # Feign clients for service integration
│   │   ├── image/        # Image service client
│   │   ├── user/         # User service client
│   ├── config/           # Configuration classes
│   │   ├── FeignClientConfiguration.java
│   │   ├── KafkaProducerConfiguration.java
│   │   ├── RedisConfiguration.java
│   ├── controller/       # REST API controllers
│   │   ├── SupportTicketController.java
│   ├── dto/              # Data transfer objects
│   │   ├── SupportTicketDto.java
│   │   ├── TicketResolutionDto.java
│   ├── entity/           # JPA entities
│   │   ├── SupportTicket.java
│   │   ├── TicketDetails.java
│   │   ├── TicketResolution.java
│   ├── enums/            # Enumerations
│   │   ├── SupportStatus.java
│   │   ├── TicketPriority.java
│   ├── exception/        # Custom exceptions
│   │   ├── InvalidTicketDetailsException.java
│   │   ├── UnauthorizedException.java
│   ├── kafka/            # Kafka message producers and models
│   │   ├── SupportTicketConfirmation.java
│   │   ├── SupportTicketProducer.java
│   │   ├── SupportTicketResolutionConfirmation.java
│   ├── mapper/           # DTO-Entity mappers
│   │   ├── SupportTicketMapper.java
│   ├── repository/       # Data repositories
│   │   ├── ISupportTicketRepository.java
│   ├── request/          # Request models
│   │   ├── AddSupportTicketRequest.java
│   │   ├── UpdateSupportTicketRequest.java
│   ├── response/         # Response models
│   │   ├── SupportTicketResponse.java
│   ├── service/          # Business logic
│   │   ├── ISupportTicketService.java
│   │   ├── SupportTicketService.java
│   │   ├── cache/        # Caching service
│   │   │   ├── SupportTicketCacheService.java
│   ├── util/             # Utility classes
│   │   ├── RoleUtil.java
│   │   ├── UserRoleContext.java
│── src/main/resources/
│   ├── application.yml   # Application configuration
│── pom.xml               # Dependencies and build configuration
```

## Installation & Setup

### Prerequisites

- Java 21 installed
- Maven installed
- PostgreSQL database set up
- Redis server running
- Kafka broker running
- User Service and Image Service accessible

### Configuration

The service relies on Spring Cloud Config for centralized configuration. Key properties in `application.yml`:

```yaml
spring:
  application:
    name: support-service
  datasource:
    url: jdbc:postgresql://localhost:5432/supportdb
    username: fayupable
    password: fayupable
    
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    
  cloud:
    config:
      import: optional:configserver:http://localhost:8888
      
  redis:
    host: localhost
    port: 6379
    
  kafka:
    bootstrap-servers: localhost:9092
    
server:
  port: 8100
  
application:
  config:
    user-url: http://localhost:8070/api/users
    image-url: http://localhost:8110/images
```

### Running the Server

1. Clone the repository:
   ```sh
   git clone https://github.com/your-org/support-service.git
   cd support-service
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

### Ticket Management Endpoints

- **Get all tickets (staff only):**
  ```sh
  GET /support-tickets/all
  Headers: userId: 7f9c63b3-e615-4a9a-8c0e-b4e9d132979d
  ```

- **Get all tickets with Redis caching (staff only):**
  ```sh
  GET /support-tickets/all/redis
  Headers: userId: 7f9c63b3-e615-4a9a-8c0e-b4e9d132979d
  ```

- **Create a new support ticket:**
  ```sh
  POST /support-tickets/add
  Content-Type: multipart/form-data
  Headers: userId: 7f9c63b3-e615-4a9a-8c0e-b4e9d132979d
  
  addSupportTicketRequest: {
    "userId": "7f9c63b3-e615-4a9a-8c0e-b4e9d132979d",
    "status": "PENDING",
    "priority": "MEDIUM",
    "ticketDetails": [
      {
        "description": "I need assistance with my account."
      }
    ],
    "resolutions": []
  }
  attachment: [binary file]
  ```

- **Update a ticket (user):**
  ```sh
  PUT /support-tickets/update/{supportTicketId}
  Content-Type: application/json
  Headers: userId: 7f9c63b3-e615-4a9a-8c0e-b4e9d132979d
  
  {
    "userId": "7f9c63b3-e615-4a9a-8c0e-b4e9d132979d",
    "priority": "HIGH",
    "ticketDetails": [
      {
        "description": "Updated problem description"
      }
    ]
  }
  ```

- **Update a ticket (staff):**
  ```sh
  PUT /support-tickets/update/staff/{supportTicketId}
  Content-Type: application/json
  Headers: userId: 4a2c73b1-d615-3a9a-7c0e-a4e9d132979d
  
  {
    "status": "IN_PROGRESS",
    "priority": "HIGH",
    "resolutions": [
      {
        "resolutionMessage": "Working on your issue"
      }
    ]
  }
  ```

- **Update ticket resolution (staff):**
  ```sh
  PUT /support-tickets/update/staff/resolution/{supportTicketId}
  Content-Type: application/json
  Headers: userId: 4a2c73b1-d615-3a9a-7c0e-a4e9d132979d
  
  {
    "ticketResolutionId": "a1c4d737-3a05-41be-940f-f859de08da73",
    "resolutionMessage": "Issue has been resolved"
  }
  ```

## Data Models

### Support Ticket Entity

```java
public class SupportTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID supportTicketId;
    
    private UUID userId;
    private UUID attachmentId;
    
    @Enumerated(EnumType.STRING)
    private SupportStatus status;
    
    @Enumerated(EnumType.STRING)
    private TicketPriority priority;
    
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TicketDetails> ticketDetails = new HashSet<>();
    
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TicketResolution> resolutions = new HashSet<>();
    
    @Column(name = "ticket_created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "ticket_updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
```

### Ticket Details Entity

```java
public class TicketDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID detailsId;
    
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "support_ticket_id", nullable = false)
    private SupportTicket ticket;
}
```

### Ticket Resolution Entity

```java
public class TicketResolution {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID resolutionId;
    
    private String resolutionMessage;
    
    @Column(name = "closed_at")
    private LocalDateTime closedAt;
    
    @ManyToOne
    @JoinColumn(name = "support_ticket_id", nullable = false)
    private SupportTicket ticket;
}
```

## Service Implementation Details

### Role-Based Access Control

The service implements custom role-based access control using the `RoleUtil` class, which verifies user permissions through the User Service:

```java
public boolean hasRoleOrHigher(UUID userId, String requiredRole) {
    String userRole = userClient.getRoleByUserId(userId);
    
    if (userRole == null || !roleHierarchy.containsKey(userRole)) {
        return false;
    }
    
    return roleHierarchy.get(userRole) >= roleHierarchy.get(requiredRole);
}
```

Role hierarchy:
- ROLE_ADMIN (level 4)
- ROLE_MODERATOR (level 3)
- ROLE_SUPPORT_STAFF (level 2)
- ROLE_USER (level 1)

### Ticket Creation Flow

1. **Validate User Access**: Ensure the user has at least USER role
2. **Process Attachment**: If present, upload to Image Service
3. **Create Support Ticket**: Map request to entity and save to database
4. **Send Confirmation**: Send Kafka event for email notification
5. **Cache Ticket**: Store in Redis for faster retrieval

### Redis Caching Strategy

The service implements smart caching with TTLs based on ticket status:

```java
private long determineTTL(SupportTicket ticket) {
    switch (ticket.getStatus()) {
        case PENDING:
        case IN_PROGRESS:
            return TimeUnit.HOURS.toSeconds(6);
        case CLOSED:
        case RESOLVED:
            return TimeUnit.MINUTES.toSeconds(30);
        default:
            return TimeUnit.HOURS.toSeconds(1);
    }
}
```

### Image Attachment Handling

The service delegates image management to the dedicated Image Service:

```java
private UUID processAttachment(MultipartFile attachment, UUID userId) {
    if (attachment != null && !attachment.isEmpty()) {
        try {
            List<MultipartFile> files = List.of(attachment);
            ImageResponse imageResponse = imageServiceClient.addImages(files, userId);
            List<ImageDto> uploadedImages = imageResponse.getData();
            
            if (!uploadedImages.isEmpty()) {
                return uploadedImages.get(0).getImageId();
            }
        } catch (Exception e) {
            log.error("Failed to upload attachment", e);
            throw new RuntimeException("Failed to upload attachment", e);
        }
    }
    return null;
}
```

### Kafka Event Publishing

The service publishes events to Kafka topics when tickets are created or resolved:

```java
// Ticket creation confirmation
public void sendConfirmation(SupportTicketConfirmation confirmation) {
    Message<SupportTicketConfirmation> message = MessageBuilder
        .withPayload(confirmation)
        .setHeader(KafkaHeaders.TOPIC, "support-ticket-confirmation-topic")
        .setHeader(KafkaHeaders.KEY, confirmation.getSupportTicketId().toString())
        .build();
    kafkaTemplate.send(message);
}

// Ticket resolution confirmation
public void sendResolutionConfirmation(SupportTicketResolutionConfirmation confirmation) {
    Message<SupportTicketResolutionConfirmation> message = MessageBuilder
        .withPayload(confirmation)
        .setHeader(KafkaHeaders.TOPIC, "support-ticket-resolution-topic")
        .setHeader(KafkaHeaders.KEY, confirmation.getSupportTicketId().toString())
        .build();
    resolutionKafkaTemplate.send(message);
}
```

## Common Issues & Troubleshooting

### 1. Kafka Connection Issues

**Error:** `org.apache.kafka.common.errors.TimeoutException: Failed to connect to bootstrap servers within timeout`
**Solution:** Ensure Kafka broker is running and accessible.

```sh
# Check Kafka service status
sudo systemctl status kafka

# Start Kafka if not running
sudo systemctl start zookeeper
sudo systemctl start kafka

# Verify topics exist
kafka-topics.sh --bootstrap-server localhost:9092 --list
```

### 2. Redis Connection Issues

**Error:** `io.lettuce.core.RedisConnectionException: Unable to connect to 127.0.0.1:6379`
**Solution:** Ensure Redis server is running and configured correctly.

```sh
# Check Redis service status
redis-cli ping

# Start Redis if not running
sudo systemctl start redis
```

### 3. User Service Communication Issues

**Error:** `feign.FeignException$InternalServerError: status 500 reading UserClient#getRoleByUserId(UUID)`
**Solution:** Verify that User Service is operational and properly configured.

```sh
# Check User Service health
curl http://localhost:8070/actuator/health

# Verify user endpoint is accessible
curl http://localhost:8070/api/users/{userId}/role
```

### 4. Image Service Integration Issues

**Error:** `feign.FeignException$BadRequest: status 400 reading ImageServiceClient#addImages`
**Solution:** Ensure Image Service accepts the file format and size being sent.

```sh
# Check Image Service health
curl http://localhost:8110/actuator/health

# Verify maximum file size configuration
cat application.yml | grep multipart
```

### 5. Database Connection Problems

**Error:** `org.postgresql.util.PSQLException: Connection refused`
**Solution:** Ensure PostgreSQL is running and database exists.

```sh
# Check PostgreSQL service status
sudo service postgresql status

# Start PostgreSQL if not running
sudo service postgresql start

# Verify database exists
psql -U postgres -c "SELECT datname FROM pg_database WHERE datname = 'supportdb';"
```

## Performance Optimization

### Caching Strategy

The service implements a multi-level caching strategy:

1. **Request-Scoped User Role Caching**: User roles are cached within a request to minimize repeated calls to the User Service:
   ```java
   @RequestScope
   public class UserRoleContext {
       private UUID cachedUserId;
       private String cachedRole;
       
       public String getUserRole(UUID userId) {
           if (cachedUserId != null && cachedUserId.equals(userId)) {
               return cachedRole;
           }
           // Fetch from user service if not cached
       }
   }
   ```

2. **Redis Ticket Caching**: Support tickets are cached in Redis with varying TTLs based on status:
   ```java
   private long determineTTL(SupportTicket ticket) {
       switch (ticket.getStatus()) {
           case PENDING:
           case IN_PROGRESS:
               return TimeUnit.HOURS.toSeconds(6);
           case CLOSED:
           case RESOLVED:
               return TimeUnit.MINUTES.toSeconds(30);
           default:
               return TimeUnit.HOURS.toSeconds(1);
       }
   }
   ```

3. **Bulk Data Caching**: Full ticket lists are cached to reduce database load:
   ```java
   public void cacheAllTickets(List<SupportTicketDto> tickets) {
       redisTemplate.opsForValue().set(ALL_TICKETS_KEY, tickets, 3, TimeUnit.HOURS);
   }
   ```

### Database Optimization

1. **Appropriate Indexing**: Key fields like `userId` and `status` are indexed for faster queries
2. **Lazy Loading**: One-to-many relationships use lazy loading to prevent unnecessary data retrieval
3. **Pagination**: For large result sets, pagination is implemented to limit memory usage
4. **Batch Processing**: Multi-ticket operations are handled in batches

### Asynchronous Processing

1. **Kafka Event Publishing**: Notifications are sent asynchronously via Kafka to prevent blocking:
   ```java
   public void sendConfirmation(SupportTicketConfirmation confirmation) {
       Message<SupportTicketConfirmation> message = MessageBuilder
           .withPayload(confirmation)
           .setHeader(KafkaHeaders.TOPIC, "support-ticket-confirmation-topic")
           .build();
       kafkaTemplate.send(message);
   }
   ```

2. **Non-Blocking I/O**: Redis and database operations use non-blocking approaches where possible

## Scaling Considerations

### Horizontal Scaling

The Support Ticket Service can be horizontally scaled by:

1. **Stateless Design**: The service is designed to be stateless, allowing multiple instances
2. **Service Discovery**: Registered with Eureka for dynamic scaling
3. **Load Balancing**: Client-side load balancing via Ribbon
4. **Database Connection Pooling**: Configured to handle multiple service instances

### Resilience Patterns

1. **Circuit Breaker**: Prevents cascade failures during dependency service outages
2. **Fallback Mechanisms**: Alternative paths when primary functionality is unavailable
3. **Retry Policies**: Automatic retries for transient failures
4. **Bulkhead Pattern**: Isolates failures to prevent system-wide impacts

## Security Implementation

1. **JWT Token Validation**: Authentication tokens are validated on all endpoints
2. **Role-Based Access Control**: Custom implementation ensures proper authorization:
   ```java
   private void validateAndGetStaffUserIdTest() {
       UUID userUUID = getUserIdFromHeader();
       if (!roleUtil.hasRoleOrHigherTest(userUUID, "ROLE_SUPPORT_STAFF")) {
           throw new UnauthorizedException("Only staff members can perform this action");
       }
   }
   ```

3. **Input Validation**: All incoming requests are validated before processing
4. **Security Headers**: Headers are propagated between services via Feign interceptors:
   ```java
   public void apply(RequestTemplate requestTemplate) {
       String authorizationHeader = RequestContextHolder.getRequestAttributes() != null ?
               ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                       .getRequest()
                       .getHeader(HttpHeaders.AUTHORIZATION) : null;
       if (authorizationHeader != null) {
           requestTemplate.header(HttpHeaders.AUTHORIZATION, authorizationHeader);
       }
   }
   ```

## Monitoring and Logging

1. **Comprehensive Logging**: SLF4J with contextual information:
   ```java
   log.info("Uploading attachment for user: {}", userId);
   log.error("Failed to upload attachment for user: {}", userId, e);
   ```

2. **Log Levels**:
    - **INFO**: Normal operations (ticket creation, updates)
    - **WARN**: Potential issues (ticket not found, validation warnings)
    - **ERROR**: Exceptions and processing failures
    - **DEBUG**: Detailed processing for troubleshooting

3. **Metrics Collection**: Service exposes metrics for:
    - Request volumes and response times
    - Cache hit/miss ratios
    - Database operation timings
    - Kafka message publishing success/failure rates

## Future Enhancements

1. **Advanced Search and Filtering**:
    - Elasticsearch integration for full-text search
    - Complex filtering by multiple criteria
    - Saved searches for frequent queries

2. **Enhanced Analytics**:
    - Ticket resolution time tracking
    - Staff performance metrics
    - Trend analysis for common issues

3. **Collaboration Features**:
    - Internal notes for staff
    - Multiple assignees
    - Knowledge base integration for common solutions

4. **Automated Workflows**:
    - Rule-based ticket routing
    - Automated responses for common issues
    - SLA monitoring and escalation

5. **Customer Portal Integration**:
    - Direct customer access to ticket status
    - Self-service options for common issues
    - Satisfaction surveys after ticket resolution

## Conclusion

The **Support Ticket Service** provides a robust, scalable solution for managing customer support operations within a microservices architecture. Its event-driven design, comprehensive caching strategy, and role-based security make it an efficient and secure platform for handling customer inquiries and issues. The service's integration with User Service and Image Service demonstrates effective microservice collaboration while maintaining clean separation of concerns.