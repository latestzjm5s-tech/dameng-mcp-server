# Runtime-only image (build jar locally first: ./mvnw package -DskipTests)
FROM amazoncorretto:17-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy pre-built jar from local target directory
COPY target/dameng-*.jar app.jar

RUN chown spring:spring app.jar
USER spring:spring

# Environment variables for database connection
# Override these when running the container
ENV DB_URL=jdbc:dm://localhost:5236/DAMENG \
    DB_USERNAME=SYSDBA \
    DB_PASSWORD=SYSDBA \
    SERVER_PORT=8080 \
    JAVA_OPTS="-Xms256m -Xmx512m -Djava.net.preferIPv4Stack=true"

# Expose MCP server port
EXPOSE 8080

# Health check via SSE endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -q --spider http://localhost:8080/sse || exit 1

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
