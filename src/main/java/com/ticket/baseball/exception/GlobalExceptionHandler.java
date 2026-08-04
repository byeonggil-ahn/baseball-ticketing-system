package com.ticket.baseball.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 전체 예외 처리 담당
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 직접 만든 비즈니스 예외 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handleBusinessException(
            BusinessException e) {

        log.error("[{}] {}",
                e.getErrorCode(),
                e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    // 낙관적 락 충돌 예외 처리
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<String> handleOptimisticLockException(
            ObjectOptimisticLockingFailureException e) {

        log.error("[{}] {}",
                ErrorCode.OPTIMISTIC_LOCK_CONFLICT,
                ErrorCode.OPTIMISTIC_LOCK_CONFLICT.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorCode.OPTIMISTIC_LOCK_CONFLICT.getMessage());
    }

    // 예상하지 못한 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(
            Exception e) {

        log.error("[INTERNAL_SERVER_ERROR] 서버 오류 발생", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("서버 오류가 발생했습니다.");
    }

    // DTO Validation 실패 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(
            MethodArgumentNotValidException e) {

        log.error(
                "{} ({})",
                ErrorCode.INVALID_INPUT.getMessage(),
                ErrorCode.INVALID_INPUT);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorCode.INVALID_INPUT.getMessage());
    }
}