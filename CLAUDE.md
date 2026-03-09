# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Boot MCP (Model Context Protocol) Server for DM (达梦/Dameng) database. Exposes unrestricted SQL execution tools via SSE transport for AI clients.

## Build and Development Commands

**Requires Java 17+** (use SDKMAN to switch: `source ~/.sdkman/bin/sdkman-init.sh && sdk use java 17.0.17-amzn`)

```bash
# Build (skip tests)
./mvnw clean package -DskipTests

# Build with proxy
export https_proxy=http://127.0.0.1:7897 http_proxy=http://127.0.0.1:7897 all_proxy=socks5://127.0.0.1:7897 && \
./mvnw clean package -DskipTests

# Run
./mvnw spring-boot:run

# Test
./mvnw test
```

## Docker

```bash
# Build (with proxy for ARM64 Mac)
docker build \
  --build-arg http_proxy=http://host.docker.internal:7897 \
  --build-arg https_proxy=http://host.docker.internal:7897 \
  -t dameng-mcp-server .

# Run
docker run -d -p 8080:8080 \
  -e DB_URL=jdbc:dm://host.docker.internal:5236/DAMENG \
  -e DB_USERNAME=SYSDBA \
  -e DB_PASSWORD=yourpassword \
  --name dameng-mcp \
  dameng-mcp-server
```

## Architecture

- **Framework**: Spring Boot 3.5.x + Spring AI MCP Server (WebMVC/SSE)
- **Database**: DM (达梦) via DmJdbcDriver18, no connection pool (DriverManagerDataSource)
- **Java Version**: 17
- **Transport**: SSE (HTTP) on port 8080
- **Retry**: Spring Retry on connection failures (3 attempts, exponential backoff)

### Package Structure

```
com.uniin.ioc.dameng/
├── DamengApplication.java          # Main entry point
├── config/
│   └── DatabaseConfig.java         # DataSource & JdbcTemplate configuration
├── service/
│   ├── DamengQueryService.java     # Core SQL execution (uses ConnectionCallback)
│   └── DamengMutationService.java  # Delegates to QueryService for compatibility
├── mcp/
│   └── DamengMcpTools.java         # MCP tool definitions (2 tools)
├── validator/
│   └── SqlValidator.java           # SQL validation (non-empty check only)
└── exception/
    ├── InvalidSqlException.java
    └── QueryExecutionException.java
```

### MCP Tools

| Tool | Description |
|------|-------------|
| `executeQuery` | Execute any SQL statement, return structured results (result sets + update counts) |
| `executeMutation` | Compatibility alias, delegates to `executeQuery` internally |

Both tools accept `sql` (required) and `schema` (optional) parameters.

### Configuration

Database connection via environment variables:
- `DB_URL` — JDBC URL (default: `jdbc:dm://localhost:5236/DAMENG`)
- `DB_USERNAME` — Username (default: `SYSDBA`)
- `DB_PASSWORD` — Password (default: `SYSDBA`)

Connection timeouts are auto-appended: `connectTimeout=5000&socketTimeout=10000`

## Known Pitfalls

- **JdbcTemplate.execute() ambiguity**: When using a lambda with `jdbcTemplate.execute()`, must explicitly cast to `ConnectionCallback<T>` to avoid compile error between `ConnectionCallback` and `StatementCallback` overloads.
- **No connection pool**: Uses DriverManagerDataSource (new connection per request) to avoid HikariCP timeout issues with DM database.

## Security

- Any non-empty SQL statement is allowed — no statement type restrictions
- Results may include result sets, update counts, or both
