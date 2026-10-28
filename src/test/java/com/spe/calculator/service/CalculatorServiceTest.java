package com.spe.calculator.service;

import com.spe.calculator.exception.DivisionByZeroException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Plain JUnit 5 unit tests: no Spring context, just the arithmetic. */
class CalculatorServiceTest {

    private final CalculatorService service = new CalculatorService();

    @ParameterizedTest(name = "{0} + {1} = {2}")
    @CsvSource({
            "2, 3, 5",
            "-4, 1, -3",
            "0.1, 0.2, 0.3",
            "0, 0, 0",
            "1e10, 1, 10000000001"
    })
    void add(String a, String b, String expected) {
        assertDecimalEquals(expected, service.add(dec(a), dec(b)));
    }

    @ParameterizedTest(name = "{0} - {1} = {2}")
    @CsvSource({
            "10, 4, 6",
            "4, 10, -6",
            "0.3, 0.1, 0.2",
            "-2, -2, 0"
    })
    void subtract(String a, String b, String expected) {
        assertDecimalEquals(expected, service.subtract(dec(a), dec(b)));
    }

    @ParameterizedTest(name = "{0} * {1} = {2}")
    @CsvSource({
            "6, 7, 42",
            "-3, 4, -12",
            "2.5, 0.4, 1",
            "123, 0, 0"
    })
    void multiply(String a, String b, String expected) {
        assertDecimalEquals(expected, service.multiply(dec(a), dec(b)));
    }

    @ParameterizedTest(name = "{0} / {1} = {2}")
    @CsvSource({
            "10, 2, 5",
            "7, 2, 3.5",
            "-9, 3, -3",
            "0, 5, 0",
            "1, 3, 0.3333333333333333",
            "2, 3, 0.6666666666666667"
    })
    void divide(String a, String b, String expected) {
        assertDecimalEquals(expected, service.divide(dec(a), dec(b)));
    }

    @Test
    @DisplayName("divide by zero throws DivisionByZeroException")
    void divideByZero() {
        DivisionByZeroException ex = assertThrows(DivisionByZeroException.class,
                () -> service.divide(dec("5"), BigDecimal.ZERO));
        assertEquals("Division by zero is not allowed", ex.getMessage());
    }

    @Test
    @DisplayName("divide by zero written with a scale (0.00) is still rejected")
    void divideByZeroWithScale() {
        assertThrows(DivisionByZeroException.class, () -> service.divide(dec("5"), dec("0.00")));
    }

    private static BigDecimal dec(String value) {
        return new BigDecimal(value);
    }

    /** Compares numerically, so 5 equals 5.0. */
    private static void assertDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, dec(expected).compareTo(actual),
                () -> "expected " + expected + " but was " + actual.toPlainString());
    }
}
