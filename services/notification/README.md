# Mail Sender Service

## Overview

The **Mail Sender Service** is a specialized microservice responsible for handling all email communications within a distributed system. It processes events from other services via Kafka and sends templated emails to users. This service centralizes all notification logic, ensuring consistent communication with users for events like account verification, support ticket creation, and support ticket resolution.

## Technologies Used

- **Spring Boot 3.4.2**: Framework for building Java-based microservices
- **Spring Cloud Config**: Centralized configuration management
- **Spring Cloud Netflix Eureka Client**: Service discovery and registration
- **Spring Kafka**: Integration with Apache Kafka for event-driven architecture
- **Spring Mail**: Email sending capabilities with HTML templates
- **Thymeleaf**: Template engine for HTML email templates
- **Spring Data JPA**: ORM framework for database operations
- **PostgreSQL**: Database for storing notification records
- **Lombok**: Reduces boilerplate code for Java classes
- **Docker**: Containerization for deployment

## Features

- Kafka-based event consumption for multiple event types
- HTML email templates using Thymeleaf
- Asynchronous email sending for improved performance
- Comprehensive logging for email delivery tracking
- Notification record storage in database
- Support for various notification types:
    - User verification emails with verification codes
    - Support ticket confirmation emails
    - Support ticket resolution notification emails

## Project Structure

```
mail-sender-service/
│── src/main/java/com/fayupable/test/
│   ├── config/                # Configuration classes
│   │   ├── KafkaConsumerConfig.java  # Kafka consumer configuration
│   │   ├── MailConfig.java    # Email configuration
│   ├── entity/                # JPA entities
│   │   ├── Notification.java  # Notification entity
│   ├── enums/                 # Enumerations
│   │   ├── EmailTemplate.java # Email template types
│   │   ├── NotificationType.java # Notification types
│   ├── kafka/                 # Kafka related classes
│   │   ├── NotificationConsumer.java # Kafka event consumer
│   │   ├── support/           # Support ticket DTOs
│   │   │   ├── SupportStatus.java
│   │   │   ├── SupportTicketConfirmation.java
│   │   │   ├── SupportTicketResolutionConfirmation.java
│   │   ├── user/              # User DTOs
│   │   │   ├── UserConfirmation.java
│   ├── repository/            # Database repositories
│   │   ├── INotificationRepository.java
│   ├── service/               # Business logic
│   │   ├── EmailService.java  # Email sending implementation
│   │   ├── IEmailService.java # Email service interface
│── src/main/resources/
│   ├── application.yml        # Application configuration
│   ├── templates/             # Email templates
│   │   ├── UserVerification.html
│   │   ├── SupportTicketConfirmation.html
│   │   ├── SupportTicketResolutionConfirmation.html
│── pom.xml                    # Dependencies and build configuration
```

## Installation & Setup

### Prerequisites

- Java 21 installed
- Maven installed
- PostgreSQL database set up
- Kafka broker running
- SMTP server running (or MailHog for local development)

### Configuration

Configure `application.yml` for the **Mail Sender Service**:

```yaml
spring:
  application:
    name: mail-sender-service
  config:
    import: optional:configserver:http://localhost:8888
  datasource:
    url: jdbc:postgresql://localhost:5432/mailsender
    username: fayupable
    password: fayupable
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    show-sql: true
  kafka:
    consumer:
      bootstrap-servers: localhost:9092
      group-id: mail-sender-group
      auto-offset-reset: earliest
  mail:
    host: localhost
    port: 1025
    username: fayupable@gmail.com
    password: fayupablepassword
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
    test-connection: true

server:
  port: 8130

logging:
  level:
    com.fayupable.test: INFO
    org.springframework.kafka: INFO
    org.springframework.mail: DEBUG
```

### Running the Server

1. Clone the repository:
   ```sh
   git clone https://github.com/your-org/mail-sender-service.git
   cd mail-sender-service
   ```
2. Build the project:
   ```sh
   mvn clean package
   ```
3. Run the application:
   ```sh
   mvn spring-boot:run
   ```

## Kafka Integration

