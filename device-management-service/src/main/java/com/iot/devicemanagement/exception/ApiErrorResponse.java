package com.iot.devicemanagement.exception;

import java.time.LocalDateTime;
import java.util.List;

public class ApiErrorResponse {

    private final LocalDateTime timestamp = LocalDateTime.now();
    private final int status;
    private final String error;
    private final String message;
    private final List<String> details;

    public ApiErrorResponse(int status, String error, String message, List<String> details) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getDetails() {
        return details;
    }
}
