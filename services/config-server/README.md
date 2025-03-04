
# Config Server

## Overview

The **Config Server** is a centralized configuration management service for microservices architecture. It enables dynamic and externalized configuration, allowing services to fetch their settings from a single source instead of embedding configurations within their own codebases. This setup helps in managing configurations efficiently, ensuring consistency across services and environments.

## Technologies Used

- **Spring Boot 3.4.2**: Framework for building Java applications.
- **Spring Cloud Config Server 2024.0.0**: Provides centralized external configuration management.
- **Java 21**: The programming language used.
- **Maven**: Build automation and dependency management.

## Features

- Centralized storage for configuration properties.
- Supports multiple environments (e.g., `dev`, `staging`, `prod`).
- Fetch configurations from Git-based repositories or local file storage.
- Refreshable configurations using Spring Cloud Bus.
- Secure access to configurations.

## How It Works

1. The **Config Server** fetches configuration files from a repository (Git or local storage).
2. Microservices request their configurations from the **Config Server**.
3. The server provides the configuration files dynamically based on the requesting service and environment.
4. Configurations can be refreshed without restarting services.

## Project Structure

```
config-server/
│── src/main/java/com/fayupable/configserver/
│   ├── ConfigServerApplication.java  # Main class
│── src/main/resources/
│   ├── application.yml  # Server configuration file
│── pom.xml  # Dependencies and build configuration
```

## Installation & Setup

### Prerequisites

- Java 21 installed
- Maven installed
- A Git repository (if using Git-based configuration storage)

### Configuration

Modify `application.yml` to set up the **Config Server**.

```yaml
server:
  port: 8888

spring:
  application:
    name: config-server
  cloud:
    config:
      server:
        git:
          uri: https://github.com/your-org/config-repo.git
          default-label: main
          search-paths: "configs"
```

Eureka Registry Configuration:

```yaml
eureka:
  instance:
    hostname: localhost
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
spring:
  cloud:
    config:
      override.system-properties: false
```

### Running the Server

1. Clone the repository:
   ```sh
   git clone https://github.com/your-org/config-server.git
   cd config-server
   ```
2. Build the project:
   ```sh
   mvn clean package
   ```
3. Run the application:
   ```sh
   mvn spring-boot:run
   ```

### Testing the Configuration Service

To test if the **Config Server** is running, execute:

```sh
curl -X GET http://localhost:8888/application/default
```

Expected response (example):

```json
{
  "name": "application",
  "profiles": [
    "default"
  ],
  "propertySources": [
    {
      "name": "https://github.com/your-org/config-repo.git/application.yml",
      "source": {
        "key": "value"
      }
    }
  ]
}
```

## Integrating with Microservices

Each microservice should have a `bootstrap.yml` or `application.yml` to connect with the **Config Server**:

```yaml
spring:
  application:
    name: user-service
  config:
    import: optional:configserver:http://localhost:8888/
```

This will allow the microservice to fetch its configuration from `http://localhost:8888/user-service/default`.

## Common Issues & Troubleshooting

### 1. Config Server Not Starting

**Error:** `Failed to determine a suitable driver class`
**Solution:** Ensure `spring.datasource.url` is correctly configured or remove it if not needed.

### 2. Connection Refused Error

**Error:** `I/O error on GET request for "http://localhost:8888": Connection refused`
**Solution:** Ensure the **Config Server** is running and accessible at `localhost:8888`.

### 3. Configuration Not Found

**Error:** `Could not locate PropertySource`
**Solution:** Ensure the Git repository contains the necessary configuration files.

## Conclusion

The **Config Server** is an essential part of a microservices architecture, providing a unified and flexible configuration management solution. By centralizing configurations, it reduces duplication, improves maintainability, and enhances scalability.