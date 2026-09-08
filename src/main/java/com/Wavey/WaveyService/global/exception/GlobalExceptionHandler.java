package com.Wavey.WaveyService.global.exception;

import com.Wavey.WaveyService.global.response.ApiResponse;
import com.Wavey.WaveyService.global.response.ErrorDetail;
import com.Wavey.WaveyService.global.response.FieldErrorDetail;

import jakarta.validation.ConstraintViolationException;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
        MethodArgumentTypeMismatchException.class,
        ConstraintViolationException.class,
        MissingServletRequestParameterException.class,
        HandlerMethodValidationException.class,
        HttpMessageNotReadableException.class
    })
    protected ResponseEntity<ApiResponse<Void>> invalidInput(Exception e) {
        return handleCustomException(new CustomException(ErrorCode.COMMON_INVALID_PARAMETER));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    protected ResponseEntity<ApiResponse<Void>> staleNavigation(Exception e) {
        return handleCustomException(new CustomException(ErrorCode.NAVIGATION_STALE));
    }

    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        ErrorDetail errorDetail =
                ErrorDetail.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build();

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.error(errorCode.getHttpStatus().value(), errorDetail));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        List<FieldErrorDetail> fieldErrors =
                e.getBindingResult().getFieldErrors().stream()
                        .map(
                                error ->
                                        new FieldErrorDetail(
                                                error.getField(),
                                                error.getRejectedValue() == null
                                                        ? ""
                                                        : error.getRejectedValue().toString(),
                                                error.getDefaultMessage()))
                        .collect(Collectors.toList());

        ErrorDetail errorDetail =
                ErrorDetail.builder()
                        .code(ErrorCode.COMMON_INVALID_PARAMETER.getCode())
                        .message(ErrorCode.COMMON_INVALID_PARAMETER.getMessage())
                        .errors(fieldErrors)
                        .build();

        return ResponseEntity.status(ErrorCode.COMMON_INVALID_PARAMETER.getHttpStatus())
                .body(
                        ApiResponse.error(
                                ErrorCode.COMMON_INVALID_PARAMETER.getHttpStatus().value(),
                                errorDetail));
    }
}
