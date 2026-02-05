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

import java.sql.SQLException;

/**
 * Service for executing DML mutations (INSERT/UPDATE/DELETE) on Dameng database
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DamengMutationService {

    private final JdbcTemplate jdbcTemplate;
    private final SqlValidator sqlValidator;

    /**
     * Execute a DML mutation (INSERT/UPDATE/DELETE)
     *
     * @param sql    SQL DML statement to execute
     * @param schema schema name (optional, uses current schema if null)
     * @return number of affected rows
     */
    @Retryable(
            retryFor = {
                    DataAccessResourceFailureException.class,
                    SQLException.class
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public int executeMutation(String sql, String schema) {
        log.debug("Attempting to execute mutation (with retry support)");

        // Validate SQL is a valid DML statement
        sqlValidator.validateMutation(sql);

        try {
            // Set schema if provided
            if (schema != null && !schema.isBlank()) {
                jdbcTemplate.execute("SET SCHEMA " + schema.toUpperCase());
                log.info("Set schema to: {}", schema.toUpperCase());
            }

            // Execute mutation
            log.info("Executing mutation: {}", sql);
            int affectedRows = jdbcTemplate.update(sql);

            log.info("Mutation completed, {} rows affected", affectedRows);
            return affectedRows;

        } catch (DataAccessResourceFailureException e) {
            log.warn("Database connection failure, will retry: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            if (e.getCause() instanceof SQLException) {
                log.warn("SQL connection error, will retry: {}", e.getMessage());
                throw e;
            }
            log.error("Mutation execution failed: {}", e.getMessage());
            throw new QueryExecutionException("Failed to execute mutation: " + e.getMessage(), e);
        }
    }
}
