package com.msa.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("잘못된 요청입니다.");

        return Map.of(
                "status", 400,
                "error", "BAD_REQUEST",
                "message", message
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleNotFound(IllegalArgumentException e) {
        return Map.of(
                "status", 404,
                "error", "NOT_FOUND",
                "message", e.getMessage()
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleConflict(IllegalStateException e) {
        return Map.of(
                "status", 409,
                "error", "CONFLICT",
                "message", e.getMessage()
        );
    }
}
