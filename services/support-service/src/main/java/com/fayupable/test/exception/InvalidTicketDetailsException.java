package com.fayupable.test.exception;

public class InvalidTicketDetailsException extends RuntimeException {
    public InvalidTicketDetailsException(String message) {
        super(message);
    }
}
