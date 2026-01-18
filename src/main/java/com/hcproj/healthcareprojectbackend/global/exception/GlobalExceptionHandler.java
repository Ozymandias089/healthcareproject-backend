package com.hcproj.healthcareprojectbackend.global.exception;

import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        var ec = e.getErrorCode();
        return ResponseEntity.status(ec.status())
                .body(ApiResponse.fail(ec.code(), ec.message()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        var ec = ErrorCode.INVALID_REQUEST;
        return ResponseEntity.status(ec.status())
                .body(ApiResponse.fail(ec.code(), ec.message()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraint(ConstraintViolationException e) {
        var ec = ErrorCode.INVALID_REQUEST;
        return ResponseEntity.status(ec.status())
                .body(ApiResponse.fail(ec.code(), ec.message()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception e) {
        var ec = ErrorCode.INTERNAL_ERROR;
        return ResponseEntity.status(ec.status())
                .body(ApiResponse.fail(ec.code(), ec.message()));
    }
}