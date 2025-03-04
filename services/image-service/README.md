# Image Service

## Overview

The **Image Service** is a specialized microservice responsible for handling image storage and retrieval operations within a distributed system. It provides functionality for users to upload, download, and manage images while ensuring proper authentication and authorization. This service works with binary image data stored in PostgreSQL and exposes a RESTful API for client applications.

## Technologies Used

- **Spring Boot 3.4.2**: Framework for building Java-based microservices
- **Spring Cloud Config**: Centralized configuration management
- **Spring Cloud Netflix Eureka Client**: Service discovery and registration
- **Spring Boot Web**: Used for building RESTful APIs
- **Spring Data JPA**: ORM framework for database operations
- **PostgreSQL**: Database for storing image metadata and binary data
- **Lombok**: Reduces boilerplate code for Java classes
- **Docker**: Containerization for deployment

## Features

- Upload multiple images with proper user association
- Download images by their unique identifier
- Fetch image metadata
- User-based access control
- Support for various image formats
- Detailed logging for operational visibility

## Project Structure

```
image-service/
│── src/main/java/com/fayupable/test/
│   ├── controller/       # REST API controllers
│   │   ├── ImageController.java  # Image endpoints
│   ├── dto/              # Data transfer objects
│   │   ├── ImageDto.java # Image data transfer object
│   ├── entity/           # JPA entities
│   │   ├── Image.java    # Image entity
│   ├── exception/        # Custom exceptions
│   │   ├── ImageNotFoundException.java
│   │   ├── UnauthorizedAccessException.java
│   ├── handler/          # Exception handlers
│   │   ├── GlobalExceptionHandler.java
│   ├── mapper/           # Object mappers
│   │   ├── ImageMapper.java
│   ├── repository/       # Database repositories
│   │   ├── IImageRepository.java
│   ├── response/         # Response models
│   │   ├── ImageResponse.java
│   ├── service/          # Business logic
│   │   ├── IImageService.java
│   │   ├── ImageService.java
│── src/main/resources/
│   ├── application.yml   # Application configuration
│── pom.xml               # Dependencies and build configuration
```

## Installation & Setup

### Prerequisites

- Java 21 installed
- Maven installed
- PostgreSQL database set up

### Configuration

The service uses a centralized configuration from Spring Cloud Config Server. The main configuration file `image-service.yml` contains:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/image
    username: fayupable
    password: fayupable
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    show-sql: true

  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 5MB

server:
  port: 8110
```

### Running the Server

1. Clone the repository:
   ```sh
   git clone https://github.com/your-org/image-service.git
   cd image-service
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

### Image Management Endpoints

- **Upload images:**
  ```sh
  POST http://localhost:8110/images/add
  Headers: userId: 7f9c63b3-e615-4a9a-8c0e-b4e9d132979d
  Content-Type: multipart/form-data
  
  file: [binary files]
  ```

- **Get image metadata by ID:**
  ```sh
  GET http://localhost:8110/images/image/getImage/{id}
  ```

- **Download image by ID:**
  ```sh
  GET http://localhost:8110/images/image/download/{imageId}
  ```

## Data Model

### Image Entity

The primary entity for storing image data:

