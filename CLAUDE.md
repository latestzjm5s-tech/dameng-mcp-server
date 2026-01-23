# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot MCP (Model Context Protocol) Server application that provides integration with DM (达梦/Dameng) database. It uses Spring AI's MCP Server starter to expose database operations as MCP tools.

## Build and Development Commands

```bash
# Build the project
./mvnw clean package

# Run the application
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a specific test class
./mvnw test -Dtest=DamengApplicationTests

# Run a specific test method
./mvnw test -Dtest=DamengApplicationTests#contextLoads
```

## Architecture

- **Framework**: Spring Boot 3.5.x with Spring AI MCP Server
- **Database**: DM (达梦) Database using DmJdbcDriver18
- **Java Version**: 17
- **Build Tool**: Maven with wrapper (mvnw)

### Key Dependencies
- `spring-ai-starter-mcp-server` - MCP Server functionality
- `DmJdbcDriver18` - DM database JDBC driver
- Lombok for reducing boilerplate code

### Package Structure
- Base package: `com.uniin.ioc.dameng`
- Main class: `DamengApplication`

## MCP Server Reference

For MCP tool implementation, refer to: https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html