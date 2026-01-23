package com.uniin.ioc.dameng.exception;

/**
 * Exception thrown when database query execution fails
 */
public class QueryExecutionException extends RuntimeException {

    public QueryExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