```java
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID imageId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_type")
    private String fileType;

    @Lob
    private Blob image;

    @Column(name = "download_url")
    private String downloadUrl;

    @Column(name = "image_created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "image_updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

## Implementation Details

### Image Upload Process

The service handles image uploads through a multi-step process:

1. **Request Validation**
    - Validate the user ID from the request header
    - Check file size and type constraints

2. **Image Processing**
    - Extract image metadata (filename, content type)
    - Convert MultipartFile to SQL Blob for storage
    - Generate unique identifiers for each image

3. **Database Storage**
    - Save image entity with metadata and binary data
    - Create download URL for later retrieval
    - Return DTO with image metadata to client

Example upload flow:

```java
@Transactional
public List<ImageDto> saveImages(List<MultipartFile> files, UUID userId) {
    validateUserId(userId);
    List<ImageDto> savedImages = new ArrayList<>();

    for (MultipartFile file : files) {
        Image savedImage = processAndSaveImage(file, userId);
        updateDownloadUrl(savedImage);
        savedImages.add(imageMapper.fromImage(savedImage));
    }

    return savedImages;
}
```

### Image Download Process

For image downloads, the service:

1. Locates the image by ID in the database
2. Extracts binary data from the Blob
3. Determines the content type
4. Returns the image as a downloadable resource

Example download implementation:

```java
@Transactional
public Resource downloadImage(UUID imageId) {
    Image image = imageRepository.findById(imageId)
            .orElseThrow(() -> new ImageNotFoundException("Image not found with ID: " + imageId));

    try {
        byte[] imageBytes = image.getImage().getBytes(1, (int) image.getImage().length());
        return new ByteArrayResource(imageBytes);
    } catch (SQLException e) {
        throw new RuntimeException("Failed to retrieve image", e);
    }
}
```

## Security Implementation

The service implements security through:

1. **User ID Validation**
    - Each request requires a valid user ID in the header
    - Unauthorized requests are rejected with 401 status

2. **Exception Handling**
    - Custom exceptions for different error scenarios
    - Global exception handler for consistent error responses

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<Object> handleUnauthorizedAccessException(UnauthorizedAccessException e) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(ImageNotFoundException.class)
    public ResponseEntity<Object> handleImageNotFoundException(ImageNotFoundException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
```

## Docker Support

Run the **Image Service** in a Docker container:

1. Create a `Dockerfile`:
   ```Dockerfile
   FROM openjdk:21-jdk
   WORKDIR /app
   COPY target/image-service-0.0.1-SNAPSHOT.jar app.jar
   EXPOSE 8110
   ENTRYPOINT ["java", "-jar", "app.jar"]
   ```

2. Build and run the container:
   ```sh
   docker build -t image-service .
   docker run -p 8110:8110 --name image-service --network microservices-network image-service
   ```

## Common Issues & Troubleshooting

### 1. Database Connection Issues

**Error:** `org.postgresql.util.PSQLException: Connection refused`
**Solution:** Ensure PostgreSQL is running and configured correctly.

```sh
# Check PostgreSQL service status
sudo service postgresql status
```

### 2. Large File Uploads

**Error:** `Maximum upload size exceeded`
**Solution:** Adjust the `max-file-size` and `max-request-size` in configuration.

### 3. Binary Data Handling

**Error:** `Failed to retrieve image`
**Solution:** Check that BLOB data is being stored and retrieved correctly.

### 4. Content Type Issues

**Error:** `Unknown media type`
**Solution:** Ensure file types are correctly identified and stored.

## Performance Considerations

- **Database Optimization**:
    - Consider indexing frequently queried fields
    - Use connection pooling for improved performance
    - Monitor query performance for large image retrieval

- **Memory Management**:
    - Be cautious with large image processing
    - Consider streaming for very large files
    - Configure JVM heap size appropriately

## Logging Strategy

The service uses SLF4J with comprehensive logging:

- **INFO level**: Normal operations, successful uploads/downloads
- **WARN level**: Potential issues like missing user IDs
- **ERROR level**: Exceptions and processing failures
- **DEBUG level**: Detailed processing information for troubleshooting

Example from the code:
```java
log.info("Saving {} images for user ID: {}", files.size(), userId);
log.warn("Image not found with ID: {}", id);
log.error("Error while processing image: {}", e.getMessage());
```

## Future Enhancements

1. **Improved Image Processing**:
    - Add image resizing and thumbnail generation
    - Support image cropping and filters
    - Generate optimized versions for different devices

2. **Integration with Cloud Storage**:
    - Amazon S3 integration for scalable storage
    - CDN integration for faster delivery
    - Backup and archiving strategies

3. **Advanced Security**:
    - Content validation and virus scanning
    - Watermarking for copyright protection
    - Access control based on image ownership

## Conclusion

The **Image Service** provides robust functionality for image management within a microservices architecture. Its clean separation of concerns, comprehensive exception handling, and efficient binary data processing make it a reliable component for applications that require image storage and retrieval capabilities. The service is designed to be scalable, maintainable, and secure, with thorough logging for operational visibility.