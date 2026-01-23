# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot MCP (Model Context Protocol) Server application that provides **read-only** database operations for DM (达梦/Dameng) database. It exposes database query tools via SSE (Server-Sent Events) transport for AI clients.

## Build and Development Commands

**Note**: Requires Java 17+

```bash
# Build the project
./mvnw clean package

# Build skipping tests
./mvnw clean package -DskipTests

# Run the application
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a specific test class
./mvnw test -Dtest=DamengApplicationTests
```

## Docker

```bash
# Build Docker image
docker build -t dameng-mcp-server .

# Run with environment variables
docker run -p 8080:8080 \
  -e DB_URL=jdbc:dm://host:5236/DAMENG \
  -e DB_USERNAME=SYSDBA \
  -e DB_PASSWORD=yourpassword \
  dameng-mcp-server
```

## Architecture

- **Framework**: Spring Boot 3.5.x with Spring AI MCP Server (WebMVC/SSE)
- **Database**: DM (达梦) Database using DmJdbcDriver18
- **Java Version**: 17
- **Transport**: SSE (HTTP) on port 8080

### Package Structure

```
com.uniin.ioc.dameng/
├── DamengApplication.java          # Main entry point
├── config/
│   └── DatabaseConfig.java         # JdbcTemplate configuration
├── service/
│   ├── DamengQueryService.java     # SQL query execution
│   └── DamengSchemaService.java    # Schema operations
├── mcp/
│   └── DamengMcpTools.java         # MCP tool definitions
├── validator/
│   └── SqlValidator.java           # Read-only SQL validation
└── exception/
    ├── InvalidSqlException.java
    └── QueryExecutionException.java
```

### MCP Tools

| Tool | Description |
|------|-------------|
| `executeQuery` | Execute read-only SELECT query (max 1000 rows) |
| `listTables` | List tables in schema |
| `describeTable` | Get table column structure |
| `listSchemas` | List all database schemas |

### Configuration

Database connection via environment variables:
- `DB_URL` - JDBC URL (default: `jdbc:dm://localhost:5236/DAMENG`)
- `DB_USERNAME` - Username (default: `SYSDBA`)
- `DB_PASSWORD` - Password (default: `SYSDBA`)

## Security

- Only SELECT queries allowed
- INSERT/UPDATE/DELETE/DROP operations rejected
- SQL comments blocked
- Stored procedures blocked
- Results limited to 1000 rows
