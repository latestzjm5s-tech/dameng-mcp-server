package com.uniin.ioc.dameng.service;

import com.uniin.ioc.dameng.exception.QueryExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for database schema operations (list tables, describe table, list schemas)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DamengSchemaService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * List all tables in the specified schema or current schema
     *
     * @param schema schema name (optional, uses current schema if null)
     * @return list of table names
     */
    public List<String> listTables(String schema) {
        try {
            String sql;
            List<String> tables;

            if (schema != null && !schema.isBlank()) {
                sql = "SELECT TABLE_NAME FROM ALL_TABLES WHERE OWNER = ? ORDER BY TABLE_NAME";
                tables = jdbcTemplate.queryForList(sql, String.class, schema.toUpperCase());
                log.info("Listed {} tables from schema: {}", tables.size(), schema);
            } else {
                sql = "SELECT TABLE_NAME FROM USER_TABLES ORDER BY TABLE_NAME";
                tables = jdbcTemplate.queryForList(sql, String.class);
                log.info("Listed {} tables from current schema", tables.size());
            }

            return tables;

        } catch (Exception e) {
            log.error("Failed to list tables: {}", e.getMessage());
            throw new QueryExecutionException("Failed to list tables: " + e.getMessage(), e);
        }
    }

    /**
     * Describe the structure of a specific table
     *
     * @param tableName table name to describe
     * @param schema    schema name (optional)
     * @return list of column information maps
     */
    public List<Map<String, Object>> describeTable(String tableName, String schema) {
        try {
            String sql;
            List<Map<String, Object>> columns;

            if (schema != null && !schema.isBlank()) {
                sql = "SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH, DATA_PRECISION, DATA_SCALE, NULLABLE, DATA_DEFAULT " +
                      "FROM ALL_TAB_COLUMNS WHERE TABLE_NAME = ? AND OWNER = ? ORDER BY COLUMN_ID";
                columns = jdbcTemplate.queryForList(sql, tableName.toUpperCase(), schema.toUpperCase());
            } else {
                sql = "SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH, DATA_PRECISION, DATA_SCALE, NULLABLE, DATA_DEFAULT " +
                      "FROM USER_TAB_COLUMNS WHERE TABLE_NAME = ? ORDER BY COLUMN_ID";
                columns = jdbcTemplate.queryForList(sql, tableName.toUpperCase());
            }

            log.info("Described table {} with {} columns", tableName, columns.size());
            return columns;

        } catch (Exception e) {
            log.error("Failed to describe table {}: {}", tableName, e.getMessage());
            throw new QueryExecutionException("Failed to describe table: " + e.getMessage(), e);
        }
    }

    /**
     * List all available schemas in the database
     *
     * @return list of schema names
     */
    public List<String> listSchemas() {
        try {
            String sql = "SELECT USERNAME FROM ALL_USERS ORDER BY USERNAME";
            List<String> schemas = jdbcTemplate.queryForList(sql, String.class);
            log.info("Listed {} schemas", schemas.size());
            return schemas;

        } catch (Exception e) {
            log.error("Failed to list schemas: {}", e.getMessage());
            throw new QueryExecutionException("Failed to list schemas: " + e.getMessage(), e);
        }
    }
}
