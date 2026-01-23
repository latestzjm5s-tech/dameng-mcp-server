package com.uniin.ioc.dameng.service;

import com.uniin.ioc.dameng.exception.QueryExecutionException;
import com.uniin.ioc.dameng.validator.SqlValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for executing read-only SQL queries on Dameng database
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DamengQueryService {

    private final JdbcTemplate jdbcTemplate;
    private final SqlValidator sqlValidator;

    private static final int MAX_ROWS = 1000;

    /**
     * Execute a read-only SQL SELECT query
     *
     * @param sql    SQL SELECT query to execute
     * @param schema schema name (optional, uses current schema if null)
     * @return list of row maps containing query results
     */
    public List<Map<String, Object>> executeQuery(String sql, String schema) {
        // Validate SQL is read-only
        sqlValidator.validateReadOnly(sql);

        try {
            // Set schema if provided
            if (schema != null && !schema.isBlank()) {
                jdbcTemplate.execute("SET SCHEMA " + schema.toUpperCase());
                log.info("Set schema to: {}", schema.toUpperCase());
            }

            // Execute query
            log.info("Executing query: {}", sql);
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

            // Limit results to prevent memory issues
            if (results.size() > MAX_ROWS) {
                log.warn("Query returned {} rows, limiting to {}", results.size(), MAX_ROWS);
                return results.subList(0, MAX_ROWS);
            }

            log.info("Query returned {} rows", results.size());
            return results;

        } catch (Exception e) {
            log.error("Query execution failed: {}", e.getMessage());
            throw new QueryExecutionException("Failed to execute query: " + e.getMessage(), e);
        }
    }
}
