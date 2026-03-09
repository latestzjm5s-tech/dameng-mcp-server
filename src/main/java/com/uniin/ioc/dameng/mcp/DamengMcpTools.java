package com.uniin.ioc.dameng.mcp;

import com.uniin.ioc.dameng.service.DamengMutationService;
import com.uniin.ioc.dameng.service.DamengQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

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

    @Tool(description = "Execute any SQL statement on Dameng database. " +
            "Returns structured execution results, including result sets and update counts. " +
            "Use this tool for queries, DML, DDL, schema changes, stored procedure calls, and other SQL statements.")
    public Map<String, Object> executeQuery(
            @ToolParam(description = "SQL statement to execute. Examples: SELECT * FROM users, UPDATE users SET name = 'Jane' WHERE id = 1, CALL my_proc()")
            String sql,
            @ToolParam(description = "Schema name to use (optional). If not provided, uses the current/default schema.", required = false)
            String schema) {
        log.info("MCP Tool executeQuery called with sql: {}, schema: {}", sql, schema);
        return queryService.executeQuery(sql, schema);
    }

    @Tool(description = "Execute any SQL statement on Dameng database. " +
            "Returns structured execution results, including result sets and update counts. " +
            "This tool is kept for compatibility and now supports the same unrestricted SQL surface as executeQuery.")
    public Map<String, Object> executeMutation(
            @ToolParam(description = "SQL statement to execute. Examples: INSERT INTO users (name) VALUES ('John'), CREATE TABLE test (id INT), SELECT * FROM users, CALL my_proc()")
            String sql,
            @ToolParam(description = "Schema name to use (optional). If not provided, uses the current/default schema.", required = false)
            String schema) {
        log.info("MCP Tool executeMutation called with sql: {}, schema: {}", sql, schema);
        return mutationService.executeMutation(sql, schema);
    }
}
