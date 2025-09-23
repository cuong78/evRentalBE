# Build stage
FROM maven:3.9.8-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom.xml first to leverage Docker layer caching
COPY pom.xml .
RUN mvn dependency:resolve

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -Dmaven.test.skip=true

#Stage 2: create image

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

ENV TZ=Asia/Ho_Chi_Minh

# Set working folder to App and copy complied file from above step

COPY --from=build /app/target/*.jar app.jar

# Note: SPRING_PROFILES_ACTIVE will be set by docker-compose or deployment environment
# For production deployment: ENV SPRING_PROFILES_ACTIVE=prod
# For local development: will be overridden by docker-compose

# Command to run the application
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-Xms32m", \
    "-Xmx192m", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseSerialGC", \
    "-Xss256k", \
    "-XX:MetaspaceSize=128m", \
    "-XX:MaxMetaspaceSize=128m", \
    "-XX:+ShrinkHeapInSteps", \
    "-XX:MinHeapFreeRatio=10", \
    "-XX:MaxHeapFreeRatio=20", \
    "-jar", \
    "app.jar"]
