package com.iot.devicemanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), List.of());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(ValidationException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), List.of());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(ConflictException exception) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        List<String> details = exception.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> error instanceof FieldError fieldError
                        ? fieldError.getField() + ": " + fieldError.getDefaultMessage()
                        : error.getDefaultMessage())
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", details);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), List.of());
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, String message, List<String> details) {
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(status.value(), status.getReasonPhrase(), message, details));
    }
}
