# Multi-stage build for optimal image size
FROM maven:3.9-amazoncorretto-17 AS build
WORKDIR /workspace/app

# Copy pom.xml first for dependency caching
COPY pom.xml .

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src src
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM amazoncorretto:17-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy built artifact from build stage
COPY --from=build /workspace/app/target/dameng-*.jar app.jar

# Environment variables for database connection
# Override these when running the container
ENV DB_URL=jdbc:dm://localhost:5236/DAMENG \
    DB_USERNAME=SYSDBA \
    DB_PASSWORD=SYSDBA \
    SERVER_PORT=8080 \
    JAVA_OPTS="-Xms256m -Xmx512m -Djava.net.preferIPv4Stack=true"

# Expose MCP server port
EXPOSE 8080

# Health check using curl instead of wget
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
