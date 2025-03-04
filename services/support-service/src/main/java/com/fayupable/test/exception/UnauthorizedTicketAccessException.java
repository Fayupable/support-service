package com.fayupable.test.exception;

public class UnauthorizedTicketAccessException extends RuntimeException {
    public UnauthorizedTicketAccessException(String message) {
        super(message);
    }
}
