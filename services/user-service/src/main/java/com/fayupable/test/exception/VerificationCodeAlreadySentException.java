package com.fayupable.test.exception;

public class VerificationCodeAlreadySentException extends RuntimeException {
    public VerificationCodeAlreadySentException(String message) {
        super(message);
    }
}