### Kafka Consumer Configuration

The service consumes events from three different Kafka topics, each requiring a specific deserializer configuration:

```java
@Configuration
public class KafkaConsumerConfig {
    private <T> ConsumerFactory<String, T> createConsumerFactory(Class<T> clazz) {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, clazz.getName());

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(),
                new JsonDeserializer<>(clazz));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserConfirmation> userKafkaListenerContainerFactory() {
        return createFactory(UserConfirmation.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SupportTicketConfirmation> supportKafkaListenerContainerFactory() {
        return createFactory(SupportTicketConfirmation.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SupportTicketResolutionConfirmation> supportTicketResolutionKafkaListenerContainerFactory() {
        return createFactory(SupportTicketResolutionConfirmation.class);
    }
}
```

### Event Consumption

The service consumes three types of events:

1. **User Verification Events**:
   ```java
   @KafkaListener(
       topics = "user-confirmation-topic",
       groupId = "user-group",
       containerFactory = "userKafkaListenerContainerFactory"
   )
   public void consumeUserConfirmation(UserConfirmation userConfirmation) {
       log.info("Consuming user confirmation message: {}", userConfirmation);
       saveUserConfirmation(userConfirmation);
       try {
           sendUserConfirmationEmail(userConfirmation);
       } catch (MessagingException e) {
           log.error("MessagingException occurred while sending user confirmation email", e);
           throw new RuntimeException("Failed to send user confirmation email", e);
       }
   }
   ```

2. **Support Ticket Confirmation Events**:
   ```java
   @KafkaListener(
       topics = "support-ticket-confirmation-topic",
       groupId = "support-group",
       containerFactory = "supportKafkaListenerContainerFactory"
   )
   public void consumeSupportTicketConfirmation(SupportTicketConfirmation supportTicketConfirmation) {
       log.info("Consuming support ticket confirmation message: {}", supportTicketConfirmation);
       saveSupportTicketConfirmation(supportTicketConfirmation);
       try {
           sendSupportTicketConfirmationEmail(supportTicketConfirmation);
       } catch (MessagingException e) {
           log.error("MessagingException occurred while sending support ticket confirmation email", e);
           throw new RuntimeException("Failed to send support ticket confirmation email", e);
       }
   }
   ```

3. **Support Ticket Resolution Events**:
   ```java
   @KafkaListener(
       topics = "support-ticket-resolution-topic",
       groupId = "support-group",
       containerFactory = "supportTicketResolutionKafkaListenerContainerFactory"
   )
   public void consumeSupportTicketResolutionConfirmation(SupportTicketResolutionConfirmation supportTicketResolutionConfirmation) {
       log.info("Consuming support ticket resolution confirmation message: {}", supportTicketResolutionConfirmation);
       saveSupportTicketResolutionConfirmation(supportTicketResolutionConfirmation);
       try {
           sendSupportTicketResolutionConfirmationEmail(supportTicketResolutionConfirmation);
       } catch (MessagingException e) {
           log.error("MessagingException occurred while sending support ticket resolution confirmation email", e);
           throw new RuntimeException("Failed to send support ticket resolution confirmation email", e);
       }
   }
   ```

## Email Service Implementation

### Mail Configuration

The service uses Spring's JavaMailSender with the following configuration:

```java
@Configuration
public class MailConfig {
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("localhost");
        mailSender.setPort(1025);
        String emailUserName = "fayupable@gmail.com";
        mailSender.setUsername(emailUserName);
        String emailPassword = "fayupablepassword";
        mailSender.setPassword(emailPassword);

        Properties properties = mailSender.getJavaMailProperties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.debug", "true");

        return mailSender;
    }
}
```

### Email Templates

The service uses Thymeleaf templates for emails:

1. **User Verification Template** (`UserVerification.html`):
    - Contains verification code
    - Displays expiration time
    - Includes instructions for using the code

2. **Support Ticket Confirmation Template** (`SupportTicketConfirmation.html`):
    - Shows ticket ID
    - Displays current ticket status
    - Includes ticket priority

