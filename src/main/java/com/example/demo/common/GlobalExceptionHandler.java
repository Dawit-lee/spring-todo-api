package com.example.demo.common;

import java.util.List;

import com.example.demo.todo.TodoNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TodoNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(TodoNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(404, exception.getMessage(), List.of()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        List<ApiError.FieldError> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiError.FieldError(error.getField(), error.getDefaultMessage()))
                .toList();
        return new ResponseEntity<>(new ApiError(400, "입력값이 올바르지 않습니다.", errors), headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception, Object body, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        return super.handleExceptionInternal(exception,
                new ApiError(status.value(), messageFor(status.value()), List.of()), headers, status, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception exception) {
        log.error("요청 처리 중 예상하지 못한 오류", exception);
        return ResponseEntity.internalServerError()
                .body(new ApiError(500, messageFor(500), List.of()));
    }

    static String messageFor(int status) {
        return switch (status) {
            case 400 -> "요청 형식이 올바르지 않습니다. JSON 본문과 경로 값을 확인하세요.";
            case 404 -> "요청한 리소스를 찾을 수 없습니다.";
            case 405 -> "지원하지 않는 HTTP 메서드입니다.";
            case 406 -> "지원하지 않는 응답 형식입니다.";
            case 415 -> "지원하지 않는 Content-Type입니다. application/json을 사용하세요.";
            default -> status >= 500 ? "서버 내부 오류가 발생했습니다." : "요청을 처리할 수 없습니다.";
        };
    }
}
