package com.example.assignment.exception;

import com.example.assignment.domain.dto.ResultResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {


  @ExceptionHandler(GlobalException.class)
  public ResponseEntity<ResultResponse> handleGlobalException(GlobalException ex) {
    ResultResponse resultResponse = ex.getResultResponse();
    return new ResponseEntity<>(resultResponse, resultResponse.getStatus());
  }
}
