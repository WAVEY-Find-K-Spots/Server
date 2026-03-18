package com.Wavey.WaveyService.global.exception;

import com.Wavey.WaveyService.global.response.CommonResponse;
import com.Wavey.WaveyService.global.response.ErrorDetail;
import com.Wavey.WaveyService.global.response.FieldErrorDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<CommonResponse<Void>> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        ErrorDetail errorDetail = ErrorDetail.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(CommonResponse.error(errorCode.getHttpStatus().value(), errorDetail));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<CommonResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<FieldErrorDetail> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDetail(
                        error.getField(),
                        error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                        error.getDefaultMessage()))
                .collect(Collectors.toList());

        ErrorDetail errorDetail = ErrorDetail.builder()
                .code(ErrorCode.COMMON_INVALID_PARAMETER.getCode())
                .message(ErrorCode.COMMON_INVALID_PARAMETER.getMessage())
                .errors(fieldErrors)
                .build();

        return ResponseEntity
                .status(ErrorCode.COMMON_INVALID_PARAMETER.getHttpStatus())
                .body(CommonResponse.error(ErrorCode.COMMON_INVALID_PARAMETER.getHttpStatus().value(), errorDetail));
    }
}