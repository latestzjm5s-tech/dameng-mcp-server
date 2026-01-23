package com.uniin.ioc.dameng.mcp;

import com.uniin.ioc.dameng.service.DamengQueryService;
import com.uniin.ioc.dameng.service.DamengSchemaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * MCP Tools for Dameng database read-only operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DamengMcpTools {

    private final DamengQueryService queryService;
    private final DamengSchemaService schemaService;

    @Tool(description = "Execute a read-only SQL SELECT query on Dameng database. " +
            "Returns query results as a list of row maps. Maximum 1000 rows returned. " +
            "Only SELECT statements are allowed; INSERT/UPDATE/DELETE/DROP operations are rejected.")
    public List<Map<String, Object>> executeQuery(
            @ToolParam(description = "SQL SELECT query to execute. Example: SELECT * FROM users WHERE age > 18")
            String sql,
            @ToolParam(description = "Schema name to use (optional). If not provided, uses the current/default schema.", required = false)
            String schema) {
        log.info("MCP Tool executeQuery called with sql: {}, schema: {}", sql, schema);
        return queryService.executeQuery(sql, schema);
    }

    @Tool(description = "List all tables in the specified schema or current schema. " +
            "Returns a list of table names sorted alphabetically.")
    public List<String> listTables(
            @ToolParam(description = "Schema name (optional). If not provided, lists tables from the current/default schema.", required = false)
            String schema) {
        log.info("MCP Tool listTables called with schema: {}", schema);
        return schemaService.listTables(schema);
    }

    @Tool(description = "Describe the structure of a specific table. " +
            "Returns column information including name, data type, length, precision, scale, nullable, and default value.")
    public List<Map<String, Object>> describeTable(
            @ToolParam(description = "Name of the table to describe. Example: USERS")
            String tableName,
            @ToolParam(description = "Schema name (optional). If not provided, searches in the current/default schema.", required = false)
            String schema) {
        log.info("MCP Tool describeTable called with tableName: {}, schema: {}", tableName, schema);
        return schemaService.describeTable(tableName, schema);
    }

    @Tool(description = "List all available schemas (users) in the Dameng database. " +
            "Returns a list of schema names sorted alphabetically.")
    public List<String> listSchemas() {
        log.info("MCP Tool listSchemas called");
        return schemaService.listSchemas();
    }
}
