package com.retry.exception;

/**
 * Created by zbyte on 17-8-2.
 */
public class RetryException extends RuntimeException {
    public RetryException(String message) {
        super(message);
    }
}
