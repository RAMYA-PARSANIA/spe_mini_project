package com.spe.calculator.controller;

import com.spe.calculator.dto.CalculationResponse;
import com.spe.calculator.exception.InvalidInputException;
import com.spe.calculator.service.CalculatorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.function.BinaryOperator;

@RestController
@RequestMapping("/api/v1")
public class CalculatorController {

    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    // Missing or non-numeric parameters are rejected by Spring before these methods run
    // and turned into HTTP 400 by GlobalExceptionHandler.

    @GetMapping("/add")
    public CalculationResponse add(@RequestParam double a, @RequestParam double b) {
        return calculate("add", a, b, calculatorService::add);
    }

    @GetMapping("/subtract")
    public CalculationResponse subtract(@RequestParam double a, @RequestParam double b) {
        return calculate("subtract", a, b, calculatorService::subtract);
    }

    @GetMapping("/multiply")
    public CalculationResponse multiply(@RequestParam double a, @RequestParam double b) {
        return calculate("multiply", a, b, calculatorService::multiply);
    }

    @GetMapping("/divide")
    public CalculationResponse divide(@RequestParam double a, @RequestParam double b) {
        return calculate("divide", a, b, calculatorService::divide);
    }

    private CalculationResponse calculate(String operation, double a, double b,
                                          BinaryOperator<BigDecimal> function) {
        BigDecimal x = toDecimal("a", a);
        BigDecimal y = toDecimal("b", b);
        return new CalculationResponse(operation, x, y, function.apply(x, y));
    }

    /** Rejects NaN/Infinity (which parse as valid doubles) and converts to an exact decimal. */
    private static BigDecimal toDecimal(String name, double value) {
        if (!Double.isFinite(value)) {
            throw new InvalidInputException("Parameter '" + name + "' must be a finite number");
        }
        return BigDecimal.valueOf(value).stripTrailingZeros();
    }
}
