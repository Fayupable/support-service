FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar image-service.jar
EXPOSE 8110
ENTRYPOINT ["java", "-jar", "image-service.jar"]