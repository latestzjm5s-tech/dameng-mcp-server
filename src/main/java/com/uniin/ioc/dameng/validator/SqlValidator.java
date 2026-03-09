package com.uniin.ioc.dameng.validator;

import com.uniin.ioc.dameng.exception.InvalidSqlException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Basic SQL validation shared by all execution paths.
 */
@Slf4j
@Component
public class SqlValidator {

    /**
     * Validates that the SQL text is present.
     *
     * @param sql the SQL query to validate
     * @throws InvalidSqlException if the query is blank
     */
    public void validateSql(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new InvalidSqlException("SQL query cannot be empty");
        }

        log.debug("SQL validation passed");
    }
}
