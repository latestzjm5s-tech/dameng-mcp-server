package com.uniin.ioc.dameng.mcp;

import com.uniin.ioc.dameng.service.DamengMutationService;
import com.uniin.ioc.dameng.service.DamengQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * MCP Tools for Dameng database operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DamengMcpTools {

    private final DamengQueryService queryService;
    private final DamengMutationService mutationService;

    @Tool(description = "Execute a read-only SQL SELECT query on Dameng database. " +
            "Returns query results as a list of row maps. Maximum 1000 rows returned. " +
            "Only SELECT statements are allowed; INSERT/UPDATE/DELETE/DROP operations are rejected. " +
            "Use this tool for all database operations: querying data, listing tables (SELECT TABLE_NAME FROM USER_TABLES), " +
            "describing table structure (SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH, NULLABLE FROM USER_TAB_COLUMNS WHERE TABLE_NAME = 'XXX'), " +
            "listing schemas (SELECT USERNAME FROM ALL_USERS), etc.")
    public List<Map<String, Object>> executeQuery(
            @ToolParam(description = "SQL SELECT query to execute. Example: SELECT * FROM users WHERE age > 18")
            String sql,
            @ToolParam(description = "Schema name to use (optional). If not provided, uses the current/default schema.", required = false)
            String schema) {
        log.info("MCP Tool executeQuery called with sql: {}, schema: {}", sql, schema);
        return queryService.executeQuery(sql, schema);
    }

    @Tool(description = "Execute a DML/DDL mutation on Dameng database. " +
            "Returns the number of affected rows. " +
            "Supports: INSERT/UPDATE/DELETE (DML) and CREATE/DROP/ALTER/TRUNCATE/COMMENT (DDL) statements.")
    public String executeMutation(
            @ToolParam(description = "SQL DML/DDL statement to execute. Examples: INSERT INTO users (name) VALUES ('John'), UPDATE users SET name = 'Jane' WHERE id = 1, DELETE FROM users WHERE id = 1, CREATE TABLE test (id INT), DROP TABLE test, COMMENT ON TABLE users IS 'User table', COMMENT ON COLUMN users.name IS 'User name'")
            String sql,
            @ToolParam(description = "Schema name to use (optional). If not provided, uses the current/default schema.", required = false)
            String schema) {
        log.info("MCP Tool executeMutation called with sql: {}, schema: {}", sql, schema);
        int affectedRows = mutationService.executeMutation(sql, schema);
        return String.format("Mutation executed successfully. %d row(s) affected.", affectedRows);
    }
}
