package com.spe.calculator.exception;

public class DivisionByZeroException extends InvalidInputException {

    public DivisionByZeroException() {
        super("Division by zero is not allowed");
    }
}
