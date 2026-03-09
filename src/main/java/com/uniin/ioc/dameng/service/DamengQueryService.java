package com.uniin.ioc.dameng.service;

import com.uniin.ioc.dameng.exception.QueryExecutionException;
import com.uniin.ioc.dameng.validator.SqlValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for executing arbitrary SQL statements on Dameng database.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DamengQueryService {

    private final JdbcTemplate jdbcTemplate;
    private final SqlValidator sqlValidator;

    /**
     * Execute an arbitrary SQL statement.
     *
     * @param sql    SQL statement to execute
     * @param schema schema name (optional, uses current schema if null)
     * @return execution result containing result sets and update counts
     */
    @Retryable(
            retryFor = {
                    DataAccessResourceFailureException.class,
                    SQLException.class
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public Map<String, Object> executeQuery(String sql, String schema) {
        log.debug("Attempting to execute SQL (with retry support)");

        sqlValidator.validateSql(sql);

        try {
            log.info("Executing SQL: {}", sql);
            return jdbcTemplate.execute((org.springframework.jdbc.core.ConnectionCallback<Map<String, Object>>) connection -> {
                if (connection == null) {
                    throw new QueryExecutionException("Failed to obtain database connection", null);
                }

                if (schema != null && !schema.isBlank()) {
                    try (Statement schemaStatement = connection.createStatement()) {
                        schemaStatement.execute("SET SCHEMA " + schema.toUpperCase());
                        log.info("Set schema to: {}", schema.toUpperCase());
                    }
                }

                try (Statement statement = connection.createStatement()) {
                    List<Map<String, Object>> executionResults = new ArrayList<>();
                    boolean hasResultSet = statement.execute(sql);
                    int resultIndex = 0;

                    while (true) {
                        if (hasResultSet) {
                            try (ResultSet resultSet = statement.getResultSet()) {
                                List<Map<String, Object>> rows = extractRows(resultSet);
                                Map<String, Object> result = new LinkedHashMap<>();
                                result.put("index", resultIndex++);
                                result.put("type", "resultSet");
                                result.put("rowCount", rows.size());
                                result.put("rows", rows);
                                executionResults.add(result);
                            }
                        } else {
                            int affectedRows = statement.getUpdateCount();
                            if (affectedRows == -1) {
                                break;
                            }

                            Map<String, Object> result = new LinkedHashMap<>();
                            result.put("index", resultIndex++);
                            result.put("type", "updateCount");
                            result.put("affectedRows", affectedRows);
                            executionResults.add(result);
                        }

                        hasResultSet = statement.getMoreResults();
                    }

                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("sql", sql);
                    response.put("schema", schema == null || schema.isBlank() ? null : schema.toUpperCase());
                    response.put("resultCount", executionResults.size());
                    response.put("results", executionResults);
                    return response;
                }
            });

        } catch (DataAccessResourceFailureException e) {
            log.warn("Database connection failure, will retry: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            if (e.getCause() instanceof SQLException) {
                log.warn("SQL connection error, will retry: {}", e.getMessage());
                throw e;
            }
            log.error("SQL execution failed: {}", e.getMessage());
            throw new QueryExecutionException("Failed to execute SQL: " + e.getMessage(), e);
        }
    }

    private List<Map<String, Object>> extractRows(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (resultSet.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnLabel(i), resultSet.getObject(i));
            }
            rows.add(row);
        }

        return rows;
    }
}
