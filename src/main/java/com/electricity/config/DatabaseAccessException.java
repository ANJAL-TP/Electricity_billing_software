package com.electricity.config;

/** Runtime exception used when a database-backed screen cannot load or save its data. */
public final class DatabaseAccessException extends RuntimeException {

    public DatabaseAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
