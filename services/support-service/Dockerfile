FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar support-service.jar
EXPOSE 8120
ENTRYPOINT ["java", "-jar", "support-service.jar"]