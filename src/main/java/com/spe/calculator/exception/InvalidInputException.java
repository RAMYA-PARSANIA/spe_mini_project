package com.spe.calculator.exception;

/** Thrown when a request is syntactically valid but its values cannot be calculated. Mapped to HTTP 400. */
public class InvalidInputException extends RuntimeException {

    public InvalidInputException(String message) {
        super(message);
    }
}
