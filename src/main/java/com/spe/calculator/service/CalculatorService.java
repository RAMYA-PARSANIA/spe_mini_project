package com.spe.calculator.service;

import com.spe.calculator.exception.DivisionByZeroException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Core arithmetic. BigDecimal is used instead of double so that results are exact
 * decimal values (e.g. 0.1 + 0.2 = 0.3, not 0.30000000000000004).
 */
@Service
public class CalculatorService {

    // 16 significant digits, rounding HALF_EVEN (IEEE 754 decimal64)
    private static final MathContext PRECISION = MathContext.DECIMAL64;

    public BigDecimal add(BigDecimal a, BigDecimal b) {
        return normalize(a.add(b, PRECISION));
    }

    public BigDecimal subtract(BigDecimal a, BigDecimal b) {
        return normalize(a.subtract(b, PRECISION));
    }

    public BigDecimal multiply(BigDecimal a, BigDecimal b) {
        return normalize(a.multiply(b, PRECISION));
    }

    public BigDecimal divide(BigDecimal a, BigDecimal b) {
        if (b.signum() == 0) {
            throw new DivisionByZeroException();
        }
        return normalize(a.divide(b, PRECISION));
    }

    /** Drops trailing zeros so 5.00 is returned as 5. */
    private static BigDecimal normalize(BigDecimal value) {
        return value.stripTrailingZeros();
    }
}
