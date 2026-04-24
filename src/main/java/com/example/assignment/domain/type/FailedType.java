package com.example.assignment.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FailedType {
  ;

  private final HttpStatus status;
  private final String message;
}