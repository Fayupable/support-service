# Discovery Service

## Overview
The **Discovery Service** is a critical component of the Support Ticket System's microservices architecture. It provides service registration and discovery capabilities using Netflix Eureka, enabling dynamic service communication and load balancing.

## Technologies Used
- **Spring Boot 3.4.2**
- **Spring Cloud Netflix Eureka 2024.0.0**
- **Java 21**
- **Maven**

## Features
- Service registration and discovery
- Health monitoring for microservices
- Load balancing
- Dynamic service scaling
- Integration with Config Server

## Project Structure
```
discovery-service/
│── src/main/java/com/fayupable/discoveryservice/
│   ├── DiscoveryServiceApplication.java
│── src/main/resources/
│   ├── application.yml
│   ├── discoveryservice.yml
│── pom.xml
```

## Setup & Configuration

### Prerequisites
- Java 21
- Maven
- Config Server running on port 8888

### Configuration

Main configuration in `application.yml`:
```yaml
spring:
  config:
    import: optional:configserver:http://localhost:8888
  application:
    name: discovery-service
```

Service configuration in `discoveryservice.yml`:
```yaml
eureka:
  instance:
    hostname: localhost
  client:
    registerWithEureka: false
    fetchRegistry: false
    serviceUrl:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
server:
  port: 8761
```

### Running the Service
```sh
mvn clean package
mvn spring-boot:run
```

## Service Integration

To register a microservice with Eureka, add to the service's `application.yml`:
```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
  instance:
    preferIpAddress: true
```

## Testing

Verify server status:
```sh
curl -X GET http://localhost:8761/eureka/apps
```

Access Eureka Dashboard:
```
http://localhost:8761
```

## Troubleshooting

### Common Issues

1. **Port Binding Failure**
    - Error: `Failed to bind to port 8761`
    - Solution: Check if port is already in use

2. **Service Registration Issues**
    - Error: `Service not appearing in Eureka dashboard`
    - Solution: Verify service's Eureka client configuration

3. **Connection Problems**
    - Error: `Connection refused to localhost:8761`
    - Solution: Ensure Discovery Service is running

## Conclusion
The Discovery Service is essential for the Support Ticket System's microservices architecture, enabling dynamic service discovery and load balancing across all components.


