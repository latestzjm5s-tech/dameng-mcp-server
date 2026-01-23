package com.uniin.ioc.dameng.validator;

import com.uniin.ioc.dameng.exception.InvalidSqlException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Validates SQL queries to ensure only read-only SELECT statements are executed
 */
@Slf4j
@Component
public class SqlValidator {

    // Pattern to match SELECT statements (must start with SELECT)
    private static final Pattern SELECT_PATTERN =
            Pattern.compile("^\\s*SELECT\\s+", Pattern.CASE_INSENSITIVE);

    // Pattern to detect write operations
    private static final Pattern WRITE_OPERATIONS =
            Pattern.compile("\\b(INSERT|UPDATE|DELETE|DROP|CREATE|ALTER|TRUNCATE|GRANT|REVOKE|MERGE)\\b",
                    Pattern.CASE_INSENSITIVE);

    // Pattern to detect dangerous functions and procedures
    private static final Pattern DANGEROUS_FUNCTIONS =
            Pattern.compile("\\b(EXEC|EXECUTE|SP_|XP_|CALL)\\b",
                    Pattern.CASE_INSENSITIVE);

    // Pattern to detect comments that might hide malicious code
    private static final Pattern SQL_COMMENTS =
            Pattern.compile("(/\\*|\\*/|--)", Pattern.CASE_INSENSITIVE);

    /**
     * Validates that the SQL query is a read-only SELECT statement
     *
     * @param sql the SQL query to validate
     * @throws InvalidSqlException if the query is not a valid read-only SELECT
     */
    public void validateReadOnly(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new InvalidSqlException("SQL query cannot be empty");
        }

        String trimmedSql = sql.trim();

        // Must start with SELECT
        if (!SELECT_PATTERN.matcher(trimmedSql).find()) {
            throw new InvalidSqlException("Only SELECT queries are allowed");
        }

        // Check for write operations (including in subqueries)
        if (WRITE_OPERATIONS.matcher(trimmedSql).find()) {
            throw new InvalidSqlException("Write operations (INSERT/UPDATE/DELETE/DROP/CREATE/ALTER/TRUNCATE/GRANT/REVOKE/MERGE) are not allowed");
        }

        // Check for dangerous functions
        if (DANGEROUS_FUNCTIONS.matcher(trimmedSql).find()) {
            throw new InvalidSqlException("Stored procedures and system functions (EXEC/EXECUTE/SP_/XP_/CALL) are not allowed");
        }

        // Check for SQL comments that might hide malicious code
        if (SQL_COMMENTS.matcher(trimmedSql).find()) {
            throw new InvalidSqlException("SQL comments are not allowed for security reasons");
        }

        log.debug("SQL validation passed: {}", trimmedSql);
    }
}
