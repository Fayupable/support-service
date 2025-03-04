FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar auth-service.jar
EXPOSE 8070
ENTRYPOINT ["java", "-jar", "auth-service.jar"]