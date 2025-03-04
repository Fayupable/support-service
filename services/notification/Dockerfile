FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar notification.jar
EXPOSE 8040
ENTRYPOINT ["java", "-jar", "notification.jar"]