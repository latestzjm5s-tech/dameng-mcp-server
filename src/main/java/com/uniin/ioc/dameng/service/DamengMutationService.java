package com.uniin.ioc.dameng.service;

import com.uniin.ioc.dameng.exception.QueryExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Map;

/**
 * Compatibility service for executing arbitrary SQL through the mutation tool.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DamengMutationService {

    private final DamengQueryService queryService;

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
    public Map<String, Object> executeMutation(String sql, String schema) {
        log.debug("Attempting to execute SQL through mutation service");
        try {
            return queryService.executeQuery(sql, schema);
        } catch (DataAccessResourceFailureException e) {
            log.warn("Database connection failure, will retry: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            if (e.getCause() instanceof SQLException) {
                log.warn("SQL connection error, will retry: {}", e.getMessage());
                throw e;
            }
            log.error("Mutation execution failed: {}", e.getMessage());
            throw new QueryExecutionException("Failed to execute mutation SQL: " + e.getMessage(), e);
        }
    }
}
