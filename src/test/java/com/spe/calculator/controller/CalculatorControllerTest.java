package com.spe.calculator.controller;

import com.spe.calculator.service.CalculatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Spring MVC tests for the web layer: request mapping, parameter binding,
 * JSON serialisation and error handling. Uses the real CalculatorService.
 */
@WebMvcTest(CalculatorController.class)
@Import(CalculatorService.class)
class CalculatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void addReturnsJsonResult() throws Exception {
        mockMvc.perform(get("/api/v1/add").param("a", "2").param("b", "3"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"operation":"add","a":2,"b":3,"result":5}
                        """, true));
    }

    @Test
    void subtractReturnsJsonResult() throws Exception {
        mockMvc.perform(get("/api/v1/subtract").param("a", "10").param("b", "4.5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("subtract"))
                .andExpect(jsonPath("$.result").value(5.5));
    }

    @Test
    void multiplyReturnsJsonResult() throws Exception {
        mockMvc.perform(get("/api/v1/multiply").param("a", "-3").param("b", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("multiply"))
                .andExpect(jsonPath("$.result").value(-12));
    }

    @Test
    void divideReturnsJsonResult() throws Exception {
        mockMvc.perform(get("/api/v1/divide").param("a", "7").param("b", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("divide"))
                .andExpect(jsonPath("$.result").value(3.5));
    }

    @Test
    void decimalArithmeticIsExact() throws Exception {
        mockMvc.perform(get("/api/v1/add").param("a", "0.1").param("b", "0.2"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"result\":0.3}")));
    }

    @Test
    void divideByZeroReturns400WithErrorBody() throws Exception {
        mockMvc.perform(get("/api/v1/divide").param("a", "5").param("b", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Division by zero is not allowed"))
                .andExpect(jsonPath("$.path").value("/api/v1/divide"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void missingParameterReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/add").param("a", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Missing required parameter 'b'"));
    }

    @Test
    void emptyParameterReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/add").param("a", "").param("b", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void nonNumericParameterReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/multiply").param("a", "abc").param("b", "2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parameter 'a' must be a number but was 'abc'"));
    }

    @Test
    void nonFiniteParameterReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/subtract").param("a", "1").param("b", "Infinity"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parameter 'b' must be a finite number"));

        mockMvc.perform(get("/api/v1/add").param("a", "NaN").param("b", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parameter 'a' must be a finite number"));
    }
}
