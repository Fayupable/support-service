FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar user-service.jar
EXPOSE 8100
ENTRYPOINT ["java", "-jar", "user-service.jar"]