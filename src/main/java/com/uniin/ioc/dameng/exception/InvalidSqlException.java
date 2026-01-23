package com.uniin.ioc.dameng.exception;

/**
 * Exception thrown when SQL validation fails (non-SELECT queries, dangerous operations, etc.)
 */
public class InvalidSqlException extends RuntimeException {

    public InvalidSqlException(String message) {
        super(message);
    }
}
