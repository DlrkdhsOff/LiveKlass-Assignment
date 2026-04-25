package com.example.assignment.exception;

import com.example.assignment.domain.dto.ResultResponse;
import com.example.assignment.domain.type.FailedType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // GlobalException 처리
  @ExceptionHandler(GlobalException.class)
  public ResponseEntity<ResultResponse> handleGlobalException(GlobalException ex) {
    ResultResponse resultResponse = ex.getResultResponse();
    return new ResponseEntity<>(resultResponse, resultResponse.getStatus());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ResultResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult()
        .getFieldErrors()
        .getFirst()
        .getDefaultMessage();
    ResultResponse resultResponse = new ResultResponse(HttpStatus.BAD_REQUEST, message);
    return new ResponseEntity<>(resultResponse, HttpStatus.BAD_REQUEST);
  }

  // 유효하지 않은 Enum 값 처리 (ex. DRAFT, OPEN, CLOSED 외의 값)
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ResultResponse> handleHttpMessageNotReadable() {

    ResultResponse resultResponse = ResultResponse.of(FailedType.INVALID_INPUT_VALUE);
    return new ResponseEntity<>(resultResponse, HttpStatus.BAD_REQUEST);
  }
}
