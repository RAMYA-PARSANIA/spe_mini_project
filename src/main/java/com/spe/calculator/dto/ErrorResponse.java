package com.spe.calculator.dto;

import java.time.Instant;

/** Structured error body returned for every 4xx produced by the API. */
public record ErrorResponse(Instant timestamp, int status, String error, String message, String path) {
}