3. **Support Ticket Resolution Template** (`SupportTicketResolutionConfirmation.html`):
    - Shows ticket ID
    - Includes resolution message
    - Displays resolution time
    - Shows final ticket status

### Email Sending

The service implements asynchronous email sending:

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService implements IEmailService {
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Override
    @Async
    public void sendUserVerificationCode(String destinationEmail, String verificationCode, LocalDateTime verificationExpiredDate) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper;

        try {
            messageHelper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_RELATED, StandardCharsets.UTF_8.name());
            messageHelper.setFrom("fayupable@gmail.com");
            messageHelper.setTo(destinationEmail);
            messageHelper.setSubject(USER_VERIFICATION.getSubject());

            final String templateName = USER_VERIFICATION.getTemplate();
            Map<String, Object> variables = new HashMap<>();
            variables.put("verificationCode", verificationCode);
            variables.put("verificationCodeExpiration", verificationExpiredDate);

            Context context = new Context();
            context.setVariables(variables);

            String htmlTemplate = templateEngine.process(templateName, context);
            messageHelper.setText(htmlTemplate, true);

            javaMailSender.send(mimeMessage);
            log.info("Email sent to {} with template {}", destinationEmail, templateName);

        } catch (MessagingException | MailException e) {
            log.error("Error sending email to {}", destinationEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    // Other email sending methods...
}
```

## Data Models

### Email Templates

The service uses an enumeration to manage email templates:

```java
@Getter
public enum EmailTemplate {
    USER_VERIFICATION("UserVerification.html", "User Verification Successful"),
    SUPPORT_TICKET_CONFIRMATION("SupportTicketConfirmation.html", "Support Ticket Confirmation"),
    SUPPORT_TICKET_RESOLUTION_CONFIRMATION("SupportTicketResolutionConfirmation.html", "Support Ticket Resolution Confirmation");
    
    private final String template;
    private final String subject;

    EmailTemplate(String template, String subject) {
        this.template = template;
        this.subject = subject;
    }
}
```

### Notification Types

```java
public enum NotificationType {
    USER_VERIFICATION,
    SUPPORT_CONFIRMATION,
    SUPPORT_RESOLUTION_CONFIRMATION
}
```

### Notification Entity

The service persists records of sent notifications:

```java
@Entity
@Table(name = "notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "notification_id")
    private UUID notificationId;
    private NotificationType notificationType;
    private LocalDateTime sendAt;
}
```

## Docker Support

Run the **Mail Sender Service** in a Docker container:

1. Create a `Dockerfile`:
   ```Dockerfile
   FROM openjdk:21-jdk
   WORKDIR /app
   COPY target/mail-sender-service-0.0.1-SNAPSHOT.jar app.jar
   EXPOSE 8040
   ENTRYPOINT ["java", "-jar", "app.jar"]
   ```

2. Build and run the container:
   ```sh
   docker build -t mail-sender-service .
   docker run -p 8130:8130 --name mail-sender-service --network microservices-network mail-sender-service
   ```

3. Run with Docker Compose including dependencies:

```yaml
spring:
  config:
    import: optional:configserver:http://localhost:8888
  application:
    name: notification-service
```
   ```yaml
   server:
   port: 8040
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/notification
       username: fayupable
       password: fayupable
       driver-class-name: org.postgresql.Driver

     jpa:
       hibernate:
         ddl-auto: update
       database-platform: org.hibernate.dialect.PostgreSQLDialect
       show-sql: true

    
     mail:
       host: localhost
       port: 1025
       username: fayupable
       password: fayupable
       properties:
         mail:
           smtp:
             trust: "*"
           auth: true
           starttls:
             enabled: true
           connectiontimeout: 5000
           timeout: 3000
           writetimeout: 5000

   ```
## Common Issues & Troubleshooting

### 1. Kafka Connection Issues

**Error:** `org.apache.kafka.common.errors.TimeoutException: Failed to connect to bootstrap servers within timeout`
**Solution:** Ensure Kafka broker is running and accessible.

```sh
# Check Kafka service status
sudo systemctl status kafka
[...]
# Start Kafka if not running
sudo systemctl start zookeeper
sudo systemctl start kafka

# Verify topics exist
kafka-topics.sh --bootstrap-server localhost:9092 --list
```

### 2. Email Sending Failures

**Error:** `org.springframework.mail.MailSendException: Mail server connection failed`
**Solution:** Verify SMTP server configuration or use MailHog for local development.

```sh
# Check if MailHog is running
docker ps | grep mailhog

# Start MailHog if not running
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog

# Access MailHog UI to view sent emails
open http://localhost:8025
```

### 3. Template Rendering Issues

**Error:** `org.thymeleaf.exceptions.TemplateInputException: Error resolving template`
**Solution:** Ensure template files exist in the correct location and with proper naming.

```sh
# Verify template files exist in resources/templates directory
ls -la src/main/resources/templates/*.html
```

### 4. Database Connection Problems

**Error:** `org.postgresql.util.PSQLException: Connection refused`
**Solution:** Ensure PostgreSQL is running and configured correctly.

```sh
# Check PostgreSQL service status
sudo service postgresql status
[...]
# Start PostgreSQL if not running
sudo service postgresql start

# Verify database exists
psql -U postgres -c "SELECT datname FROM pg_database WHERE datname = 'mailsender';"
```

### 5. Serialization/Deserialization Issues

**Error:** `org.springframework.kafka.support.converter.ConversionException: Failed to deserialize payload`
**Solution:** Ensure the message format matches the expected class structure.

```sh
# Check consumer factory configuration
# Ensure JsonDeserializer is properly configured with trusted packages
```

## Performance Considerations

- **Asynchronous Email Sending**: All email sending methods are annotated with `@Async` to prevent blocking the main Kafka consumer thread
- **Connection Pooling**: Database connection pooling is configured for optimal performance
- **Retry Mechanism**: Failed email attempts can be retried with exponential backoff
- **Batch Processing**: Messages are processed in batches for improved throughput

## Scaling Considerations

The Mail Sender Service can be horizontally scaled by:

1. **Adding more instances**: Multiple instances can consume from the same Kafka topics
2. **Configuring consumer groups**: Ensures messages are distributed among consumers
3. **Setting appropriate partition counts**: Allows parallel processing of messages

## Monitoring and Logging

The service includes comprehensive logging:

```java
// Example of detailed logging in NotificationConsumer
log.info("Consuming user confirmation message: {}", userConfirmation);
log.info("Saving user confirmation notification for user {}", userConfirmation.getEmail());
log.info("User confirmation notification saved for user {}", userConfirmation.getEmail());
log.info("Sending user confirmation email to user {}", userConfirmation.getEmail());
log.info("User confirmation email sent to user {}", userConfirmation.getEmail());
```

For production environments, consider integrating with:
- **Prometheus**: For metrics collection
- **Grafana**: For visualization and alerting
- **ELK Stack**: For centralized logging

## Security Considerations

- **Transport Layer Security**: Use TLS for SMTP connections
- **Sensitive Data Handling**: Avoid logging sensitive user information
- **Environment Variables**: Store credentials in environment variables rather than configuration files
- **Input Validation**: Validate email addresses and other user inputs
- **Rate Limiting**: Implement rate limiting to prevent abuse

## Future Enhancements

1. **Enhanced Email Templates**:
   - Responsive design for mobile viewing
   - Localization support for multiple languages
   - Personalized content based on user preferences

2. **Notification Preferences**:
   - Allow users to opt-out of certain notification types
   - Support for notification frequency controls

3. **Delivery Tracking**:
   - Track email opens and link clicks
   - Implement delivery status monitoring

4. **Additional Notification Channels**:
   - SMS notifications through integration with SMS providers
   - Push notifications for mobile applications
   - In-app notifications for web applications

## Conclusion

The **Mail Sender Service** provides a robust, scalable solution for handling all email communications within a microservices architecture. By centralizing email templates and notification logic, it ensures consistent user communication across the entire system. Its event-driven design allows for loose coupling with other services while maintaining high reliability and performance for critical user notifications.