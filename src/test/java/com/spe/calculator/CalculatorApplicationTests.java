package com.spe.calculator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test: boots the full application context (controllers, service,
 * exception handler, Actuator) and calls real endpoints through MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CalculatorApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void addEndpointWorksEndToEnd() throws Exception {
        mockMvc.perform(get("/api/v1/add?a=2&b=3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("add"))
                .andExpect(jsonPath("$.result").value(5));
    }

    @Test
    void divideByZeroIsRejectedEndToEnd() throws Exception {
        mockMvc.perform(get("/api/v1/divide?a=1&b=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Division by zero is not allowed"));
    }

    @Test
    void webPageIsServed() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<title>Calculator</title>")));
    }

    @Test
    void actuatorHealthIsUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
